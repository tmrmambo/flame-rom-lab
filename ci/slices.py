import csv, collections, os, json
B = r'C:\Users\mujica\flame-lab\bundle'
rows = list(csv.DictReader(open(os.path.join(B, 'meta', 'inventory.csv'), encoding='utf-8')))
def top(r):
    p = r['path'].strip('/').split('/')
    if r['part'] == 'system':   # ext4 root: /system/<dir>/...
        p = p[1:] if p and p[0] == 'system' else p
    return p[0] if p else ''
c = collections.Counter((r['part'], top(r)) for r in rows)
for k, v in sorted(c.items()):
    print(f'{k[0]:<11}{k[1]:<14}{v}')
print()
# size of manifest text per part, so slices are balanced on reading effort
sz = collections.Counter()
for dp, _, fns in os.walk(os.path.join(B, 'manifests')):
    for f in fns:
        part = os.path.relpath(dp, os.path.join(B, 'manifests')).split(os.sep)[0]
        sz[part] += os.path.getsize(os.path.join(dp, f))
print('manifest text KB by part:', {k: v // 1024 for k, v in sz.items()})
# native side
els = [json.loads(l) for l in open(os.path.join(B, 'meta', 'elf_endpoints.jsonl'), encoding='utf-8')]
print('elf by part:', dict(collections.Counter(e['part'] for e in els)))
print('elf with hosts:', sum(1 for e in els if e.get('hosts')), ' with urls:', sum(1 for e in els if e.get('n_urls')))
# init rc services
rc = 0; svc = 0
for dp, _, fns in os.walk(os.path.join(B, 'tree')):
    for f in fns:
        if f.endswith('.rc'):
            rc += 1
            svc += sum(1 for l in open(os.path.join(dp, f), encoding='utf-8', errors='replace') if l.startswith('service '))
print('rc files', rc, 'services', svc)
# text tree size by part
ts = collections.Counter()
for dp, _, fns in os.walk(os.path.join(B, 'tree')):
    part = os.path.relpath(dp, os.path.join(B, 'tree')).split(os.sep)[0]
    ts[part] += len(fns)
print('text files by part:', dict(ts))
print('perf-ish pkgs:', [r['package'] for r in rows if any(k in r['package'].lower() for k in ('joyose', 'powerkeeper', 'daemon', 'perf', 'thermal', 'gamebooster', 'greenguard', 'cloud'))])
