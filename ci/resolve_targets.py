import json, os, re, csv, collections
B = r'C:\Users\mujica\flame-lab\bundle'
M = [json.loads(l) for l in open(os.path.join(B, 'meta', 'manifests.jsonl'), encoding='utf-8')]
E = {}
for l in open(os.path.join(B, 'meta', 'apk_endpoints.jsonl'), encoding='utf-8'):
    d = json.loads(l); E[(d['part'], d['path'])] = d
byp = {m.get('package'): m for m in M if m.get('package')}

print('=== PUSH CHECK ===')
for key in ('xmsf', 'mipush', 'xmpush', 'push'):
    for m in M:
        if key in (m.get('package', '') + m['path']).lower():
            print(f"  by name '{key}': {m.get('package')}  [{m['part']}] {m['path']}")
for pk in ('com.google.android.gms', 'com.google.android.gsf', 'com.android.vending', 'com.xiaomi.xmsf'):
    m = byp.get(pk)
    print(f'  {pk:<28}', ('present [%s] %s' % (m['part'], m['path'])) if m else 'NOT in bundle')
gms = byp.get('com.google.android.gms')
if gms:
    print('  gms has C2DM/FCM receiver actions:', any('c2dm' in a.lower() or 'gcm' in a.lower() for c in gms.get('components', []) for a in c['actions']))
# who carries xmpush / mipush strings or services
hits = []
for (part, path), d in E.items():
    nl = [x for x in (d.get('native_libs') or []) if 'push' in x.lower()]
    tr = [t for t in (d.get('trackers') or []) if t in ('xmpush',)]
    if tr or nl:
        hits.append((part, path, d.get('package'), tr, nl))
print('  APKs with xmpush strings or push-native libs:', len(hits))
for h in hits[:30]: print('   ', h[0], h[1], h[2], h[3], h[4])
ms = []
for m in M:
    for c in m.get('components', []):
        if 'xiaomi.mipush' in c['name'].lower() or 'xmpush' in c['name'].lower() or 'mipush' in ' '.join(c['actions']).lower():
            ms.append((m.get('package'), c['kind'], c['name'][:60])); break
print('  packages declaring MiPush components/actions:', len(ms))
for x in ms[:25]: print('   ', x)

print('\n=== JADX TARGETS (resolved paths) ===')
want = ['com.android.settings', 'com.android.provision', 'com.android.systemui', 'com.miui.securitycenter',
        'com.miui.global.packageinstaller', 'com.miui.guardprovider', 'com.xiaomi.joyose', 'com.miui.powerkeeper',
        'com.miui.daemon', 'com.miui.misightservice', 'com.android.htmlviewer', 'com.xiaomi.finddevice',
        'com.xiaomi.account', 'com.miui.securityadd', 'com.miui.securitycore', 'com.xiaomi.mipicks',
        'com.android.providers.downloads.ui', 'com.qualcomm.location', 'com.xiaomi.hypercomm', 'com.miui.vsimcore',
        'com.miui.cloudservice', 'com.miui.cloudbackup', 'com.miui.analytics']
rows = []
for pk in want:
    m = byp.get(pk)
    if not m: print('  MISSING', pk); continue
    sz = m['size'] if 'size' in m else 0
    rows.append((m['part'], m['path'], pk))
# jars + native
fl = {}
for part in os.listdir(os.path.join(B, 'filelists')):
    p = part[:-4]
    for l in open(os.path.join(B, 'filelists', part), encoding='utf-8'):
        c = l.rstrip('\n').split('\t')
        if len(c) > 5 and c[1] == 'f': fl[(p, c[0])] = int(c[5] or 0)
for (p, path), sz in fl.items():
    if os.path.basename(path) in ('MiuiBooster.jar', 'miui-services.jar', 'miui-framework.jar', 'miui-wifi-service.jar', 'NetworkBoost.jar') :
        rows.append((p, path, 'JAR'))
for p, path, tag in rows:
    sz = fl.get((p, path), 0)
    print(f'  {p:<11}{path:<62}{sz/1e6:7.1f} MB  {tag}')
with open(r'C:\Users\mujica\flame-lab\analysis\jadx_targets.txt', 'w', encoding='utf-8') as f:
    for p, path, tag in rows:
        f.write(f'{p}|{path}|{tag}\n')
for (p, path), sz in fl.items():
    if path in ('/system_ext/bin/mcd', '/bin/mcd'): print('  native:', p, path, sz)
