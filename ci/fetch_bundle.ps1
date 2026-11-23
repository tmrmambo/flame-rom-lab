param([long]$Run)
# Fetch the 'bundle' artifact of a run with aria2 (16 connections) and unpack it to .\bundle
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$aria = (Get-ChildItem tools\aria2 -Recurse -Filter aria2c.exe | Select-Object -First 1).FullName
$id = (gh api "repos/tmrmambo/flame-rom-lab/actions/runs/$Run/artifacts" --jq '.artifacts[0].id' | Select-Object -First 1).Trim()
$tok = (gh auth token | Select-Object -First 1).Trim()
$r = curl.exe -s -D - -o NUL -H "Authorization: Bearer $tok" "https://api.github.com/repos/tmrmambo/flame-rom-lab/actions/artifacts/$id/zip"
$loc = $r -split "`n" | Where-Object { $_ -match '^location:' } | Select-Object -First 1
$url = ($loc -replace '^location:\s*','').Trim()
Remove-Item dl\bundle_art*, bundle -Recurse -Force -ErrorAction SilentlyContinue
& $aria -x16 -s16 --console-log-level=error --summary-interval=0 -d dl -o bundle_art.zip $url | Out-Null
Expand-Archive -Force dl\bundle_art.zip dl\bundle_art
tar -xzf dl\bundle_art\bundle.tar.gz -C .
"files: " + (Get-ChildItem bundle -Recurse -File | Measure-Object).Count
