import os, re, json, collections
ROOT = r'C:\Users\mujica\flame-lab'
J = os.path.join(ROOT, 'jadx_src')
pats = [r'unsupported[_ ]?software', r'software[_ ]?(is[_ ]?)?not[_ ]?supported', r'UnsupportedSoftware', r'not_support(ed)?_software',
        r'unsupported[_ ]?(rom|version|build|system|device)', r'EXCEPTION_UNSUPPORTED|unsupportedRom|isUnsupported']
rx = re.compile('|'.join(pats), re.I)
hits = collections.defaultdict(list)
for lab in sorted(os.listdir(J)):
    base = os.path.join(J, lab, 'sources')
    for dp, _, fns in os.walk(base):
        for fn in fns:
            if not fn.endswith('.java'): continue
            p = os.path.join(dp, fn)
            try:
                for i, l in enumerate(open(p, encoding='utf-8', errors='replace'), 1):
                    if rx.search(l):
                        hits[lab].append((os.path.relpath(p, base).replace('\\', '/'), i, l.strip()[:200]))
            except OSError:
                pass
print('labels with hits:', {k: len(v) for k, v in hits.items()})
for lab, v in hits.items():
    for h in v[:12]: print(f'  {lab}/{h[0]}:{h[1]}: {h[2]}')
print()
# text files in bundle (props, xml, rc, manifests, boot)
rx2 = re.compile(r'unsupported[_ ]?software|software[_ ]?not[_ ]?supported|UnsupportedSoftware', re.I)
n = 0
for sub in ('bundle', 'bundle_boot', 'bundle_apex'):
    for dp, _, fns in os.walk(os.path.join(ROOT, sub)):
        for fn in fns:
            p = os.path.join(dp, fn)
            try:
                if os.path.getsize(p) > 6_000_000: continue
                for i, l in enumerate(open(p, encoding='utf-8', errors='replace'), 1):
                    if rx2.search(l):
                        n += 1
                        if n <= 15: print('  BUNDLE', os.path.relpath(p, ROOT), i, l.strip()[:200])
            except OSError:
                pass
print('bundle text hits:', n)
