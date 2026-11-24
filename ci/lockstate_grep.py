import os, re, json, csv
R = r'C:\Users\mujica\flame-lab'
rx = re.compile(r'lockstate|lock_region|customized\.region|ro\.miui\.build\.region|boot\.hwc\b|show_version_mismatch', re.I)
seen = 0
for sub in ('tree', 'boot'):
    for dp, _, fns in os.walk(os.path.join(R, 'bundle', sub)):
        for fn in fns:
            p = os.path.join(dp, fn)
            try:
                for i, l in enumerate(open(p, encoding='utf-8', errors='replace'), 1):
                    if rx.search(l) and not l.lstrip().startswith('#'):
                        seen += 1
                        if seen <= 40: print(os.path.relpath(p, os.path.join(R, 'bundle')).replace('\\', '/') + f':{i}: ' + l.strip()[:170])
            except OSError:
                pass
for dp, _, fns in os.walk(os.path.join(R, 'bundle_boot')):
    for fn in fns:
        p = os.path.join(dp, fn)
        try:
            for i, l in enumerate(open(p, encoding='utf-8', errors='replace'), 1):
                if rx.search(l):
                    print('BOOT ' + os.path.relpath(p, R).replace('\\', '/') + f':{i}: ' + l.strip()[:200])
        except OSError:
            pass
print('total text hits in bundle/tree+boot:', seen)

# --- targets for decompiling the platform-signed packages that the plan wants to modify (not yet decompiled)
done = set(os.listdir(os.path.join(R, 'jadx_src')))
man = {}
for l in open(os.path.join(R, 'bundle', 'meta', 'manifests.jsonl'), encoding='utf-8'):
    d = json.loads(l)
    if d.get('package'): man[d['package']] = d
need = []
for r in csv.DictReader(open(os.path.join(R, 'analysis', 'PLAN_SIGNING.csv'), encoding='utf-8')):
    if r['signed_by'] == 'platform' and r['action'] in ('PATCH', 'STUB', 'DISABLE', 'HOLD', 'UNSURE') and r['target'] not in done:
        m = man.get(r['target'])
        if m: need.append((r['target'], m['part'], m['path']))
extra = ['com.android.phone', 'com.android.updater', 'com.miui.home', 'com.mi.android.globallauncher']
for pk in extra:
    if pk in man and pk not in done and pk not in [n[0] for n in need]:
        need.append((pk, man[pk]['part'], man[pk]['path']))
with open(os.path.join(R, 'analysis', 'jadx_targets2.txt'), 'w', encoding='utf-8') as f:
    for pk, part, path in need: f.write(f'{part}|{path}|{pk}\n')
print('targets2:', len(need))
for pk, part, path in need: print('  ', pk, part, path)
