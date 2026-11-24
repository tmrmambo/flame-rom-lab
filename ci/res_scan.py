#!/usr/bin/env python3
"""Scan the resource tables of every APK under a mount root for blocking 'unsupported software' style texts.
usage: res_scan.py <mount_root> <out_tsv>
Prints/writes: apk, resource (type/name), config, value (cut)."""
import os, re, subprocess, sys
from concurrent.futures import ProcessPoolExecutor

ROOT, OUT = sys.argv[1], sys.argv[2]
PAT = re.compile(
    r'unsupported software|software.{0,25}(is )?not supported|not supported.{0,25}(software|rom|system)|'
    r'unsupported (rom|system|version|build|firmware)|this (rom|system|software|version) .{0,40}(not|isn.t) (supported|compatible)|'
    r'不支持的软件|软件不受支持|不受支持的(软件|系统|版本)|不支持.{0,6}(此|该|当前)?(系统|软件|版本)|当前(系统|软件|版本).{0,8}不(受)?支持', re.I)
RES = re.compile(r'^\s+resource 0x[0-9a-fA-F]+ (\S+)')
VAL = re.compile(r'^\s+\(([^)]*)\)\s+(.*)$')


def scan(path):
    hits = []
    try:
        r = subprocess.run(['aapt2', 'dump', 'resources', path], capture_output=True, text=True, errors='replace', timeout=600)
    except Exception as e:  # noqa
        return path, hits, 'ERR ' + str(e)[:80]
    cur = ''
    for line in r.stdout.splitlines():
        m = RES.match(line)
        if m:
            cur = m.group(1); continue
        if '"' not in line: continue
        v = VAL.match(line)
        if v and PAT.search(v.group(2)):
            hits.append((cur, v.group(1) or 'default', v.group(2)[:200]))
    return path, hits, ('rc=%d' % r.returncode if r.returncode else '')


def main():
    apks = []
    for dp, _, fns in os.walk(ROOT):
        for f in fns:
            if f.endswith('.apk'):
                apks.append(os.path.join(dp, f))
    apks.sort(key=lambda p: -os.path.getsize(p))
    print('apks', len(apks), flush=True)
    n = bad = 0
    with open(OUT, 'w', encoding='utf-8') as fo, ProcessPoolExecutor(4) as ex:
        fo.write('apk\tresource\tconfig\tvalue\n')
        for path, hits, err in ex.map(scan, apks):
            if err:
                bad += 1
                if bad <= 10: print('  scan problem', path, err, flush=True)
            for res, cfg, val in hits:
                n += 1
                fo.write(f'{os.path.relpath(path, ROOT)}\t{res}\t{cfg}\t{val}\n')
    print('hits', n, 'problems', bad)


main()
