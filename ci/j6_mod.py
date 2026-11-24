import re,collections
d=collections.defaultdict(set)
for l in open('jadx_out/j6_cons.txt',encoding='utf8'):
    m=re.match(r'\s*(\S+?):(\d+)\s+(.*)',l)
    if not m: continue
    f,_,t=m.groups()
    for s in re.findall(r'getCloudData\w*\([^)]*?"([^"]+)"(?:\s*,\s*"([^"]+)")?',t):
        d[f.split('/')[0]+' '+f.split('/')[-1]].add('/'.join(x for x in s if x))
for k,v in sorted(d.items()): print(k,sorted(v))
