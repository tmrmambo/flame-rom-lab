import json, os
B = r'C:\Users\mujica\flame-lab\bundle_apex'
for l in open(os.path.join(B, 'meta', 'manifests.jsonl'), encoding='utf-8'):
    d = json.loads(l)
    if d.get('package') == 'com.android.bluetooth':
        print(d['part'], d['path'], 'uid=', d.get('sharedUserId'), 'persistent=', d.get('persistent'))
        perms = d.get('perms', [])
        print('perms (net/priv):', [p for p in perms if any(k in p for k in ('INTERNET', 'PHONE_STATE', 'LOCATION', 'ACCOUNTS', 'miui', 'xiaomi'))])
        print('xiaomi-ish perms/components:')
        for p in perms:
            if 'miui' in p.lower() or 'xiaomi' in p.lower(): print('   perm', p)
        for c in d.get('components', []):
            if any(k in (c['name'] + ' ' + ' '.join(c['actions'])).lower() for k in ('miui', 'xiaomi', 'analytic', 'mistat', 'cloud')):
                print('   comp', c['kind'], c['name'], c['actions'][:2])
for l in open(os.path.join(B, 'meta', 'apk_endpoints.jsonl'), encoding='utf-8'):
    d = json.loads(l)
    if d.get('package') == 'com.android.bluetooth':
        print('hosts:', d.get('hosts')); print('trackers:', d.get('trackers')); print('n_urls:', d.get('n_urls'))
        for u in (d.get('urls') or [])[:12]: print('  ', u)
# which other xiaomi-ish things live in the bt/wifi/tethering apexes (non-apk files)
for tsv in ('system.com.android.bt.tsv', 'system.com.android.wifi.tsv', 'system.com.google.android.tethering_compressed.tsv'):
    p = os.path.join(B, 'filelists', tsv)
    rows = [l.split('\t') for l in open(p, encoding='utf-8')][1:]
    names = [r[0] for r in rows if any(k in r[0].lower() for k in ('miui', 'xiaomi', 'mi_'))]
    print(tsv, 'files', len(rows), 'xiaomi-named', names[:10])
