import csv, json, os, re, hashlib, collections
R = r'C:\Users\mujica\flame-lab'
C = os.path.join(R, 'certs_out')
A = os.path.join(R, 'analysis')

# 1) cert digest -> seinfo from every *_mac_permissions.xml
sei = {}
for fn in os.listdir(os.path.join(C, 'sec')):
    if 'mac_permissions' not in fn: continue
    txt = open(os.path.join(C, 'sec', fn), encoding='utf-8', errors='replace').read()
    for m in re.finditer(r'<signer\s+signature="([0-9a-fA-F\s]+)"\s*>(.*?)</signer>', txt, re.S):
        der = bytes.fromhex(re.sub(r'\s+', '', m.group(1)))
        dig = hashlib.sha256(der).hexdigest()
        s = re.search(r'seinfo value="([^"]+)"', m.group(2))
        sei[dig] = (s.group(1) if s else '?', fn.split('__')[-1].replace('_mac_permissions.xml', ''))
print('mac_permissions signers:', {k[:12]: v for k, v in sei.items()})

# 2) apk -> cert, package, uid
certs = {}
for r in csv.DictReader(open(os.path.join(C, 'apk_certs.tsv'), encoding='utf-8'), delimiter='\t'):
    certs[r['apk']] = r
man = {}
for l in open(os.path.join(R, 'bundle', 'meta', 'manifests.jsonl'), encoding='utf-8'):
    d = json.loads(l); man[d['part'] + d['path']] = d
rows = []
for k, r in certs.items():
    d = man.get(k) or man.get(k.replace('system/system/', 'system/system/'))
    rows.append({'apk': k, 'status': r['status'], 'sha': r['sha256'].split(';')[0], 'dn': r['dn'].split(';')[0][:60],
                 'pkg': (d or {}).get('package', ''), 'uid': (d or {}).get('sharedUserId', ''), 'persistent': (d or {}).get('persistent', ''),
                 'seinfo': sei.get(r['sha256'].split(';')[0], ('', ''))[0]})
print('apks joined with a manifest:', sum(1 for x in rows if x['pkg']), 'of', len(rows))
by = collections.Counter((x['sha'][:12], x['seinfo'], x['dn'][:40]) for x in rows if x['sha'])
print('top signers (sha12, seinfo, dn):')
for k, n in by.most_common(8): print('  ', n, k)

plat = {s for s, v in sei.items() if v[0] == 'platform'}
print('platform digests:', [p[:12] for p in plat])
plat_pk = {x['pkg'] for x in rows if x['sha'] in plat and x['pkg']}
print('platform-signed packages:', len(plat_pk), ' uid.system among them:', sum(1 for x in rows if x['sha'] in plat and x['uid'] == 'android.uid.system'))
other_uid_sys = [x for x in rows if x['uid'] == 'android.uid.system' and x['sha'] not in plat]
print('android.uid.system but NOT platform cert:', [x['pkg'] for x in other_uid_sys][:10])

# 3) plan x signing
plan = list(csv.DictReader(open(os.path.join(A, 'PLAN_TIERS.csv'), encoding='utf-8')))
pk2 = {x['pkg']: x for x in rows if x['pkg']}
out = []
for p in plan:
    t = p['target']; x = pk2.get(t)
    signed = 'platform' if (x and x['sha'] in plat) else ('other' if x else 'n/a')
    out.append({**p, 'signed_by': signed, 'uid': x['uid'] if x else '', 'cert12': x['sha'][:12] if x else ''})
with open(os.path.join(A, 'PLAN_SIGNING.csv'), 'w', newline='', encoding='utf-8') as f:
    w = csv.DictWriter(f, fieldnames=list(out[0].keys()), lineterminator='\n'); w.writeheader(); w.writerows(out)
c = collections.Counter((o['signed_by'], o['action']) for o in out if o['signed_by'] != 'n/a')
print('plan packages by (signed_by, action):', dict(c))
print('--- platform-signed targets that need MODIFICATION (PATCH/STUB/DISABLE):')
for o in out:
    if o['signed_by'] == 'platform' and o['action'] in ('PATCH', 'STUB', 'DISABLE', 'HOLD'):
        print(f"  {o['tier']:<4}{o['action']:<8}{o['target']:<44}uid={o['uid']:<22}{o['note_or_evidence'][:70]}")
print('--- platform-signed targets planned as pure REMOVE:', sum(1 for o in out if o['signed_by'] == 'platform' and o['action'] == 'REMOVE'))
for o in out:
    if o['signed_by'] == 'platform' and o['action'] == 'REMOVE':
        print(f"  {o['tier']:<4}{o['target']:<48}uid={o['uid']}")
# 4) non-package PATCH items (props, rc, jars...) classified separately
np = [o for o in out if o['signed_by'] == 'n/a' and o['action'] in ('PATCH', 'DISABLE', 'STUB')]
print('non-package modifications (props/rc/xml/jar/other):', len(np))
kinds = collections.Counter(('prop' if o['target'].startswith(('prop:', 'persist.', 'ro.')) else 'rc' if ':/etc/init' in o['target'] or '.rc:' in o['target'] else 'jar' if o['target'].endswith('.jar') or 'jar' in o['target'] else 'other') for o in np)
print(dict(kinds))
