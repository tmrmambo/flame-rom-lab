import os, re
D = r'C:\Users\mujica\flame-lab\jadx_src'
J = r'C:\Users\mujica\flame-lab\jadx_out'
lab = 'com.android.provision'
for dp, dns, fns in os.walk(os.path.join(D, lab)):
    depth = dp[len(D):].count(os.sep)
    if depth <= 3: print(dp[len(D):], len(fns), 'files')
    if depth >= 3: dns[:] = []
print()
txt = open(os.path.join(J, 'mcd.analysis.txt'), encoding='utf-8', errors='replace').read()
st = txt.split('--- strings')[-1].splitlines()
keys = ('http', '.com', '.cn', 'mid.', 'upload', 'report', 'cloud', 'server', 'xiaomi', 'miui', 'socket', 'ssl', 'curl', 'key', 'token', 'feature', 'octvm', 'cgroup')
sel = [s for s in st if any(k in s.lower() for k in keys)]
print('mcd strings total', len(st), 'selected', len(sel))
for s in sel[:60]: print('  ', s[:140])
imports = [l for l in txt.split('--- dynsyms')[1].split('--- strings')[0].splitlines() if l.strip()]
print('imports', len(imports), ' network-ish:', [i for i in imports if any(k in i for k in ('socket', 'connect', 'curl', 'SSL', 'getaddrinfo', 'send', 'recv'))][:15])
print('rc files:', open(os.path.join(J, 'mcd.rc_files.txt')).read()[:300])
