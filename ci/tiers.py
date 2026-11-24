import csv, collections, os, re
A = r'C:\Users\mujica\flame-lab\analysis'
rows = list(csv.DictReader(open(os.path.join(A, 'ledger_merged.csv'), encoding='utf-8')))

# ---- owner/orchestrator rulings after the JADX round (target -> (verdict, tier, note))
OV = {
 'com.xiaomi.misettings':               ('KEEP', '-', 'removal crashes Settings boot receiver (J1: MiuiSettingsReceiver no try/catch)'),
 'com.miui.guardprovider':              ('HOLD', 'T3', 'verifier rejects path-based installs without it (J3); keep until tested'),
 'com.miui.securitycenter':             ('PATCH', 'T2', 'verification agent + permission UI: never remove; disable ad/report components, block hosts'),
 'com.miui.global.packageinstaller':    ('REMOVE', 'T2', 'after-install scanner/ads; system does not wait for it (J3)'),
 'com.xiaomi.mipicks':                  ('REMOVE', 'T2', 'GetApps; permission definer also in com.miui.system (J5)'),
 'com.android.providers.downloads.ui':  ('REMOVE', 'T2', 'disguised ad app; DocumentsUI serves VIEW_DOWNLOADS (J6)'),
 'com.android.htmlviewer':              ('PATCH', 'T2', 'disguised cloud-control client: keep provider+permission, disable upload/boot/OTA components (J6)'),
 'com.xiaomi.joyose':                   ('REMOVE', 'T2', 'cloud perf control; system_server null-checks, rebind spam only (J4)'),
 'com.miui.powerkeeper':                ('PATCH', 'T2', 'KEEP app (thermal brightness, 13 dependents); disable CloudUpdateJobService, block host (J4)'),
 'com.miui.misightservice':             ('DISABLE', 'T2', 'pure telemetry (J4)'),
 'com.miui.daemon':                     ('HOLD', 'T3', 'consumes cloud data, sets persist.sys.*; decide after Joyose/PowerKeeper test (J4)'),
 'com.miui.analytics':                  ('REMOVE', 'T2', 'guarded callers (J5/J1) but wifi-service rebinds ~1/s: check logcat/battery (J7)'),
 'com.xiaomi.account':                  ('REMOVE', 'T2', 'remove with cloud group; wizard steps skip when absent (J2)'),
 'com.miui.cloudservice':               ('REMOVE', 'T2', 'remove together with micloudsync (same uid); Settings entry hidden on Global (J1)'),
 'com.miui.micloudsync':                ('REMOVE', 'T2', 'same uid as cloudservice'),
 'com.miui.cloudbackup':                ('REMOVE', 'T2', 'consumer of PROVISION_COMPLETE broadcast; guarded (J2/J5)'),
 'com.xiaomi.finddevice':               ('REMOVE', 'T3', 'persistent, in core-apps list, Settings factory-reset check queries it (J5): test as DISABLE first'),
 'com.xiaomi.xmsf':                     ('REMOVE', 'T3', 'MiPush; owner OK; 12 packages carry own SDK'),
 'com.xiaomi.xmsfkeeper':               ('REMOVE', 'T3', 'MiPush keeper'),
 'com.miui.securityadd':                ('UNSURE', 'T3', 'game booster add-on with Firebase; Settings/Security Center reference it'),
 'com.miui.securitycore':               ('UNSURE', 'T3', 'disguised (ad/experiment hosts); dependencies unchecked'),
 'com.miui.home':                       ('KEEP', '-', 'one launcher must stay (provision provider)'),
 'com.android.provision':               ('KEEP', '-', 'no patch needed (J2)'),
 'com.qualcomm.location':               ('KEEP', '-', 'owner: Qualcomm untouched; upload prop unset (J6)'),
 'com.xiaomi.hypercomm':                ('KEEP', '-', 'modem framework, persistent (J6)'),
 'com.miui.vsimcore':                   ('KEEP', '-', 'no network (J6)'),
}
def tier(r):
    t = r['target']
    if t in OV: return OV[t]
    v, k = r['verdict'], r['risk']
    # Google/GMS-family packages are the owner's call, not telemetry: list them separately
    if t.startswith(('com.google.', 'com.android.vending')) and v in ('REMOVE', 'DISABLE', 'STUB', 'PATCH'):
        return (v, 'OPT', 'Google app: optional, owner decides (not Xiaomi telemetry)')
    # rows that are notes / sub-items rather than a package, property, rc service or file are kept in T-lists but flagged
    if v in ('KEEP', 'UNSURE'): return (v, '-', '')
    if v in ('REMOVE', 'DISABLE', 'PATCH', 'STUB'):
        if k == 'L': return (v, 'T1', '')
        if k == 'M': return (v, 'T2', '')
        return (v, 'T3', '')
    return (v, '-', '')
out = collections.defaultdict(list)
for r in rows:
    v, t, n = tier(r)
    if t == '-': continue
    out[t].append((r, v, n))
# one target may appear several times (several agents): keep the overriding / highest-tier row only once
def uniq(lst):
    seen = {}; res = []
    for r, v, n in lst:
        k = r['target']
        if k in seen: continue
        seen[k] = 1; res.append((r, v, n))
    return res
with open(os.path.join(A, 'PLAN_TIERS.csv'), 'w', newline='', encoding='utf-8') as f:
    w = csv.writer(f, lineterminator='\n')
    w.writerow(['tier', 'action', 'target', 'part', 'category', 'risk', 'source', 'note_or_evidence'])
    for t in ('T1', 'T2', 'T3', 'OPT'):
        for r, v, n in uniq(out[t]):
            w.writerow([t, v, r['target'], r['part'], r['category'], r['risk'], r['source'], (n or r['evidence'])[:300]])
for t in ('T1', 'T2', 'T3', 'OPT'):
    u = uniq(out[t])
    print(t, len(u), dict(collections.Counter(v for _, v, _ in u)))
print('OPT:', [r['target'] for r, _, _ in uniq(out['OPT'])])
print('T3:', [(r['target'], v) for r, v, _ in uniq(out['T3'])])
