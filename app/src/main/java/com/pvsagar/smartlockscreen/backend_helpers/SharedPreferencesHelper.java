package com.pvsagar.smartlockscreen.backend_helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.pvsagar.smartlockscreen.baseclasses.Passphrase;

/**
 * Created by aravind on 19/9/14.
 */
public class SharedPreferencesHelper {
    private static final String LOG_TAG = SharedPreferencesHelper.class.getSimpleName();
    private static final String PACKAGE_NAME = SharedPreferencesHelper.class.getPackage().getName();
    private static final String KEY_MASTER_PASSWORD = PACKAGE_NAME + ".MASTER_PASSWORD";
    private static final String KEY_MASTER_PASSWORD_TYPE = PACKAGE_NAME + ".MASTER_PASSWORD_TYPE";

    private static SharedPreferences preferences;

    private static void initPreferences(Context context){
        if(preferences==null){
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public static String getMasterPasswordString(Context context){
        initPreferences(context);
        return preferences.getString(KEY_MASTER_PASSWORD, "");
    }

    public static String getMasterPasswordType(Context context){
        initPreferences(context);
        return preferences.getString(KEY_MASTER_PASSWORD_TYPE, Passphrase.TYPE_NONE);
    }

    public static void setMasterPassword(Context context, String passwordString, String passwordType){
        if(Utility.checkForNullAndWarn(passwordString, LOG_TAG) || Utility.checkForNullAndWarn(passwordType, LOG_TAG)){
            return;
        }
        initPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_MASTER_PASSWORD, passwordString);
        editor.putString(KEY_MASTER_PASSWORD_TYPE, passwordType);
        editor.apply();
    }
}
