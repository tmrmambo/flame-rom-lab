# Owner decisions (binding for all later stages)

Source: owner messages, 2026-11-24.

1. Scope: Xiaomi components and the pre-installs Xiaomi pulled in (partners, ads, Facebook pangu etc.).
   **Qualcomm components are NOT touched for now** (CneApp, QCC, QMS config, xtra-daemon, Qualcomm HALs): verdict KEEP / deferred.
   Exception to check: packages that are named Qualcomm/AOSP but carry Xiaomi SDK hosts (e.g. com.qualcomm.location) are
   judged as possibly disguised Xiaomi code (JADX will tell).
2. Connectivity checks: a plain request that carries no sensitive device data may stay. Patching connect.rom.miui.com / baidu
   to Google-only is optional and low priority (do it only if the resource keys are easy to change; never remove the check).
3. Push: the Intl build ships BOTH Google FCM (GMS) and Xiaomi MiPush (com.xiaomi.xmsf + 11 packages with XMPushService).
   Xiaomi push is not needed: Xiaomi packages (xmsf, xmsfkeeper, MiPush services) may be removed with their dependents.
4. Device today runs the CHINA ROM. Intl package is being flashed over it. Never look at / reuse CN firmware content.
5. Firmware partitions: only the "AP part" is packaged. Interpretation (confirm before building):
   included  = super, boot, vendor_boot, dtbo, vbmeta, vbmeta_system, recovery (+ metadata/userdata erase policy)
   excluded  = modem(NON-HLOS), bluetooth(BTFM), dsp, and ALL boot-chain firmware (xbl*, abl, tz, hyp*, aop*, devcfg, keymint,
               featenabler, uefi*, shrm, qupfw, cpucp, multiimg*, storsec, qweslicstore, logfs, ...). persist never.
   Consequence: the device keeps its CN boot chain and CN modem/dsp/bt. Open risk: version compatibility between those and the
   Android 16 Intl vendor image (need the CN ROM version currently installed).
6. Setup wizard stall on CN-hardware: a friend says a version flag lives in `cust` (or similar). Evaluate AFTER the first
   experiment (flash unmodified Intl AP part, observe), not before.
