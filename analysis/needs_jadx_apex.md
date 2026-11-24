# JADX / APEX needs (auto-collected from reports)

## JADX
### com.android.settings  (asked by ['A4', 'D'])
- [A4] does DeviceNameEditActivity/MiuiDeviceNamePreference call any micloud/account API or only Settings.Global device_name? Also com.xiaomi.account (not mine) may sy
- [A4] does WifiTetherSettings enable path call TetherProvisioningActivity/entitlement or AccountApSettings conditionally? needs APEX: com.android.tethering, com.andro
- [D] does device_name write call xiaomi account/cloud?,,Report G answer: UNSURE
### miui  (asked by ['A6'])
- [A6] services.jar/MiuiBooster.jar: remove cloud fetch without breaking boot
- [A6] framework.jar: which classes call analytics/mistat/cloud; stub per class
- [A6] services.jar: ephemeris download (GNSS), Joyose hooks, cloud-control callers"
### com.qti.qcc  (asked by ['A1', 'A4'])
- [A1] what it uploads; do not remove alone
- [A4] what does it do with INTERNET?
### com.android.provision  (asked by ['A4', 'D'])
- [A4] GlobalDefaultActivity/CheckWifiActivity - is there a Skip path with no network and is eula shown from local assets when offline?
- [D] is network/account step skippable? Also check FindDevice activation (persistent, uid finddevice)",,Report G answer: UNSURE
### com.qualcomm.qti.cne  (asked by ['A1'])
- [A1] any network upload?"
### com.qualcomm.location  (asked by ['A4'])
- [A4] do the Xiaomi SDK strings correspond to live code (MiStat init)?"
### com.xiaomi.hypercomm  (asked by ['A4'])
- [A4] what does it communicate with (Xiaomi cloud or modem)?"
### com.miui.vsimcore  (asked by ['A4'])
- [A4] does it contact a server at boot?
### com.android.systemui  (asked by ['A4'])
- [A4] any ad/analytics receiver at boot?
### MiSightService/MiuiDaemon  (asked by ['A6'])
- [A6] do they bind IHypSysSsi? If service absent, hwservice lookup fails only for those clients"
### NetworkBoost.jar  (asked by ['A6'])
- [A6] servers contacted, SLA/slalib link; com.xiaomi.slalib.jar and mi-okhttp.jar (xiaomi.net hosts) likely belong"
### Provision  (asked by ['E'])
- [E] does Next require server reply

## APEX
- Bluetooth  <- ['D']
- NetworkStack  <- ['D']
- Tethering  <- ['D']
- adservices  <- ['A6']
- com.android.bluetooth for core name  <- ['A4']
- com.android.bt  <- ['A5']
- com.android.tethering  <- ['A4', 'A5', 'D']
- com.android.wifi  <- ['A4', 'A5', 'A6']
- confirmation.  <- ['A1']
- connectivity  <- ['A6']
- conscrypt  <- ['A6']
- ipsec  <- ['A6']
- mediaprovider  <- ['A6']
- ondevicepersonalization  <- ['A6']
- password logic  <- ['A4']
- tethering  <- ['A6']
- verify target  <- ['A1']
