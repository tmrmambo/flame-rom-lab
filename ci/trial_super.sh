#!/usr/bin/env bash
# Trial: download firmware, pull super.img out, convert to raw, dump layout, unpack
# logical partitions, probe each filesystem and prove extraction works (EROFS + ext4).
# Output: report/ (small text files only) -> uploaded as artifact.
set -uo pipefail
URL="${1:?firmware url required}"
EXPECT_SIZE="${2:-6559454050}"
W="$HOME/work"; R="$PWD/report"
mkdir -p "$W" "$R"
cd "$W"
df -h / | tee "$R/00_df_start.txt"

echo "::group::download (aria2, 16 conns)"
T0=$(date +%s)
aria2c -x16 -s16 -k8M --file-allocation=none --summary-interval=0 --console-log-level=warn -o fw.tgz "$URL" || { echo "DOWNLOAD FAILED"; exit 1; }
echo "download_seconds=$(( $(date +%s) - T0 ))" | tee "$R/01_download.txt"
SZ=$(stat -c %s fw.tgz); echo "size=$SZ expected=$EXPECT_SIZE" | tee -a "$R/01_download.txt"
[ "$SZ" = "$EXPECT_SIZE" ] || echo "SIZE MISMATCH" | tee -a "$R/01_download.txt"
md5sum fw.tgz | tee -a "$R/01_download.txt"
echo "::endgroup::"

echo "::group::extract super.img (+ small files for later use)"
tar -tzf fw.tgz > "$R/02_tar_listing.txt"
tar -xzf fw.tgz --wildcards '*/images/super.img' '*/images/vbmeta.img' '*/images/vbmeta_system.img' \
    '*/images/anti_version.txt' '*/images/boot.img' '*/images/vendor_boot.img' '*/images/dtbo.img' \
    '*/misc.txt' --strip-components=1
ls -l images misc.txt | tee "$R/03_extracted.txt"
rm -f fw.tgz
df -h / | tee "$R/03_df_after_extract.txt"
echo "::endgroup::"

echo "::group::sparse -> raw"
file images/super.img | tee "$R/04_super_file.txt"
if file images/super.img | grep -qi 'sparse'; then
  simg2img images/super.img super.raw || { echo "simg2img FAILED"; exit 1; }
  rm -f images/super.img
else
  mv images/super.img super.raw
fi
ls -l super.raw | tee -a "$R/04_super_file.txt"
echo "::endgroup::"

echo "::group::lp layout"
lpdumps super.raw > "$R/05_lpdump.txt" 2>&1; echo "lpdumps exit=$?" | tee -a "$R/05_lpdump.txt"
echo "::endgroup::"

echo "::group::lpunpack"
mkdir -p parts
lpunpack super.raw parts 2>&1 | tee "$R/06_lpunpack.txt"
echo "lpunpack exit=${PIPESTATUS[0]}" | tee -a "$R/06_lpunpack.txt"
rm -f super.raw
ls -l parts | tee "$R/06_parts_ls.txt"
echo "::endgroup::"

echo "::group::probe partitions"
: > "$R/07_fs_probe.txt"
for f in parts/*.img; do
  n=$(basename "$f" .img)
  [ -s "$f" ] || continue
  echo "--- $n  $(stat -c %s "$f") bytes  $(file -b "$f" | cut -c1-100)" | tee -a "$R/07_fs_probe.txt"
done
echo "::endgroup::"

echo "::group::extraction proof (EROFS with xattrs, kernel mount, ext4 loop mount)"
fsck.erofs --help 2>&1 | head -n 25 > "$R/08_fsck_help.txt"
: > "$R/08_extract_test.txt"
# smallest NON-EMPTY erofs partition
SMALL=$(find parts -name '*.img' -size +0 -printf '%s %p\n' | sort -n | while read -r sz f; do
  file -b "$f" | grep -q EROFS && { echo "$f"; break; }; done)
echo "erofs extract test on: $SMALL" | tee -a "$R/08_extract_test.txt"
mkdir -p ex_erofs
fsck.erofs --extract=ex_erofs --xattrs --force --overwrite "$SMALL" > "$R/08_erofs_extract_log.txt" 2>&1
echo "fsck.erofs exit=$?" | tee -a "$R/08_extract_test.txt"
tail -n 4 "$R/08_erofs_extract_log.txt" | tee -a "$R/08_extract_test.txt"
echo "files=$(find ex_erofs -type f | wc -l) dirs=$(find ex_erofs -type d | wc -l) symlinks=$(find ex_erofs -type l | wc -l)" | tee -a "$R/08_extract_test.txt"
echo "xattr sample:" | tee -a "$R/08_extract_test.txt"
( cd ex_erofs && for f in $(find . -type f | head -n 3); do getfattr -h -d -m - "$f" 2>&1 | head -n 4; done ) | tee -a "$R/08_extract_test.txt"

mkdir -p mnt_erofs
sudo mount -t erofs -o loop,ro "$SMALL" mnt_erofs 2>&1 | tee -a "$R/08_extract_test.txt"
if mountpoint -q mnt_erofs; then
  echo "kernel erofs mount: OK" | tee -a "$R/08_extract_test.txt"
  sudo getfattr -h -d -m - "mnt_erofs/$(cd mnt_erofs && find . -type f | head -n 1)" 2>&1 | head -n 4 | tee -a "$R/08_extract_test.txt"
  sudo umount mnt_erofs
else
  echo "kernel erofs mount: NOT AVAILABLE" | tee -a "$R/08_extract_test.txt"
fi

echo "--- ext4 system" | tee -a "$R/08_extract_test.txt"
mkdir -p mnt_sys
sudo mount -t ext4 -o loop,ro,noload parts/system_a.img mnt_sys 2>&1 | tee -a "$R/08_extract_test.txt"
if mountpoint -q mnt_sys; then
  echo "ext4 mount OK; top level:" | tee -a "$R/08_extract_test.txt"
  ls mnt_sys | head -n 30 | tee -a "$R/08_extract_test.txt"
  sudo getfattr -h -n security.selinux mnt_sys/system/bin/sh mnt_sys/system/build.prop 2>&1 | head -n 6 | tee -a "$R/08_extract_test.txt"
  sudo find mnt_sys -type f | wc -l | sed 's/^/ext4 file count: /' | tee -a "$R/08_extract_test.txt"
  sudo umount mnt_sys
fi
dumpe2fs -h parts/system_a.img 2>&1 | head -n 45 > "$R/08_ext4_dumpe2fs.txt" || true
echo "::endgroup::"

cp images/anti_version.txt misc.txt "$R/" 2>/dev/null || true
df -h / | tee "$R/99_df_end.txt"
echo "DONE"
