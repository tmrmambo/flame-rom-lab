#!/usr/bin/env bash
# Shared prep for deep-analysis jobs: download firmware, extract super, unpack, mount every _a partition read-only.
# usage: prep_mount.sh <firmware_url>   (mounts under /mnt/lab/<part>)
set -uo pipefail
URL="${1:?firmware url required}"
BIN="$HOME/lab-bin"; export PATH="$BIN:$PATH"
W="$HOME/work"; mkdir -p "$W"; cd "$W"
log() { echo "[$(date +%H:%M:%S)] $*"; }
log "download"
aria2c -x16 -s16 -k8M --file-allocation=none --summary-interval=0 --console-log-level=warn -o fw.tgz "$URL" || { echo "DOWNLOAD FAILED"; exit 1; }
tar -xzf fw.tgz --strip-components=1 --wildcards '*/images/super.img' && rm -f fw.tgz
log "super -> raw -> parts"
simg2img images/super.img super.raw && rm -f images/super.img
mkdir -p parts && lpunpack super.raw parts >/dev/null 2>&1; rm -f super.raw
sudo mkdir -p /mnt/lab
for img in parts/*_a.img; do
  [ -s "$img" ] || continue
  n=$(basename "$img" _a.img); sudo mkdir -p "/mnt/lab/$n"
  if file -b "$img" | grep -q EROFS; then T=erofs; O=loop,ro; else T=ext4; O=loop,ro,noload; fi
  sudo mount -t "$T" -o "$O" "$img" "/mnt/lab/$n" && log "mounted $n ($T)" || log "MOUNT FAILED $n"
done
df -h / | tail -n 1
