#!/usr/bin/env bash
# Where does the text "Unsupported software" (zh: 不支持的软件) come from?
#  1) strings of every firmware image except super/userdata (ASCII, UTF-16LE, and the zh text)
#  2) resource tables of every APK/overlay inside the logical partitions
# usage: strings_job.sh <firmware_url>
set -uo pipefail
URL="${1:?firmware url required}"
HERE="$PWD"; BIN="$HOME/lab-bin"; export PATH="$BIN:$PATH"
OUT="$HERE/strings_out"; mkdir -p "$OUT"
W="$HOME/work"; mkdir -p "$W"; cd "$W"
log() { echo "[$(date +%H:%M:%S)] $*" | tee -a "$OUT/log.txt"; }

log "download"
aria2c -x16 -s16 -k8M --file-allocation=none --summary-interval=0 --console-log-level=warn -o fw.tgz "$URL" || { log "DOWNLOAD FAILED"; exit 1; }
tar -xzf fw.tgz --strip-components=1 --exclude='*/images/userdata.img'
rm -f fw.tgz

log "firmware image strings"
STRICT='unsupported (software|rom|system|version|build|firmware)|software.{0,25}not supported|not supported.{0,25}(software|rom)|unsupported_software'
: > "$OUT/img_strings_strict.txt"; : > "$OUT/img_strings_counts.txt"
for f in images/*; do
  [ -f "$f" ] || continue
  case "$f" in *super.img) continue ;; esac
  n=$(basename "$f")
  a=$( { strings -a -n 6 "$f"; strings -a -el -n 6 "$f"; } 2>/dev/null | grep -iE "$STRICT" | sort -u | head -n 20 )
  z=$(grep -a -c -E '不支持的软件|软件不受支持|不受支持' "$f" 2>/dev/null || true)
  broad=$( { strings -a -n 6 "$f"; strings -a -el -n 6 "$f"; } 2>/dev/null | grep -ciE 'unsupported|not supported' )
  echo -e "$n\tbroad=$broad\tzh_utf8_lines=$z" >> "$OUT/img_strings_counts.txt"
  if [ -n "$a" ]; then { echo "### $n"; echo "$a"; } >> "$OUT/img_strings_strict.txt"; fi
done
cat "$OUT/img_strings_counts.txt" | awk -F'\t' '$2!="broad=0" || $3!="zh_utf8_lines=0"' | head -n 50

log "super -> partitions"
simg2img images/super.img super.raw && rm -f images/super.img
mkdir -p parts && lpunpack super.raw parts >/dev/null 2>&1; rm -f super.raw
sudo mkdir -p /mnt/lab
for img in parts/*_a.img; do
  [ -s "$img" ] || continue
  n=$(basename "$img" _a.img); sudo mkdir -p "/mnt/lab/$n"
  if file -b "$img" | grep -q EROFS; then T=erofs; O=loop,ro; else T=ext4; O=loop,ro,noload; fi
  sudo mount -t "$T" -o "$O" "$img" "/mnt/lab/$n" && log "mounted $n" || log "MOUNT FAILED $n"
done

log "resource scan"
python3 "$HERE/ci/res_scan.py" /mnt/lab "$OUT/res_hits.tsv" 2>&1 | tee -a "$OUT/log.txt"

log "text-file scan (props, xml, rc) for the same phrases"
sudo grep -rIlE -i 'unsupported[_ ]?software|software[_ ]?not[_ ]?supported' /mnt/lab 2>/dev/null | head -n 40 > "$OUT/text_hits.txt" || true
wc -l "$OUT/text_hits.txt"
cd "$HERE"; tar -czf strings_out.tar.gz strings_out; ls -l strings_out.tar.gz
log "DONE"
