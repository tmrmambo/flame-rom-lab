import csv, json, collections, os
B = r'C:\Users\mujica\flame-lab\bundle'
rows = list(csv.DictReader(open(os.path.join(B, 'meta', 'inventory.csv'), encoding='utf-8')))
print('apk rows', len(rows))
bad = [r for r in rows if r['error']]
print('errors', len(bad))
for r in bad:
    print('  ', r['part'], r['path'], '| size', r['size'])
print()
by = collections.Counter(r['part'] for r in rows)
print('by part', dict(by))
ok = [r for r in rows if not r['error']]
net = [r for r in ok if r['internet'] == '1']
print('ok', len(ok), 'with INTERNET', len(net))
xi = [r for r in ok if any(k in (r['package'] + r['path']).lower() for k in ('miui', 'xiaomi', '.mi.', 'com.mi', 'milink', 'micloud', 'mipay'))]
print('xiaomi-ish by name', len(xi), ' of which INTERNET', sum(1 for r in xi if r['internet'] == '1'))
print('persistent', sum(1 for r in ok if r['persistent'] == '1'), ' boot_receiver', sum(1 for r in ok if r['boot_receiver'] == '1'))
print('with tracker keywords', sum(1 for r in ok if r['trackers']))
print('sharedUserId top:', collections.Counter(r['sharedUserId'] for r in ok if r['sharedUserId']).most_common(6))
print()
for f in ('jar_endpoints.jsonl', 'elf_endpoints.jsonl', 'apk_endpoints.jsonl'):
    n = 0; hosts = set()
    for l in open(os.path.join(B, 'meta', f), encoding='utf-8'):
        d = json.loads(l); n += 1
        hosts.update(d.get('hosts') or [])
    print(f, 'rows', n, 'distinct xiaomi-ish hosts', len(hosts))
print()
print('lp slot / flags:')
for l in open(os.path.join(B, 'meta', 'lpdump.txt'), encoding='utf-8'):
    if 'Header flags' in l or 'Metadata slot count' in l:
        print('  ', l.strip())
print()
print('boot headers:')
for f in ('boot_header.txt', 'vendor_boot_header.txt'):
    p = os.path.join(B, 'boot', f)
    print(' ', f, '->', open(p, encoding='utf-8', errors='replace').read()[:160].replace('\n', ' | '))
print('fstab files:')
for dp, dn, fn in os.walk(os.path.join(B, 'boot')):
    for x in fn:
        if 'fstab' in x:
            print('  ', os.path.relpath(os.path.join(dp, x), B))
