package com.android.provision.global;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.provision.CloudBackupConfigHelper;
import com.android.provision.DefaultPreferenceHelper;
import com.android.provision.MiuiCustomizeUtil;
import com.android.provision.R;
import com.android.provision.SimInfoUtils;
import com.android.provision.Utils;
import com.android.provision.activities.BaseActivity;
import com.android.provision.activities.BootVideoActivity;
import com.android.provision.activities.CarouselSettingActivity;
import com.android.provision.activities.CongratulationActivity;
import com.android.provision.activities.FingerprintActivity;
import com.android.provision.activities.FontStyleActivity;
import com.android.provision.activities.HomeSettingsActivity;
import com.android.provision.activities.KindTipActivity;
import com.android.provision.activities.LocationInformationActivity;
import com.android.provision.activities.NavigationModePickerActivity;
import com.android.provision.activities.OtherSettingsActivity;
import com.android.provision.activities.RecentTaskStyleActivity;
import com.android.provision.fragment.BootVideoFragment;
import com.android.provision.fragment.FontStyleFragment;
import com.android.provision.manager.PreLoadManager;
import com.android.provision.receiver.NetWorkChangedReceiver;
import com.android.provision.state.ParentalControlState;
import com.android.provision.utils.CarouselUtils;
import com.android.provision.utils.DefaultHomeUtils;
import com.android.provision.utils.FontStyleUtils;
import com.android.provision.utils.MccHelper;
import com.android.provision.utils.MiAppsHelper;
import com.android.provision.utils.NetworkUtils;
import com.android.provision.utils.RemoveSiprogoManager;
import java.util.ArrayList;
import miui.accounts.ExtraAccountManager;
import miui.os.Build;
import miuix.appcompat.app.AppCompatActivity;
import miuix.device.DeviceUtils;

/* JADX INFO: loaded from: classes.dex */
public class GlobalDefaultActivity extends AppCompatActivity {
    private static final String AUTHORITY_FIND_DEVICE = "com.xiaomi.finddevice.provider";
    private static final String KEY_STATE_PREFIX = "com.android.provision.STATE_";
    private static final String PREF_STATE_GLOBAL = "pref_state_global";
    private static final String STATE_ENTER_CURRENTSTATE = "com.android.provision:state_enter_currentstate";
    private static final String TAG = "GlobalDefaultActivity";
    private static int sUserProvisioningState;
    private boolean mIsNetworkRegistered = false;
    private NetWorkChangedReceiver mNetorkReceiver;
    private StateMachine mStateMachine;

    public static class FingerprintState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            return false;
        }
    }

    public static class KindTipState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            return true;
        }
    }

    public static class TermsState extends State {
    }

    public static class ThemePickerState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // miuix.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mStateMachine = new StateMachine(this);
        boolean z = true;
        if (bundle != null && !bundle.getBoolean(STATE_ENTER_CURRENTSTATE, true)) {
            z = false;
        }
        this.mStateMachine.start(z);
        registerNetworkChangedReceiver();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStart() {
        super.onStart();
        sUserProvisioningState = ((DevicePolicyManager) getSystemService(DevicePolicyManager.class)).getUserProvisioningState();
        Log.i(TAG, "GlobalDefaultActivity getUserProvisioningState=" + sUserProvisioningState);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // miuix.appcompat.app.AppCompatActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(STATE_ENTER_CURRENTSTATE, this.mStateMachine.mCurrentState instanceof AccountState);
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

    @Override // miuix.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        unRegisterNetworkChangedReceiver();
    }

    public void run(int i) {
        this.mStateMachine.run(i);
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        Log.d(TAG, "onActivityResult requestCode=" + i + ", resultCode=" + i2);
        this.mStateMachine.onResult(i2, intent);
        if (intent != null) {
            this.mStateMachine.setBootVideoSkiped(intent.getBooleanExtra(BootVideoFragment.EXTRA_BOOTVIDEO_FORCE_SKIPED, false));
        }
        this.mStateMachine.run(i2);
    }

    public static class State {
        public static final String PREFIX = "com.android.provision.global.GlobalDefaultActivity$";
        public String className;
        protected Context context;
        protected Handler mHandler = new Handler();
        protected StateMachine mStateMachine;
        protected String packageName;
        public Class<?> targetClass;

        protected boolean canBackTo() {
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

        public void onEnter(boolean z, boolean z2) {
            Activity activity = (Activity) this.context;
            Log.d(GlobalDefaultActivity.TAG, "targetClass is " + this.targetClass);
            Intent intent = getIntent();
            intent.putExtra(BaseActivity.EXTRA_DISABLE_BACK, z ^ true);
            intent.putExtra(BaseActivity.EXTRA_TO_NEXT, z2);
            activity.startActivityForResult(intent, 0);
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
    }

    public static class AccountState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
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
            bundle.putString("androidPackageName", this.context.getPackageName());
            intent.putExtras(bundle);
            return intent;
        }

        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            return super.isAvailable(z) && !Build.IS_INTERNATIONAL_BUILD && !Utils.isInProvisionState(this.context) && (Build.IS_PRIVATE_BUILD || NetworkUtils.isCaptivePortalValidated(this.context)) && ExtraAccountManager.getXiaomiAccount(this.context) == null;
        }

        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public void onEnter(boolean z, boolean z2) {
            super.onEnter(z, z2);
            Settings.System.putInt(this.context.getContentResolver(), CloudBackupConfigHelper.KEY_IS_BACKUP_EXIST, 1);
        }
    }

    public static class CloudServiceState extends State {
        private static final String INTENT_KEY_CAN_INSTALL = "extra_can_install";
        private static final String INTENT_KEY_VERSION = "extra_master_key_version";
        private long mMasterKeyVersion = -1;
        private boolean mCanInstall = false;

        @Override // com.android.provision.global.GlobalDefaultActivity.State
        protected Intent getIntent() {
            Intent intent = super.getIntent();
            intent.putExtra(INTENT_KEY_VERSION, this.mMasterKeyVersion);
            intent.putExtra(INTENT_KEY_CAN_INSTALL, this.mCanInstall);
            return intent;
        }

        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            return (Build.IS_INTERNATIONAL_BUILD || !super.isAvailable(z) || Utils.isInProvisionState(this.context) || ExtraAccountManager.getXiaomiAccount(this.context) == null || Settings.Global.getInt(this.context.getContentResolver(), "provision_cloudService_enabled", 0) != 0 || Utils.isRedmiDigitOrNoteSeries()) ? false : true;
        }

        public void onLoginResult(int i, Intent intent) {
            if (intent != null) {
                this.mMasterKeyVersion = intent.getLongExtra(INTENT_KEY_VERSION, -1L);
                this.mCanInstall = intent.getBooleanExtra(INTENT_KEY_CAN_INSTALL, false);
            }
        }
    }

    public static class FindDeviceState extends State {
        private boolean mPasswordLogin = true;

        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            return !Build.IS_INTERNATIONAL_BUILD && super.isAvailable(z) && !Utils.isInProvisionState(this.context) && ExtraAccountManager.getXiaomiAccount(this.context) != null && TextUtils.isEmpty(getLastSessionUserId(this.context)) && this.mPasswordLogin && Settings.Global.getInt(this.context.getContentResolver(), "provision_findDevice_enabled", 0) == 0 && Utils.isRedmiDigitOrNoteSeries();
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
                Log.e(GlobalDefaultActivity.TAG, "getLastSessionUserId error");
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
    }

    public static class CloudBackupState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            return !Build.IS_INTERNATIONAL_BUILD && super.isAvailable(z) && !Utils.isInProvisionState(this.context) && Utils.isNetworkAvailable(this.context) && isAccountExist() && Settings.System.getInt(this.context.getContentResolver(), CloudBackupConfigHelper.KEY_IS_BACKUP_EXIST, 1) == 1;
        }

        public boolean isAccountExist() {
            return ExtraAccountManager.getXiaomiAccount(this.context) != null;
        }
    }

    public static class ActivateDeviceState extends State {
        private static final String ASSOCIATED_ACCOUNT = "associated_account";
        private String mAssociatedAccount;

        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            return (!super.isAvailable(z) || Build.IS_INTERNATIONAL_BUILD || isNetworkAvailable() || Utils.isInProvisionState(this.context) || TextUtils.isEmpty(getLastSessionUserId(this.context))) ? false : true;
        }

        @Override // com.android.provision.global.GlobalDefaultActivity.State
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
                Log.e(GlobalDefaultActivity.TAG, "getLastSessionUserId error");
                return null;
            } finally {
                cursorQuery.close();
            }
        }

        private Uri buildUri(String str) {
            return Uri.withAppendedPath(Uri.parse("content://com.xiaomi.finddevice.provider"), str);
        }

        private boolean isSimCardDetected() {
            return SimInfoUtils.getActiveSimCount(this.context, true) > 0;
        }
    }

    public static class HomeSettingsState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (!super.isAvailable(z) || !Build.IS_INTERNATIONAL_BUILD || MiuiCustomizeUtil.disabledHomeLayoutOptional() || Utils.isTabletDevice()) {
                return false;
            }
            return !Utils.isInProvisionState(this.context);
        }
    }

    public static class NavigationModePickerState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            return (MiuiCustomizeUtil.isNeedRemoveSystemNavigationMode() || Utils.isInProvisionState(this.context) || Utils.isTabletDevice()) ? false : true;
        }
    }

    public static class RecentTaskStyleState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            return (Utils.isTabletDevice() || DeviceUtils.isMiuiLiteV2()) ? false : true;
        }
    }

    public static class OtherState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            return !Utils.isInProvisionState(this.context);
        }
    }

    public static class FontState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            Log.i(GlobalDefaultActivity.TAG, " here is FontState isAvailable func ");
            return (Utils.isAlreadyEnterFontStyleFromGms(this.context) || !Utils.isMiSansSupportLanguages() || FontStyleFragment.getFontList(this.context).size() != 2 || Utils.isFoldDevice() || Utils.isInProvisionState(this.context)) ? false : true;
        }
    }

    public static class CarouselState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            return !Utils.isInProvisionState(this.context) && CarouselUtils.shouldShowNewCarousel(this.context);
        }
    }

    public static class LocationInState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            String locationPermissionsSetPackage = MiuiCustomizeUtil.getLocationPermissionsSetPackage();
            return !TextUtils.isEmpty(locationPermissionsSetPackage) && Utils.applicationInstalled(this.context, locationPermissionsSetPackage);
        }
    }

    public static class CongratulationState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public void onLeave() {
            super.onLeave();
            if (!Utils.isAlreadyEnterFontStyleFromGms(this.context)) {
                if (Build.IS_INTERNATIONAL_BUILD && "10".equalsIgnoreCase(FontStyleUtils.getLocalFontId(this.context))) {
                    FontStyleUtils.applyFont(this.context, "10");
                } else {
                    FontStyleUtils.applyFont(this.context, FontStyleUtils.MISANS_FONT_ID);
                }
            }
            if (Utils.isTabletDevice()) {
                Utils.setNavigationBarFullScreen(this.context, true);
            } else {
                Utils.setNavigationBarFullScreen(this.context, DefaultPreferenceHelper.isPrefFullscreen());
            }
            Context context = this.context;
            if (context == null || Utils.isGestureLineShow(context)) {
                return;
            }
            Utils.hideGestureLine(this.context, false);
        }
    }

    public static class GestureTutorialState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (super.isAvailable(z)) {
                return Utils.isTabletDevice();
            }
            return false;
        }
    }

    public static class BootVideoState extends State {
        private boolean mForceSkiped = false;
        private boolean mIsFullScreen = false;

        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (super.isAvailable(z) && !Build.IS_INTERNATIONAL_BUILD) {
                boolean z2 = this.context.getResources().getBoolean(R.bool.support_boot_video);
                Log.i(GlobalDefaultActivity.TAG, "isBootVideoExist=" + z2 + ",mForceSkiped=" + this.mForceSkiped);
                if (z2 && !this.mForceSkiped) {
                    return true;
                }
            }
            return false;
        }

        public void setForceSkiped(boolean z) {
            this.mForceSkiped = z;
        }

        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public void onEnter(boolean z, boolean z2) {
            super.onEnter(z, z2);
            this.mIsFullScreen = DefaultPreferenceHelper.isPrefFullscreen();
            Log.i(GlobalDefaultActivity.TAG, "onEnter mIsFullScreen=" + this.mIsFullScreen);
        }

        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public void onLeave() {
            super.onLeave();
        }
    }

    public static class IndusAppsState extends State {
        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            if (!super.isAvailable(z) || MiAppsHelper.shouldSkipMiApps(this.context)) {
                return false;
            }
            boolean zIsIndusAppsStateAvailable = MiAppsHelper.isIndusAppsStateAvailable(this.context);
            Log.i(GlobalDefaultActivity.TAG, "IndusAppsState isAvailable: " + zIsIndusAppsStateAvailable);
            return zIsIndusAppsStateAvailable;
        }
    }

    public static class RecommendedState extends State {
        private static final int OPERATION_START_MI_APPS = 2;

        @Override // com.android.provision.global.GlobalDefaultActivity.State
        public boolean isAvailable(boolean z) {
            boolean z2 = false;
            if (super.isAvailable(z) && !MiAppsHelper.shouldSkipMiApps(this.context)) {
                int recommendedOperation = DefaultPreferenceHelper.getRecommendedOperation();
                boolean zIsCaptivePortalValidated = NetworkUtils.isCaptivePortalValidated(this.context);
                boolean zIsMiAppsIntentAvailable = MiAppsHelper.isMiAppsIntentAvailable(this.context);
                boolean zShouldShowIndusApps = MiAppsHelper.shouldShowIndusApps(this.context);
                if (recommendedOperation == 2 && zIsCaptivePortalValidated && zIsMiAppsIntentAvailable && !zShouldShowIndusApps) {
                    z2 = true;
                }
                Log.i(GlobalDefaultActivity.TAG, "RecommendedState isAvailable: " + z2 + ", operation=" + recommendedOperation + ", isCaptivePortalValidated=" + zIsCaptivePortalValidated + ", isMiAppsIntentAvailable=" + zIsMiAppsIntentAvailable + ", shouldShowIndusApps=" + zShouldShowIndusApps);
            }
            return z2;
        }
    }

    public static class StateMachine {
        private State mAccountState;
        private BootVideoState mBootVideoState;
        private CloudServiceState mCloudServiceState;
        private State mCompleteState;
        private Context mContext;
        private State mCurrentState;
        private FindDeviceState mFindDeviceState;
        private ArrayList<State> mStateStack;
        private SparseArray<StateInfo> mStates;

        private class StateInfo {
            private State current;
            private State next;

            public StateInfo(StateMachine stateMachine, State state) {
                this.current = state;
            }
        }

        public StateMachine(Context context) {
            this.mContext = context;
            init();
        }

        public void start(boolean z) {
            restoreState();
            int size = this.mStateStack.size() - 1;
            if (z) {
                State state = this.mCurrentState;
                if ((state instanceof RecommendedState) || (state instanceof IndusAppsState)) {
                    MiAppsHelper.display((GlobalDefaultActivity) this.mContext, MiAppsHelper.PRE_PAGE);
                } else {
                    state.onEnter(size >= 0 ? this.mStateStack.get(size).canBackTo() : true, true);
                }
                saveState();
            }
        }

        public void onResult(int i, Intent intent) {
            if (this.mCurrentState == this.mAccountState) {
                this.mCloudServiceState.onLoginResult(i, intent);
                this.mFindDeviceState.onLoginResult(i, intent);
            }
        }

        public void run(int i) {
            if (i == -1) {
                transitToNext();
            } else if (i == 0) {
                transitToPrevious();
            } else {
                transitToOthers();
            }
        }

        private State getNextAvailableState(State state) {
            do {
                State nextState = state.getNextState();
                state = nextState == null ? getStateInfo(state).next : nextState;
            } while (!state.isAvailable(true));
            Log.d(GlobalDefaultActivity.TAG, "getNextAvailableState is " + state);
            return state;
        }

        private State getPreviousAvailableState(ArrayList<State> arrayList) {
            State state = this.mCurrentState;
            if (arrayList.size() > 0) {
                while (true) {
                    int size = arrayList.size() - 1;
                    if (size < 0) {
                        return null;
                    }
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
            }
            if (!state.isAvailable(false)) {
                Log.w(GlobalDefaultActivity.TAG, "" + state + " can not go back, stop here");
            }
            return state;
        }

        private void transitToNext() {
            Log.d(GlobalDefaultActivity.TAG, "transitToNext mCurrentState is " + this.mCurrentState);
            this.mCurrentState.onLeave();
            State state = this.mCurrentState;
            if (state == this.mCompleteState) {
                Activity activity = (Activity) this.mContext;
                if (MccHelper.getInstance().isSupportTrustonic(activity) || MccHelper.getInstance().isSupportCustomizedTrustonic(activity)) {
                    Utils.goToTrustonicApp((Activity) this.mContext, activity.getIntent());
                } else {
                    Utils.goToNextPage((Activity) this.mContext, activity.getIntent(), -1);
                }
                if (Utils.applicationInstalled(activity, DefaultHomeUtils.MIUI_HOME_PACKAGE_NAME) && Utils.applicationInstalled(activity, DefaultHomeUtils.POCO_LAUNCHER_PACKAGE_NAME)) {
                    Log.i(GlobalDefaultActivity.TAG, "nextStep: set miui defaultLauncher");
                    DefaultHomeUtils.setDefaultHome(activity, DefaultHomeUtils.MIUI_HOME_PACKAGE_NAME);
                }
                RemoveSiprogoManager.checkIsRemoveSiprogo((Activity) this.mContext);
                clearState(GlobalDefaultActivity.PREF_STATE_GLOBAL);
                clearState(Utils.PREF_STATE);
                activity.finish();
                return;
            }
            this.mStateStack.add(state);
            State state2 = this.mCurrentState;
            State nextAvailableState = getNextAvailableState(state2);
            this.mCurrentState = nextAvailableState;
            if ((nextAvailableState instanceof RecommendedState) || (nextAvailableState instanceof IndusAppsState)) {
                MiAppsHelper.display((GlobalDefaultActivity) this.mContext, MiAppsHelper.PRE_PAGE);
            } else {
                nextAvailableState.onEnter(state2.canBackTo(), true);
            }
            ((Activity) this.mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            saveState();
        }

        private void transitToPrevious() {
            if (this.mStateStack.size() <= 0) {
                clearState(GlobalDefaultActivity.PREF_STATE_GLOBAL);
                ((Activity) this.mContext).finish();
                return;
            }
            State previousAvailableState = getPreviousAvailableState(this.mStateStack);
            this.mCurrentState.onLeave();
            if (previousAvailableState == null) {
                clearState(GlobalDefaultActivity.PREF_STATE_GLOBAL);
                ((Activity) this.mContext).finish();
                return;
            }
            this.mCurrentState = previousAvailableState;
            int size = this.mStateStack.size() - 1;
            State state = this.mCurrentState;
            if ((state instanceof RecommendedState) || (state instanceof IndusAppsState)) {
                MiAppsHelper.display((GlobalDefaultActivity) this.mContext, "next");
            } else {
                state.onEnter(size >= 0 ? this.mStateStack.get(size).canBackTo() : true, false);
            }
            ((Activity) this.mContext).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            saveState();
        }

        private void transitToOthers() {
            State nextState = this.mCurrentState.getNextState();
            if (nextState == null || nextState == this.mCompleteState) {
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
            this.mStates = new SparseArray<>();
            this.mStateStack = new ArrayList<>();
            State stateCreate = State.create("AccountState");
            State className = State.create("CloudServiceState").setPackageName("com.miui.cloudservice").setClassName("com.miui.cloudservice.ui.ProvisionSettingActivity");
            State className2 = State.create("FindDeviceState").setPackageName("com.miui.cloudservice").setClassName("com.miui.cloudservice.ui.FindDeviceSettingsActivity");
            State className3 = State.create("CloudBackupState").setPackageName("com.miui.cloudbackup").setClassName("com.miui.cloudbackup.ui.ProvisionRestoreActivity");
            State targetClass = State.create("FingerprintState").setTargetClass(FingerprintActivity.class);
            State targetClass2 = State.create("OtherState").setTargetClass(OtherSettingsActivity.class);
            State stateCreate2 = ParentalControlState.create();
            State targetClass3 = State.create("FontState").setTargetClass(FontStyleActivity.class);
            State targetClass4 = State.create("CarouselState").setTargetClass(CarouselSettingActivity.class);
            State targetClass5 = State.create("IndusAppsState").setTargetClass(GlobalDefaultActivity.class);
            State targetClass6 = State.create("LocationInState").setTargetClass(LocationInformationActivity.class);
            State targetClass7 = State.create("CongratulationState").setTargetClass(CongratulationActivity.class);
            State targetClass8 = State.create("HomeSettingsState").setTargetClass(HomeSettingsActivity.class);
            State targetClass9 = State.create("NavigationModePickerState").setTargetClass(NavigationModePickerActivity.class);
            State targetClass10 = State.create("RecentTaskStyleState").setTargetClass(RecentTaskStyleActivity.class);
            State targetClass11 = State.create("KindTipState").setTargetClass(KindTipActivity.class);
            State className4 = State.create("ThemePickerState").setPackageName("com.android.thememanager").setClassName("com.android.thememanager.activity.ThemeProvisionActivity");
            State targetClass12 = State.create("RecommendedState").setTargetClass(GlobalDefaultActivity.class);
            State targetClass13 = State.create("BootVideoState").setTargetClass(BootVideoActivity.class);
            State className5 = State.create("GestureTutorialState").setPackageName(DefaultHomeUtils.MIUI_HOME_PACKAGE_NAME).setClassName("com.miui.home.recents.settings.GestureTutorialProvisionActivity");
            addState(stateCreate);
            setNextState(stateCreate, className);
            addState(className);
            setNextState(className, className2);
            addState(className2);
            setNextState(className2, className3);
            addState(className3);
            setNextState(className3, targetClass);
            addState(targetClass);
            setNextState(targetClass, targetClass2);
            addState(targetClass2);
            setNextState(targetClass2, stateCreate2);
            addState(stateCreate2);
            setNextState(stateCreate2, targetClass3);
            addState(targetClass3);
            setNextState(targetClass3, targetClass4);
            addState(targetClass4);
            setNextState(targetClass4, targetClass5);
            addState(targetClass5);
            setNextState(targetClass5, targetClass6);
            addState(targetClass6);
            setNextState(targetClass6, targetClass8);
            addState(targetClass8);
            setNextState(targetClass8, className4);
            addState(className4);
            setNextState(className4, targetClass12);
            addState(targetClass12);
            setNextState(targetClass12, targetClass13);
            addState(targetClass13);
            setNextState(targetClass13, className5);
            addState(className5);
            setNextState(className5, targetClass9);
            addState(targetClass9);
            setNextState(targetClass9, targetClass10);
            addState(targetClass10);
            if (Utils.isFlipDevice()) {
                setNextState(targetClass10, targetClass11);
                addState(targetClass11);
                state = targetClass7;
                setNextState(targetClass11, state);
            } else {
                state = targetClass7;
                setNextState(targetClass10, state);
            }
            addState(state);
            this.mCompleteState = state;
            this.mCurrentState = stateCreate;
            this.mAccountState = stateCreate;
            this.mCloudServiceState = (CloudServiceState) className;
            this.mFindDeviceState = (FindDeviceState) className2;
            this.mBootVideoState = (BootVideoState) targetClass13;
        }

        private void addState(State state) {
            state.setStateMachine(this);
            this.mStates.put(state.getClass().hashCode(), new StateInfo(this, state));
            PreLoadManager.get().addGlobalActivityClass(state);
        }

        public State getState(Class<? extends State> cls) {
            return this.mStates.get(cls.hashCode()).current;
        }

        private StateInfo getStateInfo(State state) {
            return this.mStates.get(state.getClass().hashCode());
        }

        private StateInfo getStateInfo(String str) {
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
            if (ActivateDeviceState.class.getSimpleName().equals(str)) {
                return this.mStates.get(ActivateDeviceState.class.hashCode());
            }
            if (OtherState.class.getSimpleName().equals(str)) {
                return this.mStates.get(OtherState.class.hashCode());
            }
            if (CarouselState.class.getSimpleName().equals(str)) {
                return this.mStates.get(CarouselState.class.hashCode());
            }
            if (IndusAppsState.class.getSimpleName().equals(str)) {
                return this.mStates.get(IndusAppsState.class.hashCode());
            }
            if (CongratulationState.class.getSimpleName().equals(str)) {
                return this.mStates.get(CongratulationState.class.hashCode());
            }
            if (TermsState.class.getSimpleName().equals(str)) {
                return this.mStates.get(TermsState.class.hashCode());
            }
            if (ThemePickerState.class.getSimpleName().equals(str)) {
                return this.mStates.get(ThemePickerState.class.hashCode());
            }
            if (RecommendedState.class.getSimpleName().equals(str)) {
                return this.mStates.get(RecommendedState.class.hashCode());
            }
            if (FingerprintState.class.getSimpleName().equals(str)) {
                return this.mStates.get(FingerprintState.class.hashCode());
            }
            if (LocationInState.class.getSimpleName().equals(str)) {
                return this.mStates.get(LocationInState.class.hashCode());
            }
            if (HomeSettingsState.class.getSimpleName().equals(str)) {
                return this.mStates.get(HomeSettingsState.class.hashCode());
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
            if (BootVideoState.class.getSimpleName().equals(str)) {
                return this.mStates.get(BootVideoState.class.hashCode());
            }
            if (GestureTutorialState.class.getSimpleName().equals(str)) {
                return this.mStates.get(GestureTutorialState.class.hashCode());
            }
            if (FontState.class.getSimpleName().equals(str)) {
                return this.mStates.get(FontState.class.hashCode());
            }
            if (ParentalControlState.class.getSimpleName().equals(str)) {
                return this.mStates.get(ParentalControlState.class.hashCode());
            }
            return null;
        }

        private void restoreState() {
            int i = 0;
            SharedPreferences sharedPreferences = this.mContext.getSharedPreferences(GlobalDefaultActivity.PREF_STATE_GLOBAL, 0);
            String simpleName = AccountState.class.getSimpleName();
            if (getStateInfo(simpleName) == null) {
                return;
            }
            this.mCurrentState = getStateInfo(simpleName).current;
            while (simpleName != null) {
                simpleName = sharedPreferences.getString(GlobalDefaultActivity.KEY_STATE_PREFIX + i, null);
                if (simpleName != null) {
                    if (i != 0) {
                        this.mStateStack.add(this.mCurrentState);
                    }
                    Log.i(GlobalDefaultActivity.TAG, " state is " + simpleName + " and getStateInfo(state) is " + getStateInfo(simpleName));
                    if (getStateInfo(simpleName) != null) {
                        this.mCurrentState = getStateInfo(simpleName).current;
                    }
                }
                i++;
            }
            if (this.mCurrentState.isAvailable(true)) {
                return;
            }
            State previousAvailableState = getPreviousAvailableState(this.mStateStack);
            State state = this.mCurrentState;
            if (state == previousAvailableState || previousAvailableState == null) {
                this.mCurrentState = getNextAvailableState(state);
            }
        }

        private void saveState() {
            SharedPreferences.Editor editorEdit = this.mContext.getSharedPreferences(GlobalDefaultActivity.PREF_STATE_GLOBAL, 0).edit();
            editorEdit.clear();
            for (int i = 0; i < this.mStateStack.size(); i++) {
                editorEdit.putString(GlobalDefaultActivity.KEY_STATE_PREFIX + i, this.mStateStack.get(i).getClass().getSimpleName());
            }
            editorEdit.putString(GlobalDefaultActivity.KEY_STATE_PREFIX + this.mStateStack.size(), this.mCurrentState.getClass().getSimpleName());
            editorEdit.apply();
        }

        private void clearState(String str) {
            this.mContext.getSharedPreferences(str, 0).edit().clear().apply();
        }

        private void setNextState(State state, State state2) {
            this.mStates.get(state.getClass().hashCode()).next = state2;
        }

        public void setBootVideoSkiped(boolean z) {
            BootVideoState bootVideoState = this.mBootVideoState;
            if (bootVideoState != null) {
                bootVideoState.setForceSkiped(z);
            }
        }
    }
}
