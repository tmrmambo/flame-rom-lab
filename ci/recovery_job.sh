#!/usr/bin/env bash
# Does recovery contain the 'show_version_mismatch' handler / the 'Unsupported software' screen?
# usage: recovery_job.sh <firmware_url>
set -uo pipefail
URL="${1:?firmware url required}"
HERE="$PWD"; BIN="$HOME/lab-bin"; export PATH="$BIN:$PATH"
OUT="$HERE/recovery_out"; mkdir -p "$OUT"; W="$HOME/work"; mkdir -p "$W"; cd "$W"
log() { echo "[$(date +%H:%M:%S)] $*" | tee -a "$OUT/log.txt"; }
aria2c -x16 -s16 -k8M --file-allocation=none --summary-interval=0 --console-log-level=warn -o fw.tgz "$URL" || exit 1
tar -xzf fw.tgz --strip-components=1 --wildcards '*/images/recovery.img' '*/images/vendor_boot.img' '*/images/boot.img' && rm -f fw.tgz
curl -fsSL --retry 3 "https://android.googlesource.com/platform/system/tools/mkbootimg/+/refs/heads/main/unpack_bootimg.py?format=TEXT" | base64 -d > "$BIN/unpack_bootimg.py"
ls -l images | tee "$OUT/images_ls.txt"
for b in recovery vendor_boot boot; do
  python3 "$BIN/unpack_bootimg.py" --boot_img "images/$b.img" --out "bi_$b" > "$OUT/${b}_header.txt" 2>&1
  ls -l "bi_$b" > "$OUT/${b}_parts.txt"
  for rd in "bi_$b"/*ramdisk*; do
    [ -s "$rd" ] || continue
    rdn=$(basename "$rd"); d="$W/rd_${b}_${rdn}"; mkdir -p "$d"; rdabs=$(realpath "$rd")
    case "$(file -b "$rdabs")" in
      *LZ4*)  lz4 -dc "$rdabs" | ( cd "$d" && cpio -idm --no-absolute-filenames 2>/dev/null ) ;;
      *gzip*) gzip -dc "$rdabs" | ( cd "$d" && cpio -idm --no-absolute-filenames 2>/dev/null ) ;;
      *cpio*) ( cd "$d" && cpio -idm --no-absolute-filenames < "$rdabs" 2>/dev/null ) ;;
    esac
    ( cd "$d" && find . | sort ) > "$OUT/${b}_${rdn}_ls.txt"
    log "$b/$rdn: $(find "$d" -type f | wc -l) files"
  done
done
log "== search: version_mismatch / unsupported / region lock strings in ramdisk files and kernels"
: > "$OUT/hits.txt"
for d in "$W"/rd_*; do
  find "$d" -type f 2>/dev/null | while read -r f; do
    h=$( { strings -a -n 5 "$f"; strings -a -el -n 5 "$f"; } 2>/dev/null | grep -iE 'version_mismatch|show_version|Unsupported software|software.{0,15}(mismatch|not supported)|lock_region|mismatch' | sort -u | head -n 8 )
    [ -n "$h" ] && { echo "### ${f#$W/}"; echo "$h"; } >> "$OUT/hits.txt"
  done
done
for k in bi_recovery bi_vendor_boot bi_boot; do
  for f in "$k"/kernel; do [ -f "$f" ] && { echo "### $f"; strings -a -n 6 "$f" | grep -iE 'show_version|version_mismatch|lock_region' | sort -u | head -n 5; } >> "$OUT/hits.txt"; done
done
head -n 80 "$OUT/hits.txt"
log "== recovery binary + res (names only)"
find "$W"/rd_recovery_* -maxdepth 4 \( -name 'recovery*' -o -path '*res-*' -o -name '*.png' \) 2>/dev/null | head -n 120 > "$OUT/recovery_files.txt"
wc -l "$OUT/recovery_files.txt"
for f in $(find "$W"/rd_recovery_* -type f -name 'recovery' 2>/dev/null | head -n 2); do
  { echo "# $f $(stat -c %s "$f")"; strings -a -n 5 "$f" | grep -iE 'mismatch|show_|locale|--[a-z_]+' | sort -u | head -n 120; } >> "$OUT/recovery_args.txt"
done
cd "$HERE"; tar -czf recovery_out.tar.gz recovery_out; ls -l recovery_out.tar.gz
