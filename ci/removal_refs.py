import csv, json, os, re, collections, time
R = r'C:\Users\mujica\flame-lab'
A = os.path.join(R, 'analysis')
plan = list(csv.DictReader(open(os.path.join(A, 'PLAN_SIGNING.csv'), encoding='utf-8')))
rm = {p['target'] for p in plan if p['action'] == 'REMOVE' and p['signed_by'] in ('platform', 'other')}
dis = {p['target'] for p in plan if p['action'] in ('DISABLE', 'STUB', 'HOLD') and p['signed_by'] == 'platform'}
targets = sorted(rm | dis)
removed_all = {p['target'] for p in plan if p['action'] == 'REMOVE'}
print('targets', len(targets), '(REMOVE', len(rm), '+ DISABLE/STUB/HOLD', len(dis), ')')
man = {}
for l in open(os.path.join(R, 'bundle', 'meta', 'manifests.jsonl'), encoding='utf-8'):
    d = json.loads(l)
    if d.get('package'): man[d['package']] = d
# build matchers: package name; plus provider authorities of the target
auth = {}
for t in targets:
    m = man.get(t)
    if not m: continue
    au = [a for c in m.get('components', []) for a in (c.get('authorities') or '').split(';') if a]
    auth[t] = set(au)
tok = {}
for t in targets:
    tok[t] = [t] + sorted(auth.get(t, []))
big = re.compile('|'.join(re.escape(x) for t in targets for x in tok[t]))
inv = {}
for t in targets:
    for x in tok[t]: inv[x] = t
refs = collections.defaultdict(lambda: collections.defaultdict(list))   # target -> source -> [(file,line,text)]
t0 = time.time()
J = os.path.join(R, 'jadx_src')
for lab in sorted(os.listdir(J)):
    if lab in removed_all:  # code that disappears with its package is not a dependent
        continue
    base = os.path.join(J, lab, 'sources')
    for dp, _, fns in os.walk(base):
        for fn in fns:
            if not fn.endswith('.java') or fn == 'R.java': continue
            p = os.path.join(dp, fn)
            try:
                txt = open(p, encoding='utf-8', errors='replace').read()
            except OSError:
                continue
            if not big.search(txt): continue
            for i, l in enumerate(txt.splitlines(), 1):
                for m in big.finditer(l):
                    t = inv[m.group(0)]
                    if t == lab: continue
                    if len(refs[t][lab]) < 4:
                        refs[t][lab].append((os.path.relpath(p, base).replace('\\', '/'), i, l.strip()[:150]))
print('java scan done in', int(time.time() - t0), 's')
# manifests of kept packages + xml/rc/prop text
bundle_hits = collections.defaultdict(lambda: collections.defaultdict(list))
for sub in ('manifests', 'tree'):
    for dp, _, fns in os.walk(os.path.join(R, 'bundle', sub)):
        for fn in fns:
            p = os.path.join(dp, fn)
            try:
                if os.path.getsize(p) > 3_000_000: continue
                txt = open(p, encoding='utf-8', errors='replace').read()
            except OSError:
                continue
            m = big.search(txt)
            if not m: continue
            rel = os.path.relpath(p, os.path.join(R, 'bundle')).replace('\\', '/')
            # skip manifest of removed packages themselves
            if sub == 'manifests':
                owner = None
                for pk, d in man.items():
                    if rel.endswith((d['path'].lstrip('/') + '.txt')) and rel.split('/')[1] == d['part']:
                        owner = pk; break
                if owner in removed_all: continue
            for mm in big.finditer(txt):
                t = inv[mm.group(0)]
                if rel not in bundle_hits[t][sub] and len(bundle_hits[t][sub]) < 6:
                    bundle_hits[t][sub].append(rel)
# permissions defined by the target and requested by kept packages
perm_users = {}
for t in targets:
    m = man.get(t)
    if not m: continue
    dp = set(m.get('defined_perms', []))
    if not dp: continue
    us = [pk for pk, d in man.items() if pk != t and pk not in removed_all and dp & set(d.get('perms', []))]
    if us: perm_users[t] = us
res = []
for t in targets:
    r = refs.get(t, {})
    b = bundle_hits.get(t, {})
    pu = perm_users.get(t, [])
    res.append({'target': t, 'action': next(p['action'] for p in plan if p['target'] == t), 'tier': next(p['tier'] for p in plan if p['target'] == t),
                'signed_by': next(p['signed_by'] for p in plan if p['target'] == t), 'uid': (man.get(t) or {}).get('sharedUserId', ''),
                'code_ref_labels': sorted(r.keys()), 'n_code_refs': sum(len(v) for v in r.values()),
                'manifest_refs': b.get('manifests', []), 'xml_rc_refs': b.get('tree', []), 'perm_users_kept': pu[:12], 'n_perm_users': len(pu),
                'samples': {k: v[:2] for k, v in list(r.items())[:5]}})
json.dump(res, open(os.path.join(A, 'removal_refs.json'), 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
clean = [x for x in res if not x['code_ref_labels'] and not x['manifest_refs'] and not x['n_perm_users']]
print('no code/manifest/permission dependents found:', len(clean), 'of', len(res))
print('  clean REMOVE:', sum(1 for x in clean if x['action'] == 'REMOVE'), ' clean DISABLE/STUB/HOLD:', sum(1 for x in clean if x['action'] != 'REMOVE'))
print('with dependents:')
for x in res:
    if x in clean: continue
    print(f"  {x['tier']:<3}{x['action']:<8}{x['target']:<42} code_labels={x['code_ref_labels'][:5]} manifest={len(x['manifest_refs'])} xml/rc={len(x['xml_rc_refs'])} perm_users={x['n_perm_users']}")
