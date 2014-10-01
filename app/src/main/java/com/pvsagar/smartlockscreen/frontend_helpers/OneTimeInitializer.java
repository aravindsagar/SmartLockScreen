package com.pvsagar.smartlockscreen.frontend_helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pvsagar.smartlockscreen.SetMasterPassword;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.receivers.AdminActions;

/**
 * Created by aravind on 1/10/14.
 */
public class OneTimeInitializer {
    private static final String LOG_TAG = OneTimeInitializer.class.getSimpleName();

    public static boolean initialize(Context activityContext){
        boolean isMasterPasswordSet = false;
        if(!(activityContext instanceof Activity)){
            Log.e(LOG_TAG, "Context passed should be an activity context");
            return false;
        }
        AdminActions.initAdmin(activityContext);
        if(SharedPreferencesHelper.getMasterPasswordType(activityContext) == null){
            activityContext.startActivity(new Intent(activityContext, SetMasterPassword.class));
        } else {
            isMasterPasswordSet = true;
        }
        if(!isMasterPasswordSet){
            isMasterPasswordSet = SharedPreferencesHelper.getMasterPasswordType(activityContext) != null;
        }
        return isMasterPasswordSet && AdminActions.isAdminEnabled();
    }
}
