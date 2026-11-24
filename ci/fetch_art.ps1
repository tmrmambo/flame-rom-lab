param([long]$Run, [string]$Name, [string]$Tar, [string]$Dest)
# fetch one artifact of a run with aria2 and unpack its tar.gz into .\  (Name = artifact name, Tar = inner file)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$aria = (Get-ChildItem tools\aria2 -Recurse -Filter aria2c.exe | Select-Object -First 1).FullName
$id = (gh api "repos/tmrmambo/flame-rom-lab/actions/runs/$Run/artifacts" --jq '.artifacts[0].id' | Select-Object -First 1).Trim()
$tok = (gh auth token | Select-Object -First 1).Trim()
$r = curl.exe -s -D - -o NUL -H "Authorization: Bearer $tok" "https://api.github.com/repos/tmrmambo/flame-rom-lab/actions/artifacts/$id/zip"
$loc = $r -split "`n" | Where-Object { $_ -match '^location:' } | Select-Object -First 1
$url = ($loc -replace '^location:\s*','').Trim()
Remove-Item "dl\${Name}_art*", $Dest -Recurse -Force -ErrorAction SilentlyContinue
& $aria -x8 -s8 --console-log-level=error --summary-interval=0 -d dl -o "${Name}_art.zip" $url | Out-Null
Expand-Archive -Force "dl\${Name}_art.zip" "dl\${Name}_art"
tar -xzf "dl\${Name}_art\$Tar" -C .
"$Name -> $Dest : " + (Get-ChildItem $Dest -Recurse -File | Measure-Object).Count + " files"
