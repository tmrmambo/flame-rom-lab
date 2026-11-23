#!/usr/bin/env python3
"""Dependency / reference queries over the analysis bundle.

usage:
  labq.py find  <substr>            packages / paths whose package or path contains substr
  labq.py who   <package>           everything that references <package> (see below)
  labq.py perm  <permission>        who defines / who requests a permission
  labq.py uid   <sharedUserId>      packages sharing a uid
  labq.py acts  <action-substr>     components listening for an intent action
  labq.py hosts <package>           endpoints / trackers found in that package
  labq.py summary                   counts only

`who` reports, for the target package:
  - same sharedUserId peers
  - packages that request a permission the target defines
  - other manifests / text files in the bundle whose text mentions the package name
    or any of its provider authorities (rc files, permission xml, sysconfig, vintf, overlays ...)
  - uses-library names it provides is not tracked (libs are listed under 'libs')
Output is capped (default 40 lines per section; use --all). Bundle dir: env LAB_BUNDLE or ./bundle
"""
import json
import os
import sys

B = os.environ.get('LAB_BUNDLE', os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'bundle'))
CAP = 10 ** 9 if '--all' in sys.argv else 40
args = [a for a in sys.argv[1:] if a != '--all']


def load_manifests():
    out = []
    p = os.path.join(B, 'meta', 'manifests.jsonl')
    with open(p, encoding='utf-8') as f:
        for l in f:
            out.append(json.loads(l))
    return out


def show(title, lines):
    print(f'## {title} ({len(lines)})')
    for x in lines[:CAP]:
        print('  ' + x)
    if len(lines) > CAP:
        print(f'  ... {len(lines) - CAP} more (use --all)')


def grep_tree(tokens, skip_prefix=None):
    hits = {}
    toks = [t.encode() for t in tokens if t]
    for sub in ('tree', 'manifests', 'boot'):
        base = os.path.join(B, sub)
        for dp, _, fns in os.walk(base):
            for fn in fns:
                p = os.path.join(dp, fn)
                rel = os.path.relpath(p, B).replace('\\', '/')
                if skip_prefix and rel.startswith(skip_prefix):
                    continue
                try:
                    data = open(p, 'rb').read()
                except OSError:
                    continue
                for t in toks:
                    if t in data:
                        hits.setdefault(rel, set()).add(t.decode())
    return hits


def main():
    if not args:
        print(__doc__)
        return
    cmd, a = args[0], (args[1] if len(args) > 1 else '')
    M = load_manifests()
    if cmd == 'summary':
        print('manifests', len(M), 'with package', sum(1 for m in M if m.get('package')))
        return
    if cmd == 'find':
        show('matches', [f"{m['part']:<10} {m.get('package', ''):<55} {m['path']}" for m in M
                         if a.lower() in (m.get('package', '') + m['path']).lower()])
    elif cmd == 'who':
        tgt = [m for m in M if m.get('package') == a]
        if not tgt:
            print('package not found (manifest may have failed); try: labq.py find', a)
            return
        t = tgt[0]
        print(f"# {a}  [{t['part']}] {t['path']}  uid={t.get('sharedUserId') or '-'} persistent={t.get('persistent')}")
        if t.get('sharedUserId'):
            show('same sharedUserId', [f"{m.get('package')}  [{m['part']}]" for m in M
                                       if m.get('sharedUserId') == t['sharedUserId'] and m.get('package') != a])
        dp = set(t.get('defined_perms', []))
        if dp:
            show('request a permission this package defines',
                 [f"{m.get('package')}  needs {sorted(dp & set(m.get('perms', [])))}" for m in M
                  if m.get('package') != a and dp & set(m.get('perms', []))])
        auths = [c['authorities'] for c in t.get('components', []) if c.get('authorities')]
        toks = [a] + [x for au in auths for x in au.split(';')]
        own = f"manifests/{t['part']}/{t['path'].lstrip('/')}.txt"
        hits = grep_tree(toks, skip_prefix=None)
        hits.pop(own, None)
        show('text files mentioning package/authorities',
             [f'{k}  <- {sorted(v)}' for k, v in sorted(hits.items())])
    elif cmd == 'perm':
        show('defined by', [f"{m.get('package')}  [{m['part']}]" for m in M if a in m.get('defined_perms', [])])
        show('requested by', [f"{m.get('package')}  [{m['part']}]" for m in M if a in m.get('perms', [])])
    elif cmd == 'uid':
        show('packages', [f"{m.get('package')}  [{m['part']}] {m['path']}" for m in M if m.get('sharedUserId') == a])
    elif cmd == 'acts':
        res = []
        for m in M:
            for c in m.get('components', []):
                if any(a in x for x in c['actions']):
                    res.append(f"{m.get('package')}  {c['kind']} {c['name']}  exported={c['exported']}")
        show('listeners', res)
    elif cmd == 'hosts':
        with open(os.path.join(B, 'meta', 'apk_endpoints.jsonl'), encoding='utf-8') as f:
            for l in f:
                d = json.loads(l)
                if d.get('package') == a:
                    print('hosts   :', d.get('hosts'))
                    print('trackers:', d.get('trackers'))
                    print('n_urls  :', d.get('n_urls'))
                    show('urls', d.get('urls') or [])
                    print('libs    :', d.get('native_libs'))
                    return
        print('not found')
    else:
        print(__doc__)


if __name__ == '__main__':
    main()
