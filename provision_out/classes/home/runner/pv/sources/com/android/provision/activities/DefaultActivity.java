package com.android.provision.activities;

import android.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import com.android.provision.AbnormalBackService;
import com.android.provision.CloudBackupConfigHelper;
import com.android.provision.Constants;
import com.android.provision.CoverScreenService;
import com.android.provision.DefaultPreferenceHelper;
import com.android.provision.EnterpriseNfcSetupHelper;
import com.android.provision.HomeSearchBarHelper;
import com.android.provision.IOnFocusListener;
import com.android.provision.InputMethodAndSubtypeUtil;
import com.android.provision.ProvisionApplication;
import com.android.provision.ProvisionStateHolder;
import com.android.provision.SimInfoUtils;
import com.android.provision.StatusBarControllerService;
import com.android.provision.TimeReceiver;
import com.android.provision.Utils;
import com.android.provision.animtool.LanguageAnimTool;
import com.android.provision.cust.buz.chinamobile.ChinaMobileBuzSupporter;
import com.android.provision.fragment.BootVideoFragment;
import com.android.provision.fragment.InputMethodFragment;
import com.android.provision.fragment.OtherSettingsFragment;
import com.android.provision.fragment.SimCardDetectionFragment;
import com.android.provision.fragment.StartupFragment;
import com.android.provision.global.WizardManagerHelper;
import com.android.provision.manager.PreLoadManager;
import com.android.provision.miconnect.MiConnectServer;
import com.android.provision.receiver.NetWorkChangedReceiver;
import com.android.provision.utils.ActivityCollector;
import com.android.provision.utils.AuraMediator;
import com.android.provision.utils.Base64Utils;
import com.android.provision.utils.BoostHelper;
import com.android.provision.utils.CarouselConstant;
import com.android.provision.utils.CspAutoSwitchManager;
import com.android.provision.utils.DefaultHomeUtils;
import com.android.provision.utils.FingerprintReader;
import com.android.provision.utils.FontSizeUtils;
import com.android.provision.utils.ImmersiveUtils;
import com.android.provision.utils.MccHelper;
import com.android.provision.utils.MediaPlayerPool;
import com.android.provision.utils.MinorControlUtils;
import com.android.provision.utils.MiuiDockUtils;
import com.android.provision.utils.NetworkUtils;
import com.android.provision.utils.NotchAdapterUtils;
import com.android.provision.utils.OTHelper;
import com.android.provision.utils.PageIntercepHelper;
import com.android.provision.utils.PreLoadWifi;
import com.android.provision.utils.UserManagerHelper;
import com.android.provision.utils.VoiceWakeupUtils;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import miui.accounts.ExtraAccountManager;
import miui.enterprise.EnterpriseManagerStub;
import miui.enterprise.RestrictionsHelperStub;
import miui.os.Build;
import miui.os.HyperOSCustFeatureResolve;
import miui.os.MiuiInit;
import miui.telephony.SubscriptionManager;
import miui.util.FeatureParser;
import miui.util.MiuiMultiDisplayTypeInfo;
import miuix.device.DeviceUtils;
import miuix.mgl.physics.ParticleFlag;
import miuix.provision.ProvisionBaseActivity;

/* JADX INFO: loaded from: classes.dex */
public class DefaultActivity extends ProvisionBaseActivity {
    private static final String AUTHORITY_FIND_DEVICE = "com.xiaomi.finddevice.provider";
    private static final String AUTH_TOKEN_TYPE = "micloud";
    public static final String HUANJI_ACTION = "com.intent.action.HuanjiProvision";
    private static final String HUANJI_OLD_FINGERPRINT = "huanji_old_fingerprint";
    private static final String HUANJI_PACKAGE_NAME = "com.miui.huanji";
    public static final int JELLY_BEAN_MR1_API_LEVEL = 17;
    private static final String KEY_IDM_MODE = "idm_mode";
    private static final String KEY_STATE_PREFIX = "com.android.provision.STATE_";
    private static int MiMoverDirect = 0;
    private static final int NEXT_REQUEST_CODE = 10000;
    private static final String OTA_ANDROID_ID = "ota_android_id";
    private static final String PROVISION_COMPLETE_BROADCAST = "android.provision.action.PROVISION_COMPLETE";
    private static final String PROVISION_START_BROADCAST = "android.provision.action.PROVISION_START";
    private static final int RESULT_CHECK_USER_UNLOCK_NFC = 124;
    private static final int RESULT_CHECK_USER_UNLOCK_WORK_SETUP_INTERRUPTED_NFC = 125;
    public static long START_TIME = 0;
    private static final String STATE_ENTER_CURRENTSTATE = "com.android.provision:state_enter_currentstate";
    private static final String SUPPORT_CM_LANGUAGE_BO = "support_cm_language_bo";
    private static final String TAG = "Provision_DefaultActivity";
    private static final String TIBETIC_LANGUAGES = "bo_CN";
    public static final String USER_SETUP_COMPLETE = "user_setup_complete";
    private NetWorkChangedReceiver mNetorkReceiver;
    private StateMachine mStateMachine;
    private boolean mIsNetworkRegistered = false;
    private boolean mIsBtReceiverRegistered = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public interface IKeyEvent {
        void keyDownDispatcher(int i, KeyEvent keyEvent);
    }

    public static class KindTipState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return true;
        }
    }

    public static class OtherState extends State {
    }

    public static class ThemePickerState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return false;
        }
    }

    @Override // miuix.provision.ProvisionBaseActivity
    public boolean hasNavigationButton() {
        return false;
    }

    @Override // miuix.provision.ProvisionBaseActivity
    public boolean hasPreview() {
        return false;
    }

    @Override // miuix.provision.ProvisionBaseActivity
    public boolean hasTitle() {
        return false;
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // miuix.provision.ProvisionBaseActivity, miuix.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) throws Throwable {
        super.onCreate(bundle);
        Log.d(TAG, "DefaultActivity onCreate");
        if (deviceIsProvisioned()) {
            finishSetup(false);
            return;
        }
        if (Utils.isFlipDevice()) {
            Utils.disableScreenshots(this);
            startService(new Intent(this, (Class<?>) CoverScreenService.class));
        }
        boolean z = true;
        if (MiuiMultiDisplayTypeInfo.isIndependentRearDevice()) {
            Intent intent = new Intent(this, (Class<?>) SubScreenActivity.class);
            intent.addFlags(268435456);
            startActivity(intent, ActivityOptions.makeBasic().setLaunchDisplayId(1).toBundle());
            try {
                this.mHandler.postDelayed(new Runnable() { // from class: com.android.provision.activities.DefaultActivity$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onCreate$0();
                    }
                }, 1000L);
            } catch (Exception e) {
                Log.e(TAG, "moveTaskToFront err: " + e);
            }
        }
        PageIntercepHelper.getInstance().register();
        PageIntercepHelper.getInstance().setCallback(new PageIntercepHelper.Callback() { // from class: com.android.provision.activities.DefaultActivity$$ExternalSyntheticLambda2
            @Override // com.android.provision.utils.PageIntercepHelper.Callback
            public final void onActivityResult(int i, int i2, Intent intent2) throws Throwable {
                this.f$0.onActivityResult(i, i2, intent2);
            }
        });
        if (MccHelper.getInstance().isSupportTrustonic(ProvisionApplication.getContext())) {
            UserManagerHelper.getInstance().disableUserSpace(true);
        }
        MiuiDockUtils.disableInitDockStatus(getApplication());
        START_TIME = System.currentTimeMillis();
        Utils.sendBroadcastAsUser(this, new Intent(PROVISION_START_BROADCAST));
        this.mStateMachine = new StateMachine(this);
        if (bundle != null && !bundle.getBoolean(STATE_ENTER_CURRENTSTATE, true)) {
            z = false;
        }
        this.mStateMachine.start(z);
        startService(new Intent(this, (Class<?>) AbnormalBackService.class));
        boolean z2 = Build.IS_INTERNATIONAL_BUILD;
        if (z2) {
            startService(new Intent(this, (Class<?>) StatusBarControllerService.class));
        } else {
            Utils.enableStatusBar(this, false);
        }
        initFullScreenMode();
        registerNetworkChangedReceiver();
        if (!z2) {
            HomeSearchBarHelper.checkAndSetHomeSearchBarOnProvision(this);
        }
        ProvisionStateHolder.getInstance().setStateMachine(this.mStateMachine);
        if (this.mNewBackBtn != null) {
            Log.i(TAG, "back button set accessibility no");
            this.mNewBackBtn.setImportantForAccessibility(2);
            if (this.mNewBackBtn.getParent() != null) {
                Log.i(TAG, "back button remove");
                ((ViewGroup) this.mNewBackBtn.getParent()).removeView(this.mNewBackBtn);
            }
        }
        Button button = this.mConfirmButton;
        if (button != null) {
            button.setVisibility(8);
        }
        if (z2) {
            Settings.Secure.putString(getContentResolver(), OTA_ANDROID_ID, Settings.Secure.getString(getContentResolver(), "android_id"));
        }
        CspAutoSwitchManager.deleteGLPA(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$0() {
        ((ActivityManager) getSystemService("activity")).moveTaskToFront(getTaskId(), 0);
    }

    @Override // miuix.provision.ProvisionBaseActivity, android.app.Activity, android.view.Window.Callback
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (this.mStateMachine.getCurrentState() instanceof IKeyEvent) {
            Log.i(TAG, " here is onWindowFocusChanged ");
            ((StartupState) this.mStateMachine.getCurrentState()).onWindowFocusChanged(z);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // miuix.appcompat.app.AppCompatActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        StateMachine stateMachine = this.mStateMachine;
        if (stateMachine != null) {
            bundle.putBoolean(STATE_ENTER_CURRENTSTATE, stateMachine.mCurrentState instanceof StartupState);
        }
    }

    @Override // miuix.provision.ProvisionBaseActivity, miuix.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        PageIntercepHelper.getInstance().unregisterReceiver();
        Utils.enableStatusBar(this, true);
        unRegisterNetworkChangedReceiver();
        super.onDestroy();
        if (deviceIsProvisioned()) {
            Process.killProcess(Process.myPid());
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) throws Throwable {
        Log.i(TAG, "onActivityResult requestCode: " + i + " resultCode =  " + i2);
        if (i == 33 && Utils.shouldNotFinishDefaultActivity()) {
            this.mStateMachine.resumeState();
            return;
        }
        if (i == 1) {
            if (i2 == -1) {
                if (isManagedProvisioningDpcDownloaded()) {
                    nextAction(125);
                    return;
                } else {
                    nextAction(124);
                    return;
                }
            }
            return;
        }
        if (intent != null && intent.hasExtra(Utils.MI_MOVER_DIRECT_TAG)) {
            MiMoverDirect = intent.getIntExtra(Utils.MI_MOVER_DIRECT_TAG, 0);
        }
        if (i == 3510) {
            onAuraActivityResult(i, i2);
            return;
        }
        this.mStateMachine.onResult(i2, intent);
        if (intent != null) {
            this.mStateMachine.setMultiSimSettingsSkiped(intent.getBooleanExtra(SimCardDetectionFragment.EXTRA_MUTISIMSETTINGS_FORCE_SKIPED, false));
            this.mStateMachine.setBootVideoSkiped(intent.getBooleanExtra(BootVideoFragment.EXTRA_BOOTVIDEO_FORCE_SKIPED, false));
        }
        this.mStateMachine.run(i2);
    }

    private boolean isManagedProvisioningDpcDownloaded() {
        return Settings.Secure.getInt(getContentResolver(), EnterpriseNfcSetupHelper.MANAGED_PROVISIONING_DPC_DOWNLOADED, 0) == 1;
    }

    void nextAction(int i) {
        Log.i(TAG, " here is nextAction ");
        startActivityForResult(WizardManagerHelper.getNextIntent(getIntent(), i), 10000);
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (EnterpriseNfcSetupHelper.isProcessingNfcIntent(intent)) {
            Log.i(TAG, " here is onNewIntent ");
            EnterpriseNfcSetupHelper.startSavingNfcIntent(this, intent, 1);
        }
    }

    private void onAuraActivityResult(int i, int i2) {
        AuraMediator.getInstance().onActivityResult(i, i2, new AuraMediator.OnPrimaryFlowCompletedListener() { // from class: com.android.provision.activities.DefaultActivity.1
            @Override // com.android.provision.utils.AuraMediator.OnPrimaryFlowCompletedListener
            public void onCompleted() throws Throwable {
                DefaultActivity.this.mStateMachine.run(-1);
            }

            @Override // com.android.provision.utils.AuraMediator.OnPrimaryFlowCompletedListener
            public void onCancelled() throws Throwable {
                DefaultActivity.this.mStateMachine.run(0);
            }
        });
    }

    private void registerNetworkChangedReceiver() {
        if (!Build.IS_INTERNATIONAL_BUILD || this.mIsNetworkRegistered) {
            return;
        }
        this.mIsNetworkRegistered = true;
        this.mNetorkReceiver = new NetWorkChangedReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(this.mNetorkReceiver, intentFilter);
    }

    private void unRegisterNetworkChangedReceiver() {
        if (Build.IS_INTERNATIONAL_BUILD && this.mIsNetworkRegistered) {
            this.mIsNetworkRegistered = false;
            unregisterReceiver(this.mNetorkReceiver);
        }
    }

    public void run(int i) throws Throwable {
        this.mStateMachine.run(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishSetup(boolean z) throws Throwable {
        Log.d(TAG, "finishSetup");
        notifyChinaMobileSetupFinish();
        Utils.enableStatusBar(this, true);
        ImmersiveUtils.disableImmersion(this);
        if (!Utils.isGestureLineShow(this) && !z) {
            Utils.hideGestureLine(this, false);
        }
        if (!DefaultPreferenceHelper.isPrefFullscreen()) {
            Utils.setNavigationBarFullScreen(this, false);
        }
        if (!Utils.getMinorsModeFlag(this)) {
            MinorControlUtils.disableMinorControl();
        }
        Utils.closeBluetoothIfNeed(this);
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getApplicationInfo("com.google.android.setupwizard", ParticleFlag.repulsiveParticle);
            packageManager.setApplicationEnabledSetting("com.google.android.setupwizard", 2, 0);
        } catch (PackageManager.NameNotFoundException unused) {
        }
        Settings.Secure.putInt(getContentResolver(), "device_provisioned", 1);
        Utils.setWallperProvisioned(this, true);
        Settings.Secure.putInt(getContentResolver(), USER_SETUP_COMPLETE, 1);
        ComponentName componentName = new ComponentName(this, (Class<?>) DefaultActivity.class);
        if (packageManager != null) {
            packageManager.setComponentEnabledSetting(componentName, 2, 1);
        }
        try {
            stopService(new Intent(this, (Class<?>) AbnormalBackService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Utils.isFlipDevice()) {
            try {
                stopService(new Intent(this, (Class<?>) CoverScreenService.class));
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        String fingerprint = FingerprintReader.getFingerprint(getApplicationContext());
        if (fingerprint != null) {
            Settings.System.putString(getContentResolver(), HUANJI_OLD_FINGERPRINT, fingerprint);
        }
        Utils.sendProvisionCompleteBroadcast(this);
        Utils.startCongratulationPage(this, z);
        this.mHandler.postDelayed(new Runnable() { // from class: com.android.provision.activities.DefaultActivity$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$finishSetup$1();
            }
        }, z ? 1000L : 0L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$finishSetup$1() {
        if (MiuiMultiDisplayTypeInfo.isIndependentRearDevice()) {
            ActivityCollector.finishActivity(SubScreenActivity.class);
        }
        finish();
    }

    private boolean deviceIsProvisioned() {
        if (HyperOSCustFeatureResolve.getBoolean("provision_config_remove_provision_device", false)) {
            return true;
        }
        return Utils.isProvisioned(this);
    }

    private void initFullScreenMode() {
        boolean z = Build.IS_INTERNATIONAL_BUILD;
        DefaultPreferenceHelper.setPrefFullscreen(!z);
        Utils.setNavigationBarFullScreen(this, !z);
    }

    private void notifyChinaMobileSetupFinish() {
        try {
            ChinaMobileBuzSupporter.getInstance().onSetupFinish();
        } catch (Exception e) {
            Log.e(TAG, "Error notifying China Mobile setup finish", e);
        }
    }

    public static class State {
        public static final String PREFIX = "com.android.provision.activities.DefaultActivity$";
        public String className;
        protected Context context;
        protected Handler mHandler = new Handler();
        protected StateMachine mStateMachine;
        protected String packageName;
        public Class<?> targetClass;

        public boolean canBackTo() {
            return true;
        }

        public State getNextState() {
            return null;
        }

        public void onLeave() {
        }

        public static State create(String str) {
            try {
                return (State) Class.forName(PREFIX + str).newInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
                return null;
            } catch (InstantiationException e3) {
                e3.printStackTrace();
                return null;
            }
        }

        public State setPackageName(String str) {
            this.packageName = str;
            return this;
        }

        public State setClassName(String str) {
            this.className = str;
            return this;
        }

        public State setTargetClass(Class<?> cls) {
            this.targetClass = cls;
            return this;
        }

        public State setStateMachine(StateMachine stateMachine) {
            this.mStateMachine = stateMachine;
            this.context = stateMachine.mContext;
            return this;
        }

        public void setStateContext(Context context) {
            this.context = context;
        }

        public void onEnter(boolean z, boolean z2) {
            DefaultActivity defaultActivity = (DefaultActivity) this.context;
            Log.d(DefaultActivity.TAG, "targetClass is " + this.targetClass);
            Intent intent = getIntent();
            intent.putExtra(BaseActivity.EXTRA_DISABLE_BACK, z ^ true);
            intent.putExtra(BaseActivity.EXTRA_TO_NEXT, z2);
            defaultActivity.startActivityForResult(intent, 0);
        }

        public boolean isAvailable(boolean z) {
            return this.context.getPackageManager().resolveActivity(getIntent(), 0) != null;
        }

        protected Intent getIntent() {
            Intent intent = new Intent();
            if (TextUtils.isEmpty(this.packageName)) {
                intent.setClass(this.context, this.targetClass);
                return intent;
            }
            intent.setClassName(this.packageName, this.className);
            return intent;
        }

        public String getPageTag() {
            return "";
        }
    }

    @Override // miuix.appcompat.app.AppCompatActivity, android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (this.mStateMachine.getCurrentState() instanceof IKeyEvent) {
            ((IKeyEvent) this.mStateMachine.getCurrentState()).keyDownDispatcher(i, keyEvent);
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    public static class StartupState extends State implements IKeyEvent, IOnFocusListener {
        private boolean mHasBooted;
        private PendingIntent mShutDownPendingIntent;
        private Fragment.SavedState savedState;
        private StartupFragment startupFragment;
        private final int ONE_MINUTE = 60000;
        private final int DELAY_SHUTDOWN_2H = 7200000;

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return true;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public void onEnter(boolean z, boolean z2) {
            FragmentManager supportFragmentManager = ((DefaultActivity) this.context).getSupportFragmentManager();
            this.startupFragment = new StartupFragment();
            if (supportFragmentManager.findFragmentByTag(StartupFragment.class.getSimpleName()) == null) {
                buildStartupFragment(this.startupFragment, supportFragmentManager);
            } else {
                this.startupFragment.setInitialSavedState(this.savedState);
                buildStartupFragment(this.startupFragment, supportFragmentManager);
            }
            addShutdownTask();
            Utils.isShowLanguageListAnim = true;
        }

        private void buildStartupFragment(Fragment fragment, FragmentManager fragmentManager) {
            FragmentTransaction fragmentTransactionBeginTransaction = fragmentManager.beginTransaction();
            fragmentTransactionBeginTransaction.replace(R.id.content, fragment, StartupFragment.class.getSimpleName());
            fragmentTransactionBeginTransaction.commitAllowingStateLoss();
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public void onLeave() {
            FragmentManager supportFragmentManager = ((DefaultActivity) this.context).getSupportFragmentManager();
            Fragment fragmentFindFragmentByTag = supportFragmentManager.findFragmentByTag(StartupFragment.class.getSimpleName());
            if (fragmentFindFragmentByTag != null) {
                this.savedState = supportFragmentManager.saveFragmentInstanceState(fragmentFindFragmentByTag);
            }
            cancelShutdownTask();
        }

        public void setBooted(boolean z) {
            this.mHasBooted = z;
        }

        private void addShutdownTask() {
            if (!Build.IS_DEBUGGABLE) {
                Utils.printLog(DefaultActivity.TAG, "send shutdown msg");
                AlarmManager alarmManager = (AlarmManager) this.context.getSystemService("alarm");
                Intent intent = new Intent(this.context, (Class<?>) TimeReceiver.class);
                intent.setAction(TimeReceiver.ACTION);
                this.mShutDownPendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, 67108864);
                alarmManager.setExact(2, SystemClock.elapsedRealtime() + 7200000, this.mShutDownPendingIntent);
                return;
            }
            Utils.printLog(DefaultActivity.TAG, "debugable version don't need to send shutdown msg");
        }

        public void cancelShutdownTask() {
            if (this.mShutDownPendingIntent != null) {
                ((AlarmManager) this.context.getSystemService("alarm")).cancel(this.mShutDownPendingIntent);
                Utils.printLog(DefaultActivity.TAG, "cancel shut down " + this.mShutDownPendingIntent);
                this.mShutDownPendingIntent = null;
            }
        }

        @Override // com.android.provision.activities.DefaultActivity.IKeyEvent
        public void keyDownDispatcher(int i, KeyEvent keyEvent) {
            StartupFragment startupFragment = this.startupFragment;
            if (startupFragment != null) {
                startupFragment.onKeyDownChild(i, keyEvent);
            }
        }

        @Override // com.android.provision.IOnFocusListener
        public void onWindowFocusChanged(boolean z) {
            StartupFragment startupFragment = this.startupFragment;
            if (startupFragment != null) {
                startupFragment.onWindowFocusChanged(z);
            }
        }
    }

    public static class LanguageState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return Constants.KEY_CLICK_LANGUAGE_LANGUAGE;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        protected Intent getIntent() {
            Intent intent = super.getIntent();
            intent.putExtra(LanguageAnimTool.LANG_SHOW_ANIM, true);
            return intent;
        }
    }

    public static class ZonePickerState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            super.isAvailable(z);
            return false;
        }
    }

    public static class InputMethodState extends State {
        private static List<InputMethodInfo> tmpImmList = new ArrayList();
        static long loadTime = 0;

        private static void updateImmList(List<InputMethodInfo> list) {
            if (tmpImmList == null) {
                tmpImmList = new ArrayList();
            }
            tmpImmList.clear();
            if (list == null) {
                return;
            }
            tmpImmList.addAll(list);
        }

        private boolean needUpdateInputMethod() {
            return Math.abs(System.currentTimeMillis() - loadTime) > 2000;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            List<InputMethodInfo> arrayList;
            if (super.isAvailable(z) && !Utils.isCustSupportImeConfig(this.context)) {
                InputMethodManager inputMethodManager = (InputMethodManager) this.context.getSystemService(Constants.KEY_INPUT_METHOD);
                if (needUpdateInputMethod()) {
                    arrayList = inputMethodManager.getInputMethodList();
                    updateImmList(arrayList);
                    Log.i(DefaultActivity.TAG, "getInputMethodList updateImmList");
                } else {
                    arrayList = new ArrayList<>(tmpImmList);
                }
                loadTime = System.currentTimeMillis();
                ArrayList arrayList2 = new ArrayList(tmpImmList);
                String string = Locale.getDefault().toString();
                Log.i(DefaultActivity.TAG, "Current local :" + string);
                if (arrayList != null) {
                    if (InputMethodFragment.sEmojiInputMethodSetDefault.contains(string)) {
                        InputMethodInfo inputMethodInfo = null;
                        InputMethodInfo inputMethodInfo2 = null;
                        for (int size = arrayList.size() - 1; size >= 0; size--) {
                            InputMethodInfo inputMethodInfo3 = arrayList.get(size);
                            if (InputMethodFragment.GLOBAL_DEFAULT_IME_YANDEX_KEYBOARD.equals(inputMethodInfo3.getId())) {
                                inputMethodInfo = inputMethodInfo3;
                            } else if (InputMethodFragment.GLOBAL_DEFAULT_IME_EMOJI.equals(inputMethodInfo3.getId())) {
                                inputMethodInfo2 = inputMethodInfo3;
                            }
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("is yandex input: ");
                        sb.append(inputMethodInfo != null);
                        sb.append(", emoji input: ");
                        sb.append(inputMethodInfo2 != null);
                        sb.append(", currentLocal: ");
                        sb.append(string);
                        Log.i(DefaultActivity.TAG, sb.toString());
                        if (inputMethodInfo != null && (("ru_RU".equalsIgnoreCase(string) && isSupportYandexInputMethod(CarouselConstant.Region.RUSSIA)) || ("tr_TR".equalsIgnoreCase(string) && isSupportYandexInputMethod(CarouselConstant.Region.TURKEY)))) {
                            Log.i(DefaultActivity.TAG, "yandex Input Set");
                            setInputMethod(inputMethodInfo.getId(), arrayList, false);
                            return false;
                        }
                        if (inputMethodInfo2 != null) {
                            Log.i(DefaultActivity.TAG, "emoji Input Set");
                            setInputMethod(inputMethodInfo2.getId(), arrayList, false);
                            return false;
                        }
                    }
                    for (int size2 = arrayList.size() - 1; size2 >= 0; size2--) {
                        InputMethodInfo inputMethodInfo4 = arrayList.get(size2);
                        if (Build.IS_GLOBAL_BUILD && setGlobalDefaultInputMethodSucceed(inputMethodInfo4, string, inputMethodManager, arrayList)) {
                            return false;
                        }
                        if (InputMethodFragment.sBlackImmSet.contains(inputMethodInfo4.getId())) {
                            arrayList.remove(size2);
                        }
                    }
                    if (arrayList.size() == 1 || (Utils.isShieldInputMethods() && arrayList.size() > 1)) {
                        setInputMethod(arrayList.get(0).getId(), arrayList2, false);
                    } else {
                        if (shouldChangeSogouInBo(arrayList)) {
                            return !setDefaultInputMethod(inputMethodManager, InputMethodFragment.IME_SOGO, arrayList2);
                        }
                        if (!domesticAndNotCM() || arrayList.size() <= 1) {
                            return arrayList.size() > 1;
                        }
                        return !distributeDefaultInputMethod(inputMethodManager, arrayList2);
                    }
                }
            }
            return false;
        }

        private boolean isSupportYandexInputMethod(String str) {
            try {
                String str2 = SystemProperties.get("ro.miui.build.region");
                String str3 = miuix.core.util.SystemProperties.get("ro.miui.region", "");
                Log.i(DefaultActivity.TAG, "Tr sku: " + str2 + " region: " + str3);
                return str.equalsIgnoreCase(str2) && str.equalsIgnoreCase(str3);
            } catch (Exception e) {
                Log.i(DefaultActivity.TAG, "is tr exception: " + e.getLocalizedMessage());
                return false;
            }
        }

        private void setInputMethod(String str, List<InputMethodInfo> list, boolean z) {
            Log.i(DefaultActivity.TAG, "setInputMethod infoId=" + str);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) != null) {
                        Log.i(DefaultActivity.TAG, "setInputMethod index" + i + "_" + list.get(i).getId());
                    }
                }
            }
            String string = Settings.Secure.getString(this.context.getContentResolver(), "default_input_method");
            if (str != null && DefaultPreferenceHelper.isHasSetInputMethod() && str.equals(string)) {
                Log.d(DefaultActivity.TAG, "setInputMethod no need change infoId:" + str);
                return;
            }
            DefaultPreferenceHelper.completeSetInputMethod();
            Settings.Secure.putString(this.context.getContentResolver(), "default_input_method", str);
            if (z) {
                InputMethodAndSubtypeUtil.enableInputMethods(this.context.getContentResolver(), str);
            }
            if (InputMethodFragment.sGlobalDefaultImmSet.contains(str)) {
                DefaultPreferenceHelper.setImmId(str);
            } else {
                DefaultPreferenceHelper.setImmId("");
            }
            InputMethodAndSubtypeUtil.saveInputMethodSubtypeList(this.context.getContentResolver(), list, this.context.getResources().getConfiguration().keyboard == 2);
        }

        private boolean shouldChangeSogouInBo(List<InputMethodInfo> list) {
            String language = Utils.getLanguage();
            Log.w(DefaultActivity.TAG, "choose language:" + language);
            return TextUtils.equals(language, DefaultActivity.TIBETIC_LANGUAGES) && list.size() > 1 && (FeatureParser.getBoolean(DefaultActivity.SUPPORT_CM_LANGUAGE_BO, false) || domesticAndNotCM());
        }

        private boolean domesticAndNotCM() {
            return (Build.IS_GLOBAL_BUILD || Build.IS_CM_CUSTOMIZATION) ? false : true;
        }

        private boolean distributeDefaultInputMethod(InputMethodManager inputMethodManager, List<InputMethodInfo> list) {
            long jAbs = Math.abs(new SecureRandom().nextLong()) % 100;
            if (Utils.isTabletDevice()) {
                if (jAbs < 35) {
                    return setDefaultInputMethod(inputMethodManager, InputMethodFragment.IME_BAIDU, list);
                }
                if (jAbs < 70) {
                    return setDefaultInputMethod(inputMethodManager, InputMethodFragment.IME_XUNFEI, list);
                }
                return setDefaultInputMethod(inputMethodManager, InputMethodFragment.IME_SOGO, list);
            }
            if (jAbs < 40) {
                return setDefaultInputMethod(inputMethodManager, InputMethodFragment.IME_BAIDU, list);
            }
            if (jAbs < 69) {
                return setDefaultInputMethod(inputMethodManager, InputMethodFragment.IME_XUNFEI, list);
            }
            return setDefaultInputMethod(inputMethodManager, InputMethodFragment.IME_SOGO, list);
        }

        private boolean setDefaultInputMethod(InputMethodManager inputMethodManager, String str, List<InputMethodInfo> list) {
            if (list == null) {
                Log.d(DefaultActivity.TAG, "setDefaultInputMethod empty");
                return true;
            }
            String[] strArr = {InputMethodFragment.IME_BAIDU, InputMethodFragment.IME_SOGO, InputMethodFragment.IME_XUNFEI};
            ArrayList arrayList = new ArrayList();
            Iterator<InputMethodInfo> it = list.iterator();
            while (it.hasNext()) {
                arrayList.add(it.next().getId());
            }
            if (!arrayList.contains(str)) {
                String str2 = (String) arrayList.get(0);
                Log.w(DefaultActivity.TAG, "List of installed input methods is not contains targetIme : " + str);
                str = str2;
            }
            String string = Settings.Secure.getString(this.context.getContentResolver(), "default_input_method");
            if (DefaultPreferenceHelper.isHasSetInputMethod() && containsMethod(strArr, string) && containsMethod(strArr, str)) {
                Log.v(DefaultActivity.TAG, "setDefaultInputMethod not need set,defaultIme|hasSetDefaultInputMethod: " + str + "/" + string);
                return true;
            }
            Log.v(DefaultActivity.TAG, "set default input method : " + str);
            setInputMethod(str, inputMethodManager.getInputMethodList(), false);
            OTHelper.rdInputmethodEvent(str);
            return true;
        }

        private boolean containsMethod(String[] strArr, String str) {
            if (strArr != null && str != null) {
                for (String str2 : strArr) {
                    if (str2.equals(str)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean setGlobalDefaultInputMethodSucceed(InputMethodInfo inputMethodInfo, String str, InputMethodManager inputMethodManager, List<InputMethodInfo> list) {
            Log.i(DefaultActivity.TAG, "setGlobalDefaultInputMethodSucceed info=" + inputMethodInfo.getId() + ", currentLocal=" + str);
            if (InputMethodFragment.sGlobalDefaultImmSet.contains(inputMethodInfo.getId())) {
                ArrayList arrayList = new ArrayList();
                for (int subtypeCount = inputMethodInfo.getSubtypeCount() - 1; subtypeCount >= 0; subtypeCount--) {
                    Log.d(DefaultActivity.TAG, "setGlobalDefaultInput locale=" + inputMethodInfo.getSubtypeAt(subtypeCount).getLocale() + ",info=" + inputMethodInfo.getId());
                    if (TextUtils.equals(str, inputMethodInfo.getSubtypeAt(subtypeCount).getLocale())) {
                        arrayList.add(inputMethodInfo);
                        setInputMethod(inputMethodInfo.getId(), arrayList, false);
                        return true;
                    }
                }
                for (int size = list.size() - 1; size >= 0; size--) {
                    String id = list.get(size).getId();
                    if (TextUtils.equals(InputMethodFragment.GLOBAL_DEFAULT_IME_GBOARD, id)) {
                        arrayList.add(inputMethodInfo);
                        setInputMethod(id, arrayList, false);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static class WifiState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        protected Intent getIntent() {
            Intent intent = super.getIntent();
            intent.putExtra("wifi_setup_wizard", true);
            return intent;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (Utils.isCmTest() || !super.isAvailable(z)) {
                return false;
            }
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.MiuiSettings");
            if (this.context.getPackageManager().resolveActivity(intent, 0) != null) {
                return true;
            }
            Log.w(DefaultActivity.TAG, "MiuiSettings not exist, skip wifi step");
            return false;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return MediaPlayerPool.TAG_DEFAULT_LOAD;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public void onEnter(boolean z, boolean z2) {
            PreLoadWifi.cancel();
            BoostHelper.getInstance().boostOtherProcess(this.context, "com.android.settings");
            super.onEnter(z, z2);
        }
    }

    public static class GoogleAccountState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return super.isAvailable(z) && !Utils.isNewGlobalOOBE() && Build.IS_INTERNATIONAL_BUILD && !Utils.hasGoogleAccount(this.context);
        }
    }

    public static class AccountState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        protected Intent getIntent() {
            Intent intent = new Intent("com.xiaomi.account.action.XIAOMI_ACCOUNT_WELCOME");
            intent.setPackage("com.xiaomi.account");
            intent.addFlags(67108864);
            Bundle bundle = new Bundle();
            bundle.putBoolean("extra_show_find_device", true);
            bundle.putBoolean("extra_show_skip_login", true);
            bundle.putBoolean("show_sync_settings", false);
            bundle.putBoolean("extra_disable_back_key", false);
            bundle.putBoolean("extra_find_pwd_on_pc", true);
            bundle.putBoolean("extra_add_account_from_provision", true);
            bundle.putInt(Utils.MI_MOVER_DIRECT_TAG, DefaultActivity.MiMoverDirect);
            bundle.putString("androidPackageName", this.context.getPackageName());
            intent.putExtras(bundle);
            return intent;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return super.isAvailable(z) && !RestrictionsHelperStub.getInstance().isRestriction("disallow_mi_account") && !Build.IS_INTERNATIONAL_BUILD && (Build.IS_PRIVATE_BUILD || NetworkUtils.isCaptivePortalValidated(this.context)) && ExtraAccountManager.getXiaomiAccount(this.context) == null && Utils.queryCTAStatus(this.context, "com.xiaomi.account");
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public void onEnter(boolean z, boolean z2) {
            super.onEnter(z, z2);
            Settings.System.putInt(this.context.getContentResolver(), CloudBackupConfigHelper.KEY_IS_BACKUP_EXIST, 1);
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return "xiaomi_account";
        }
    }

    public static class CloudServiceState extends State {
        private static final String INTENT_KEY_CAN_INSTALL = "extra_can_install";
        private static final String INTENT_KEY_PASSWORD_LOGIN = "extra_password_login";
        private static final String INTENT_KEY_VERSION = "extra_master_key_version";
        private static boolean mPasswordLogin = true;
        private long mMasterKeyVersion = -1;
        private boolean mCanInstall = false;

        @Override // com.android.provision.activities.DefaultActivity.State
        protected Intent getIntent() {
            Intent intent = super.getIntent();
            intent.putExtra(INTENT_KEY_VERSION, this.mMasterKeyVersion);
            intent.putExtra(INTENT_KEY_CAN_INSTALL, this.mCanInstall);
            intent.putExtra(INTENT_KEY_PASSWORD_LOGIN, mPasswordLogin);
            return intent;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return !Build.IS_INTERNATIONAL_BUILD && super.isAvailable(z) && ExtraAccountManager.getXiaomiAccount(this.context) != null && Settings.Global.getInt(this.context.getContentResolver(), "provision_cloudService_enabled", 0) == 0 && !Utils.isRedmiDigitOrNoteSeries() && Utils.queryCTAStatus(this.context, "com.xiaomi.account");
        }

        public void onLoginResult(int i, Intent intent) {
            if (intent != null) {
                this.mMasterKeyVersion = intent.getLongExtra(INTENT_KEY_VERSION, -1L);
                this.mCanInstall = intent.getBooleanExtra(INTENT_KEY_CAN_INSTALL, false);
                mPasswordLogin = intent.getBooleanExtra("password_login", true);
            }
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return "xiaomi_cloud";
        }
    }

    public static class FindDeviceState extends State {
        private boolean mPasswordLogin = true;

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            boolean z2 = Settings.Global.getInt(this.context.getContentResolver(), "provision_findDevice_enabled", 0) == 0;
            boolean z3 = ExtraAccountManager.getXiaomiAccount(this.context) != null;
            boolean zIsEmpty = TextUtils.isEmpty(getLastSessionUserId(this.context));
            Log.i(DefaultActivity.TAG, "FindDeviceState: findDevice:" + z2 + ",isHasXiaomiCount:" + z3 + ",isSessionUserIdEmpty:" + zIsEmpty);
            return !Build.IS_INTERNATIONAL_BUILD && super.isAvailable(z) && z3 && zIsEmpty && this.mPasswordLogin && z2 && Utils.isRedmiDigitOrNoteSeries() && Utils.queryCTAStatus(this.context, "com.xiaomi.account");
        }

        private String getLastSessionUserId(Context context) {
            Cursor cursorQuery = context.getContentResolver().query(buildUri("lastSessionUserId"), null, null, null, null);
            try {
                if (cursorQuery == null) {
                    return null;
                }
                cursorQuery.moveToFirst();
                return cursorQuery.getString(0);
            } catch (Exception unused) {
                Log.e(DefaultActivity.TAG, "getLastSessionUserId error");
                return null;
            } finally {
                cursorQuery.close();
            }
        }

        private Uri buildUri(String str) {
            return Uri.withAppendedPath(Uri.parse("content://com.xiaomi.finddevice.provider"), str);
        }

        public void onLoginResult(int i, Intent intent) {
            if (intent != null) {
                this.mPasswordLogin = intent.getBooleanExtra("password_login", true);
            }
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return "find_device";
        }
    }

    public static class CloudBackupState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public void onEnter(boolean z, boolean z2) {
            super.onEnter(z, z2);
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (EnterpriseManagerStub.ENTERPRISE_ACTIVATED || Build.IS_INTERNATIONAL_BUILD || !super.isAvailable(z)) {
                return false;
            }
            if (DefaultActivity.checkHuanjiFinish(this.context) || Settings.System.getInt(this.context.getContentResolver(), CloudBackupConfigHelper.KEY_IS_BACKUP_EXIST, 1) != 1) {
                Log.i(DefaultActivity.TAG, "CloudBackupState skip, because huanji is finish or key_is_backup_exist is 0");
                return false;
            }
            if (Utils.isNetworkAvailable(this.context) || Settings.Secure.getInt(this.context.getContentResolver(), "huanji_request_cache", 0) == 1) {
                return DefaultActivity.MiMoverDirect == 2 && (isAccountExist() || isHuanjiRestoreSupported(this.context)) && Utils.queryCTAStatus(this.context, "com.xiaomi.account");
            }
            Log.i(DefaultActivity.TAG, "CloudBackupState skip, because network unavailable or huanji_request_cache is 0");
            return false;
        }

        public boolean isAccountExist() {
            return ExtraAccountManager.getXiaomiAccount(this.context) != null;
        }

        public static boolean isHuanjiRestoreSupported(Context context) {
            if (context == null || Build.IS_INTERNATIONAL_BUILD || Utils.isTabletDevice()) {
                return false;
            }
            Intent intent = new Intent(DefaultActivity.HUANJI_ACTION);
            intent.setPackage(DefaultActivity.HUANJI_PACKAGE_NAME);
            return intent.resolveActivity(context.getPackageManager()) != null;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return "data_recovery";
        }
    }

    public static final boolean checkHuanjiFinish(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), "huanji_finished", 0) == 1;
        } catch (Exception e) {
            Log.e(TAG, "get huanji exception. ", e);
            return false;
        }
    }

    public static class FingerprintState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (super.isAvailable(z)) {
                return !MiuiSettings.Secure.hasCommonPassword(this.context);
            }
            return false;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return "password";
        }
    }

    public static class SimCardDetectionState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        protected Intent getIntent() {
            Intent intent = super.getIntent();
            if (intent != null && EnterpriseManagerStub.ENTERPRISE_ACTIVATED) {
                intent.putExtra("from_provision", true);
            }
            return intent;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (!super.isAvailable(z)) {
                return false;
            }
            int simState = TelephonyManager.getDefault().getSimState();
            boolean zIsWifiOnly = Utils.isWifiOnly(this.context);
            StringBuilder sb = new StringBuilder();
            sb.append("SimCardDetectionState simState=");
            sb.append(simState);
            sb.append(",");
            sb.append(!zIsWifiOnly);
            Log.i(DefaultActivity.TAG, sb.toString());
            return (zIsWifiOnly || simState == 5) ? false : true;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return "sim_card";
        }
    }

    public static class MiMoverState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (!super.isAvailable(z) || !Utils.supportMiMover(this.context)) {
                return false;
            }
            Log.d(DefaultActivity.TAG, "MiConnectServer destroyed");
            MiConnectServer.getInstance().destroy();
            return !DefaultActivity.checkHuanjiFinish(this.context);
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return "sim_card";
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        protected Intent getIntent() {
            Intent intent = super.getIntent();
            intent.putExtra(DefaultActivity.KEY_IDM_MODE, !Build.IS_TABLET && MiConnectServer.getInstance().getConnectedStatus());
            return intent;
        }
    }

    public static class ActivationState extends State {
        private static final String PREF_UPDATER_ACTIVATION_RESULT = "updater_activation_result";
        private boolean mIsInit = false;
        private SharedPreferences mPref;
        private boolean mSuccess;

        private boolean isRomAllowed() {
            return false;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (!super.isAvailable(z)) {
                return false;
            }
            if (!this.mIsInit) {
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
                this.mPref = defaultSharedPreferences;
                this.mSuccess = defaultSharedPreferences.getBoolean(PREF_UPDATER_ACTIVATION_RESULT, false);
                this.mIsInit = true;
            }
            boolean zIsRomAllowed = isRomAllowed();
            Log.i(DefaultActivity.TAG, "isRomAllowed: " + zIsRomAllowed + " mSuccess: " + this.mSuccess);
            return zIsRomAllowed && !this.mSuccess;
        }

        public void setSuccess() {
            this.mSuccess = true;
            this.mPref.edit().putBoolean(PREF_UPDATER_ACTIVATION_RESULT, true).apply();
        }
    }

    public static class ActivateDeviceState extends State {
        private static final String ASSOCIATED_ACCOUNT = "associated_account";
        private String mAssociatedAccount;

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (Build.IS_INTERNATIONAL_BUILD) {
                return false;
            }
            super.isAvailable(z);
            return false;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        protected Intent getIntent() {
            Intent intent = super.getIntent();
            intent.putExtra(ASSOCIATED_ACCOUNT, this.mAssociatedAccount);
            return intent;
        }

        private boolean isNetworkAvailable() {
            NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.context.getSystemService("connectivity")).getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        private String getLastSessionUserId(Context context) {
            Cursor cursorQuery = context.getContentResolver().query(buildUri("lastSessionUserId"), null, null, null, null);
            try {
                if (cursorQuery == null) {
                    return null;
                }
                cursorQuery.moveToFirst();
                String string = cursorQuery.getString(0);
                this.mAssociatedAccount = string;
                return string;
            } catch (Exception unused) {
                Log.e(DefaultActivity.TAG, "getLastSessionUserId error");
                return null;
            } finally {
                cursorQuery.close();
            }
        }

        private Uri buildUri(String str) {
            return Uri.withAppendedPath(Uri.parse("content://com.xiaomi.finddevice.provider"), str);
        }
    }

    public static class MultiSimSettingsState extends State {
        private boolean mForceSkiped;

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (!super.isAvailable(z)) {
                Log.d(DefaultActivity.TAG, " here is MultiSimSettingsState's isAvailable function ");
                return false;
            }
            if (this.mForceSkiped) {
                Log.d(DefaultActivity.TAG, "force skip MultiSimSettingsState");
                return false;
            }
            Utils.sendBroadcastAsUser(this.context, new Intent("com.miui.action.SIM_DETECTION"));
            List subscriptionInfoList = SubscriptionManager.getDefault().getSubscriptionInfoList();
            boolean zIsSingleSim = Utils.isSingleSim();
            int activeSimCount = SimInfoUtils.getActiveSimCount(this.context, true);
            Log.d(DefaultActivity.TAG, String.format("MultiSimSettingsState  isSingleSim: %s, ActiveSimCount: %d, SimListSize: %d", Boolean.valueOf(zIsSingleSim), Integer.valueOf(activeSimCount), Integer.valueOf(subscriptionInfoList != null ? subscriptionInfoList.size() : 0)));
            return !zIsSingleSim && activeSimCount > 1 && subscriptionInfoList != null && subscriptionInfoList.size() > 1;
        }

        public void setForceSkiped(boolean z) {
            this.mForceSkiped = z;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return "double_sim_card";
        }
    }

    public static class EnterpriseActivationState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (super.isAvailable(z)) {
                return EnterpriseManagerStub.ENTERPRISE_ACTIVATED;
            }
            return false;
        }
    }

    public static class DataUsageState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return super.isAvailable(z) && !Utils.isTabletDevice() && !Build.IS_INTERNATIONAL_BUILD && TelephonyManager.getDefault().getSimState() == 5;
        }
    }

    public static class CMTermsState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return Utils.isCmTest() || Utils.isCmVersion();
        }
    }

    public static class CUTermsState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return Utils.isCUVersion() || Utils.isCTVersion();
        }
    }

    public static class NavigationModePickerState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return super.isAvailable(z) && !Utils.isTabletDevice() && !Utils.getHuanjiSettingsStatus(this.context) && NotchAdapterUtils.checkDeviceHasNavigationBar() && this.context.getPackageManager().resolveActivity(Utils.getGestureIntent(this.context), 0) != null && Utils.navigationPickerDisplayable();
        }
    }

    public static class RecentTaskStyleState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return (Utils.isTabletDevice() || DeviceUtils.isMiuiLiteV2()) ? false : true;
        }
    }

    public static class GestureTutorialState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (!super.isAvailable(z) || Build.IS_INTERNATIONAL_BUILD) {
                return false;
            }
            return Utils.isTabletDevice();
        }
    }

    public static class BootVideoState extends State {
        private boolean mForceSkiped = false;
        private boolean mIsFullScreen = false;

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (super.isAvailable(z) && !Build.IS_INTERNATIONAL_BUILD) {
                boolean z2 = this.context.getResources().getBoolean(com.android.provision.R.bool.support_boot_video);
                Log.i(DefaultActivity.TAG, "isBootVideoExist=" + z2 + ",mForceSkiped=" + this.mForceSkiped);
                if (z2 && !this.mForceSkiped) {
                    return true;
                }
            }
            return false;
        }

        public void setForceSkiped(boolean z) {
            this.mForceSkiped = z;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public void onEnter(boolean z, boolean z2) {
            super.onEnter(z, z2);
            this.mIsFullScreen = DefaultPreferenceHelper.isPrefFullscreen();
            Log.i(DefaultActivity.TAG, "onEnter mIsFullScreen=" + this.mIsFullScreen);
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public void onLeave() {
            super.onLeave();
        }
    }

    public static class RecommendedState extends State {
        public static final String AURA_PACKAGE_NAME = "com.aura.oobe.xiaomi";
        private static final String AURA_PRIMARY_ACTIVITY_NAME = "com.ironsource.appmanager.ui.activities.XiaomiPrimaryWizardActivity";
        private static final int AURA_PRIMARY_FLOW_REQUEST_CODE = 3510;
        public static final String CLOUD_CONTROL_URL = "https://api.setting.intl.miui.com/setting/v1/config?";
        private static final String MI_APPS_ACTION = "com.xiaomi.market.FirstRecommend";
        private static final String MI_APPS_CODE = "aHR0cHM6Ly9hcHAubWFya2V0LnhpYW9taS5jb20vYXBtL3JlZGlyZWN0P3VybEtleT1lc3NlbnRpYWxfY2Ru";
        private static final String MI_APPS_CONTENT = Base64Utils.getContents(MI_APPS_CODE);
        private static final String MI_APPS_REF = "provision";
        private static final int MI_APPS_REQUEST_CODE = 3511;
        private static final int OPERATION_NONE = 0;
        public static final int OPERATION_START_IRONSOURCE = 1;
        private static final int OPERATION_START_MI_APPS = 2;
        public static final int REQUEST_EXCEPTION = -1;
        private Intent mMiAppsIntent;
        private int mOperation = -1;

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (super.isAvailable(z) && Utils.isGlobalBuild()) {
                this.mOperation = DefaultPreferenceHelper.getRecommendedOperation();
                Log.d(DefaultActivity.TAG, "get recommended operation:" + this.mOperation);
                int i = this.mOperation;
                if (-1 != i && i != 0 && NetworkUtils.isCaptivePortalValidated(this.context)) {
                    int i2 = this.mOperation;
                    if (i2 == 1) {
                        boolean zIronsourceAvailable = ironsourceAvailable(this.context);
                        if (!zIronsourceAvailable) {
                            Log.d(DefaultActivity.TAG, "ironsource is not available");
                        }
                        return zIronsourceAvailable;
                    }
                    if (i2 == 2) {
                        boolean zMiAppsAvailable = miAppsAvailable(this.context);
                        if (!zMiAppsAvailable) {
                            Log.d(DefaultActivity.TAG, "mi apps is not available");
                        }
                        return zMiAppsAvailable;
                    }
                    Log.d(DefaultActivity.TAG, "other operation:" + this.mOperation);
                }
            }
            return false;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void display(Context context) {
            int i = this.mOperation;
            if (i == 1) {
                AuraMediator.getInstance().startPrimaryFlowActivityForResult((DefaultActivity) context);
                return;
            }
            if (i == 2) {
                ((DefaultActivity) context).startActivityForResult(this.mMiAppsIntent, 3511);
                return;
            }
            Log.d(DefaultActivity.TAG, "display operation is :" + this.mOperation);
        }

        private boolean ironsourceAvailable(Context context) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(AURA_PACKAGE_NAME, AURA_PRIMARY_ACTIVITY_NAME));
            return Utils.intentAvailable(context, intent);
        }

        private boolean miAppsAvailable(Context context) {
            if (this.mMiAppsIntent == null) {
                Intent intent = new Intent("com.xiaomi.market.FirstRecommend");
                this.mMiAppsIntent = intent;
                intent.putExtra("url", MI_APPS_CONTENT);
                this.mMiAppsIntent.putExtra("ref", "provision");
            }
            return Utils.intentAvailable(context, this.mMiAppsIntent);
        }
    }

    public static class TermsState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return "terms";
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return Build.IS_INTERNATIONAL_BUILD;
        }
    }

    public static class TermsAndStatementState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return "terms";
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return !Build.IS_INTERNATIONAL_BUILD;
        }
    }

    public static class PrivacyState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return false;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return OtherSettingsFragment.PRIVACY;
        }
    }

    public static class FontSizeState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return !Build.IS_INTERNATIONAL_BUILD;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public void onLeave() {
            super.onLeave();
            FontSizeUtils.sendUiModeChangeMessage(this.context);
        }
    }

    public static class PrivacyCenterState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            boolean z2;
            try {
                Bundle bundleCall = this.context.getContentResolver().call(Uri.parse("content://com.miui.permcenter.privacycenter"), "queryShowCTA", (String) null, (Bundle) null);
                z2 = bundleCall != null ? bundleCall.getBoolean("show", false) : false;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(DefaultActivity.TAG, " PrivacyCenterState + show is " + z2);
            return !Build.IS_INTERNATIONAL_BUILD && z2;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return "privacy_center";
        }
    }

    public static class LocalePickerState extends State {
        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            String[] custVariants;
            boolean z2 = Build.IS_GLOBAL_BUILD;
            if (!z2) {
                return false;
            }
            try {
                custVariants = MiuiInit.getCustVariants();
            } catch (Exception unused) {
                custVariants = null;
            }
            return z2 && !MccHelper.getInstance().isTaiwanLocale() && custVariants != null && custVariants.length > 0;
        }
    }

    public static class VoiceAssistState extends State {
        private static final String SOUND_TRIGGER_SUPPORT = "sound_trigger_support";
        private static final int STATE_LOW_POWER_SUPPORT = 1;
        private static final int STATE_NOT_SUPPORT = -1;
        private static final String VOICE_ASSIST_COMPLETED = "voice_assist_completed";

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            boolean zIsAvailable = super.isAvailable(z);
            if (!zIsAvailable) {
                return zIsAvailable;
            }
            if (Settings.Global.getInt(this.context.getContentResolver(), "huanji_restored_navigationbar", 0) != 0) {
                return false;
            }
            return (isPkgInstalled("com.miui.voicetrigger") && isSupported() && !isVoiceAssistCompleted() && isVoiceAssistAgreed()) || isLabWakeupGuideEnable();
        }

        private boolean isVoiceAssistAgreed() {
            boolean z = true;
            try {
                Bundle bundleCall = this.context.getContentResolver().call(Uri.parse("content://com.miui.permcenter.privacycenter"), "queryCTAStatus", "com.miui.voiceassist", (Bundle) null);
                if (bundleCall == null) {
                    Log.d(DefaultActivity.TAG, "isVoiceAssistAgreed: bundle is null");
                } else {
                    z = bundleCall.getBoolean("is_agreed");
                }
            } catch (Exception e) {
                Log.e(DefaultActivity.TAG, "isVoiceAssistAgreed fail " + e.getMessage());
            }
            Log.d(DefaultActivity.TAG, "isVoiceAssistAgreed: " + z);
            return z;
        }

        private boolean isLabWakeupGuideEnable() {
            if (isVoiceAssistCompleted() || VoiceWakeupUtils.getApSupportStatus(this.context) != 1) {
                return false;
            }
            Intent intent = new Intent();
            intent.setClassName("com.miui.voiceassist", "com.xiaomi.voiceassistant.BootupLabVoiceTriggerGuideActivity");
            return intent.resolveActivity(this.context.getPackageManager()) != null;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        protected Intent getIntent() {
            Intent intent = new Intent();
            if (isPkgInstalled("com.miui.voicetrigger") && isSupported() && !isVoiceAssistCompleted()) {
                intent.setClassName("com.miui.voicetrigger", "com.miui.voicetrigger.SetupTrainingActivity");
                return intent;
            }
            if (isLabWakeupGuideEnable()) {
                intent.setClassName("com.miui.voiceassist", "com.xiaomi.voiceassistant.BootupLabVoiceTriggerGuideActivity");
            }
            return intent;
        }

        private boolean isPkgInstalled(String str) {
            if (str != null && !"".equals(str)) {
                try {
                    if (this.context.getPackageManager().getApplicationInfo(str, 0) != null) {
                        return true;
                    }
                } catch (Exception unused) {
                }
            }
            return false;
        }

        private boolean isVoiceAssistCompleted() {
            return Settings.Secure.getInt(this.context.getContentResolver(), VOICE_ASSIST_COMPLETED, 0) != 0;
        }

        private boolean isSupported() {
            return getVoiceTriggerType() > -1;
        }

        private int getVoiceTriggerType() {
            int i = Settings.Secure.getInt(this.context.getContentResolver(), SOUND_TRIGGER_SUPPORT, -1000);
            Log.v(DefaultActivity.TAG, "voice trigger prop: " + i);
            if (i != -1000) {
                return i;
            }
            return 1;
        }

        @Override // com.android.provision.activities.DefaultActivity.State
        public String getPageTag() {
            return "voice_assist";
        }
    }

    public static class AIButtonState extends State {
        private static final String PERSEUS = "perseus";

        @Override // com.android.provision.activities.DefaultActivity.State
        public boolean isAvailable(boolean z) {
            return !Build.IS_GLOBAL_BUILD && FeatureParser.getBoolean("support_ai_task", false);
        }
    }

    public static class StateMachine {
        private State mAccountState;
        private BootVideoState mBootVideoState;
        private CloudServiceState mCloudServiceState;
        private Context mContext;
        private State mCurrentState;
        private FindDeviceState mFindDeviceState;
        private State mMultiSimSettingsState;
        private ArrayList<State> mStateStack;
        private SparseArray<StateInfo> mStates;

        public class StateInfo {
            private State current;
            private State next;

            public StateInfo(StateMachine stateMachine, State state) {
                this.current = state;
            }

            public State getNext() {
                return this.next;
            }
        }

        public StateMachine(Context context) {
            this.mContext = context;
            init();
            PreLoadManager.get().setCompleteDefaultActivityLoad();
        }

        public void start(boolean z) {
            restoreState();
            int size = this.mStateStack.size() - 1;
            if (z) {
                this.mCurrentState.onEnter(size >= 0 ? this.mStateStack.get(size).canBackTo() : false, true);
                saveState();
            }
        }

        public void onResult(int i, Intent intent) {
            if (this.mCurrentState == this.mAccountState) {
                this.mCloudServiceState.onLoginResult(i, intent);
                this.mFindDeviceState.onLoginResult(i, intent);
            }
        }

        public void run(int i) throws Throwable {
            Log.i(DefaultActivity.TAG, "run code: " + i);
            if (PageIntercepHelper.getInstance().isIngoreCode(i)) {
                return;
            }
            if (i == -1) {
                transitToNext();
            } else if (i == 0) {
                transitToPrevious();
            } else {
                transitToOthers();
            }
        }

        public State getCurrentState() {
            return this.mCurrentState;
        }

        public ArrayList<State> getStateStack() {
            return this.mStateStack;
        }

        private State getNextAvailableState(State state) {
            do {
                State nextState = state.getNextState();
                state = nextState == null ? getStateInfo(state).next : nextState;
                if (state == null) {
                    break;
                }
            } while (!state.isAvailable(true));
            Log.d(DefaultActivity.TAG, "getNextAvailableState is " + state);
            return state;
        }

        private State getPreviousAvailableState(ArrayList<State> arrayList) {
            State state = this.mCurrentState;
            while (true) {
                int size = arrayList.size() - 1;
                State state2 = arrayList.get(size);
                if (!state2.canBackTo()) {
                    break;
                }
                arrayList.remove(size);
                if (state2.isAvailable(false)) {
                    state = state2;
                    break;
                }
                state = state2;
            }
            if (!state.isAvailable(false)) {
                Log.w(DefaultActivity.TAG, "" + state + " can not go back, stop here");
            }
            return state;
        }

        private void transitToNext() throws Throwable {
            Log.d(DefaultActivity.TAG, "transitToNext mCurrentState is " + this.mCurrentState);
            this.mCurrentState.onLeave();
            if (needFinish()) {
                DefaultActivity defaultActivity = (DefaultActivity) this.mContext;
                if (Utils.isNewGlobalOOBE()) {
                    Utils.goToNextPage((Activity) this.mContext, defaultActivity.getIntent(), -1);
                    ((DefaultActivity) this.mContext).overridePendingTransition(com.android.provision.R.anim.slide_in_right, com.android.provision.R.anim.slide_out_left);
                    if (Utils.shouldNotFinishDefaultActivity()) {
                        return;
                    }
                    defaultActivity.finish();
                    return;
                }
                defaultActivity.finishSetup(true);
                clearState();
                return;
            }
            State state = this.mCurrentState;
            if (state instanceof ActivationState) {
                ((ActivationState) state).setSuccess();
            }
            this.mStateStack.add(this.mCurrentState);
            State state2 = this.mCurrentState;
            State nextAvailableState = getNextAvailableState(state2);
            this.mCurrentState = nextAvailableState;
            if (nextAvailableState instanceof RecommendedState) {
                ((RecommendedState) nextAvailableState).display(this.mContext);
            } else if (nextAvailableState instanceof LanguageState) {
                Log.d(DefaultActivity.TAG, "transitToNext: LanguageState");
                Utils.checkAndActivateEsimAfterFactoryReset(this.mContext);
            } else {
                nextAvailableState.onEnter(state2.canBackTo(), true);
                ((DefaultActivity) this.mContext).overridePendingTransition(com.android.provision.R.anim.slide_in_right, com.android.provision.R.anim.slide_out_left);
            }
            saveState();
        }

        private boolean needFinish() {
            return getNextAvailableState(this.mCurrentState) == null;
        }

        private void transitToPrevious() {
            if (this.mStateStack.size() <= 0) {
                return;
            }
            State previousAvailableState = getPreviousAvailableState(this.mStateStack);
            this.mCurrentState.onLeave();
            State state = this.mCurrentState;
            this.mCurrentState = previousAvailableState;
            if (previousAvailableState instanceof StartupState) {
                ((StartupState) previousAvailableState).setBooted(true);
            }
            int size = this.mStateStack.size() - 1;
            State state2 = this.mCurrentState;
            if (state2 instanceof RecommendedState) {
                ((RecommendedState) state2).display(this.mContext);
            } else {
                state2.onEnter(size >= 0 && this.mStateStack.get(size).canBackTo(), false);
                if (!(state instanceof LanguageState)) {
                    ((DefaultActivity) this.mContext).overridePendingTransition(com.android.provision.R.anim.slide_in_left, com.android.provision.R.anim.slide_out_right);
                }
            }
            saveState();
        }

        private void transitToOthers() {
            State nextState = this.mCurrentState.getNextState();
            if (nextState == null) {
                return;
            }
            this.mStateStack.add(this.mCurrentState);
            this.mCurrentState = nextState;
            int size = this.mStateStack.size() - 1;
            this.mCurrentState.onEnter(size >= 0 && this.mStateStack.get(size).canBackTo(), true);
            saveState();
        }

        private void init() {
            State state;
            State state2;
            State state3;
            State state4;
            this.mStates = new SparseArray<>();
            this.mStateStack = new ArrayList<>();
            State stateCreate = State.create("StartupState");
            State targetClass = State.create("LanguageState").setTargetClass(LanguagePickerActivity.class);
            State targetClass2 = State.create("ZonePickerState").setTargetClass(ZonePickerActivity.class);
            State targetClass3 = State.create("InputMethodState").setTargetClass(InputMethodActivity.class);
            State className = State.create("WifiState").setPackageName("com.android.settings").setClassName("com.android.settings.wifi.WifiProvisionSettingsActivity");
            State targetClass4 = State.create("FontSizeState").setTargetClass(FontSizeActivity.class);
            State targetClass5 = State.create("SimCardDetectionState").setTargetClass(SimCardDetectionActivity.class);
            State targetClass6 = State.create("MultiSimSettingsState").setTargetClass(MultiSimSettingsActivity.class);
            State className2 = State.create("EnterpriseActivationState").setPackageName("com.xiaomi.ep").setClassName("com.xiaomi.ep.provision.MiEpActivationProvisionActivity");
            State className3 = State.create("ActivationState").setPackageName(Utils.PACKAGE_NAME_UPDATER).setClassName("com.android.updater.ActivationActivity");
            State className4 = State.create("ActivateDeviceState").setPackageName("com.miui.cloudservice").setClassName("com.miui.cloudservice.ui.ActivateDeviceActivity");
            State stateCreate2 = State.create("AccountState");
            State className5 = State.create("CloudServiceState").setPackageName("com.miui.cloudservice").setClassName("com.miui.cloudservice.ui.ProvisionSettingActivity");
            State className6 = State.create("FindDeviceState").setPackageName("com.miui.cloudservice").setClassName("com.miui.cloudservice.ui.FindDeviceSettingsActivity");
            State className7 = State.create("CloudBackupState").setPackageName("com.miui.cloudbackup").setClassName("com.miui.cloudbackup.ui.ProvisionRestoreActivity");
            State targetClass7 = State.create("GoogleAccountState").setTargetClass(GoogleAccountActivity.class);
            State targetClass8 = State.create("FingerprintState").setTargetClass(FingerprintActivity.class);
            State targetClass9 = State.create("OtherState").setTargetClass(OtherSettingsActivity.class);
            State targetClass10 = State.create("TermsState").setTargetClass(TermsActivity.class);
            State targetClass11 = State.create("TermsAndStatementState").setTargetClass(TermsAndStatementActivity.class);
            State stateCreate3 = State.create("PrivacyState");
            if (stateCreate3 != null) {
                stateCreate3.setTargetClass(PrivacyProvisionActivity.class);
            }
            State className8 = State.create("PrivacyCenterState").setPackageName("com.miui.securitycenter").setClassName("com.miui.permcenter.provision.PrivacyExtraProvisionActivity");
            State targetClass12 = State.create("CMTermsState").setTargetClass(CMTermsActivity.class);
            State targetClass13 = State.create("CUTermsState").setTargetClass(CUTermsActivity.class);
            State targetClass14 = State.create("LocalePickerState").setTargetClass(LocalePickerActivity.class);
            State className9 = State.create("ThemePickerState").setPackageName("com.android.thememanager").setClassName("com.android.thememanager.activity.ThemeProvisionActivity");
            State stateCreate4 = State.create("VoiceAssistState");
            State targetClass15 = State.create("NavigationModePickerState").setTargetClass(NavigationModePickerActivity.class);
            State targetClass16 = State.create("RecentTaskStyleState").setTargetClass(RecentTaskStyleActivity.class);
            State targetClass17 = State.create("KindTipState").setTargetClass(KindTipActivity.class);
            State className10 = State.create("GestureTutorialState").setPackageName(DefaultHomeUtils.MIUI_HOME_PACKAGE_NAME).setClassName("com.miui.home.recents.settings.GestureTutorialProvisionActivity");
            State targetClass18 = State.create("AIButtonState").setTargetClass(AIButtonActivity.class);
            State targetClass19 = State.create("RecommendedState").setTargetClass(DefaultActivity.class);
            State targetClass20 = State.create("BootVideoState").setTargetClass(BootVideoActivity.class);
            State className11 = State.create("MiMoverState").setPackageName(DefaultActivity.HUANJI_PACKAGE_NAME).setClassName("com.miui.huanji.provision.ui.ProvisionCTAActivity");
            addState(stateCreate);
            setNextState(stateCreate, targetClass);
            addState(targetClass);
            setNextState(targetClass, targetClass14);
            addState(targetClass14);
            setNextState(targetClass14, targetClass2);
            addState(targetClass2);
            setNextState(targetClass2, targetClass3);
            addState(targetClass3);
            boolean z = EnterpriseManagerStub.ENTERPRISE_ACTIVATED;
            if (z || Utils.isNewGlobalOOBE()) {
                setNextState(targetClass3, targetClass10);
            } else {
                setNextState(targetClass3, className);
                addState(className);
                setNextState(className, targetClass4);
                addState(targetClass4);
                setNextState(targetClass4, targetClass10);
            }
            addState(targetClass10);
            setNextState(targetClass10, targetClass11);
            addState(targetClass11);
            if (z) {
                setNextState(targetClass11, className);
                addState(className);
            }
            if (Utils.supportMiMover(this.mContext)) {
                if (z) {
                    setNextState(className, targetClass5);
                    addState(targetClass5);
                    setNextState(targetClass5, className2);
                    addState(className2);
                    setNextState(className2, targetClass9);
                } else {
                    setNextState(targetClass11, targetClass9);
                }
                addState(targetClass9);
                setNextState(targetClass9, className8);
                addState(className8);
                setNextState(className8, className11);
                addState(className11);
                state = className3;
                setNextState(className11, state);
            } else {
                state = className3;
                if (z) {
                    setNextState(className, targetClass5);
                    addState(targetClass5);
                    setNextState(targetClass5, className2);
                    addState(className2);
                    setNextState(className2, targetClass9);
                    addState(targetClass9);
                    setNextState(targetClass9, className8);
                    addState(className8);
                    setNextState(className8, state);
                } else {
                    setNextState(targetClass11, state);
                }
            }
            addState(state);
            setNextState(state, className4);
            addState(className4);
            if (Utils.isNewGlobalOOBE()) {
                state2 = targetClass6;
            } else {
                state2 = targetClass6;
                setNextState(className4, state2);
                addState(state2);
                setNextState(state2, targetClass7);
                addState(targetClass7);
                setNextState(targetClass7, stateCreate2);
                addState(stateCreate2);
                setNextState(stateCreate2, className7);
                addState(className7);
                setNextState(className7, className5);
                addState(className5);
                setNextState(className5, className6);
                addState(className6);
                setNextState(className6, targetClass8);
                addState(targetClass8);
                setNextState(targetClass8, targetClass18);
                addState(targetClass18);
                setNextState(targetClass18, stateCreate4);
                addState(stateCreate4);
                setNextState(stateCreate4, className10);
                addState(className10);
                if (Utils.supportMiMover(this.mContext)) {
                    state3 = targetClass12;
                    setNextState(className10, state3);
                } else {
                    state3 = targetClass12;
                    if (z) {
                        setNextState(className10, state3);
                    } else {
                        setNextState(className10, targetClass9);
                        addState(targetClass9);
                        setNextState(targetClass9, className8);
                        addState(className8);
                        setNextState(className8, state3);
                    }
                }
                addState(state3);
                setNextState(state3, targetClass13);
                addState(targetClass13);
                setNextState(targetClass13, className9);
                addState(className9);
                setNextState(className9, targetClass15);
                addState(targetClass15);
                setNextState(targetClass15, targetClass16);
                addState(targetClass16);
                if (Utils.isFlipDevice()) {
                    setNextState(targetClass16, targetClass17);
                    addState(targetClass17);
                    state4 = targetClass19;
                    setNextState(targetClass17, state4);
                } else {
                    state4 = targetClass19;
                    setNextState(targetClass16, state4);
                }
                addState(state4);
                setNextState(state4, targetClass20);
                addState(targetClass20);
                this.mAccountState = stateCreate2;
                this.mCloudServiceState = (CloudServiceState) className5;
                this.mFindDeviceState = (FindDeviceState) className6;
                this.mBootVideoState = (BootVideoState) targetClass20;
            }
            this.mCurrentState = stateCreate;
            this.mMultiSimSettingsState = state2;
        }

        private void addState(State state) {
            state.setStateMachine(this);
            this.mStates.put(state.getClass().hashCode(), new StateInfo(this, state));
            PreLoadManager.get().addDefaultActivityClass(state);
        }

        public State getState(Class<? extends State> cls) {
            return this.mStates.get(cls.hashCode()).current;
        }

        public StateInfo getStateInfo(State state) {
            return this.mStates.get(state.getClass().hashCode());
        }

        private StateInfo getStateInfo(String str) {
            if (StartupState.class.getSimpleName().equals(str)) {
                return this.mStates.get(StartupState.class.hashCode());
            }
            if (LanguageState.class.getSimpleName().equals(str)) {
                return this.mStates.get(LanguageState.class.hashCode());
            }
            if (ZonePickerState.class.getSimpleName().equals(str)) {
                return this.mStates.get(ZonePickerState.class.hashCode());
            }
            if (WifiState.class.getSimpleName().equals(str)) {
                return this.mStates.get(WifiState.class.hashCode());
            }
            if (InputMethodState.class.getSimpleName().equals(str)) {
                return this.mStates.get(InputMethodState.class.hashCode());
            }
            if (AccountState.class.getSimpleName().equals(str)) {
                return this.mStates.get(AccountState.class.hashCode());
            }
            if (CloudServiceState.class.getSimpleName().equals(str)) {
                return this.mStates.get(CloudServiceState.class.hashCode());
            }
            if (FindDeviceState.class.getSimpleName().equals(str)) {
                return this.mStates.get(FindDeviceState.class.hashCode());
            }
            if (CloudBackupState.class.getSimpleName().equals(str)) {
                return this.mStates.get(CloudBackupState.class.hashCode());
            }
            if (GoogleAccountState.class.getSimpleName().equals(str)) {
                return this.mStates.get(GoogleAccountState.class.hashCode());
            }
            if (DataUsageState.class.getSimpleName().equals(str)) {
                return this.mStates.get(DataUsageState.class.hashCode());
            }
            if (SimCardDetectionState.class.getSimpleName().equals(str)) {
                return this.mStates.get(SimCardDetectionState.class.hashCode());
            }
            if (ActivationState.class.getSimpleName().equals(str)) {
                return this.mStates.get(ActivationState.class.hashCode());
            }
            if (ActivateDeviceState.class.getSimpleName().equals(str)) {
                return this.mStates.get(ActivateDeviceState.class.hashCode());
            }
            if (OtherState.class.getSimpleName().equals(str)) {
                return this.mStates.get(OtherState.class.hashCode());
            }
            if (MiMoverState.class.getSimpleName().equals(str)) {
                return this.mStates.get(MiMoverState.class.hashCode());
            }
            if (MultiSimSettingsState.class.getSimpleName().equals(str)) {
                return this.mStates.get(MultiSimSettingsState.class.hashCode());
            }
            if (TermsState.class.getSimpleName().equals(str)) {
                return this.mStates.get(TermsState.class.hashCode());
            }
            if (EnterpriseActivationState.class.getSimpleName().equals(str)) {
                return this.mStates.get(EnterpriseActivationState.class.hashCode());
            }
            if (LocalePickerState.class.getSimpleName().equals(str)) {
                return this.mStates.get(LocalePickerState.class.hashCode());
            }
            if (ThemePickerState.class.getSimpleName().equals(str)) {
                return this.mStates.get(ThemePickerState.class.hashCode());
            }
            if (FingerprintState.class.getSimpleName().equals(str)) {
                return this.mStates.get(FingerprintState.class.hashCode());
            }
            if (CMTermsState.class.getSimpleName().equals(str)) {
                return this.mStates.get(CMTermsState.class.hashCode());
            }
            if (RecommendedState.class.getSimpleName().equals(str)) {
                return this.mStates.get(RecommendedState.class.hashCode());
            }
            if (VoiceAssistState.class.getSimpleName().equals(str)) {
                return this.mStates.get(VoiceAssistState.class.hashCode());
            }
            if (NavigationModePickerState.class.getSimpleName().equals(str)) {
                return this.mStates.get(NavigationModePickerState.class.hashCode());
            }
            if (RecentTaskStyleState.class.getSimpleName().equals(str)) {
                return this.mStates.get(RecentTaskStyleState.class.hashCode());
            }
            if (KindTipState.class.getSimpleName().equals(str)) {
                return this.mStates.get(KindTipState.class.hashCode());
            }
            if (AIButtonState.class.getSimpleName().equals(str)) {
                return this.mStates.get(AIButtonState.class.hashCode());
            }
            if (CUTermsState.class.getSimpleName().equals(str)) {
                return this.mStates.get(CUTermsState.class.hashCode());
            }
            if (PrivacyState.class.getSimpleName().equals(str)) {
                return this.mStates.get(PrivacyState.class.hashCode());
            }
            if (BootVideoState.class.getSimpleName().equals(str)) {
                return this.mStates.get(BootVideoState.class.hashCode());
            }
            if (GestureTutorialState.class.getSimpleName().equals(str)) {
                return this.mStates.get(GestureTutorialState.class.hashCode());
            }
            if (PrivacyCenterState.class.getSimpleName().equals(str)) {
                return this.mStates.get(PrivacyCenterState.class.hashCode());
            }
            if (FontSizeState.class.getSimpleName().equals(str)) {
                return this.mStates.get(FontSizeState.class.hashCode());
            }
            if (TermsAndStatementState.class.getSimpleName().equals(str)) {
                return this.mStates.get(TermsAndStatementState.class.hashCode());
            }
            return null;
        }

        private void restoreState() {
            int i = 0;
            SharedPreferences sharedPreferences = this.mContext.getSharedPreferences(Utils.PREF_STATE, 0);
            String simpleName = StartupState.class.getSimpleName();
            this.mCurrentState = getStateInfo(simpleName).current;
            while (simpleName != null) {
                simpleName = null;
                try {
                    simpleName = sharedPreferences.getString(DefaultActivity.KEY_STATE_PREFIX + i, null);
                } catch (Exception unused) {
                }
                if (simpleName != null) {
                    if (i != 0) {
                        this.mStateStack.add(this.mCurrentState);
                    }
                    Log.w(DefaultActivity.TAG, " state is " + simpleName + " and getStateInfo(state) is " + getStateInfo(simpleName));
                    if (getStateInfo(simpleName) != null) {
                        this.mCurrentState = getStateInfo(simpleName).current;
                    }
                }
                i++;
            }
            if (this.mCurrentState.isAvailable(true)) {
                return;
            }
            this.mCurrentState = getPreviousAvailableState(this.mStateStack);
        }

        private void saveState() {
            SharedPreferences.Editor editorEdit = this.mContext.getSharedPreferences(Utils.PREF_STATE, 0).edit();
            editorEdit.clear();
            for (int i = 0; i < this.mStateStack.size(); i++) {
                Log.w(DefaultActivity.TAG, " saveState is " + this.mStateStack.get(i).getClass().getSimpleName());
                editorEdit.putString(DefaultActivity.KEY_STATE_PREFIX + i, this.mStateStack.get(i).getClass().getSimpleName());
            }
            editorEdit.putString(DefaultActivity.KEY_STATE_PREFIX + this.mStateStack.size(), this.mCurrentState.getClass().getSimpleName());
            editorEdit.apply();
        }

        private void clearState() {
            this.mContext.getSharedPreferences(Utils.PREF_STATE, 0).edit().clear().apply();
        }

        private void setNextState(State state, State state2) {
            this.mStates.get(state.getClass().hashCode()).next = state2;
        }

        public void setMultiSimSettingsSkiped(boolean z) {
            ((MultiSimSettingsState) this.mMultiSimSettingsState).setForceSkiped(z);
        }

        public void setBootVideoSkiped(boolean z) {
            BootVideoState bootVideoState = this.mBootVideoState;
            if (bootVideoState != null) {
                bootVideoState.setForceSkiped(z);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void resumeState() {
            ((DefaultActivity) this.mContext).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            State state = this.mCurrentState;
            if (state != null && state.isAvailable(true)) {
                this.mCurrentState.onEnter(true, true);
                saveState();
            } else {
                transitToPrevious();
            }
        }
    }

    @Override // android.app.Activity
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return super.onTouchEvent(motionEvent);
    }
}
