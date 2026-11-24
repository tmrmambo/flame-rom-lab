import os, re, collections, csv
R = r'C:\Users\mujica\flame-lab\reports'
jadx = collections.defaultdict(list)   # package -> [(agent, question)]
apex = collections.defaultdict(set)
pat = re.compile(r'needs?\s+JADX\s*:?\s*`?([A-Za-z0-9_.\-/ ]+?)`?\s*[:\-–—]\s*(.{5,220})', re.I)
pat_apex = re.compile(r'needs?\s+APEX\s*:?\s*`?([A-Za-z0-9_.,\- /]+)', re.I)
for code in sorted(os.listdir(R)):
    for fn in ('report.md', 'ledger.csv'):
        p = os.path.join(R, code, fn)
        if not os.path.exists(p): continue
        for line in open(p, encoding='utf-8-sig', errors='replace'):
            for m in pat.finditer(line):
                pk = m.group(1).strip().strip('`')
                q = m.group(2).strip().rstrip('|').strip()
                if len(pk) < 80: jadx[pk].append((code, q[:160]))
            for m in pat_apex.finditer(line):
                for a in re.split(r'[,/]', m.group(1)):
                    a = a.strip().strip('`')
                    if 2 < len(a) < 40: apex[a].add(code)
out = os.path.join(r'C:\Users\mujica\flame-lab\analysis', 'needs_jadx_apex.md')
with open(out, 'w', encoding='utf-8') as f:
    f.write('# JADX / APEX needs (auto-collected from reports)\n\n## JADX\n')
    for pk, lst in sorted(jadx.items(), key=lambda kv: -len(kv[1])):
        f.write(f'### {pk}  (asked by {sorted({a for a,_ in lst})})\n')
        seen = set()
        for a, q in lst:
            if q not in seen: f.write(f'- [{a}] {q}\n'); seen.add(q)
    f.write('\n## APEX\n')
    for a, s in sorted(apex.items()): f.write(f'- {a}  <- {sorted(s)}\n')
print('jadx packages:', len(jadx), ' questions:', sum(len(v) for v in jadx.values()))
for pk, lst in sorted(jadx.items(), key=lambda kv: -len(kv[1]))[:40]: print(f'  {len(lst):>2}  {pk}')
print('apex names:', sorted(apex))
