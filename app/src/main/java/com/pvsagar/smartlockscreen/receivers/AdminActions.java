package com.pvsagar.smartlockscreen.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.services.BaseService;

/**
 * Created by aravind on 9/9/14.
 * Class extending the DeviceAdminReceiver class so that the app will have admin privileges to
 * change password and lock screen automatically
 */
public class AdminActions extends DeviceAdminReceiver {
    private static final String LOG_TAG = AdminActions.class.getSimpleName();

    private static DevicePolicyManager mDPM;
    private static ComponentName mDeviceAdmin;

    private static String currentPassphraseType;
    private static String currentPassphraseString;

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static String getCurrentPassphraseType() {
        return currentPassphraseType;
    }

    public static String getCurrentPassphraseString() {
        return currentPassphraseString;
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        showToast(context, "Smart Lockscreen Admin Enabled");
        AdminActions.initializeAdminObjects(context);
        context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_DETECT_WIFI));
        context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_ADD_GEOFENCES));
        context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_DETECT_ENVIRONMENT));
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        super.onPasswordChanged(context, intent);
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        super.onPasswordSucceeded(context, intent);
    }

    public static void initAdmin(Context context){
        initializeAdminObjects(context);
        if(!isAdminEnabled()){
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    R.string.device_admin_description);
            context.startActivity(intent);
        }
    }

    public static void initializeAdminObjects(Context context){
        if(mDPM == null || mDeviceAdmin == null) {
            mDPM = (DevicePolicyManager) context.
                    getSystemService(Context.DEVICE_POLICY_SERVICE);
            mDeviceAdmin = new ComponentName(context, AdminActions.class);
        }
    }

    public static boolean isAdminEnabled(){
        if(mDPM != null && mDeviceAdmin != null){
            return mDPM.isAdminActive(mDeviceAdmin);
        } else {
            return false;
        }
    }

    public static boolean changePassword(String password, String passphraseType){
        currentPassphraseType = passphraseType;
        if(passphraseType.equals(Passphrase.TYPE_PATTERN)){
            currentPassphraseString = password;
            password = "";
        }
        if(isAdminEnabled()) {
            mDPM.resetPassword(password, 0);
            return true;
        }
        else {
            Log.e(LOG_TAG, "No admin privileges, cannot change password.");
            return false;
        }
    }
}
