#!/usr/bin/env bash
# JADX job: decompile the targets listed in analysis/jadx_targets.txt (part|path|tag), smallest first, with per-target timeout.
# Also dumps strings/dynamic symbols of the mcd native daemon. Everything ends up in jadx_out/ (compressed per target).
# usage: jadx_job.sh <firmware_url>
set -uo pipefail
HERE="$PWD"; BIN="$HOME/lab-bin"; export PATH="$BIN:$PATH"
bash "$HERE/ci/prep_mount.sh" "$1" || exit 1
OUT="$HERE/${3:-jadx_out}"; mkdir -p "$OUT" "$HOME/jx"
export JAVA_OPTS="-Xmx10g"
SUM="$OUT/_summary.tsv"; echo -e "label\ttag\tpath\tsize\tseconds\texit\tjava_files\tout_mb" > "$SUM"

# order: smallest first so most results exist early
while IFS='|' read -r part path tag; do
  f="/mnt/lab/$part$path"; [ -f "$f" ] && echo -e "$(stat -c %s "$f")\t$part\t$path\t$tag" || echo "MISSING $f" >&2
done < "$HERE/${2:-analysis/jadx_targets.txt}" | sort -n > "$HOME/jx/order.txt"

while IFS=$'\t' read -r size part path tag; do
  f="/mnt/lab/$part$path"
  label=$( [ "$tag" = JAR ] && basename "$path" .jar || echo "$tag" )
  d="$HOME/jx/$label"; rm -rf "$d"; mkdir -p "$d"
  if [ "$size" -gt 40000000 ]; then TO=1500; else TO=700; fi
  echo "[$(date +%H:%M:%S)] jadx $label ($((size/1000000)) MB, timeout ${TO}s)"
  t0=$(date +%s)
  timeout "$TO" jadx --no-res --threads-count 4 --no-debug-info --show-bad-code --log-level ERROR -d "$d" "$f" > "$OUT/$label.jadx.log" 2>&1
  rc=$?; t1=$(date +%s)
  nj=$(find "$d" -name '*.java' 2>/dev/null | wc -l); mb=$(du -sm "$d" 2>/dev/null | cut -f1)
  echo -e "$label\t$tag\t$part$path\t$size\t$((t1-t0))\t$rc\t$nj\t$mb" >> "$SUM"
  # small digests that help the readers
  if [ "$nj" -gt 0 ]; then
    grep -rhoE 'https?://[A-Za-z0-9._~:/?#@!$&()*+,;=%-]{6,160}' "$d" --include=*.java 2>/dev/null | sort | uniq -c | sort -rn | head -n 300 > "$OUT/$label.urls.txt"
    tar -C "$HOME/jx" -cf - "$label" | zstd -T0 -3 -q -o "$OUT/$label.tar.zst"
  fi
  rm -rf "$d"
done < "$HOME/jx/order.txt"

echo "== mcd (native)"
M=/mnt/lab/system_ext/bin/mcd
if [ -f "$M" ]; then
  { file "$M"; echo "--- needed/dynamic"; readelf -d "$M" 2>/dev/null | grep -E 'NEEDED|SONAME|RUNPATH'; echo "--- dynsyms (imports)"; readelf --dyn-syms -W "$M" 2>/dev/null | awk '{print $8}' | sort -u | head -n 400; echo "--- strings"; strings -n 6 "$M"; } > "$OUT/mcd.analysis.txt" 2>&1
  wc -l "$OUT/mcd.analysis.txt"
fi
for r in /mnt/lab/system_ext/etc/init/*mcd*.rc /mnt/lab/system_ext/etc/init/*.rc; do grep -l 'mcd' "$r" 2>/dev/null; done | sort -u | head -n 5 > "$OUT/mcd.rc_files.txt"
cat "$SUM"
du -sh "$OUT"
tar -cf "${3:-jadx_out}.tar" -C "$HERE" "${3:-jadx_out}" && ls -l "${3:-jadx_out}.tar"
