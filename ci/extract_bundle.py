#!/usr/bin/env python3
"""Walk mounted partitions and emit a compact, text-only analysis bundle.

usage: extract_bundle.py <mount_root> <out_dir>
Run as root (needs to read every file and its security.selinux xattr).
"""
import csv
import hashlib
import json
import multiprocessing as mp
import os
import re
import shutil
import subprocess
import sys
import time
import zipfile
from concurrent.futures import ProcessPoolExecutor, ThreadPoolExecutor

MNT, OUT = sys.argv[1], sys.argv[2]
T0 = time.time()


def log(*a):
    print(f"[{time.time() - T0:6.0f}s]", *a, flush=True)


TEXT_EXT = {'.rc', '.prop', '.xml', '.conf', '.cfg', '.json', '.txt', '.sh', '.policy', '.cil',
            '.te', '.ini', '.properties', '.list', '.csv', '.yaml', '.yml', '.rules', '.mk'}
TEXT_NAMES = {'hosts', 'build.prop', 'default.prop', 'file_contexts', 'property_contexts',
              'service_contexts', 'hwservice_contexts', 'vndservice_contexts', 'seapp_contexts',
              'fstab', 'passwd', 'group', 'ueventd.rc'}
SKIP_COPY = ('/media/', '/fonts/')
TEXT_MAX = 4 << 20

URL_RE = re.compile(rb'https?://[A-Za-z0-9._~:/?#@!$&()*+,;=%\-\[\]]{3,200}')
HOST_RE = re.compile(
    rb'(?<![a-z0-9.\-])(?:[a-z0-9][a-z0-9.\-]{0,60}\.)?'
    rb'(?:mi\.com|xiaomi\.(?:com|net|cn)|miui\.com|mi-img\.com|mifile\.cn|duokan\.com|'
    rb'xmpush\.[a-z.]{2,15}|mistat\.[a-z.]{2,15}|micloud\.[a-z.]{2,15}|miwifi\.com|mipay\.com|'
    rb'mi-idc\.com|miui\.[a-z]{2,6})', re.I)
SO_RE = re.compile(rb'lib[A-Za-z0-9_.+\-]{1,60}\.so(?:\.\d+)*')
TRACKERS = [b'umeng', b'appsflyer', b'adjust.com', b'firebase', b'crashlytics', b'bugly', b'sentry',
            b'google-analytics', b'jpush', b'getui', b'baidu', b'tencent', b'alipay', b'facebook',
            b'inmobi', b'mintegral', b'unity3d', b'applovin', b'ironsource', b'mob.com', b'xmpush',
            b'mistat', b'joyose', b'analytics']
ATTR = re.compile(r'^A: (\S+?)(?:\(0x[0-9a-fA-F]+\))?=(.*)$')
NS = 'http://schemas.android.com/apk/res/android:'


# ---------------------------------------------------------------- manifest parsing
def aval(v):
    m = re.match(r'"(.*)" \(Raw: ', v)
    if m:
        return m.group(1)
    m = re.match(r'"(.*)"$', v)
    if m:
        return m.group(1)
    m = re.match(r'\(type 0x(\w+)\)0x([0-9a-fA-F]+)', v)
    if m:
        t, x = m.groups()
        n = int(x, 16)
        return (n != 0) if t == '12' else n
    return v


def parse_manifest(txt):
    root = None
    stack = []
    for line in txt.splitlines():
        s = line.lstrip()
        ind = len(line) - len(s)
        if s.startswith('E: '):
            el = {'name': s[3:].split(' (line')[0].strip(), 'attrs': {}, 'ch': []}
            while stack and stack[-1][0] >= ind:
                stack.pop()
            if stack:
                stack[-1][1]['ch'].append(el)
            elif root is None:
                root = el
            stack.append((ind, el))
        elif s.startswith('A: ') and stack:
            m = ATTR.match(s)
            if m:
                an = m.group(1).replace(NS, 'android:')
                stack[-1][1]['attrs'][an] = aval(m.group(2))
    return root


def summarize_manifest(root):
    m = {'package': '', 'sharedUserId': '', 'persistent': False, 'coreApp': False, 'minSdk': '',
         'targetSdk': '', 'perms': [], 'defined_perms': [], 'libs': [], 'components': [],
         'app_class': '', 'app_process': ''}
    if not root:
        return m
    a = root['attrs']
    m['package'] = a.get('package', '')
    m['sharedUserId'] = a.get('android:sharedUserId', '')
    m['coreApp'] = bool(a.get('coreApp', False))
    for c in root['ch']:
        n, ca = c['name'], c['attrs']
        if n == 'uses-sdk':
            m['minSdk'] = ca.get('android:minSdkVersion', '')
            m['targetSdk'] = ca.get('android:targetSdkVersion', '')
        elif n.startswith('uses-permission'):
            m['perms'].append(ca.get('android:name', ''))
        elif n == 'permission':
            m['defined_perms'].append(ca.get('android:name', ''))
        elif n == 'application':
            m['persistent'] = bool(ca.get('android:persistent', False))
            m['app_class'] = ca.get('android:name', '')
            m['app_process'] = ca.get('android:process', '')
            for k in c['ch']:
                kn, ka = k['name'], k['attrs']
                if kn == 'uses-library':
                    m['libs'].append(ka.get('android:name', ''))
                elif kn in ('activity', 'activity-alias', 'service', 'receiver', 'provider'):
                    actions = []
                    for f in k['ch']:
                        if f['name'] == 'intent-filter':
                            actions += [x['attrs'].get('android:name', '') for x in f['ch'] if x['name'] == 'action']
                    m['components'].append({
                        'kind': kn, 'name': ka.get('android:name', ''),
                        'exported': ka.get('android:exported', None),
                        'enabled': ka.get('android:enabled', None),
                        'process': ka.get('android:process', ''),
                        'permission': ka.get('android:permission', ''),
                        'authorities': ka.get('android:authorities', ''),
                        'target': ka.get('android:targetActivity', ''),
                        'actions': actions})
    return m


# ---------------------------------------------------------------- byte scanning
def scan_bytes(blob, acc):
    for u in URL_RE.findall(blob):
        acc['urls'].add(u.decode('latin1'))
    for h in HOST_RE.findall(blob):
        pass
    for m in HOST_RE.finditer(blob):
        acc['hosts'].add(m.group(0).decode('latin1').lower())
    low = blob.lower()
    for t in TRACKERS:
        if t in low:
            acc['trackers'].add(t.decode())


def new_acc():
    return {'urls': set(), 'hosts': set(), 'trackers': set()}


def finish_acc(acc, cap=300):
    return {'urls': sorted(acc['urls'])[:cap], 'n_urls': len(acc['urls']),
            'hosts': sorted(acc['hosts'])[:100], 'trackers': sorted(acc['trackers'])}


def scan_zip(path, dex=True):
    acc = new_acc()
    libs = set()
    n_dex = 0
    try:
        with zipfile.ZipFile(path) as z:
            for i in z.infolist():
                nm = i.filename
                if nm.startswith('lib/') and nm.endswith('.so'):
                    libs.add(nm.split('/')[-1])
                if i.file_size > 120 << 20:
                    continue
                low = nm.lower()
                if re.fullmatch(r'classes\d*\.dex', nm) and dex:
                    scan_bytes(z.read(nm), acc)
                    n_dex += 1
                elif nm == 'resources.arsc':
                    scan_bytes(z.read(nm), acc)
                elif nm.startswith('assets/') and i.file_size < 512 << 10 and \
                        low.endswith(('.json', '.xml', '.txt', '.properties', '.conf', '.cfg', '.js', '.html')):
                    scan_bytes(z.read(nm), acc)
    except Exception as e:  # noqa
        return finish_acc(acc) | {'native_libs': sorted(libs), 'n_dex': n_dex, 'zip_error': str(e)[:120]}
    return finish_acc(acc) | {'native_libs': sorted(libs), 'n_dex': n_dex}


def scan_apk(args):
    part, rel, p = args
    res = {'part': part, 'path': rel, 'size': os.path.getsize(p)}
    if res['size'] == 0:
        res['error'] = 'zero-byte placeholder'
        res['scan'] = {}
        return res
    last = ''
    try:
        for exe in ('aapt2', '/usr/bin/aapt2'):   # new Google build first, apt build as fallback
            r = subprocess.run([exe, 'dump', 'xmltree', '--file', 'AndroidManifest.xml', p],
                               capture_output=True, text=True, timeout=180)
            txt = r.stdout
            if r.returncode == 0 and txt:
                d = os.path.join(OUT, 'manifests', part, rel.lstrip('/') + '.txt')
                os.makedirs(os.path.dirname(d), exist_ok=True)
                with open(d, 'w', encoding='utf-8') as f:
                    f.write(txt)
                res['manifest'] = summarize_manifest(parse_manifest(txt))
                last = ''
                break
            last = (r.stderr or 'empty manifest')[:200]
        if last:
            res['error'] = last
    except Exception as e:  # noqa
        res['error'] = str(e)[:200]
    res['scan'] = scan_zip(p)
    return res


def scan_jar(args):
    part, rel, p = args
    return {'part': part, 'path': rel, 'size': os.path.getsize(p), 'scan': scan_zip(p)}


def scan_elf(args):
    part, rel, p = args
    acc = new_acc()
    try:
        if os.path.getsize(p) <= 60 << 20:
            with open(p, 'rb') as f:
                blob = f.read()
            scan_bytes(blob, acc)
            so = sorted({m.decode('latin1') for m in SO_RE.findall(blob)})[:60]
        else:
            so = []
    except Exception as e:  # noqa
        return {'part': part, 'path': rel, 'error': str(e)[:100]}
    return {'part': part, 'path': rel, 'so_refs': so, **finish_acc(acc, 60)}


# ---------------------------------------------------------------- filesystem walk
def selinux(p):
    try:
        return os.getxattr(p, 'security.selinux', follow_symlinks=False).rstrip(b'\0').decode()
    except OSError:
        return ''


def describe(args):
    p, rel, st = args
    import stat as S
    mode = S.S_IMODE(st.st_mode)
    if S.S_ISDIR(st.st_mode):
        t = 'd'
    elif S.S_ISLNK(st.st_mode):
        t = 'l'
    elif S.S_ISREG(st.st_mode):
        t = 'f'
    else:
        t = 'o'
    sha = magic = link = ''
    if t == 'l':
        try:
            link = os.readlink(p)
        except OSError:
            pass
    elif t == 'f':
        h = hashlib.sha256()
        try:
            with open(p, 'rb') as f:
                first = True
                while True:
                    b = f.read(1 << 20)
                    if not b:
                        break
                    if first:
                        magic = 'elf' if b[:4] == b'\x7fELF' else 'zip' if b[:4] == b'PK\x03\x04' else ''
                        first = False
                    h.update(b)
            sha = h.hexdigest()
        except OSError as e:
            sha = f'ERR:{e.errno}'
    return [rel, t, oct(mode)[2:], st.st_uid, st.st_gid, st.st_size, selinux(p), sha, magic, link]


def want_text(rel, size):
    if size > TEXT_MAX or any(s in rel for s in SKIP_COPY):
        return False
    base = os.path.basename(rel)
    ext = os.path.splitext(base)[1].lower()
    return ext in TEXT_EXT or base in TEXT_NAMES or '/etc/' in rel or base.startswith(('init', 'fstab'))


def sniff_text(p):
    try:
        with open(p, 'rb') as f:
            return b'\0' not in f.read(8192)
    except OSError:
        return False


def scan_part(part):
    root = os.path.join(MNT, part)
    entries = []
    for dp, dns, fns in os.walk(root):
        for nm in dns + fns:
            p = os.path.join(dp, nm)
            try:
                st = os.lstat(p)
            except OSError:
                continue
            rel = '/' + os.path.relpath(p, root).replace(os.sep, '/')
            entries.append((p, rel, st))
    with ThreadPoolExecutor(4) as ex:
        rows = list(ex.map(describe, entries, chunksize=64))
    rows.sort(key=lambda r: r[0])
    os.makedirs(os.path.join(OUT, 'filelists'), exist_ok=True)
    with open(os.path.join(OUT, 'filelists', f'{part}.tsv'), 'w', newline='', encoding='utf-8') as f:
        w = csv.writer(f, delimiter='\t', lineterminator='\n')
        w.writerow(['path', 'type', 'mode', 'uid', 'gid', 'size', 'selinux', 'sha256', 'magic', 'link'])
        w.writerows(rows)
    apks, jars, elfs = [], [], []
    ncopied = bcopied = 0
    for (p, rel, st), r in zip(sorted(entries, key=lambda e: e[1]), rows):
        if r[1] != 'f':
            continue
        if rel.endswith('.apk'):
            apks.append((part, rel, p))
        elif rel.endswith('.jar') and ('/framework/' in rel or '/javalib/' in rel):
            jars.append((part, rel, p))
        elif r[8] == 'elf' and any(x in rel for x in ('/bin/', '/sbin/', '/xbin/')):
            elfs.append((part, rel, p))
        if r[8] == '' and want_text(rel, r[5]) and sniff_text(p):
            d = os.path.join(OUT, 'tree', part, rel.lstrip('/'))
            try:
                os.makedirs(os.path.dirname(d), exist_ok=True)
                shutil.copyfile(p, d)
                ncopied += 1
                bcopied += r[5]
            except OSError:
                pass
    log(f'{part}: entries={len(rows)} apk={len(apks)} jar={len(jars)} elf_bin={len(elfs)} '
        f'text_copied={ncopied} ({bcopied / 1e6:.1f} MB)')
    return apks, jars, elfs


def main():
    os.makedirs(os.path.join(OUT, 'meta'), exist_ok=True)
    apks, jars, elfs = [], [], []
    for part in sorted(os.listdir(MNT)):
        a, j, e = scan_part(part)
        apks += a
        jars += j
        elfs += e
    log(f'total apk={len(apks)} jar={len(jars)} elf={len(elfs)}; scanning (4 procs)')
    ctx = mp.get_context('fork')
    with ProcessPoolExecutor(4, mp_context=ctx) as ex:
        apk_res = list(ex.map(scan_apk, apks, chunksize=4))
        log('apk scan done')
        jar_res = list(ex.map(scan_jar, jars, chunksize=2))
        elf_res = list(ex.map(scan_elf, elfs, chunksize=8))
    log('all scans done; writing outputs')

    inv_cols = ['part', 'path', 'size', 'package', 'sharedUserId', 'persistent', 'coreApp', 'priv_app',
                'minSdk', 'targetSdk', 'n_perms', 'internet', 'net_perms', 'n_activity', 'n_service',
                'n_receiver', 'n_provider', 'n_exported', 'boot_receiver', 'n_urls', 'n_hosts', 'hosts_top',
                'trackers', 'n_native_libs', 'error']
    with open(os.path.join(OUT, 'meta', 'inventory.csv'), 'w', newline='', encoding='utf-8') as fi, \
            open(os.path.join(OUT, 'meta', 'manifests.jsonl'), 'w', encoding='utf-8') as fm, \
            open(os.path.join(OUT, 'meta', 'components.tsv'), 'w', encoding='utf-8') as fc, \
            open(os.path.join(OUT, 'meta', 'apk_endpoints.jsonl'), 'w', encoding='utf-8') as fe:
        w = csv.writer(fi, lineterminator='\n')
        w.writerow(inv_cols)
        fc.write('\t'.join(['part', 'apk', 'package', 'kind', 'name', 'exported', 'enabled', 'process',
                            'permission', 'authorities', 'actions']) + '\n')
        for r in sorted(apk_res, key=lambda x: (x['part'], x['path'])):
            mf = r.get('manifest') or {}
            sc = r.get('scan', {})
            perms = mf.get('perms', [])
            comps = mf.get('components', [])
            net = [p.split('.')[-1] for p in perms if any(k in p for k in (
                'INTERNET', 'ACCESS_NETWORK_STATE', 'ACCESS_WIFI_STATE', 'CHANGE_NETWORK_STATE',
                'READ_PHONE_STATE', 'READ_PRIVILEGED_PHONE_STATE', 'GET_ACCOUNTS', 'ACCESS_FINE_LOCATION',
                'READ_SMS', 'READ_CONTACTS', 'READ_CALL_LOG', 'RECEIVE_BOOT_COMPLETED'))]
            cnt = lambda k: sum(1 for c in comps if c['kind'] == k)  # noqa
            boot = any('BOOT_COMPLETED' in a for c in comps for a in c['actions'])
            w.writerow([r['part'], r['path'], r['size'], mf.get('package', ''), mf.get('sharedUserId', ''),
                        int(mf.get('persistent', False)), int(mf.get('coreApp', False)),
                        int('/priv-app/' in r['path']), mf.get('minSdk', ''), mf.get('targetSdk', ''),
                        len(perms), int('android.permission.INTERNET' in perms), ';'.join(sorted(set(net))),
                        cnt('activity') + cnt('activity-alias'), cnt('service'), cnt('receiver'), cnt('provider'),
                        sum(1 for c in comps if c['exported'] is True), int(boot),
                        sc.get('n_urls', 0), len(sc.get('hosts', [])), ';'.join(sc.get('hosts', [])[:5]),
                        ';'.join(sc.get('trackers', [])), len(sc.get('native_libs', [])),
                        r.get('error', '') or sc.get('zip_error', '')])
            fm.write(json.dumps({'part': r['part'], 'path': r['path'], **mf}, ensure_ascii=False) + '\n')
            for c in comps:
                fc.write('\t'.join([r['part'], r['path'], mf.get('package', ''), c['kind'], c['name'],
                                    str(c['exported']), str(c['enabled']), c['process'], c['permission'],
                                    c['authorities'], ','.join(c['actions'])]) + '\n')
            fe.write(json.dumps({'part': r['part'], 'path': r['path'], 'package': mf.get('package', ''),
                                 **{k: sc.get(k) for k in ('urls', 'n_urls', 'hosts', 'trackers', 'native_libs')}},
                                ensure_ascii=False) + '\n')
    with open(os.path.join(OUT, 'meta', 'jar_endpoints.jsonl'), 'w', encoding='utf-8') as f:
        for r in jar_res:
            f.write(json.dumps(r, ensure_ascii=False) + '\n')
    with open(os.path.join(OUT, 'meta', 'elf_endpoints.jsonl'), 'w', encoding='utf-8') as f:
        for r in elf_res:
            f.write(json.dumps(r, ensure_ascii=False) + '\n')
    errs = [r for r in apk_res if r.get('error')]
    log(f'inventory rows={len(apk_res)} manifest_errors={len(errs)}')
    for r in errs[:15]:
        log('  manifest error:', r['path'], r['error'][:100])


if __name__ == '__main__':
    main()
