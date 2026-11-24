import os, tarfile, sys
from compression import zstd
J = r'C:\Users\mujica\flame-lab\jadx_out'
D = r'C:\Users\mujica\flame-lab\jadx_src'
os.makedirs(D, exist_ok=True)
tot = 0
for f in sorted(os.listdir(J)):
    if not f.endswith('.tar.zst'): continue
    label = f[:-8]
    dest = os.path.join(D, label)
    if os.path.isdir(dest) and os.listdir(dest):
        continue
    try:
        with zstd.open(os.path.join(J, f), 'rb') as z, tarfile.open(fileobj=z, mode='r|') as t:
            t.extractall(D, filter='data')
    except Exception as e:
        print('ERR', label, str(e)[:120]); continue
n = 0
for dp, _, fns in os.walk(D):
    n += sum(1 for x in fns if x.endswith('.java'))
print('labels', len(os.listdir(D)), 'java files', n)
for lab in ('mcd',):
    p = os.path.join(J, lab + '.analysis.txt')
    print(lab, 'analysis size', os.path.getsize(p) if os.path.exists(p) else 'missing')
    if os.path.exists(p) and os.path.getsize(p):
        print(open(p, encoding='utf-8', errors='replace').read()[:1500])
print('urls files sizes:', {f: os.path.getsize(os.path.join(J, f)) for f in os.listdir(J) if f.endswith('.urls.txt')})
