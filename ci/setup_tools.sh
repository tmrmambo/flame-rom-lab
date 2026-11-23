#!/usr/bin/env bash
# Install / fetch every tool the lab needs. Prints a sha256 for each downloaded binary
# so we can pin them later. Safe to re-run.
set -uo pipefail

BIN="$HOME/lab-bin"
mkdir -p "$BIN"
export PATH="$BIN:$PATH"
echo "$BIN" >> "${GITHUB_PATH:-/dev/null}"

echo "::group::apt"
sudo apt-get update -qq
sudo apt-get install -y -qq erofs-utils android-sdk-libsparse-utils aria2 zstd \
  openjdk-17-jdk-headless aapt apktool xxd file attr python3 unzip lz4 cpio binutils 2>&1 | tail -n 3
echo "::endgroup::"

echo "::group::partition tools (Rprop/aosp15_partition_tools, prebuilt AOSP15, glibc x86_64)"
BASE="https://raw.githubusercontent.com/Rprop/aosp15_partition_tools/HEAD/linux_glibc_x86_64"
for t in lpmake lpunpack lpdumps lpadd; do
  curl -fsSL --retry 3 -o "$BIN/$t" "$BASE/$t" && chmod +x "$BIN/$t" || echo "FAILED to fetch $t"
done
( cd "$BIN" && sha256sum lpmake lpunpack lpdumps lpadd 2>/dev/null )
echo "::endgroup::"

echo "::group::avbtool (AOSP external/avb)"
# googlesource serves raw files base64-encoded with ?format=TEXT
if curl -fsSL --retry 3 "https://android.googlesource.com/platform/external/avb/+/refs/heads/main/avbtool.py?format=TEXT" | base64 -d > "$BIN/avbtool.py" 2>/dev/null \
   && head -n1 "$BIN/avbtool.py" | grep -q python; then
  chmod +x "$BIN/avbtool.py"; ln -sf "$BIN/avbtool.py" "$BIN/avbtool"
  sha256sum "$BIN/avbtool.py"
else
  echo "FAILED to fetch avbtool from googlesource"; rm -f "$BIN/avbtool.py"
fi
echo "::endgroup::"

echo "::group::jadx"
JADX_TAG=$(curl -fsSL https://api.github.com/repos/skylot/jadx/releases/latest | python3 -c 'import sys,json;print(json.load(sys.stdin)["tag_name"])')
echo "jadx tag: $JADX_TAG"
curl -fsSL --retry 3 -o /tmp/jadx.zip "https://github.com/skylot/jadx/releases/download/${JADX_TAG}/jadx-${JADX_TAG#v}.zip" \
  && mkdir -p "$HOME/jadx" && unzip -q -o /tmp/jadx.zip -d "$HOME/jadx" \
  && ln -sf "$HOME/jadx/bin/jadx" "$BIN/jadx" && sha256sum /tmp/jadx.zip
echo "::endgroup::"

echo "::group::aapt2 (Google maven; apt's is too old for Android 16 resource tables)"
AAPT2_V=9.5.2-16666295
if curl -fsSL --retry 3 -o /tmp/aapt2.jar "https://dl.google.com/android/maven2/com/android/tools/build/aapt2/${AAPT2_V}/aapt2-${AAPT2_V}-linux.jar" \
   && unzip -q -o -j /tmp/aapt2.jar aapt2 -d "$BIN"; then
  chmod +x "$BIN/aapt2"; sha256sum /tmp/aapt2.jar; "$BIN/aapt2" version
else
  echo "FAILED to fetch new aapt2"
fi
echo "::endgroup::"

echo "== availability summary"
for t in mkfs.erofs fsck.erofs dump.erofs simg2img img2simg aria2c zstd java aapt aapt2 apktool lpmake lpunpack lpdumps lpadd avbtool jadx; do
  printf '%-12s ' "$t"; (command -v "$t" || echo MISSING)
done
echo "== erofs-utils version"; (fsck.erofs --help 2>&1 | head -n 3) || true
echo "== lp tools run?"
for t in lpunpack lpdumps lpmake; do
  printf '%-10s ' "$t"; ("$BIN/$t" --help >/dev/null 2>&1; echo "exit=$?") || true
done
python3 "$BIN/avbtool.py" version 2>&1 | head -n 2 || true
