# Extra requirements from the owner (apply to every analysis agent)

## 1. Disguised telemetry
Reporting/telemetry/ad/push services often masquerade as system components (generic or AOSP-looking names,
`com.android.*`/`android.*`/`com.qualcomm.*`/`vendor.*` package names, `persistent`, `android.uid.system`,
hidden in `mi_ext`/`product/pangu`/`odm`, native daemons with neutral names, services started from `*.rc`
with `oneshot`/`disabled` tricks, jobs started by properties or `on boot` triggers).
Do NOT classify by name. Classify by evidence:
- INTERNET / network-state / phone-state / location / account / usage-stats / package-usage permissions,
- hosts and URL strings (`apk_endpoints.jsonl`, `elf_endpoints.jsonl`) and tracker keywords,
- BOOT_COMPLETED / CONNECTIVITY_CHANGE / USER_PRESENT / package-added receivers, JobScheduler/Alarm services,
- exported providers/services that let other Xiaomi apps feed data in,
- native libs it carries or loads (`native_libs`, `so_refs`).
For every package named like AOSP/Google/Qualcomm that is NOT in AOSP/GMS (compare with the other packages in the
bundle, with `/system/etc/permissions/*.xml` and with sharedUserId peers), say whether it looks genuine or disguised and why.
Record disguised items with category `telemetry` and put `DISGUISED` at the start of `notes`.

## 2. Hidden network gates (features that should work offline but may require the vendor server)
Find out, from manifests, properties, rc files, permission/sysconfig XML, overlay values and the endpoint data, whether any of these
needs a vendor round trip or a vendor-account check. Report each as `GATE-<n>` in the ledger (category `other`,
verdict KEEP/PATCH/UNSURE, evidence) and answer YES / NO / UNSURE in your report. "UNSURE" is a valid answer; use "needs JADX: <package>: <question>".

| # | Question | Where to look first |
|---|---|---|
| G1 | Can the device name be changed offline? Does any code path push it to a vendor server? | Settings (`system_ext/priv-app/Settings`), `settings` providers, MiuiSystemUI, device-name related components and overlays |
| G2 | Hotspot (name/password/enable), tethering, Bluetooth name: offline? any vendor-side check? | Settings, MiuiSystemUI, tethering/bluetooth/wifi components, `*.xml` overlays, rc and props |
| G3 | Does installing an app (adb, file manager, browser, third-party) need an online scan, upload of the package list or the APK, or a vendor verdict? | package installer(s) in `product/priv-app`, `system/priv-app`, `mi_ext`; Security Center / "virus scan" / "guard provider" / "app install verification" components; `installer`-related props |
| G4 | Does first boot/setup wizard require network, Xiaomi account, activation, region/server selection, or terms uploaded? | `system_ext/priv-app/Provision`, Settings, `com.miui.*` account/cloud/activation services |
| G5 | Do permission prompts, app-opening, "safe browsing", "app lock", "app verification" or "ad/recommendation" components depend on the vendor cloud? | Security Center, `MiuiSystemUI`, launcher, Settings "Additional settings", `pangu` |
| G6 | Anything else that stops working or becomes restricted offline (OTA/anti-rollback checks done by servers, SIM/regional locks, data-saver, "find device" lock) | whatever you find |

Ownership: A4 (system_ext) answers G1, G2, G4 first; A3 answers G3, G5; A5 answers G3 for the AOSP package installer and G2 for
Bluetooth/Wi-Fi/tethering parts in `system`; D answers all of G1–G6 from the properties/overlays/sysconfig/rc side; E answers G4 in depth.
Everyone else reports any gate they stumble on.

## 3. Known blind spot
34 APEX containers under `/system/apex` (Bluetooth, Wi-Fi/Connectivity, Conscrypt, ART ...) are not unpacked in the bundle.
If a conclusion depends on their content, say `needs APEX: <name>`; the orchestrator will extract them in CI.

## 4. Device facts (from `bundle/pkg/getvar-all.txt`)
product `flame`, bootloader unlocked, current slot a (slot b unused/never booted), `anti:1` (package `anti_version.txt` = 1),
`secure:yes`, `variant: SM_ UFS`, Virtual A/B (`snapshot-update-status: none`), `max-download-size` 370147328. There is no `hwc`/region
variable in the output. The device previously ran the China ROM.
