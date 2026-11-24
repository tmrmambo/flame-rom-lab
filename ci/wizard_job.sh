#!/usr/bin/env bash
# Decompile wizard-related APKs WITH resources (R.string names, layouts, strings.xml) and report who shows
# "Unsupported software" (string software_version_mismatch). usage: wizard_job.sh <firmware_url>
set -uo pipefail
HERE="$PWD"; BIN="$HOME/lab-bin"; export PATH="$BIN:$PATH"
bash "$HERE/ci/prep_mount.sh" "$1" || exit 1
OUT="$HERE/wizard_out"; mkdir -p "$OUT"
export JAVA_OPTS="-Xmx10g"
declare -A T=(
  [SetupWizard]=/mnt/lab/system_ext/priv-app/SetupWizard/SetupWizard.apk
  [GooglePartnerSetup]=/mnt/lab/product/priv-app/GooglePartnerSetup/GooglePartnerSetup.apk
  [SearchSelector]=/mnt/lab/mi_ext/product/priv-app/SearchSelector/SearchSelector.apk
  [TrustonicSetup]=/mnt/lab/mi_ext/product/priv-app/com.trustonic.telecoms.setup/com.trustonic.telecoms.setup.apk
)
for k in "${!T[@]}"; do
  f="${T[$k]}"; [ -f "$f" ] || { echo "MISSING $k $f"; continue; }
  d="$HOME/jw/$k"; rm -rf "$d"; mkdir -p "$d"
  echo "[$(date +%H:%M:%S)] jadx $k ($(stat -c %s "$f") bytes)"
  timeout 900 jadx --threads-count 4 --show-bad-code --log-level ERROR -d "$d" "$f" > "$OUT/$k.jadx.log" 2>&1; echo "  rc=$?"
  echo "  java files: $(find "$d/sources" -name '*.java' 2>/dev/null | wc -l)"
  # strings that look like the blocker, in every values* folder
  grep -rIn -E 'Unsupported software|not supported by your device|software_version_mismatch|不支持的软件' "$d/resources/res" 2>/dev/null | head -n 20 > "$OUT/$k.string_hits.txt"
  grep -rIn --include=*.java -E 'software_version_mismatch|version_mismatch|isSupportedSoftware|UnsupportedSoftware' "$d/sources" 2>/dev/null | grep -v '/R\.java' | head -n 40 > "$OUT/$k.java_hits.txt"
  grep -rIn --include=*.java -E '"ro\.(miui|product|boot|build)\.[A-Za-z_.]+"|"persist\.[A-Za-z_.]+"' "$d/sources/com/google/android/setupwizard" 2>/dev/null | head -n 120 > "$OUT/$k.props_read.txt"
  tar -C "$HOME/jw" -cf - "$k" | zstd -T0 -3 -q -o "$OUT/$k.tar.zst"
  rm -rf "$d"
done
tar -czf wizard_out.tar.gz wizard_out; ls -l wizard_out.tar.gz
