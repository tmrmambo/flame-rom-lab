#!/usr/bin/env bash
# Trial: download firmware, pull super.img out, convert to raw, dump layout, unpack
# logical partitions, probe each filesystem and prove EROFS extraction works.
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
wc -l "$R/02_tar_listing.txt"
tar -xzf fw.tgz --wildcards '*/images/super.img' '*/images/vbmeta.img' '*/images/vbmeta_system.img' \
    '*/images/anti_version.txt' '*/images/boot.img' '*/images/vendor_boot.img' '*/images/dtbo.img' \
    '*/misc.txt' --strip-components=1
ls -l images misc.txt | tee "$R/03_extracted.txt"
rm -f fw.tgz                    # free ~6.5 GB
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
df -h / | tee "$R/04_df_after_raw.txt"
echo "::endgroup::"

echo "::group::lp layout"
lpdumps super.raw > "$R/05_lpdump.txt" 2>&1; echo "lpdumps exit=$?" | tee -a "$R/05_lpdump.txt"
head -n 80 "$R/05_lpdump.txt"
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
  echo "--- $n  $(stat -c %s "$f") bytes  $(file -b "$f" | cut -c1-80)" | tee -a "$R/07_fs_probe.txt"
  dump.erofs -s "$f" 2>&1 | head -n 12 >> "$R/07_fs_probe.txt" || true
done
echo "::endgroup::"

echo "::group::EROFS extraction proof"
fsck.erofs --help 2>&1 | head -n 25 | tee "$R/08_fsck_help.txt"
# smallest erofs partition first
SMALL=$(ls -S parts/*.img | tail -n 1)
echo "extract test on: $SMALL" | tee "$R/08_extract_test.txt"
mkdir -p ex_test
fsck.erofs --extract=ex_test --force --overwrite "$SMALL" > "$R/08_extract_log.txt" 2>&1
echo "fsck extract exit=$?" | tee -a "$R/08_extract_test.txt"
tail -n 5 "$R/08_extract_log.txt" | tee -a "$R/08_extract_test.txt"
echo "files: $(find ex_test -type f | wc -l)  dirs: $(find ex_test -type d | wc -l)  symlinks: $(find ex_test -type l | wc -l)" | tee -a "$R/08_extract_test.txt"
echo "xattr sample (selinux context preserved?):" | tee -a "$R/08_extract_test.txt"
( cd ex_test && f=$(find . -type f | head -n 1) && getfattr -d -m - "$f" 2>&1 | head -n 5 ) | tee -a "$R/08_extract_test.txt"
echo "::endgroup::"

cp images/anti_version.txt misc.txt "$R/" 2>/dev/null || true
df -h / | tee "$R/99_df_end.txt"
echo "DONE"
