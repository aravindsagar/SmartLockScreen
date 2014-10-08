package com.pvsagar.smartlockscreen.backend_helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Collections;
import java.util.List;

/**
 * Created by aravind on 19/9/14.
 */
public class SharedPreferencesHelper {
    private static final String LOG_TAG = SharedPreferencesHelper.class.getSimpleName();
    private static final String PACKAGE_NAME = SharedPreferencesHelper.class.getPackage().getName();
    private static final String KEY_MASTER_PASSWORD = PACKAGE_NAME + ".MASTER_PASSWORD";
    private static final String KEY_MASTER_PASSWORD_TYPE = PACKAGE_NAME + ".MASTER_PASSWORD_TYPE";
    private static final String KEY_DEVICE_OWNER_USER_ID = PACKAGE_NAME + ".DEVICE_OWNER_USER_ID";
    private static final String KEY_PREFIX_OVERLAP_CHOICE = PACKAGE_NAME + ".OVERLAP_CHOICE_AMONG";

    private static SharedPreferences preferences;

    private static void initPreferences(Context context){
        if(preferences==null){
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public static String getMasterPasswordString(Context context){
        initPreferences(context);
        return preferences.getString(KEY_MASTER_PASSWORD, null);
    }

    public static String getMasterPasswordType(Context context){
        initPreferences(context);
        return preferences.getString(KEY_MASTER_PASSWORD_TYPE, null);
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

    public static long getDeviceOwnerUserId(Context context){
        initPreferences(context);
        return preferences.getLong(KEY_DEVICE_OWNER_USER_ID, 1);
    }

    public static void setDeviceOwnerUserId(Context context, long id){
        initPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_DEVICE_OWNER_USER_ID, id);
        editor.apply();
    }

    public static void setEnvironmentOverlapChoice(List<Long> overlappingEnvironmentIds,
                                                   long chosenEnvironmentId, Context context){
        initPreferences(context);
        if(overlappingEnvironmentIds == null || overlappingEnvironmentIds.size() < 1){
            return;
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(buildKeyForEnvironmentIds(overlappingEnvironmentIds), chosenEnvironmentId);
        editor.apply();
    }

    private static String buildKeyForEnvironmentIds(List<Long> environmentIds){
        Collections.sort(environmentIds);
        String key = String.copyValueOf(KEY_PREFIX_OVERLAP_CHOICE.toCharArray());
        for(long i:environmentIds){
            key = key.concat("_" + i);
        }
        return key;
    }
}
