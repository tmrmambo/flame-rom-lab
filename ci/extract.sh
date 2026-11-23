#!/usr/bin/env bash
# Stage B: build the analysis bundle (text-only) from the firmware.
# usage: extract.sh <firmware_url> [expected_size]
set -uo pipefail
URL="${1:?firmware url required}"
EXPECT_SIZE="${2:-6559454050}"
HERE="$PWD"; OUT="$HERE/bundle"; W="$HOME/work"; BIN="$HOME/lab-bin"
export PATH="$BIN:$PATH"
mkdir -p "$OUT"/{meta,pkg,boot,avb,fw} "$W"
LOG="$OUT/meta/stage_log.txt"
log() { echo "[$(date +%H:%M:%S)] $*" | tee -a "$LOG"; }
cd "$W"

log "== 1. download"
aria2c -x16 -s16 -k8M --file-allocation=none --summary-interval=0 --console-log-level=warn -o fw.tgz "$URL" || { log "DOWNLOAD FAILED"; exit 1; }
SZ=$(stat -c %s fw.tgz); log "size=$SZ expected=$EXPECT_SIZE md5=$(md5sum fw.tgz | cut -d' ' -f1)"

log "== 2. extract tgz (all but userdata.img)"
tar -xzf fw.tgz --strip-components=1 --exclude='*/images/userdata.img' || log "tar returned $?"
rm -f fw.tgz
df -h / | tee -a "$LOG"

log "== 3. package scripts + metadata, image hashes"
cp -f ./*.bat ./*.sh ./*.py ./misc.txt "$OUT/pkg/" 2>/dev/null
cp -f images/*.xml images/*.txt images/about.html "$OUT/pkg/" 2>/dev/null
ls -l images > "$OUT/meta/images_ls.txt"
( cd images && sha256sum * ) > "$OUT/meta/images_sha256.txt" 2>&1

log "== 4. AVB info on vbmeta/boot/dtbo"
for f in vbmeta vbmeta_system boot vendor_boot dtbo; do
  python3 "$BIN/avbtool.py" info_image --image "images/$f.img" > "$OUT/avb/$f.txt" 2>&1 || log "avbtool failed on $f"
done

log "== 5. firmware-image version strings (rollback/fuse hints)"
PAT='rollback|anti[-_ ]?rb|antirb|QC_IMAGE_VERSION|IMAGE_VARIANT|OEM_IMAGE|sec_?ver|\bsvn\b|min.?version|fuse'
for f in images/*.mbn images/*.elf images/*.melf images/*.bin images/*.fv; do
  [ -f "$f" ] || continue
  n=$(basename "$f")
  { echo "# $n  size=$(stat -c %s "$f")"; strings -n 8 "$f" | grep -iE "$PAT" | sort -u | head -n 40; } > "$OUT/fw/$n.txt" 2>/dev/null
done

log "== 6. boot / vendor_boot ramdisks (fstab, init rc, module list)"
curl -fsSL --retry 3 "https://android.googlesource.com/platform/system/tools/mkbootimg/+/refs/heads/main/unpack_bootimg.py?format=TEXT" | base64 -d > "$BIN/unpack_bootimg.py" 2>/dev/null
for b in boot vendor_boot; do
  rm -rf "bi_$b"
  python3 "$BIN/unpack_bootimg.py" --boot_img "images/$b.img" --out "bi_$b" --format=mkbootimg > "$OUT/boot/${b}_header.txt" 2>&1 || log "unpack_bootimg failed: $b"
  for rd in "bi_$b"/*ramdisk*; do
    [ -f "$rd" ] || continue
    rdn=$(basename "$rd"); d="$W/rd_${b}_${rdn}"; mkdir -p "$d"; rdabs=$(realpath "$rd")
    kind=$(file -b "$rdabs")
    case "$kind" in
      *LZ4*)  lz4 -dc "$rdabs" | ( cd "$d" && cpio -idm --no-absolute-filenames 2>/dev/null ) ;;
      *gzip*) gzip -dc "$rdabs" | ( cd "$d" && cpio -idm --no-absolute-filenames 2>/dev/null ) ;;
      *cpio*) ( cd "$d" && cpio -idm --no-absolute-filenames < "$rdabs" 2>/dev/null ) ;;
      *) log "unknown ramdisk type for $rdn: $kind" ;;
    esac
    ( cd "$d" && find . | sort ) > "$OUT/boot/${b}_${rdn}_ls.txt"
    ( cd "$d" && find . -type f -size -1M | while read -r f; do
        grep -Iq . "$f" 2>/dev/null && install -D "$f" "$OUT/boot/${b}/${rdn}/${f#./}"
      done )
    log "ramdisk $b/$rdn: $kind, files=$(find "$d" -type f | wc -l)"
  done
done

log "== 7. super: sparse -> raw -> lpunpack"
if file images/super.img | grep -qi sparse; then simg2img images/super.img super.raw || log "simg2img FAILED"; rm -f images/super.img
else mv images/super.img super.raw; fi
lpdumps super.raw > "$OUT/meta/lpdump.txt" 2>&1
mkdir -p parts; lpunpack super.raw parts > "$OUT/meta/lpunpack.txt" 2>&1; log "lpunpack exit=$?"
rm -f super.raw
for f in parts/*_a.img; do
  [ -s "$f" ] || continue
  python3 "$BIN/avbtool.py" info_image --image "$f" > "$OUT/avb/$(basename "$f" .img).txt" 2>&1
done

log "== 8. mount every _a partition read-only"
sudo mkdir -p /mnt/lab
for img in parts/*_a.img; do
  [ -s "$img" ] || continue
  n=$(basename "$img" _a.img)
  sudo mkdir -p "/mnt/lab/$n"
  if file -b "$img" | grep -q EROFS; then T=erofs; O=loop,ro; else T=ext4; O=loop,ro,noload; fi
  if sudo mount -t "$T" -o "$O" "$img" "/mnt/lab/$n"; then log "mounted $n ($T)"; else log "MOUNT FAILED $n"; fi
done
df -h | grep -E 'lab|Filesystem' | tee -a "$LOG"

log "== 9. walk + scan (python)"
sudo env "PATH=$PATH" python3 "$HERE/ci/extract_bundle.py" /mnt/lab "$OUT" 2>&1 | tee -a "$LOG"

log "== 10. pack"
sudo chown -R "$(id -u):$(id -g)" "$OUT"
cd "$HERE"
du -sh bundle/* | tee -a "$LOG"
tar -czf bundle.tar.gz bundle
ls -l bundle.tar.gz | tee -a "$LOG"
log "DONE"
