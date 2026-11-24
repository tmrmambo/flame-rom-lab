import csv, os, collections, glob, sys
R = r'C:\Users\mujica\flame-lab\reports'
COLS = ['id','part','target','category','verdict','risk','evidence','depended_by','notes']
allrows = []
for code in sorted(os.listdir(R)):
    d = os.path.join(R, code)
    if not os.path.isdir(d): continue
    rp = os.path.join(d, 'report.md'); lp = os.path.join(d, 'ledger.csv')
    print(f"{code:<4} report={'%6d B' % os.path.getsize(rp) if os.path.exists(rp) else 'MISSING':<9} ledger={'yes' if os.path.exists(lp) else 'MISSING'}", end='')
    if os.path.exists(lp):
        try:
            rows = list(csv.DictReader(open(lp, encoding='utf-8-sig', newline='')))
        except Exception as e:
            print('  PARSE ERROR', e); continue
        bad = [r for r in rows if None in r or any(c not in r for c in COLS)]
        print(f"  rows={len(rows)} malformed={len(bad)}", end='')
        for r in rows:
            r.pop(None, None)
            r['source'] = code
            allrows.append(r)
    print()
print()
out = os.path.join(r'C:\Users\mujica\flame-lab\analysis', 'ledger_merged.csv')
with open(out, 'w', newline='', encoding='utf-8') as f:
    w = csv.DictWriter(f, fieldnames=COLS + ['source'], extrasaction='ignore', lineterminator='\n')
    w.writeheader(); w.writerows(allrows)
print('merged rows', len(allrows), '->', out)
print('by verdict', dict(collections.Counter(r['verdict'] for r in allrows)))
print('by category', dict(collections.Counter(r['category'] for r in allrows)))
act = [r for r in allrows if r['verdict'] in ('REMOVE','STUB','DISABLE','PATCH')]
print('actionable', len(act), dict(collections.Counter((r['verdict'], r['risk']) for r in act)))
# targets with conflicting verdicts across agents
by = collections.defaultdict(set)
for r in allrows: by[r['target']].add((r['source'], r['verdict']))
conf = {t: v for t, v in by.items() if len({x[1] for x in v}) > 1}
print('targets with conflicting verdicts:', len(conf))
for t, v in list(conf.items())[:15]: print('  ', t, sorted(v))
# REMOVE rows with no depended_by
print('REMOVE/STUB/DISABLE without depended_by:', sum(1 for r in act if r['verdict'] != 'PATCH' and not (r['depended_by'] or '').strip()))
