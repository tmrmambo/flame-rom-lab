#!/usr/bin/env bash
# Boot-chain only: fetch firmware, unpack boot/vendor_boot ramdisks, copy text files (fstab, init rc ...).
# usage: boot_only.sh <firmware_url>
set -uo pipefail
URL="${1:?firmware url required}"
HERE="$PWD"; OUT="$HERE/bundle_boot"; W="$HOME/work"; BIN="$HOME/lab-bin"
export PATH="$BIN:$PATH"
mkdir -p "$OUT" "$W"; cd "$W"
log() { echo "[$(date +%H:%M:%S)] $*" | tee -a "$OUT/log.txt"; }
aria2c -x16 -s16 -k8M --file-allocation=none --summary-interval=0 --console-log-level=warn -o fw.tgz "$URL" || exit 1
tar -xzf fw.tgz --strip-components=1 --wildcards '*/images/boot.img' '*/images/vendor_boot.img' '*/images/dtbo.img' && rm -f fw.tgz
curl -fsSL --retry 3 "https://android.googlesource.com/platform/system/tools/mkbootimg/+/refs/heads/main/unpack_bootimg.py?format=TEXT" | base64 -d > "$BIN/unpack_bootimg.py"
for b in boot vendor_boot; do
  python3 "$BIN/unpack_bootimg.py" --boot_img "images/$b.img" --out "bi_$b" --format=mkbootimg > "$OUT/${b}_header.txt" 2>&1
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
    ( cd "$d" && find . \( -type f -o -type l \) -size -1024k | while read -r f; do
        [ -f "$f" ] && [ -s "$f" ] && grep -Iq . "$f" 2>/dev/null && install -D "$f" "$OUT/${b}/${rdn}/${f#./}"
      done )
    log "$b/$rdn: files=$(find "$d" -type f | wc -l) copied=$(find "$OUT/$b/$rdn" -type f 2>/dev/null | wc -l)"
  done
done
# kernel command line / dtb info for the record
for b in boot vendor_boot; do ls -l "bi_$b"/ >> "$OUT/log.txt" 2>&1; done
cd "$HERE"; tar -czf bundle_boot.tar.gz bundle_boot; ls -l bundle_boot.tar.gz | tee -a "$OUT/log.txt"
