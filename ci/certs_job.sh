#!/usr/bin/env bash
# Signing-certificate map of the whole system image + the files that bind identities to certificates.
# usage: certs_job.sh <firmware_url>
set -uo pipefail
HERE="$PWD"; BIN="$HOME/lab-bin"; export PATH="$BIN:$PATH"
bash "$HERE/ci/prep_mount.sh" "$1" || exit 1
OUT="$HERE/certs_out"; mkdir -p "$OUT/sec"
if ! command -v apksigner >/dev/null 2>&1; then sudo apt-get install -y -qq apksigner >/dev/null 2>&1 || true; fi
if ! command -v apksigner >/dev/null 2>&1; then
  curl -fsSL --retry 3 -o /tmp/bt.zip https://dl.google.com/android/repository/build-tools_r35-linux.zip && unzip -q -o /tmp/bt.zip -d /tmp/bt && \
  printf '#!/bin/sh\nexec java -jar %s "$@"\n' "$(find /tmp/bt -name apksigner.jar | head -n1)" > "$BIN/apksigner" && chmod +x "$BIN/apksigner"
fi
command -v apksigner || { echo "NO APKSIGNER"; exit 1; }

sudo env "PATH=$PATH" python3 - <<'PY'
import os, re, subprocess, csv
from concurrent.futures import ThreadPoolExecutor
ROOT='/mnt/lab'; OUT=os.environ.get('OUT_DIR') or os.path.join(os.getcwd(),'certs_out')
apks=[]
for dp,_,fns in os.walk(ROOT):
    for f in fns:
        if f.endswith('.apk'): apks.append(os.path.join(dp,f))
apks.sort()
def run(p):
    if os.path.getsize(p)==0: return p,'EMPTY',[],[]
    r=subprocess.run(['apksigner','verify','-v','--print-certs','--min-sdk-version','24',p],capture_output=True,text=True,errors='replace',timeout=300)
    t=r.stdout+r.stderr
    sch=[k for k in ('v1','v2','v3','v3.1','v4') if re.search(r'Verified using %s scheme[^:]*: true'%re.escape(k),t)]
    dig=re.findall(r'Signer #\d+ certificate SHA-256 digest: ([0-9a-f]+)',t)
    dn=re.findall(r'Signer #\d+ certificate DN: (.*)',t)
    st='OK' if 'Verifies' in t.split('\n')[0] else ('FAIL:'+t.strip().split('\n')[0][:90])
    return p,st,sch,list(zip(dig,dn))
with ThreadPoolExecutor(3) as ex, open(os.path.join(OUT,'apk_certs.tsv'),'w') as fo:
    fo.write('apk\tstatus\tschemes\tsha256\tdn\n')
    for p,st,sch,cd in ex.map(run,apks):
        d=';'.join(c for c,_ in cd); n=';'.join(x for _,x in cd)
        fo.write(f"{os.path.relpath(p,ROOT)}\t{st}\t{','.join(sch)}\t{d}\t{n[:160]}\n")
print('apks',len(apks))
PY
export OUT_DIR="$OUT"

echo "== identity/cert binding files"
sudo find /mnt/lab \( -name '*mac_permissions*' -o -name '*seapp_contexts*' -o -name 'otacerts*' -o -name '*.x509.pem' -o -name '*.pk8' -o -name '*.pem' -o -name '*.avbpubkey' -o -name '*.der' -o -name 'keystore*' \) -not -path '*/apex/*' 2>/dev/null > "$OUT/sec/files.txt"
cat "$OUT/sec/files.txt" | head -n 80
while read -r f; do
  b=$(echo "$f" | sed 's#/mnt/lab/##; s#/#__#g')
  if file -b "$f" | grep -qiE 'text|xml'; then sudo cp "$f" "$OUT/sec/$b" 2>/dev/null
  else sudo sh -c "unzip -l '$f' > '$OUT/sec/$b.list' 2>&1 || file '$f' > '$OUT/sec/$b.list'" ; fi
done < "$OUT/sec/files.txt"
sudo chown -R "$(id -u):$(id -g)" "$OUT"
awk -F'\t' 'NR>1{c[$4]++} END{for(k in c) print c[k], k}' "$OUT/apk_certs.tsv" | sort -rn | head -n 25 | tee "$OUT/cert_counts.txt"
tar -czf certs_out.tar.gz certs_out; ls -l certs_out.tar.gz
