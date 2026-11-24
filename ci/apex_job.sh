#!/usr/bin/env bash
# APEX job: unpack every .apex/.capex found in the mounted partitions, mount each payload, run the same scanner as stage B.
# usage: apex_job.sh <firmware_url>
set -uo pipefail
HERE="$PWD"; BIN="$HOME/lab-bin"; export PATH="$BIN:$PATH"
bash "$HERE/ci/prep_mount.sh" "$1" || exit 1
OUT="$HERE/bundle_apex"; mkdir -p "$OUT/meta" "$OUT/list"
W="$HOME/work/apex"; mkdir -p "$W"; sudo mkdir -p /mnt/apex
find /mnt/lab -type f \( -name '*.apex' -o -name '*.capex' \) 2>/dev/null | sort > "$OUT/list/apex_files.txt"
echo "apex files: $(wc -l < "$OUT/list/apex_files.txt")"
while read -r f; do
  base=$(basename "$f"); name="${base%.*}"; part=$(echo "$f" | cut -d/ -f4)
  d="$W/$part.$name"; rm -rf "$d"; mkdir -p "$d"; cp "$f" "$d/in"
  ( cd "$d" && unzip -q -o in 2>/dev/null )
  if [ -f "$d/original_apex" ]; then mv "$d/original_apex" "$d/inner.apex"; ( cd "$d" && mkdir -p inner && cd inner && unzip -q -o ../inner.apex 2>/dev/null ); P="$d/inner/apex_payload.img"; MF="$d/inner/apex_manifest.pb"
  else P="$d/apex_payload.img"; MF="$d/apex_manifest.pb"; fi
  [ -f "$P" ] || { echo "NO PAYLOAD $part/$base"; continue; }
  { echo "== $part/$base"; stat -c 'payload %s bytes' "$P"; file -b "$P" | cut -c1-80; strings -n 4 "$MF" 2>/dev/null | head -n 4; } >> "$OUT/list/apex_info.txt"
  m="/mnt/apex/$part.$name"; sudo mkdir -p "$m"
  if file -b "$P" | grep -q EROFS; then T=erofs; O=loop,ro; else T=ext4; O=loop,ro,noload; fi
  sudo mount -t "$T" -o "$O" "$P" "$m" && echo "mounted $part.$name ($T)" || echo "MOUNT FAILED $part.$name"
done < "$OUT/list/apex_files.txt"
sudo env "PATH=$PATH" python3 "$HERE/ci/extract_bundle.py" /mnt/apex "$OUT" 2>&1 | tail -n 40
sudo chown -R "$(id -u):$(id -g)" "$OUT"
du -sh "$OUT"/* | sed 's#^#size #'
tar -czf bundle_apex.tar.gz bundle_apex && ls -l bundle_apex.tar.gz
