#!/usr/bin/env bash
# Decompile Provision WITH resources (so R.string.* names resolve and layouts are readable), find who shows 'Unsupported software'.
# usage: provision_job.sh <firmware_url>
set -uo pipefail
HERE="$PWD"; BIN="$HOME/lab-bin"; export PATH="$BIN:$PATH"
bash "$HERE/ci/prep_mount.sh" "$1" || exit 1
OUT="$HERE/provision_out"; mkdir -p "$OUT"
export JAVA_OPTS="-Xmx10g"
F=/mnt/lab/system_ext/priv-app/Provision/Provision.apk
ls -l "$F" | tee "$OUT/apk_ls.txt"
echo "== jadx (with resources)"
timeout 900 jadx --threads-count 4 --show-bad-code --log-level ERROR -d "$HOME/pv" "$F" > "$OUT/jadx.log" 2>&1; echo "jadx rc=$?"
find "$HOME/pv/sources" -name '*.java' | wc -l
echo "== usages of the mismatch strings"
grep -rn --include=*.java -E 'software_(version|account)_mismatch' "$HOME/pv/sources" | grep -v '/R\.java' | head -n 40 | tee "$OUT/usages_java.txt"
grep -rn -E 'software_(version|account)_mismatch' "$HOME/pv/resources" 2>/dev/null | head -n 40 | tee "$OUT/usages_res.txt"
echo "== raw dex scan for resource ids (little endian)"
python3 - "$F" <<'PY' | tee "$OUT/dex_id_scan.txt"
import sys, zipfile, re
z = zipfile.ZipFile(sys.argv[1])
ids = {'software_version_mismatch': 0x7f100397, 'software_version_mismatch_summary': 0x7f100398,
       'software_account_mismatch': 0x7f100395, 'software_account_mismatch_summary': 0x7f100396}
for n in z.namelist():
    if re.fullmatch(r'classes\d*\.dex', n):
        b = z.read(n)
        for k, v in ids.items():
            pat = v.to_bytes(4, 'little')
            c = b.count(pat)
            if c: print(n, k, hex(v), 'occurrences', c)
print('scan done')
PY
echo "== which java classes use the R.string names or the ints"
grep -rln --include=*.java -E 'R\.string\.software_|2131755927|2131755925' "$HOME/pv/sources" | head -n 20 | tee "$OUT/classes_using.txt"
# context: copy the classes that reference them (small)
mkdir -p "$OUT/classes"
while read -r f; do [ -f "$f" ] && cp --parents "$f" "$OUT/classes/" 2>/dev/null; done < "$OUT/classes_using.txt"
# layouts / xml that mention them
grep -rl -E 'software_(version|account)_mismatch' "$HOME/pv/resources/res" 2>/dev/null | head -n 20 | tee "$OUT/layouts_using.txt"
while read -r f; do [ -f "$f" ] && cp --parents "$f" "$OUT/classes/" 2>/dev/null; done < "$OUT/layouts_using.txt"
# the full DefaultActivity / GlobalDefaultActivity flow files, for reading
for c in com/android/provision/activities/DefaultActivity.java com/android/provision/global/GlobalDefaultActivity.java com/android/provision/Utils.java; do
  [ -f "$HOME/pv/sources/$c" ] && cp --parents "$HOME/pv/sources/$c" "$OUT/classes/"
done
cp "$HOME/pv/resources/AndroidManifest.xml" "$OUT/" 2>/dev/null
tar -czf provision_out.tar.gz provision_out; ls -l provision_out.tar.gz
