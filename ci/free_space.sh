#!/usr/bin/env bash
# Remove preinstalled software we do not need. Prints df before/after.
echo "== df before"; df -h /
sudo rm -rf /usr/share/dotnet /usr/local/lib/android /opt/ghc /usr/local/.ghcup \
  /opt/hostedtoolcache/CodeQL /usr/local/share/boost /usr/share/swift /usr/local/julia* \
  /usr/local/share/chromium
sudo docker image prune --all --force >/dev/null 2>&1 || true
sudo apt-get clean
echo "== df after"; df -h /
