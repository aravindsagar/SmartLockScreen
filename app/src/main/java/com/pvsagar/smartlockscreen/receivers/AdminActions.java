package com.pvsagar.smartlockscreen.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by aravind on 9/9/14.
 * Class extending the DeviceAdminReceiver class so that the app will have admin privileges to
 * change password and lock screen automatically
 */
public class AdminActions extends DeviceAdminReceiver {

    private static DevicePolicyManager mDPM;
    private static ComponentName mDeviceAdmin;

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        showToast(context, "Smart Lockscreen Admin Enabled");
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
        if(mDPM == null || mDeviceAdmin == null) {
            mDPM = (DevicePolicyManager) context.
                    getSystemService(context.getApplicationContext().DEVICE_POLICY_SERVICE);
            mDeviceAdmin = new ComponentName(context, AdminActions.class);
        }
        if(!isAdminEnabled()){
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Hello!");
            context.startActivity(intent);
        }
    }

    public static boolean isAdminEnabled(){
        if(mDPM != null && mDeviceAdmin != null){
            return mDPM.isAdminActive(mDeviceAdmin);
        } else {
            return false;
        }
    }

    public static void changePassword(String password){
        if(mDPM == null){
            throw new UnsupportedOperationException("Device admin not initialized. Please call" +
                    "AdminActions.initAdmin() first.");
        }
        mDPM.resetPassword(password, 0);
    }
}
