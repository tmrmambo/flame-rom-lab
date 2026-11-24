import os, csv, json, collections
B = r'C:\Users\mujica\flame-lab\bundle_apex'
info = open(os.path.join(B, 'list', 'apex_info.txt'), encoding='utf-8', errors='replace').read().split('== ')[1:]
ok = [i for i in info if 'EROFS' in i or 'ext2' in i or 'ext4' in i or 'Linux rev' in i]
print('apex entries', len(info), 'with a recognised fs payload', len(ok))
for i in info:
    h = i.splitlines()
    fs = [x for x in h if 'EROFS' in x or 'ext' in x.lower() and 'filesystem' in x.lower()]
    print(' ', h[0][:55].ljust(56), (fs[0][:50] if fs else (h[2][:50] if len(h) > 2 else '?')))
print()
fl = os.path.join(B, 'filelists')
print('filelists:', sorted(os.listdir(fl)) if os.path.isdir(fl) else 'none')
inv = os.path.join(B, 'meta', 'inventory.csv')
if os.path.exists(inv):
    rows = list(csv.DictReader(open(inv, encoding='utf-8')))
    print('apk rows in apexes:', len(rows))
    for r in rows:
        print('  ', r['part'], r['path'], r['package'], 'INTERNET' if r['internet'] == '1' else '', r['hosts_top'][:60])
for f in ('elf_endpoints.jsonl', 'jar_endpoints.jsonl'):
    p = os.path.join(B, 'meta', f)
    if os.path.exists(p):
        n = 0; hit = []
        for l in open(p, encoding='utf-8'):
            d = json.loads(l); n += 1
            if d.get('hosts'): hit.append((d['part'], d['path'], d['hosts'][:3]))
        print(f, 'rows', n, 'with Xiaomi-ish hosts', len(hit))
        for h in hit[:20]: print('   ', h)
