#!/usr/bin/env python3
"""Search the JADX Java sources.  (ripgrep is not installed on this PC.)

usage: python ci\\jgrep.py <label[,label...]|all> <regex> [--ctx N] [--max M] [--files] [-i] [--path-filter SUBSTR]
  label    directory name under jadx_src\\ (e.g. com.android.settings, miui-services)
  --files  only list files with match counts (best first step on big apps)
  --ctx N  lines of context (default 0, max 6)    --max M  cap on printed matches (default 40)
Output lines:  <label>/<relative path>:<line>: text   (long lines are cut at 220 chars)
"""
import os, re, sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'jadx_src')
a = sys.argv[1:]
if len(a) < 2:
    print(__doc__); sys.exit(1)
labels, pat = a[0], a[1]
ctx = int(a[a.index('--ctx') + 1]) if '--ctx' in a else 0
mx = int(a[a.index('--max') + 1]) if '--max' in a else 40
pf = a[a.index('--path-filter') + 1] if '--path-filter' in a else ''
ctx = min(ctx, 6)
rx = re.compile(pat, re.I if '-i' in a else 0)
labs = sorted(os.listdir(ROOT)) if labels == 'all' else labels.split(',')
files_only = '--files' in a
printed = 0
counts = []
for lab in labs:
    base = os.path.join(ROOT, lab, 'sources')
    if not os.path.isdir(base):
        print('no such label:', lab); continue
    for dp, _, fns in os.walk(base):
        for fn in fns:
            if not fn.endswith('.java'): continue
            p = os.path.join(dp, fn)
            rel = lab + '/' + os.path.relpath(p, base).replace('\\', '/')
            if pf and pf not in rel: continue
            try:
                lines = open(p, encoding='utf-8', errors='replace').read().splitlines()
            except OSError:
                continue
            hits = [i for i, l in enumerate(lines) if rx.search(l)]
            if not hits: continue
            counts.append((len(hits), rel))
            if files_only: continue
            for i in hits:
                if printed >= mx: break
                lo, hi = max(0, i - ctx), min(len(lines), i + ctx + 1)
                for j in range(lo, hi):
                    print(f"{rel}:{j + 1}{':' if j == i else '-'} {lines[j].strip()[:220]}")
                if ctx: print('--')
                printed += 1
if files_only:
    counts.sort(reverse=True)
    for n, rel in counts[:mx]: print(f'{n:>4}  {rel}')
    print(f'({len(counts)} files with matches)')
else:
    total = sum(n for n, _ in counts)
    print(f'[{printed} shown / {total} matches in {len(counts)} files]')
