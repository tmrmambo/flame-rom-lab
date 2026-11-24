package com.android.provision;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.StatusBarManager;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.face.BaseMiuiFaceManager;
import android.hardware.fingerprint.FingerprintManager;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.service.euicc.EuiccProfileInfo;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.Toast;
import com.android.provision.activities.GoogleAccountActivity;
import com.android.provision.beans.MurMur3_32Hasher;
import com.android.provision.global.ProvisionBackIconService;
import com.android.provision.global.WizardManagerHelper;
import com.android.provision.miconnect.MiMoverService;
import com.android.provision.utils.BoostHelper;
import com.android.provision.utils.CarouselConstant;
import com.android.provision.utils.HashUtils;
import com.android.provision.utils.MD5Utils;
import com.android.provision.utils.MccHelper;
import com.android.provision.utils.MisysUtils;
import com.android.provision.utils.ResetSimCountHelper;
import com.android.provision.utils.ZoneGetter;
import com.miui.performance.DeviceLevelUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import miui.enterprise.RestrictionsHelperStub;
import miui.os.Build;
import miui.os.HyperOSCustFeatureResolve;
import miui.os.MiuiInit;
import miui.telephony.TelephonyManager;
import miui.util.DeviceLevel;
import miui.util.FeatureParser;
import miui.util.FingerprintHelper;
import miui.util.MiuiMultiDisplayTypeInfo;
import miuix.core.util.HyperMaterialUtils;
import miuix.core.util.SystemProperties;
import miuix.device.DeviceUtils;
import miuix.internal.util.LiteUtils;
import miuix.mgl.physics.ParticleFlag;
import miuix.os.DeviceHelper;
import miuix.provision.ProvisionBaseActivity;

/* JADX INFO: loaded from: classes.dex */
public class Utils {
    private static final String ACTION_GESTUE = "com.android.systemui.fsgesture";
    private static final int ALL_BUCKETS = 1000;
    public static final String AUTHORITY_FOR_LAUNCHER;
    private static final String AUTHORITY_THEME_PROVIDER = "com.android.thememanager.theme_provider";
    private static final String BRAND_POCO = "poco";
    private static final String BRAND_REDMI = "Redmi";
    public static Bitmap CACHE_BITMAP = null;
    public static final String CM_PICK_STATUS = "cm_pick_status";
    private static final int COMPONENT_ENABLED_STATE_ABSENT = 5;
    public static final String CU_PICK_STATUS = "cu_pick_status";
    private static int DEFAULT_HIDE_GESTURE_LINE = 0;
    public static final String DT_FACEBOOK_ID = "dt:bdcc8a44-9f28-4421-9a68-f2cb6d394390";
    static final List<String> EEA_COUNTRY_LIST;
    public static final String ESIM_ACTIVE_SB = "eSim_Active";
    public static final String ESIM_ACTIVE_STATE = "eSim_GL_Active";
    public static final int ESIM_CARD_NUM = 0;
    public static final String ESIM_CHECK = "esim_import_properties";
    public static final String ESIM_SB = "eSim";
    public static final String ESIM_STATE = "eSim_GL";
    private static final String EXTRA_IS_ENTER = "isEnter";
    private static final String EXTRA_NAME_TYPE_FROM = "typeFrom";
    public static final String EXTRA_SHOW_FRAGMENT_TITLE = ":android:show_fragment_title";
    private static final String EXTRA_VALUE_TYPE_FROM = "typefrom_provision";
    public static final String FACEBOOK_ID_PROP = "ro.facebook.partnerid";
    public static Activity FINGER_PRINT_POINT = null;
    public static boolean FOLD_DEVICE = false;
    public static final ArrayList<String> GMS_APPS;
    public static float HALF_ALPHA = 0.0f;
    public static final int HAVE_A_ESIM_CARD = 3;
    public static final int HAVE_A_ESIM_CARD_SB = 1;
    public static final String HAVE_PAIRING_TAG = "havePairingTag";
    private static final String HIDE_GESTURE_LINE = "hide_gesture_line";
    public static final String INPUT_FACEDATA_NEED_SKIP_MIMA = "input_facedata_need_skip_password";
    public static final int INTERCONNECTION_DISABLE = 0;
    public static final int INTERCONNECTION_ENABLE = 1;
    public static final int IS_DEFAULT_ENABLE_ESIM = -1;
    private static final String IS_ENABLE_ESIM_FOR_USER = "is_enable_esim_for_user";
    public static final boolean IS_ESIM_SIM2_MODE;
    public static final boolean IS_JP;
    public static final boolean IS_JP_KD;
    public static final boolean IS_JP_SB;
    public static int IS_RTL = 0;
    public static boolean IS_START_ANIMA = false;
    public static int LOCATION_X = 0;
    public static int LOCATION_Y = 0;
    private static final String METHOD_PROVISION_COMPLETE = "provisionComplete";
    private static final String METHOD_THEME_PROVIDER = "setupProvisionResources";
    public static final String MINOR_MODE_STATUS = "minor_mode_status";
    public static final int MIUI_CLOUD_DEFAULT_ENCRYPTION = 0;
    private static String MIUI_CLOUD_ENCRYPTION = null;
    public static final String MIUI_PLUS_APPS = "com.xiaomi.mirror";
    private static final String MIUI_TERMS_AGREED_TIME = "miui_terms_agreed_time";
    public static final int MI_MOVER_AS_NEW_DEVICE = 0;
    public static final String MI_MOVER_DIRECT_TAG = "fromButton";
    public static final int MI_MOVER_RESTORE_FROM_CLOUD = 2;
    public static float NO_ALPHA = 0.0f;
    private static final Set<String> OLD_LITE_DEVICE_LIST;
    public static final int ONE_GRIDVIEW_COLUMN = 1;
    public static final String OPERATOR_STATUS = "operator_status";
    public static final String PACKAGE_NAME_DT = "de.telekom.tsc";
    public static final String PACKAGE_NAME_DT_APPSELECTOR = "com.aura.oobe.deutsche";
    public static final String PACKAGE_NAME_DT_APPSTARTER = "de.telekom.appstarter";
    private static final String PACKAGE_NAME_LAUNCHER = "com.miui.home";
    private static final String PACKAGE_NAME_POCOLAUNCHER = "com.mi.android.globallauncher";
    private static final String PACKAGE_NAME_SYSEM_UI = "com.android.systemui";
    public static final String PACKAGE_NAME_UPDATER = "com.android.updater";
    public static final String PRECUST_THEME_FOLODER_PATH;
    private static final String PRECUST_THEME_FOLODER_PATH_1 = "/data/miui/precust_theme";
    private static final String PRECUST_THEME_FOLODER_PATH_2 = "/system/etc/precust_theme";
    public static final String PREF_STATE = "pref_state";
    private static final String PROVISION_COMPLETE_BROADCAST = "android.provision.action.PROVISION_COMPLETE";
    public static final String PROVISION_IS_NOT_SUPPORT_ACCESSIBILITY = "provision_is_not_support_accessibility";
    public static final String PROVISION_IS_NOT_SUPPORT_HYPER_CONNECT_LITE = "provision_is_not_support_hyper_connect_lite";
    public static final String PROVISION_IS_NOT_SUPPORT_INTER_CONNECTIVITY = "provision_is_not_support_inter_connectivity";
    public static final String PROVISION_IS_NOT_SUPPORT_NAVIGATION_PICKER_DISPLAYABLE = "provision_is_not_support_navigation_picker_displayable";
    public static final int QUICK_START_HAVE_PAIRING = -1;
    public static final int QUICK_START_NO_PAIRING = 1;
    public static final String REGION_JP = "jp";
    public static final String REGION_TW = "tw";
    public static final int REQUEST_CODE_BACK_FROM_GOOGLE_WIZARD = 33;
    protected static final int REQUEST_CODE_TO_OUTER = 32;
    public static final String RO_MIUI_PRODUCT_HOME = "ro.miui.product.home";
    static final String RSA_MS_REGION = "persist.com.miui.rsa_ms_region";
    public static final String SETTINGS_MINOR_MODE_FLAG = "provision_minor_mode_flag";
    public static final int SET_GLOBAL_LAST_PAGE = 1;
    private static final String SOTI_APP_PACKAGENAME = "net.soti.mobicontrol.androidwork";
    public static final String STORES_ESIM_INFORMATION = "stores_esim_information";
    private static final String TAG = "Provision_Utils";
    public static final int THERE_IS_NO_ESIM_CARD = 2;
    public static final int THERE_IS_NO_ESIM_CARD_SB = 0;
    public static final int THREE_LOCKSCREEN_METHOD = 3;
    public static final int TWO_GRIDVIEW_ITEM = 2;
    public static final String XIAOMI_FACEBOOK_ID = "xiaomi:d3120e97-318f-0810-4666-a1b0610476f4";
    public static boolean abnormalFlowFinishedTag = false;
    public static final float adaptationLetterSpacing = 1.9f;
    private static Boolean blurEffectEnabledCache;
    private static final int cpuLevel;
    private static final int gpuLevel;
    public static boolean isEndBoot;
    public static boolean isEsimGPIOState;
    public static boolean isFirstBoot;
    public static boolean isFirstBuild;
    private static boolean isFirstSend;
    private static boolean isFirstStartCongratulationPage;
    public static boolean isGLpaRestoreCase;
    public static int isGlobalProvisionLastPage;
    public static boolean isLpaRestoreCase;
    public static boolean isShowLanguageListAnim;
    public static StatusBarManager mStatusBarManager;
    public static final LinkedHashMap<String, String> sMccEEAROMLanguageRegionsMap;
    public static final LinkedHashMap<String, String> sMccIndiaROMLanguageRegionsMap;
    public static final LinkedHashMap<String, String> sMccInternationalROMLanguageRegionsMap;
    public static final LinkedHashMap<String, String> sMccRussiaROMLanguageRegionsMap;
    private static final HashMap<String, String> sRegionLanguageMap;
    public static final ArrayList<String> sShowDataTrafficRegion;
    public static final HashMap<String, String> sSimDefaultRegionLanguageMap;
    private static final String[] sloganAdaptation;

    private static int getFsgState(boolean z, boolean z2) {
        int i = z2 ? 4194304 : 0;
        return z ? 2097152 | i : (-2097153) & i;
    }

    public static boolean isChinaMobileSimDefault(Context context) {
        return false;
    }

    public static boolean isMiuiSdkSupportFolme() {
        return true;
    }

    public static boolean isSupportPhrase(Context context) {
        return true;
    }

    public static boolean isSupportTranslationService(Context context) {
        return true;
    }

    public static boolean shouldNotFinishDefaultActivity() {
        return false;
    }

    static {
        String str;
        if (isPocoDevice()) {
            str = "com.mi.android.globallauncher.launcher.settings";
        } else {
            str = "com.miui.home.launcher.settings";
        }
        AUTHORITY_FOR_LAUNCHER = str;
        DEFAULT_HIDE_GESTURE_LINE = -1;
        isFirstSend = true;
        isFirstStartCongratulationPage = true;
        isFirstBuild = true;
        abnormalFlowFinishedTag = false;
        sloganAdaptation = new String[]{"zh_CN", "zh_TW"};
        isFirstBoot = true;
        isEndBoot = true;
        isShowLanguageListAnim = true;
        isEsimGPIOState = false;
        FOLD_DEVICE = SystemProperties.getBoolean("ro.config.miui_fold_screen", false);
        IS_JP_SB = CarouselConstant.CustomizedRegion.JAPAN_SOFTBANK.equals(android.os.SystemProperties.get("ro.miui.customized.region"));
        IS_JP_KD = CarouselConstant.CustomizedRegion.JAPAN_KDDI.equals(android.os.SystemProperties.get("ro.miui.customized.region"));
        IS_JP = REGION_JP.equalsIgnoreCase(android.os.SystemProperties.get("ro.miui.build.region"));
        IS_ESIM_SIM2_MODE = MiMoverService.DEFAULT_SERVICE_ID.equals(android.os.SystemProperties.get("ro.vendor.miui.esim_mode", ""));
        cpuLevel = DeviceLevel.getDeviceLevel(1, DeviceLevel.CPU);
        gpuLevel = DeviceLevel.getDeviceLevel(1, DeviceLevel.GPU);
        MIUI_CLOUD_ENCRYPTION = "micloud_end_to_end_encryption";
        LOCATION_X = -1;
        LOCATION_Y = -1;
        IS_RTL = 0;
        IS_START_ANIMA = false;
        blurEffectEnabledCache = null;
        ArrayList<String> arrayList = new ArrayList<>();
        GMS_APPS = arrayList;
        arrayList.add("com.google.android.syncadapters.contacts");
        arrayList.add("com.google.android.backuptransport");
        arrayList.add("com.google.android.onetimeinitializer");
        arrayList.add("com.google.android.partnersetup");
        arrayList.add("com.google.android.configupdater");
        arrayList.add("com.google.android.ext.shared");
        arrayList.add("com.google.android.printservice.recommendation");
        arrayList.add("com.android.vending");
        arrayList.add("com.google.android.gms");
        arrayList.add("com.google.android.gsf");
        EEA_COUNTRY_LIST = new ArrayList<String>() { // from class: com.android.provision.Utils.1
            {
                add("pl");
                add("dk");
                add("fi");
                add("no");
                add("se");
            }
        };
        HashMap<String, String> map = new HashMap<>();
        sRegionLanguageMap = map;
        map.put(REGION_TW, "zh_TW");
        map.put(REGION_JP, "ja_JP");
        HashMap<String, String> map2 = new HashMap<>();
        sSimDefaultRegionLanguageMap = map2;
        map2.put("es", "es_ES");
        map2.put("in", "en_IN");
        map2.put("us", "en_US");
        map2.put("at", "de_DE");
        map2.put("ie", "en_GB");
        map2.put("pe", "es_US");
        map2.put("cl", "es_US");
        map2.put("fr", "fr_FR");
        map2.put("pl", "pl_PL");
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        sMccInternationalROMLanguageRegionsMap = linkedHashMap;
        linkedHashMap.put("en_GB", "gb");
        linkedHashMap.put("en_US", "us");
        linkedHashMap.put("en_IN", "in");
        linkedHashMap.put("hi_IN", "in");
        linkedHashMap.put("id_ID", ZoneGetter.KEY_ID);
        linkedHashMap.put("ru_RU", "ru,ua,kz,by,kg");
        linkedHashMap.put("es_ES", "es,gq");
        linkedHashMap.put("es_US", "mx,co,ar,pe,cl");
        linkedHashMap.put("fr_FR", "fr,dz,ca,be,lu");
        linkedHashMap.put("pt_BR", "br");
        linkedHashMap.put("pt_PT", "pt,ao,mz,gw,tl");
        linkedHashMap.put("ar_EG", "eg,sa,ae,ma,dz");
        LinkedHashMap<String, String> linkedHashMap2 = new LinkedHashMap<>();
        sMccIndiaROMLanguageRegionsMap = linkedHashMap2;
        linkedHashMap2.put("en_GB", "in,bd,np,lk");
        linkedHashMap2.put("en_IN", "in");
        linkedHashMap2.put("hi_IN", "in");
        linkedHashMap2.put("bn_IN", "in");
        linkedHashMap2.put("bn_BD", "bd");
        linkedHashMap2.put("mr_IN", "in");
        linkedHashMap2.put("te_IN", "in");
        linkedHashMap2.put("ta_IN", "in,lk");
        linkedHashMap2.put("gu_IN", "in");
        linkedHashMap2.put("ne_NP", "np");
        LinkedHashMap<String, String> linkedHashMap3 = new LinkedHashMap<>();
        sMccRussiaROMLanguageRegionsMap = linkedHashMap3;
        linkedHashMap3.put("ru_RU", "ru");
        LinkedHashMap<String, String> linkedHashMap4 = new LinkedHashMap<>();
        sMccEEAROMLanguageRegionsMap = linkedHashMap4;
        linkedHashMap4.put("de_DE", "de,be,lu,at");
        linkedHashMap4.put("en_GB", "gb,ie");
        linkedHashMap4.put("fr_FR", "fr,be,lu");
        linkedHashMap4.put("it_IT", "it");
        linkedHashMap4.put("es_ES", "es");
        linkedHashMap4.put("pl_PL", "pl");
        linkedHashMap4.put("ro_RO", "ro");
        linkedHashMap4.put("nl_NL", "nl,be");
        linkedHashMap4.put("cs_CZ", "cz");
        linkedHashMap4.put("el_GR", "gr,cy");
        ArrayList<String> arrayList2 = new ArrayList<>();
        sShowDataTrafficRegion = arrayList2;
        arrayList2.add("pl");
        String str2 = PRECUST_THEME_FOLODER_PATH_1;
        if (!new File(PRECUST_THEME_FOLODER_PATH_1).exists()) {
            str2 = PRECUST_THEME_FOLODER_PATH_2;
        }
        PRECUST_THEME_FOLODER_PATH = str2;
        FINGER_PRINT_POINT = null;
        isLpaRestoreCase = false;
        isGLpaRestoreCase = false;
        HALF_ALPHA = 0.5f;
        NO_ALPHA = 1.0f;
        isGlobalProvisionLastPage = 0;
        HashSet hashSet = new HashSet();
        OLD_LITE_DEVICE_LIST = hashSet;
        hashSet.add("beryl");
        hashSet.add("xun");
        hashSet.add("sky");
        hashSet.add("breeze");
        hashSet.add("air");
        hashSet.add("lake");
        hashSet.add("flame");
        hashSet.add("gold");
        hashSet.add("obsidian");
        hashSet.add("tanzanite");
        hashSet.add("warm");
        hashSet.add("sea");
        hashSet.add("sapphire");
        hashSet.add("fire");
        hashSet.add("moon");
        hashSet.add("gale");
        hashSet.add(CarouselConstant.Rsa4Device.M7M7L);
        hashSet.add("flare");
        hashSet.add("emerald");
        hashSet.add("pond");
        hashSet.add("emerald_r");
    }

    public static int getCloudEncryption(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), MIUI_CLOUD_ENCRYPTION);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void setQuickStartPairingTag(Context context, int i) {
        try {
            Settings.Secure.putInt(context.getContentResolver(), HAVE_PAIRING_TAG, i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setEsimSecureValue(Context context) {
        try {
            Settings.Secure.putInt(context.getContentResolver(), IS_ENABLE_ESIM_FOR_USER, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getEsimSecureValue(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), IS_ENABLE_ESIM_FOR_USER);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getQuickStartPairingTag(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), HAVE_PAIRING_TAG);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public static boolean isSupportGlobalInterconnectionEntrance() {
        return Build.IS_INTERNATIONAL_BUILD && supportInterconnectivity().booleanValue();
    }

    public static boolean needSloganAdaptation(String str) {
        for (String str2 : sloganAdaptation) {
            if (str.contains(str2)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFoldDevice() {
        return MiuiMultiDisplayTypeInfo.isFoldDevice();
    }

    public static boolean isFlipDevice() {
        return MiuiMultiDisplayTypeInfo.isFlipDevice();
    }

    public static boolean alarmAssistanceLimitedRange() {
        return Build.IS_INTERNATIONAL_BUILD || Build.IS_TABLET;
    }

    public static boolean checkEsimConsistent() {
        return (isSupportEsimMode() || isSupportGoogleEsimMode()) && Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean hasGoogleAccount(Context context) {
        Account[] accountsByType = AccountManager.get(context).getAccountsByType(GoogleAccountActivity.GOOGLE_ACCOUNT_TYPE);
        return accountsByType != null && accountsByType.length > 0;
    }

    public static boolean isShowIconBtn() {
        return !TextUtils.equals(Locale.getDefault().getLanguage(), Locale.CHINESE.getLanguage());
    }

    public static boolean isShieldInputMethods() {
        return "XIG02".equals(Build.DEVICE) || MiuiCustomizeUtil.isShieldInputMethodsPage();
    }

    public static Button getNextView(Activity activity) {
        return ((ProvisionBaseActivity) activity).getNextButton();
    }

    public static View getBackView(Activity activity) {
        return ((ProvisionBaseActivity) activity).getBackButton();
    }

    public static Button getSkipView(Activity activity) {
        return ((ProvisionBaseActivity) activity).getSkipButton();
    }

    public static void updateViewVisibility(View view, View view2) {
        if (view2 == null) {
            return;
        }
        boolean zIsShowIconBtn = isShowIconBtn();
        if (view != null) {
            view.setVisibility(zIsShowIconBtn ? 8 : 0);
        }
        view2.setVisibility(zIsShowIconBtn ? 0 : 8);
    }

    public static boolean isDisableAccessibility() {
        return HyperOSCustFeatureResolve.getBoolean(PROVISION_IS_NOT_SUPPORT_ACCESSIBILITY, false);
    }

    public static String getLanguage() {
        try {
            return ActivityManagerNative.getDefault().getConfiguration().locale.toString();
        } catch (RemoteException e) {
            Log.e(TAG, "getLanguage fail", e);
            return null;
        }
    }

    public static boolean isSetCustomRegion() {
        return !TextUtils.isEmpty(getCustomizedRegion());
    }

    public static String getCustomizedRegion() {
        return android.os.SystemProperties.get("ro.miui.customized.region");
    }

    public static boolean isMiuiSupportSystemappUninstallV2() {
        return android.os.SystemProperties.getBoolean("ro.miui.support.system.app.uninstall.v2", false);
    }

    public static String getRegionLanguage() {
        return MiuiCustomizeUtil.getShowDefaultLanguage();
    }

    public static String getRegion() {
        String defaultRegionCountryCode = MiuiCustomizeUtil.getDefaultRegionCountryCode();
        return !TextUtils.isEmpty(defaultRegionCountryCode) ? defaultRegionCountryCode : "";
    }

    public static void setFacebookId(String str) {
        try {
            boolean zEquals = "eea".equals(android.os.SystemProperties.get("ro.miui.build.region", ""));
            boolean z = android.os.SystemProperties.getBoolean("ro.miui.carrier.cota", false);
            boolean zIsEmpty = TextUtils.isEmpty(android.os.SystemProperties.get(FACEBOOK_ID_PROP, ""));
            if (!zEquals || z || isSetCustomRegion() || zIsEmpty) {
                return;
            }
            android.os.SystemProperties.set(FACEBOOK_ID_PROP, str);
            android.os.SystemProperties.set("persist.sys.facebook.partnerid", str);
        } catch (Exception e) {
            Log.e(TAG, "setFacebookId fail:", e);
        }
    }

    public static boolean isCustForESIMFeature() {
        return (IS_JP_KD && TextUtils.equals(Build.DEVICE, "XIG03")) || isSupportGoogleEsimMode();
    }

    public static void sendProvisionCompleteBroadcast(Context context) {
        if (isFirstSend) {
            Intent intent = new Intent(PROVISION_COMPLETE_BROADCAST);
            intent.addFlags(32);
            intent.addFlags(16777216);
            sendBroadcastAsUser(context, intent);
            isFirstSend = false;
            Log.i(TAG, "sendProvisionCompleteBroadcast");
        }
    }

    public static void sendBroadcastAsUser(Context context, Intent intent) {
        context.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public static void startActivityAsUser(Context context, Intent intent) {
        try {
            context.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "ActivityNotFound", 1).show();
            Log.e(TAG, "startActivityAsUser ActivityNotFound:", e);
        }
    }

    public static void registerReceiverAsUser(Context context, IntentFilter intentFilter, BroadcastReceiver broadcastReceiver) {
        context.registerReceiverAsUser(broadcastReceiver, UserHandle.ALL, intentFilter, null, null);
    }

    private static boolean isContains(String[] strArr, String str) {
        if (strArr != null && !TextUtils.isEmpty(str)) {
            for (String str2 : strArr) {
                if (str2.equals(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isCmTest() {
        return Build.IS_CM_CUSTOMIZATION_TEST;
    }

    public static boolean isCmVersion() {
        return Build.IS_CM_CUSTOMIZATION;
    }

    public static boolean isCTVersion() {
        return Build.IS_CT_CUSTOMIZATION;
    }

    public static boolean isCUVersion() {
        return Build.IS_CU_CUSTOMIZATION;
    }

    public static void printLog(String str, String str2) {
        Log.i(str, str2);
    }

    public static boolean isProvisioned(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "device_provisioned", 0) != 0;
    }

    public static boolean isSingleSim() {
        return MiMoverService.DEFAULT_SERVICE_ID.equals(android.os.SystemProperties.get("ro.miui.singlesim"));
    }

    public static boolean supportDisplayApplicationPermission() {
        return FeatureParser.getBoolean("support_provision_app_permission", false) && !Build.IS_GLOBAL_BUILD && "CN".equals(Locale.getDefault().getCountry()) && "zh".equals(Locale.getDefault().getLanguage()) && Build.IS_STABLE_VERSION;
    }

    public static boolean isRTL() {
        return 1 == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault());
    }

    public static boolean applicationInstalled(Context context, String str) {
        try {
            context.getPackageManager().getApplicationInfo(str, ParticleFlag.repulsiveParticle);
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            Log.i(TAG, "appliction not found:" + str);
            return false;
        }
    }

    public static boolean intentAvailable(Context context, Intent intent) {
        return context.getPackageManager().resolveActivity(intent, 0) != null;
    }

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Intent getLicenseIntent(Context context) {
        Intent intent = new Intent("miui.intent.action.VIEW_LICENSE");
        if (!intentAvailable(context, intent)) {
            intent.setAction("android.intent.action.VIEW_LICENSE");
        }
        return intent;
    }

    public static boolean hasGmscore() {
        return MiMoverService.DEFAULT_SERVICE_ID.equals(android.os.SystemProperties.get("ro.miui.has_gmscore"));
    }

    public static boolean navigationPickerDisplayable() {
        return !HyperOSCustFeatureResolve.getBoolean(PROVISION_IS_NOT_SUPPORT_NAVIGATION_PICKER_DISPLAYABLE, false);
    }

    public static void setNavigationBarFullScreen(Context context, boolean z) {
        Log.i(TAG, "setNavigationBarFullScreen setNavigationBarFullScreen=" + z);
        if (z == isNavigationBarFullScreen(context)) {
            Log.i(TAG, "setNavigationBarFullScreen return");
        } else {
            MiuiSettings.Global.putBoolean(context.getContentResolver(), "force_fsg_nav_bar", z);
        }
    }

    public static boolean isNavigationBarFullScreen(Context context) {
        return MiuiSettings.Global.getBoolean(context.getContentResolver(), "force_fsg_nav_bar");
    }

    public static void setHideGestureLine(Context context, boolean z) {
        Settings.Global.putInt(context.getContentResolver(), HIDE_GESTURE_LINE, z ? 1 : 0);
    }

    public static void resetHideGestureLine(Context context) {
        if (DEFAULT_HIDE_GESTURE_LINE != -1) {
            Settings.Global.putInt(context.getContentResolver(), HIDE_GESTURE_LINE, DEFAULT_HIDE_GESTURE_LINE);
        }
    }

    public static boolean isMobileNetAvailable(Context context) {
        NetworkInfo activeNetworkInfo;
        return (context == null || (activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo()) == null || activeNetworkInfo.getType() != 0) ? false : true;
    }

    public static void setGestureHomeClose(Context context, boolean z) {
        Intent intent = new Intent();
        intent.setPackage(PACKAGE_NAME_SYSEM_UI);
        intent.setAction(ACTION_GESTUE);
        intent.putExtra(EXTRA_NAME_TYPE_FROM, EXTRA_VALUE_TYPE_FROM);
        intent.putExtra(EXTRA_IS_ENTER, z);
        context.sendBroadcast(intent);
    }

    public static void setGestureHomeClose(Context context, boolean z, boolean z2) {
        Log.i(TAG, "setGestureHomeClose isDisableHomeOrRecent=" + z + ",isDisableBack=" + z2 + ",getFsgState=" + getFsgState(z, z2));
        Intent intent = new Intent();
        intent.setPackage(PACKAGE_NAME_SYSEM_UI);
        intent.setAction(ACTION_GESTUE);
        intent.putExtra(EXTRA_NAME_TYPE_FROM, EXTRA_VALUE_TYPE_FROM);
        intent.putExtra(EXTRA_IS_ENTER, z);
        intent.putExtra("fsgState", getFsgState(z, z2));
        context.sendBroadcast(intent);
    }

    public static boolean networkAvailable(Context context) {
        NetworkInfo activeNetworkInfo;
        if (context == null || (activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo()) == null || !activeNetworkInfo.isAvailable()) {
            Log.d(TAG, "networkAvailable context is null");
            return false;
        }
        Log.d(TAG, "network changed type:" + activeNetworkInfo.getType());
        return true;
    }

    public static void setGmsAppEnabledStateForCn(Context context, final int i) {
        if (context == null || Build.IS_INTERNATIONAL_BUILD || isProvisioned(context)) {
            return;
        }
        final PackageManager packageManager = context.getApplicationContext().getPackageManager();
        final ArrayList arrayList = new ArrayList();
        ArrayList<String> arrayList2 = GMS_APPS;
        int size = arrayList2.size();
        int i2 = 0;
        while (i2 < size) {
            String str = arrayList2.get(i2);
            i2++;
            String str2 = str;
            if (applicationInstalled(context, str2) && packageManager.getApplicationEnabledSetting(str2) != i) {
                arrayList.add(str2);
            }
        }
        new AsyncTask<Void, Void, Void>() { // from class: com.android.provision.Utils.2
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                try {
                    ArrayList arrayList3 = arrayList;
                    int size2 = arrayList3.size();
                    int i3 = 0;
                    while (i3 < size2) {
                        Object obj = arrayList3.get(i3);
                        i3++;
                        packageManager.setApplicationEnabledSetting((String) obj, i, 0);
                    }
                    return null;
                } catch (Exception e) {
                    Log.e(Utils.TAG, "setGmsApplicationEnabled fail", e);
                    return null;
                }
            }
        }.execute(new Void[0]);
    }

    public static boolean isSupportVoiceWakeUp() {
        return !TextUtils.equals("none", android.os.SystemProperties.get("ro.vendor.audio.soundtrigger"));
    }

    public static boolean isLowPower() {
        return TextUtils.equals("true", android.os.SystemProperties.get("ro.vendor.audio.soundtrigger.lowpower"));
    }

    public static boolean isECommerceEdition() {
        return "cn".equals(android.os.SystemProperties.get("ro.miui.cust_variant"));
    }

    public static void enableStatusBar(Context context, boolean z) {
        if (mStatusBarManager == null) {
            mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        }
        mStatusBarManager.disable(z ? 0 : ParticleFlag.fixtureContactFilterParticle);
    }

    public static void setStatusBarState(Context context, boolean z) {
        Log.d(TAG, "setStatusBarEnable " + z);
        int i = z ? ParticleFlag.fixtureContactFilterParticle : 134152192;
        try {
            if (mStatusBarManager == null) {
                mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
            }
            mStatusBarManager.disable(i);
        } catch (Exception e) {
            Log.d(TAG, "setStatusBarEnable fail " + e.getMessage());
        }
    }

    public static boolean isNewGlobalOOBE() {
        return Build.IS_INTERNATIONAL_BUILD;
    }

    public static void goToTrustonicApp(Activity activity, Intent intent) {
        activity.startActivityForResult(getTrustonicSetupIntent(intent), 372);
    }

    public static Intent getTrustonicSetupIntent(Intent intent) {
        Intent nextIntentWithCustomAction = getNextIntentWithCustomAction("com.trustonic.telecoms.setup.Start", intent, -1, null);
        nextIntentWithCustomAction.setPackage("com.trustonic.telecoms.setup");
        return nextIntentWithCustomAction;
    }

    private static Intent getNextIntentWithCustomAction(String str, Intent intent, int i, Intent intent2) {
        Intent intent3 = new Intent(str);
        copyWizardManagerExtras(intent, intent3);
        intent3.putExtra("com.android.setupwizard.ResultCode", i);
        if (intent2 != null && intent2.getExtras() != null) {
            intent3.putExtras(intent2.getExtras());
        }
        intent3.putExtra(WizardManagerHelper.EXTRA_THEME, intent.getStringExtra(WizardManagerHelper.EXTRA_THEME));
        return intent3;
    }

    public static void copyWizardManagerExtras(Intent intent, Intent intent2) {
        intent2.putExtra("wizardBundle", intent.getBundleExtra("wizardBundle"));
        intent2.putExtra("firstRun", intent.getBooleanExtra("firstRun", false));
        intent2.putExtra("scriptUri", intent.getStringExtra("scriptUri"));
        intent2.putExtra("actionId", intent.getStringExtra("actionId"));
    }

    public static void goToNextPage(Activity activity, Intent intent, int i) {
        if (activity == null || intent == null) {
            throw new IllegalArgumentException("fragment or originIntent cannot be null when go to next page in SetupWizard");
        }
        activity.startActivityForResult(WizardManagerHelper.getNextIntent(intent, i), 33);
    }

    public static boolean isInProvisionState(Context context) {
        int userProvisioningState = ((DevicePolicyManager) context.getSystemService(DevicePolicyManager.class)).getUserProvisioningState();
        Log.i(TAG, "getUserProvisioningState=" + userProvisioningState);
        return userProvisioningState == 2 || userProvisioningState == 4 || userProvisioningState == 5 || userProvisioningState == 3;
    }

    public static boolean isInProvisionWorkOnlyState(Context context) {
        int userProvisioningState = ((DevicePolicyManager) context.getSystemService(DevicePolicyManager.class)).getUserProvisioningState();
        Log.i(TAG, "isInProvisionWorkOnlyState getUserProvisioningState=" + userProvisioningState);
        return userProvisioningState == 2 || userProvisioningState == 3;
    }

    public static boolean isInDlcMode(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
        boolean z = false;
        try {
            if (devicePolicyManager.getDeviceOwnerType(devicePolicyManager.getDeviceOwnerComponentOnCallingUser()) == 1) {
                z = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, " isInDlcMode called and result is " + z);
        return z;
    }

    public static void enableSOTIDeviceOwner(Context context) {
        if (Build.IS_INTERNATIONAL_BUILD && context != null && applicationInstalled(context, SOTI_APP_PACKAGENAME)) {
            try {
                Runtime.getRuntime().exec("/system/bin/dpm set-device-owner --user current net.soti.mobicontrol.androidwork/net.soti.mobicontrol.admin.DeviceAdminAdapter");
            } catch (Exception e) {
                Log.e(TAG, "run cmd failed", e);
            }
        }
    }

    public static void setWallpaperForOperator(Context context) throws Throwable {
        String str;
        if (android.os.SystemProperties.getBoolean("ro.miui.has_cust_partition", false)) {
            str = "/cust/cust/";
        } else {
            str = "/data/miui/cust/";
        }
        File file = new File(str + (isSetCustomRegion() ? getCustomizedRegion() : "") + "/wallpaper/default_wallpaper.jpg");
        if (!file.exists()) {
            return;
        }
        WallpaperManager wallpaperManager = (WallpaperManager) context.getApplicationContext().getSystemService("wallpaper");
        FileInputStream fileInputStream = null;
        try {
            try {
                FileInputStream fileInputStream2 = new FileInputStream(file);
                try {
                    wallpaperManager.setStream(fileInputStream2);
                    fileInputStream2.close();
                } catch (FileNotFoundException unused) {
                    fileInputStream = fileInputStream2;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } catch (IOException unused2) {
                    fileInputStream = fileInputStream2;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } catch (Throwable th) {
                    th = th;
                    fileInputStream = fileInputStream2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        } catch (FileNotFoundException unused3) {
        } catch (IOException unused4) {
        } catch (Throwable th2) {
            th = th2;
        }
    }

    public static boolean isSupportNFC(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.nfc");
    }

    public static boolean isVsimcoreInstalled(Context context) {
        if (context == null) {
            return false;
        }
        return applicationInstalled(context, "com.miui.vsimcore");
    }

    public static boolean isSupportXmsf(Context context) {
        return Build.IS_INTERNATIONAL_BUILD && applicationInstalled(context, "com.xiaomi.xmsf");
    }

    public static boolean isSupportSimActivateService(Context context) {
        return !isWifiOnly(context) && applicationInstalled(context, "com.xiaomi.simactivate.service");
    }

    public static boolean isSupportCloudService(Context context) {
        return applicationInstalled(context, "com.miui.cloudservice");
    }

    public static boolean isSupportCloudBackup(Context context) {
        return !Build.IS_INTERNATIONAL_BUILD && applicationInstalled(context, "com.miui.cloudbackup");
    }

    public static boolean isSupportFindDevice(Context context) {
        return applicationInstalled(context, "com.xiaomi.finddevice");
    }

    public static boolean isSupportHome(Context context) {
        return applicationInstalled(context, "com.miui.home");
    }

    public static boolean isSupportCommService(Context context) {
        return !Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportGameCenterService(Context context) {
        return Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportMiuiDaemon(Context context) {
        return applicationInstalled(context, "com.miui.daemon");
    }

    public static boolean isSupportMSA(Context context) {
        return Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportHTMLViewer(Context context) {
        return !Build.IS_INTERNATIONAL_BUILD && applicationInstalled(context, "com.android.htmlviewer");
    }

    public static boolean isSupportSecureElementApplication(Context context) {
        return !Build.IS_INTERNATIONAL_BUILD && applicationInstalled(context, "com.android.se");
    }

    public static boolean isSupportAIAssistant(Context context) {
        return !Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportPowerKeeper(Context context) {
        return !Build.IS_INTERNATIONAL_BUILD && applicationInstalled(context, "com.miui.powerkeeper");
    }

    public static boolean isSupportPowerchecker(Context context) {
        return !Build.IS_INTERNATIONAL_BUILD && applicationInstalled(context, "com.xiaomi.powerchecker");
    }

    public static boolean isSupportJoyose(Context context) {
        return !Build.IS_INTERNATIONAL_BUILD && applicationInstalled(context, "com.xiaomi.joyose");
    }

    public static boolean isSupportOs(Context context) {
        return !Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportWallpaper(Context context) {
        return !Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportUpdater(Context context) {
        return applicationInstalled(context, PACKAGE_NAME_UPDATER);
    }

    public static boolean isSupportSmartCard(Context context) {
        return applicationInstalled(context, "com.miui.tsmclient");
    }

    public static boolean isSupportRMS(Context context) {
        return !Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportSmartService(Context context) {
        return !Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportQuickApp(Context context) {
        return Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportAppVault(Context context) {
        return Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportInfoAssistant(Context context) {
        return Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportStatistics(Context context) {
        return Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportMAB(Context context) {
        return Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportSystemHelper(Context context) {
        return Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportMiAccount(Context context) {
        return Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isSupportMiCommunicationService(Context context) {
        Bundle bundle;
        boolean z = false;
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo("com.xiaomi.mi_connect_service", ParticleFlag.tensileParticle);
            z = ((applicationInfo == null || (bundle = applicationInfo.metaData) == null) ? 0 : bundle.getInt("com.xiaomi.continuity.VERSION_CODE", 0)) != 0;
            Log.i(TAG, "isLyraSupportRelay =" + z);
            return z;
        } catch (PackageManager.NameNotFoundException unused) {
            Log.i(TAG, "isLyraSupportRelay, failed to get audio relay code.");
            return z;
        }
    }

    public static boolean isGlobalBuild() {
        return Build.IS_INTERNATIONAL_BUILD;
    }

    public static String getMiuiVersion() {
        return android.os.SystemProperties.get("ro.build.version.incremental", "");
    }

    public static String getModel() {
        return android.os.SystemProperties.get("ro.product.model", "");
    }

    public static String getOsVersion() {
        return android.os.SystemProperties.get("ro.build.version.release", "");
    }

    public static String getUUID(Context context) {
        return Settings.System.getString(context.getContentResolver(), "android_id");
    }

    public static String getMiuiVersionType() {
        if (Build.IS_DEVELOPMENT_VERSION) {
            return "dev";
        }
        if (Build.IS_STABLE_VERSION) {
            return "stable";
        }
        return "";
    }

    public static String getImei() {
        String imeiForSlot = TelephonyManager.getDefault().getImeiForSlot(0);
        return !TextUtils.isEmpty(imeiForSlot) ? imeiForSlot : "";
    }

    public static String getHid() {
        if (!TextUtils.isEmpty(getImei())) {
            return String.valueOf(HashUtils.consistentHash(new MurMur3_32Hasher(0).putString(MD5Utils.hexMD5(getImei()), HashUtils.UTF_8).hash(), 1000));
        }
        return "";
    }

    public static boolean qrProvisionAvailable() {
        return isGlobalBuild();
    }

    public static boolean hasFingerPrint(Context context) {
        if (context == null) {
            return false;
        }
        try {
            return context.getPackageManager().hasSystemFeature("android.hardware.fingerprint") && FingerprintHelper.isFingerprintHardwareDetected(context);
        } catch (Exception e) {
            Log.e(TAG, "hasFingerPrint fail", e);
            return false;
        }
    }

    public static boolean hasFaceRecognition(Context context) {
        return (context == null || !((BaseMiuiFaceManager) context.getSystemService("miui_face")).isHardwareDetected() || android.os.SystemProperties.getBoolean("ro.vendor.audio.lightsensor.virtual", false)) ? false : true;
    }

    public static boolean hasPocoLauncherDefault() {
        return "com.mi.android.globallauncher".equals(android.os.SystemProperties.get(RO_MIUI_PRODUCT_HOME));
    }

    public static File createFile(String str) throws IOException {
        File file = new File(str);
        try {
            if (file.isDirectory()) {
                if (!file.exists()) {
                    file.mkdirs();
                    return file;
                }
            } else if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    if (parentFile.mkdirs()) {
                        file.createNewFile();
                        return file;
                    }
                } else {
                    file.createNewFile();
                    return file;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "create path fail: " + str, e);
        }
        return file;
    }

    public static int getDisplayHeight(Context context) {
        if (context == null) {
            return -1;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = context.getDisplay();
        if (display == null) {
            return -1;
        }
        display.getRealMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static int getDisplayWidth(Context context) {
        if (context == null) {
            return -1;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = context.getDisplay();
        if (display == null) {
            return -1;
        }
        display.getRealMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static String getMiuiVersionName() {
        return android.os.SystemProperties.get("ro.miui.ui.version.name", "");
    }

    public static void setClauseAgreed(Context context, boolean z) {
        MiuiSettings.Global.putBoolean(context.getContentResolver(), "clause_agreed", z);
    }

    public static boolean isClauseAgreed(Context context) {
        return MiuiSettings.Global.getBoolean(context.getContentResolver(), "clause_agreed");
    }

    public static void setWallperProvisioned(Context context, boolean z) {
        Settings.Global.putInt(context.getContentResolver(), "wallpaper_provisioned", z ? 1 : 0);
    }

    public static String[] queryStringArray(Context context, String str) {
        int identifier;
        Resources resources = context.getResources();
        if (resources == null || TextUtils.isEmpty(str) || (identifier = resources.getIdentifier(str, "array", context.getPackageName())) == 0) {
            return null;
        }
        return resources.getStringArray(identifier);
    }

    public static String queryString(Context context, String str) {
        int identifier;
        Resources resources = context.getResources();
        if (resources == null || TextUtils.isEmpty(str) || (identifier = resources.getIdentifier(str, "string", context.getPackageName())) == 0) {
            return null;
        }
        return resources.getString(identifier);
    }

    public static void setApplicationListEnabled(final Context context, final HashMap<String, Boolean> map) {
        if (context == null || map == null || map.isEmpty()) {
            return;
        }
        final PackageManager packageManager = context.getApplicationContext().getPackageManager();
        new AsyncTask<Void, Void, Void>() { // from class: com.android.provision.Utils.3
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                try {
                    for (String str : map.keySet()) {
                        boolean zApplicationNeedEnabled = Utils.applicationNeedEnabled(context, str, ((Boolean) map.get(str)).booleanValue());
                        Log.i(Utils.TAG, "setApplicationListEnabled package=" + str + ",needEnable=" + zApplicationNeedEnabled + ",enabled=" + map.get(str));
                        if (zApplicationNeedEnabled) {
                            packageManager.setApplicationEnabledSetting(str, ((Boolean) map.get(str)).booleanValue() ? 1 : 2, 0);
                        }
                    }
                    return null;
                } catch (Exception e) {
                    Log.e(Utils.TAG, "setApplicationListEnabled fail", e);
                    return null;
                }
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean applicationNeedEnabled(Context context, String str, boolean z) {
        if (context == null || !applicationInstalled(context, str)) {
            return false;
        }
        int applicationEnabledSetting = context.getApplicationContext().getPackageManager().getApplicationEnabledSetting(str);
        Log.i(TAG, "applicationNeedEnabled packageName=" + str + ",packageEnableState=" + applicationEnabledSetting);
        return !(z && applicationEnabledSetting == 1) && (z || applicationEnabledSetting != 2);
    }

    public static void uninstallApp(Context context, final String str) {
        if (context == null || str == null) {
            return;
        }
        final PackageManager packageManager = context.getApplicationContext().getPackageManager();
        new AsyncTask<Void, Void, Void>() { // from class: com.android.provision.Utils.4
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                try {
                    if (packageManager.getApplicationInfo(str, 0) != null) {
                        Log.i(Utils.TAG, "begin uninstallApp " + str);
                        packageManager.deletePackage(str, null, 0);
                    }
                } catch (Exception e) {
                    Log.e(Utils.TAG, "uninstallApp fail", e);
                }
                return null;
            }
        }.execute(new Void[0]);
    }

    public static void setupProvisionResources(final Context context) {
        new AsyncTask<Void, Void, Void>() { // from class: com.android.provision.Utils.5
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                try {
                    Log.i(Utils.TAG, "setupProvisionResources begin");
                    context.getContentResolver().call(Uri.parse("content://com.android.thememanager.theme_provider"), Utils.METHOD_THEME_PROVIDER, (String) null, (Bundle) null);
                    Log.i(Utils.TAG, "setupProvisionResources end");
                } catch (Exception e) {
                    Log.i(Utils.TAG, "call setupProvisionResources error");
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(new Void[0]);
    }

    public static void setupProvisionComplete(final Context context) {
        new AsyncTask<Void, Void, Void>() { // from class: com.android.provision.Utils.6
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                try {
                    Log.i(Utils.TAG, "setupProvisionComplete begin");
                    context.getContentResolver().call(Uri.parse("content://com.android.thememanager.theme_provider"), Utils.METHOD_PROVISION_COMPLETE, (String) null, (Bundle) null);
                    Log.i(Utils.TAG, "setupProvisionComplete end");
                } catch (Exception e) {
                    Log.i(Utils.TAG, "call setupProvisionComplete error");
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(new Void[0]);
    }

    public static void grantRuntimePermissions(Context context, String str, String[] strArr, UserHandle userHandle) {
        if (TextUtils.isEmpty(str) || strArr == null) {
            return;
        }
        for (String str2 : strArr) {
            try {
                context.getPackageManager().grantRuntimePermission(str, str2.trim(), userHandle);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "grantRequiredPermissions two", e);
                return;
            } catch (SecurityException e2) {
                Log.e(TAG, "grantRequiredPermissions one", e2);
                return;
            } catch (Exception e3) {
                Log.e(TAG, "grantRequiredPermissions three", e3);
                return;
            }
        }
    }

    public static boolean unSupportGetAppsModels() {
        return "tier1".equals(android.os.SystemProperties.get("ro.com.miui.rsa")) || "android-xiaomi-rvo3".equals(android.os.SystemProperties.get("ro.com.google.clientidbase.ms"));
    }

    public static boolean judgeGetAppsEnable(Context context, String str) {
        int applicationEnabledSetting;
        try {
            applicationEnabledSetting = context.getPackageManager().getApplicationEnabledSetting(str);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            applicationEnabledSetting = 5;
        }
        Log.i(TAG, " in judgeGetAppsEnable and enabled is " + applicationEnabledSetting);
        return (applicationEnabledSetting == 2 || applicationEnabledSetting == 5) ? false : true;
    }

    public static boolean isGoogleCoopeModels() {
        return "tier1".equals(android.os.SystemProperties.get("ro.com.miui.rsa")) || "android-xiaomi-rvo3".equals(android.os.SystemProperties.get("ro.com.google.clientidbase.ms")) || MiuiCustomizeUtil.isGoogleCoopeModel();
    }

    public static boolean isMiuiVersionLite() {
        return Build.IS_MIUI_LITE_VERSION;
    }

    public static boolean isPocoBrand() {
        return BRAND_POCO.equalsIgnoreCase(getProductBrand());
    }

    public static boolean isRedmiBrand() {
        return BRAND_REDMI.equalsIgnoreCase(getProductBrand());
    }

    public static String getProductBrand() {
        return android.os.SystemProperties.get("ro.product.brand", "");
    }

    public static boolean isPocoDevice() {
        return isPocoBrand() || getPhoneModel().toLowerCase().contains(BRAND_POCO);
    }

    public static boolean shouldNotShowHomePage() {
        return isMiuiVersionLite();
    }

    public static boolean shouldForceSetDrawerHome() {
        return Build.IS_INTERNATIONAL_BUILD && !hasPocoLauncherDefault() && isMiuiVersionLite() && "POCO".equalsIgnoreCase(getProductBrand());
    }

    public static void setHomeDrawerDefault(Context context) {
        Log.i(TAG, "set miui_home_drawer_default_enable");
        Settings.Global.putInt(context.getContentResolver(), "miui_home_drawer_default_enable", 1);
    }

    public static void setTermsAgreedTime(Context context) {
        Log.i(TAG, "set_terms_agreed_time");
        Settings.Global.putLong(context.getContentResolver(), MIUI_TERMS_AGREED_TIME, System.currentTimeMillis());
    }

    public static boolean isNoAnimDevice(DeviceLevelUtils deviceLevelUtils) {
        if (FOLD_DEVICE) {
            return true;
        }
        if (deviceLevelUtils != null) {
            try {
                return deviceLevelUtils.getDeviceLevel(1) == 1;
            } catch (Exception e) {
                Log.e(TAG, "isNoAnimDevice " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean isTabletDevice() {
        return Build.IS_TABLET;
    }

    public static boolean isMediaAnimEnd(MediaPlayer mediaPlayer) {
        try {
            return !mediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean isTalkbackEnable(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService("accessibility");
        return accessibilityManager != null && accessibilityManager.isEnabled() && accessibilityManager.isTouchExplorationEnabled();
    }

    public static boolean isHighTextContrastEnable(Context context) {
        AccessibilityManager accessibilityManager;
        return (context == null || (accessibilityManager = (AccessibilityManager) context.getSystemService("accessibility")) == null || !accessibilityManager.isHighContrastTextEnabled()) ? false : true;
    }

    public static boolean isSimcardEnabled(Context context) {
        return !isWifiOnly(context) && isVoiceCapable(context);
    }

    public static boolean isWifiOnly(Context context) {
        return !((android.telephony.TelephonyManager) context.getSystemService(android.telephony.TelephonyManager.class)).isDataCapable();
    }

    public static boolean isVoiceCapable(Context context) {
        android.telephony.TelephonyManager telephonyManager = (android.telephony.TelephonyManager) context.getSystemService("phone");
        return telephonyManager != null && telephonyManager.isVoiceCapable();
    }

    public static void startBackViewService(Context context) {
        if (Build.IS_TABLET && Build.IS_INTERNATIONAL_BUILD) {
            Intent intent = new Intent(ProvisionBackIconService.BACKICON_SERVICE_ACTION);
            intent.setPackage("com.android.provision");
            context.startService(intent);
        }
    }

    public static void stopBackViewService(Context context) {
        if (Build.IS_TABLET && Build.IS_INTERNATIONAL_BUILD) {
            context.sendBroadcast(new Intent(ProvisionBackIconService.BACKICON_STOP_SERVICE_INTENT));
            Intent intent = new Intent(ProvisionBackIconService.BACKICON_SERVICE_ACTION);
            intent.setPackage("com.android.provision");
            context.stopService(intent);
        }
    }

    public static boolean isRedmiDigitOrNoteSeries() {
        String phoneModel = getPhoneModel();
        if (TextUtils.isEmpty(phoneModel)) {
            return false;
        }
        return isRedmiDigitSeries(phoneModel) || isRedmiNoteSeries(phoneModel);
    }

    private static boolean isRedmiDigitSeries(String str) {
        return str.matches("(?i)^Redmi[\\s]*[0-9]+[^X]*$");
    }

    private static boolean isRedmiNoteSeries(String str) {
        return str.matches("(?i)^Redmi[\\s]*Note[\\s]*[0-9].*$");
    }

    private static String getPhoneModel() {
        String str = android.os.SystemProperties.get("ro.product.marketname", (String) null);
        return !TextUtils.isEmpty(str) ? str : Build.MODEL;
    }

    public static boolean supportMiMover(Context context) {
        if (RestrictionsHelperStub.getInstance().isRestriction("disallow_mimover")) {
            Log.d(TAG, "Device is in enterprise mode, MiMover is restricted by enterprise!");
            return false;
        }
        if (context == null) {
            return false;
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.miui.huanji", "com.miui.huanji.provision.ui.ProvisionCTAActivity"));
        return (Build.IS_INTERNATIONAL_BUILD || context.getPackageManager().queryIntentActivities(intent, ParticleFlag.particleContactFilterParticle).isEmpty()) ? false : true;
    }

    public static boolean bluetoothHasEnabled() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter == null) {
            return false;
        }
        return defaultAdapter.isEnabled();
    }

    public static void enableBluetooth() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() { // from class: com.android.provision.Utils.7
            @Override // java.lang.Runnable
            public void run() {
                BluetoothAdapter.getDefaultAdapter().enable();
            }
        });
    }

    public static void closeBluetoothIfNeed(Context context) {
        final BluetoothAdapter defaultAdapter;
        if (context != null && supportMiMover(context) && (defaultAdapter = BluetoothAdapter.getDefaultAdapter()) != null && defaultAdapter.isEnabled()) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() { // from class: com.android.provision.Utils.8
                @Override // java.lang.Runnable
                public void run() {
                    defaultAdapter.disable();
                }
            });
        }
    }

    public static void updateLocationEnabled(Context context, boolean z, int i, int i2) {
        Settings.Secure.putIntForUser(context.getContentResolver(), "location_changer", i2, i);
        ((LocationManager) context.getSystemService(LocationManager.class)).setLocationEnabledForUser(z, UserHandle.of(i));
        Settings.Global.putInt(context.getContentResolver(), "assisted_gps_enabled", z ? 1 : 0);
    }

    public static boolean isLocationEnabled(Context context) {
        return ((LocationManager) context.getSystemService(LocationManager.class)).isLocationEnabled();
    }

    public static boolean fullScreenSettings(Context context) {
        return !getHuanjiSettingsStatus(context) && DefaultPreferenceHelper.isPrefFullscreen();
    }

    public static boolean getHuanjiSettingsStatus(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "huanji_settings", 0) != 0;
    }

    public static boolean isSupportIntentFilterVerificationService() {
        return !Build.IS_INTERNATIONAL_BUILD;
    }

    public static String getTimeZone() {
        return android.os.SystemProperties.get("persist.sys.timezone", "");
    }

    public static void callStaticMethod(Class cls, String str, Class<?>[] clsArr, Object... objArr) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Method declaredMethod = getDeclaredMethod(cls, str, clsArr);
        declaredMethod.setAccessible(true);
        if (objArr.length <= 0) {
            declaredMethod.invoke(null, null);
        } else {
            declaredMethod.invoke(null, objArr);
        }
    }

    private static Method getDeclaredMethod(Class cls, String str, Class<?>[] clsArr) throws NoSuchMethodException {
        if (clsArr == null || clsArr.length <= 0) {
            Class[] clsArr2 = new Class[0];
            return cls.getDeclaredMethod(str, null);
        }
        return cls.getDeclaredMethod(str, clsArr);
    }

    public static void recordCountryCodeOnce(String str) {
        try {
            Log.i(TAG, "recordCountryCodeOnce:" + str);
            if (TextUtils.isEmpty(android.os.SystemProperties.get("ro.miui.customized.region"))) {
                if ("eea".equals(android.os.SystemProperties.get("ro.miui.build.region", "")) && "gust".equalsIgnoreCase(Build.DEVICE)) {
                    recordEaaCountryCodeOnce(str);
                    return;
                }
                ArrayList arrayList = new ArrayList();
                arrayList.add("th");
                arrayList.add("vn");
                arrayList.add("my");
                arrayList.add("ph");
                if (arrayList.contains(str) && TextUtils.isEmpty(android.os.SystemProperties.get("ro.miui.cust_variant")) && TextUtils.isEmpty(android.os.SystemProperties.get("persist.com.miui.rsa_region", "")) && "global".equals(android.os.SystemProperties.get("ro.miui.build.region"))) {
                    android.os.SystemProperties.set("persist.com.miui.rsa_region", str);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "recordCountryCodeOnce:" + e.getMessage());
        }
    }

    public static boolean isSupportGoogleEsimMode() {
        return android.os.SystemProperties.getBoolean("ro.vendor.miui.use_google_lpa", false) && judgeSupportEsimForCountry(getLocaleISO());
    }

    public static boolean isSupportGoogleEsimModeRegardlessRegion() {
        return android.os.SystemProperties.getBoolean("ro.vendor.miui.use_google_lpa", false);
    }

    public static boolean isSupportEsimMode() {
        return android.os.SystemProperties.getBoolean("ro.vendor.miui.support_esim", false) && Build.IS_INTERNATIONAL_BUILD && judgeSupportEsimForCountry(getLocaleISO());
    }

    public static boolean isSupportEsimRegardlessRegion() {
        return android.os.SystemProperties.getBoolean("ro.vendor.miui.support_esim", false) && Build.IS_INTERNATIONAL_BUILD;
    }

    public static String getLocaleISO() {
        String str = android.os.SystemProperties.get("ro.miui.region", "");
        return "".equals(str) ? android.os.SystemProperties.get("ro.product.locale.region", "") : str;
    }

    public static void setChargingMark(String str) {
        try {
            android.os.SystemProperties.set("persist.vendor.region.charger", str);
        } catch (Exception e) {
            Log.i(TAG, " setChargingMark: " + e.getMessage());
        }
    }

    public static boolean isWifiConnected(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(1);
        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean judgeSupportEsimForCountry(String str) {
        boolean zBooleanValue = false;
        try {
            Class<?> cls = Class.forName("miui.telephony.TelephonyManagerEx");
            Class[] clsArr = new Class[0];
            zBooleanValue = ((Boolean) cls.getMethod("isSupportEsimForCountry", String.class).invoke(cls.getMethod("getDefault", null).invoke(null, null), str)).booleanValue();
        } catch (Exception e) {
            Log.e(TAG, "setEsimState: ", e);
        }
        Log.d(TAG, " the judgeSupportEsimForCountry return value is " + zBooleanValue);
        return zBooleanValue;
    }

    public static int setEsimState(int i) {
        int iIntValue = 0;
        try {
            Class<?> cls = Class.forName("miui.telephony.TelephonyManagerEx");
            Class[] clsArr = new Class[0];
            iIntValue = ((Integer) cls.getMethod("setEsimState", Integer.TYPE).invoke(cls.getMethod("getDefault", null).invoke(null, null), Integer.valueOf(i))).intValue();
        } catch (Exception e) {
            Log.e(TAG, "setEsimState: ", e);
        }
        Log.d(TAG, " the setEsimState return value is " + iIntValue);
        return iIntValue;
    }

    public static int getEsimGPIOState() {
        int iIntValue;
        try {
            Class<?> cls = Class.forName("miui.telephony.TelephonyManagerEx");
            Class[] clsArr = new Class[0];
            Class[] clsArr2 = new Class[0];
            iIntValue = ((Integer) cls.getMethod("getEsimGPIOState", null).invoke(cls.getMethod("getDefault", null).invoke(null, null), null)).intValue();
        } catch (Exception e) {
            Log.e(TAG, "getEsimGPIOState: ", e);
            iIntValue = 1;
        }
        Log.d(TAG, " the getEsimGPIOState return value is " + iIntValue);
        return iIntValue;
    }

    public static boolean isEsimActive() {
        boolean zBooleanValue = false;
        if (!MccHelper.getInstance().isJaPanLocale() && !IS_ESIM_SIM2_MODE && getEsimGPIOState() == 1) {
            Log.d(TAG, " the isEsimActive return value is false");
            return false;
        }
        if (isSupportMep()) {
            return newHasEsimActive();
        }
        try {
            Class<?> cls = Class.forName("miui.telephony.TelephonyManagerEx");
            Class[] clsArr = new Class[0];
            Class[] clsArr2 = new Class[0];
            zBooleanValue = ((Boolean) cls.getMethod("isEsimActive", null).invoke(cls.getMethod("getDefault", null).invoke(null, null), null)).booleanValue();
            Log.e(TAG, "isActive = " + zBooleanValue);
            return zBooleanValue;
        } catch (Exception e) {
            Log.e(TAG, "isEsimActive: ", e);
            return zBooleanValue;
        }
    }

    public static boolean newHasEsimActive() {
        List<SubscriptionInfo> activeSubscriptionInfoList = ((SubscriptionManager) ProvisionApplication.getContext().getSystemService("telephony_subscription_service")).getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList == null || activeSubscriptionInfoList.isEmpty()) {
            Log.i(TAG, " newHasEsimActive first case");
            return false;
        }
        for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
            if (subscriptionInfo != null && subscriptionInfo.isEmbedded()) {
                Log.i(TAG, " newHasEsimActive second case");
                return true;
            }
        }
        Log.i(TAG, " newHasEsimActive third case");
        return false;
    }

    public static boolean isSupportMep() {
        try {
            boolean zHasSystemFeature = ProvisionApplication.getContext().getPackageManager().hasSystemFeature("android.hardware.telephony.euicc.mep");
            Log.i(TAG, " isSupportMep result is " + zHasSystemFeature);
            return zHasSystemFeature;
        } catch (Exception e) {
            Log.e(TAG, "isSupportMep " + e);
            return false;
        }
    }

    public static boolean isFoldLarge(Context context) {
        if (context != null && isFoldDevice()) {
            return DeviceHelper.isWideScreen(context);
        }
        return false;
    }

    public static void recordEaaCountryCodeOnce(String str) {
        try {
            Log.i(TAG, "recordEaaCountryCodeOnce start:" + str);
            if (EEA_COUNTRY_LIST.contains(str.toLowerCase()) && TextUtils.isEmpty(android.os.SystemProperties.get("ro.miui.cust_variant")) && TextUtils.isEmpty(android.os.SystemProperties.get(RSA_MS_REGION, ""))) {
                android.os.SystemProperties.set(RSA_MS_REGION, str);
            }
        } catch (Exception e) {
            Log.e(TAG, "recordEaaCountryCodeOnce:" + e.getMessage());
        }
    }

    public static boolean isMiSansSupportLanguages() {
        Locale locale = Locale.getDefault();
        try {
            Class[] clsArr = new Class[0];
            LocaleList localeList = (LocaleList) Class.forName("miui.util.font.MultiLangHelper").getMethod("getSupportedLocaleList", null).invoke(null, null);
            if (localeList != null) {
                for (int i = 0; i < localeList.size(); i++) {
                    if (localeList.get(i).equals(locale)) {
                        Log.i(TAG, " isMiSansSupportLanguages success");
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, " isMiSansSupportLanguages: ", e);
        }
        return false;
    }

    public static String getMiuiFontPathByLocale() {
        String str = android.os.SystemProperties.get("ro.miui.ui.font.mi_font_path", "/system/fonts/MiLanProVF.ttf");
        try {
            str = (String) Class.forName("miui.util.font.MultiLangHelper").getMethod("getMiuiFontPathByLocale", Locale.class).invoke(null, Locale.getDefault());
        } catch (Exception e) {
            Log.e(TAG, " getMiuiFontPathByLocale: ", e);
        }
        Log.i(TAG, " getMiuiFontPathByLocale return path is " + str);
        return str;
    }

    public static boolean isLiteOrLowDevice() {
        return isLowEndDevice() || isCpuOrGpuLowLevel();
    }

    public static boolean isLowEndDevice() {
        return DeviceLevel.IS_MIUI_LITE_VERSION || DeviceLevel.IS_MIUI_MIDDLE_VERSION || Build.IS_MIUI_LITE_VERSION;
    }

    public static boolean isCpuOrGpuLowLevel() {
        int i = cpuLevel;
        int i2 = DeviceLevel.LOW;
        return i == i2 || gpuLevel == i2;
    }

    public static void hideGestureLine(Context context, boolean z) {
        Settings.Global.putInt(context.getContentResolver(), HIDE_GESTURE_LINE, z ? 1 : 0);
    }

    public static boolean isGestureLineShow(Context context) {
        return context == null || Settings.Global.getInt(context.getContentResolver(), HIDE_GESTURE_LINE, 0) == 0;
    }

    public static void saveOperatorState(Context context, String str, boolean z) {
        if (context == null) {
            Log.i(TAG, "saveOperatorState context is null");
            return;
        }
        SharedPreferences.Editor editorEdit = context.getSharedPreferences(OPERATOR_STATUS, 0).edit();
        editorEdit.clear();
        editorEdit.putBoolean(str, z);
        editorEdit.apply();
        Log.i(TAG, " saveOperatorState type is " + str + " picked is " + z);
    }

    public static boolean getOperatorState(Context context, String str) {
        if (context == null) {
            Log.i(TAG, "getOperatorState context is null");
            return false;
        }
        boolean z = context.getSharedPreferences(OPERATOR_STATUS, 0).getBoolean(str, false);
        Log.i(TAG, " getOperatorState type is " + str + " result is " + z);
        return z;
    }

    public static void setMinorsModeFlag(Context context, boolean z) {
        if (context == null) {
            Log.i(TAG, "setMinorsModeFlag context is null");
            return;
        }
        Log.i(TAG, " setMinorsModeFlag picked is " + z);
        Settings.Secure.putInt(context.getContentResolver(), SETTINGS_MINOR_MODE_FLAG, z ? 1 : 0);
    }

    public static boolean getMinorsModeFlag(Context context) {
        if (context == null) {
            Log.i(TAG, "getMinorsModeFlag context is null");
            return false;
        }
        boolean z = Settings.Secure.getInt(context.getContentResolver(), SETTINGS_MINOR_MODE_FLAG, 1) == 1;
        Log.i(TAG, " getMinorsModeFlag result is " + z);
        return z;
    }

    public static Intent getGestureIntent(Context context) {
        String str = android.os.SystemProperties.get(RO_MIUI_PRODUCT_HOME);
        Intent intent = new Intent("com.miui.systemui.fsgesture.introduce");
        List<ResolveInfo> listQueryIntentActivities = context.getPackageManager().queryIntentActivities(intent, ParticleFlag.particleContactFilterParticle);
        if (listQueryIntentActivities.size() > 0) {
            boolean z = false;
            boolean z2 = false;
            for (ResolveInfo resolveInfo : listQueryIntentActivities) {
                if (str.equals(resolveInfo.activityInfo.packageName)) {
                    z = true;
                } else if (PACKAGE_NAME_SYSEM_UI.equals(resolveInfo.activityInfo.packageName)) {
                    z2 = true;
                }
            }
            if (z) {
                intent.setPackage(str);
                return intent;
            }
            if (z2) {
                intent.setPackage(PACKAGE_NAME_SYSEM_UI);
                return intent;
            }
            intent.setPackage(listQueryIntentActivities.get(0).activityInfo.packageName);
            return intent;
        }
        Log.i(TAG, "Error, can not query gesture introduce!");
        return intent;
    }

    public static void disableScreenshots(Activity activity) {
        if (activity != null) {
            activity.getWindow().addFlags(ParticleFlag.repulsiveParticle);
        }
    }

    public static String readEsimPowerRecordFilePersist() {
        String strMisyReadFile = MisysUtils.MisyReadFile(MisysUtils.DIR_PERSIST_STABILITY, MisysUtils.getEsimPowerRecordFilePersist().getName());
        Log.d(TAG, "readEsimPowerRecordFilePersist: " + strMisyReadFile);
        return strMisyReadFile;
    }

    public static void checkAndActivateEsimAfterFactoryReset(Context context) {
        if (context != null && Build.IS_INTERNATIONAL_BUILD) {
            if ((isSupportEsimRegardlessRegion() || isSupportGoogleEsimModeRegardlessRegion()) && !DefaultPreferenceHelper.isActivateEsimCardSelect() && ESIM_CHECK.equals(readEsimPowerRecordFilePersist())) {
                Log.d(TAG, "checkAndActivateEsimAfterFactoryReset");
                new ResetSimCountHelper().isSuccessPhoneInit(context);
            }
        }
    }

    public static List<EuiccProfileInfo> getAllEsimProfiles() {
        List<EuiccProfileInfo> list;
        Exception e;
        try {
            Class<?> cls = Class.forName("miui.telephony.TelephonyManagerEx");
            Class[] clsArr = new Class[0];
            Class[] clsArr2 = new Class[0];
            list = (List) cls.getMethod("getAllEsimProfiles", null).invoke(cls.getMethod("getDefault", null).invoke(null, null), null);
            try {
                Log.d(TAG, "profils.toString()) -> " + list.toString());
            } catch (Exception e2) {
                e = e2;
                Log.e(TAG, "getAllEsimProfiles error：" + e);
            }
        } catch (Exception e3) {
            list = null;
            e = e3;
        }
        return list == null ? new ArrayList() : list;
    }

    public static int getAllNonTestEsimNum() {
        int i = 0;
        try {
            Iterator<EuiccProfileInfo> it = getAllEsimProfiles().iterator();
            while (it.hasNext()) {
                if (it.next().getProfileClass() == 2) {
                    i++;
                    Log.d(TAG, "loop acquisitionNonTestEsimNum -> " + i);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getEsimNum error: " + e);
        }
        Log.d(TAG, "final result nonTestEsimNum -> " + i);
        return i;
    }

    public static Boolean supportInterconnectivity() {
        return Boolean.valueOf(!HyperOSCustFeatureResolve.getBoolean(PROVISION_IS_NOT_SUPPORT_INTER_CONNECTIVITY, false));
    }

    public static boolean isHyperConnectLite() {
        return HyperOSCustFeatureResolve.getBoolean(PROVISION_IS_NOT_SUPPORT_HYPER_CONNECT_LITE, false) || DeviceUtils.isMiuiLiteRom() || DeviceUtils.isMiuiMiddle();
    }

    public static boolean isFaceDisabledByAdmin(Context context) {
        if (context == null) {
            return false;
        }
        try {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
            if (devicePolicyManager == null) {
                Log.e(TAG, "isFaceDisabledByAdmin: devicePolicyManager is null");
                return false;
            }
            int currentUser = ActivityManager.getCurrentUser();
            int keyguardDisabledFeatures = devicePolicyManager.getKeyguardDisabledFeatures(null, currentUser);
            Log.i(TAG, "isFaceDisabledByAdmin : currentUser = " + currentUser + " disabledFeatures = " + keyguardDisabledFeatures);
            return (keyguardDisabledFeatures & ParticleFlag.tensileParticle) != 0;
        } catch (Exception e) {
            Log.e(TAG, "isFaceDisabledByAdmin exception " + e);
            return false;
        }
    }

    @SuppressLint({"WrongConstant"})
    public static BaseMiuiFaceManager initFaceManager(Context context) {
        if (context == null) {
            return null;
        }
        BaseMiuiFaceManager baseMiuiFaceManager = (BaseMiuiFaceManager) context.getSystemService("miui_face");
        if (baseMiuiFaceManager != null) {
            return baseMiuiFaceManager;
        }
        Log.i(TAG, "Failed to get FaceManager service.");
        return null;
    }

    @SuppressLint({"MissingPermission"})
    public static boolean hasEnrolledFace(Context context) {
        BaseMiuiFaceManager baseMiuiFaceManagerInitFaceManager = initFaceManager(context);
        if (baseMiuiFaceManagerInitFaceManager != null) {
            return baseMiuiFaceManagerInitFaceManager.hasEnrolledTemplates();
        }
        Log.i(TAG, "Failed to get FaceManager service or face feature is not supported.");
        return false;
    }

    @SuppressLint({"WrongConstant", "NewApi"})
    public static FingerprintManager initFingerManager(Context context) {
        if (context == null) {
            return null;
        }
        if (context.getPackageManager().hasSystemFeature("android.hardware.fingerprint")) {
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
            if (fingerprintManager != null) {
                Log.i(TAG, " initFingerManager : " + fingerprintManager);
                return fingerprintManager;
            }
            Log.i(TAG, "FingerprintManager not available on this device");
            return null;
        }
        Log.i(TAG, "No finger feature is supported ");
        return null;
    }

    @SuppressLint({"NewApi"})
    public static boolean hasEnrolledFinger(Context context) {
        FingerprintManager fingerprintManagerInitFingerManager = initFingerManager(context);
        if (fingerprintManagerInitFingerManager != null) {
            return fingerprintManagerInitFingerManager.hasEnrolledTemplates();
        }
        Log.i(TAG, "Failed to get FingerprintManager service or finger feature is not supported.");
        return false;
    }

    public static void startCongratulationPage(Activity activity, boolean z) {
        Activity activity2;
        if (!isFirstStartCongratulationPage || !z || Build.IS_INTERNATIONAL_BUILD) {
            return;
        }
        Intent flags = new Intent("com.android.settings.provision_congratulation").setPackage("com.android.settings").setFlags(268435456);
        try {
            try {
                BoostHelper.getInstance().boostOtherProcess(activity, "com.android.settings");
                activity2 = activity;
                try {
                    activity2.startActivity(flags, ActivityOptions.makeCustomTaskAnimation(activity2, R.anim.slide_in_right, R.anim.slide_out_left, null, null, null).toBundle());
                    Log.d(TAG, "startCongratulationPage success");
                } catch (Exception e) {
                    e = e;
                    Log.d(TAG, "startCongratulationPage fail", e);
                    activity2.startActivity(flags);
                }
            } catch (Exception e2) {
                e = e2;
                activity2 = activity;
            }
        } finally {
            isFirstStartCongratulationPage = false;
        }
    }

    public static void setCustImeConfig(Context context) {
        try {
            boolean zIsCustSupportImeConfig = MiuiInit.isCustSupportImeConfig();
            if (zIsCustSupportImeConfig) {
                Settings.Secure.putInt(context.getContentResolver(), "is_cust_support_ime_config", 1);
            }
            Log.d(TAG, "isCustSupportImeConfig " + zIsCustSupportImeConfig);
        } catch (Exception unused) {
            Log.d(TAG, "isCustSupportImeConfig fail");
        }
    }

    public static boolean isCustSupportImeConfig(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "is_cust_support_ime_config", 0) == 1;
    }

    public static boolean queryCTAStatus(Context context, String str) {
        boolean z = false;
        if (context == null) {
            Log.d(TAG, "queryCTAStatus: context is null " + str);
            return false;
        }
        try {
            Bundle bundleCall = context.getContentResolver().call(Uri.parse("content://com.miui.permcenter.privacycenter"), "queryCTAStatus", str, (Bundle) null);
            if (bundleCall == null) {
                Log.d(TAG, "queryCTAStatus: bundle is null");
            } else {
                z = bundleCall.getBoolean("is_agreed");
            }
        } catch (Exception e) {
            Log.e(TAG, "queryCTAStatus: fail " + e.getMessage());
        }
        Log.d(TAG, str + " CTAStatus is " + z);
        return z;
    }

    public static boolean isBlurEffectEnabled(Context context) {
        Boolean bool = blurEffectEnabledCache;
        if (bool != null) {
            return bool.booleanValue();
        }
        boolean z = false;
        if (context == null) {
            return false;
        }
        if (HyperMaterialUtils.isEnable() && !LiteUtils.isCommonLiteStrategy() && HyperMaterialUtils.isFeatureEnable(context)) {
            z = true;
        }
        blurEffectEnabledCache = Boolean.valueOf(z);
        Log.d(TAG, "isBlurEffectEnabled: " + blurEffectEnabledCache);
        return blurEffectEnabledCache.booleanValue();
    }

    public static boolean isPackagesSupportMetaDataFeature(Context context, String str, String str2, String str3) {
        boolean z = false;
        try {
            Bundle bundle = context.getPackageManager().getApplicationInfo(str, ParticleFlag.tensileParticle).metaData;
            if (bundle != null && TextUtils.equals(bundle.getString(str2), str3)) {
                z = true;
            }
            Log.d(TAG, "isPackagesSupportMetaDataFeature result " + z + " packageName " + str + " metaDataKey " + str2 + " flag " + str3);
            return z;
        } catch (Exception unused) {
            Log.d(TAG, "fail to obtain the metaData");
            return z;
        }
    }

    public static String getTopActivityClassName(Context context) {
        List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
        if (runningTasks == null || runningTasks.isEmpty()) {
            return null;
        }
        Log.d(TAG, "getTopActivityClassName: " + runningTasks.get(0).topActivity.getClassName());
        return runningTasks.get(0).topActivity.getClassName();
    }

    public static boolean isShowMiShareItem(Context context) {
        if (context == null) {
            Log.d(TAG, "isShowMiShareItem: context is null");
            return false;
        }
        if (Build.IS_INTERNATIONAL_BUILD) {
            return applicationInstalled(context, "com.miui.mishare.connectivity");
        }
        return false;
    }

    public static void setXiaomiShareFlag(Context context, boolean z) {
        if (context == null) {
            Log.i(TAG, "setXiaomiShareFlag context is null");
            return;
        }
        Settings.System.putInt(context.getContentResolver(), "mishare_cta_agree", z ? 1 : 0);
        Log.i(TAG, " setXiaomiShareFlag state is " + z);
    }

    public static boolean isAlreadyEnterFontStyleFromGms(Context context) {
        int i = Settings.System.getInt(context.getContentResolver(), "settings_is_font_setting_enter_already", 0);
        Log.d(TAG, "isAlreadyEnterFontStyleFromGms = " + i);
        return 1 == i;
    }

    public static int getRecentsLayoutStyleNums(Context context) {
        if (context == null) {
            Log.d(TAG, "getRecentsLayoutStyleNums: context is null");
            return 2;
        }
        try {
            Bundle bundleCall = context.getContentResolver().call(Uri.parse("content://" + AUTHORITY_FOR_LAUNCHER), "getRecentsLayoutStyleNums", (String) null, (Bundle) null);
            if (bundleCall == null) {
                Log.d(TAG, "getRecentsLayoutStyleNums: bundle is null");
                return 2;
            }
            int i = bundleCall.getInt("recentsLayoutStyleNums", 2);
            Log.d(TAG, "getRecentsLayoutStyleNums: recentsLayoutStyleNums " + i);
            return i;
        } catch (Exception e) {
            Log.e(TAG, "getRecentsLayoutStyleNums error:" + e);
            return 2;
        }
    }
}
