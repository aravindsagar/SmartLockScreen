package com.pvsagar.smartlockscreen.backend_helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.pvsagar.smartlockscreen.GeneralSettingsActivity;
import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.OverlappingEnvironmentIdsWithResolved;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by aravind on 19/9/14.
 * A helper class for getting and setting application shared preferences
 */
public class SharedPreferencesHelper {
    private static final String LOG_TAG = SharedPreferencesHelper.class.getSimpleName();
    private static final String PACKAGE_NAME = SharedPreferencesHelper.class.getPackage().getName();
    private static final String KEY_MASTER_PASSWORD = PACKAGE_NAME + ".MASTER_PASSWORD";
    private static final String KEY_MASTER_PASSWORD_TYPE = PACKAGE_NAME + ".MASTER_PASSWORD_TYPE";
    private static final String KEY_DEVICE_OWNER_USER_ID = PACKAGE_NAME + ".DEVICE_OWNER_USER_ID";
    private static final String KEY_PREFIX_OVERLAP_CHOICE = PACKAGE_NAME + ".OVERLAP_CHOICE_AMONG";
    private static final String KEY_VERSION_NAME = PACKAGE_NAME + ".VERSION_NAME";
    private static final String KEY_VERSION_CODE = PACKAGE_NAME + ".VERSION_CODE";
    private static final String KEY_DOWNLOAD_LINK = PACKAGE_NAME + ".DOWNLOAD_LINK";
    private static final String KEY_CHANGE_LOG_LINK = PACKAGE_NAME + ".CHANGE_LOG_LINK";
    private static String KEY_ENABLE_NOTIFICATION;
    private static String KEY_SHOW_LOCKSCREEN_NOTIFICATIONS;
    private static String KEY_HIDE_LOW_PRIORITY_NOTIFICATIONS;
    private static String KEY_HIDE_PERSISTENT_NOTIFICATIONS;
    private static String KEY_PATTERN_TYPE;
    private static String KEY_PATTERN_VISIBLE;
    private static SharedPreferences preferences;

    private static void initPreferences(Context context){
        if(preferences==null){
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        if(KEY_ENABLE_NOTIFICATION == null || KEY_ENABLE_NOTIFICATION.isEmpty()){
            KEY_ENABLE_NOTIFICATION = context.getResources().getString(R.string.pref_key_enable_notification);
        }
        if(KEY_SHOW_LOCKSCREEN_NOTIFICATIONS == null || KEY_SHOW_LOCKSCREEN_NOTIFICATIONS.isEmpty()){
            KEY_SHOW_LOCKSCREEN_NOTIFICATIONS = context.getResources().getString(R.string.pref_key_show_lockscreen_notifications);
        }
        if(KEY_HIDE_LOW_PRIORITY_NOTIFICATIONS== null || KEY_HIDE_LOW_PRIORITY_NOTIFICATIONS.isEmpty()){
            KEY_HIDE_LOW_PRIORITY_NOTIFICATIONS = context.getResources().getString(R.string.pref_key_hide_low_priority_notifications);
        }
        if(KEY_HIDE_PERSISTENT_NOTIFICATIONS == null || KEY_HIDE_PERSISTENT_NOTIFICATIONS.isEmpty()){
            KEY_HIDE_PERSISTENT_NOTIFICATIONS = context.getResources().getString(R.string.pref_key_hide_persistent_notifications);
        }
        if(KEY_PATTERN_TYPE == null || KEY_PATTERN_TYPE.isEmpty()){
            KEY_PATTERN_TYPE = context.getResources().getString(R.string.pref_key_pattern_type);
        }
        if(KEY_PATTERN_VISIBLE == null || KEY_PATTERN_VISIBLE.isEmpty()){
            KEY_PATTERN_VISIBLE = context.getResources().getString(R.string.pref_key_is_visible_pattern);
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
        preferences = null;
    }

    public static long getDeviceOwnerUserId(Context context){
        initPreferences(context);
        return preferences.getLong(KEY_DEVICE_OWNER_USER_ID, -1);
    }

    public static void setDeviceOwnerUserId(Context context, long id){
        initPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_DEVICE_OWNER_USER_ID, id);
        editor.apply();
        preferences = null;
    }

    public static void setEnvironmentOverlapChoice(List<Environment> overlappingEnvironments ,
                                                   long chosenEnvironmentId, Context context){
        initPreferences(context);
        if(overlappingEnvironments == null || overlappingEnvironments.size() < 1){
            return;
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(buildKeyForEnvironments(overlappingEnvironments), chosenEnvironmentId);
        editor.apply();
        preferences = null;
    }

    public static long getEnvironmentOverlapChoice(List<Environment> overlappingEnvironments, Context context){
        initPreferences(context);
        if (overlappingEnvironments == null || overlappingEnvironments.size() < 1) {
            return -1;
        }
        return preferences.getLong(buildKeyForEnvironments(overlappingEnvironments), -1);
    }

    private static String buildKeyForEnvironments(List<Environment> environments){
        Collections.sort(environments, new Comparator<Environment>() {
            @Override
            public int compare(Environment lhs, Environment rhs) {
                return (int) (lhs.getId() - rhs.getId());
            }
        });
        String key = String.copyValueOf(KEY_PREFIX_OVERLAP_CHOICE.toCharArray());
        for(Environment i:environments){
            key = key.concat("_" + i.getId());
        }
        return key;
    }

    private static String buildKeyForEnvironmentIds(List<Long> environmentIds){
        Collections.sort(environmentIds);
        String key = String.copyValueOf(KEY_PREFIX_OVERLAP_CHOICE.toCharArray());
        for(long i:environmentIds){
            key = key.concat("_" + i);
        }
        return key;
    }

    public static List<OverlappingEnvironmentIdsWithResolved> getAllEnvironmentOverlaps(Context context){
        List<OverlappingEnvironmentIdsWithResolved> overlapResolved = new ArrayList<OverlappingEnvironmentIdsWithResolved>();
        initPreferences(context);
        Map<String, ?> preferenceMap = preferences.getAll();
        for(String key:preferenceMap.keySet()){
            if(key.startsWith(KEY_PREFIX_OVERLAP_CHOICE)){
                OverlappingEnvironmentIdsWithResolved resolved = new OverlappingEnvironmentIdsWithResolved();
                resolved.setOverlappingEnvIds(getEnvironmentIdsFromKey(key));
                resolved.setResolvedEnvId((Long) preferenceMap.get(key));
                overlapResolved.add(resolved);
            }
        }
        return overlapResolved;
    }

    public static void removeEnvironmentFromOverlapPreferences(long environmentId, Context context){
        initPreferences(context);
        Map<String, ?> preferenceMap = preferences.getAll();
        for(String key:preferenceMap.keySet()){
            if(key.startsWith(KEY_PREFIX_OVERLAP_CHOICE)){
                List<Long> envIds = getEnvironmentIdsFromKey(key);
                long prefEnv = (Long) preferenceMap.get(key);
                if(envIds.contains(environmentId)){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(key);
                    envIds.remove(environmentId);
                    if(envIds.size() > 1 && prefEnv != environmentId &&
                            preferences.getLong(buildKeyForEnvironmentIds(envIds), -1) == -1){
                        editor.putLong(buildKeyForEnvironmentIds(envIds), prefEnv);
                    }
                    editor.apply();
                }
            }
        }
        preferences = null;
    }

    private static List<Long> getEnvironmentIdsFromKey(String key){
        List<Long> environmentIds = new ArrayList<Long>();
        String[] keyParts = key.split("_");
        for (int i = KEY_PREFIX_OVERLAP_CHOICE.split("_").length; i < keyParts.length; i++) {
            environmentIds.add(Long.parseLong(keyParts[i]));
        }
        return environmentIds;
    }

    public static boolean isNotificationEnabled(Context context){
        initPreferences(context);
        return preferences.getBoolean(KEY_ENABLE_NOTIFICATION, true);
    }

    public static void setWallpaperPreference(Context context, String value){
        initPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.pref_key_lockscreen_wallpaper), value);
        editor.apply();
        preferences = null;
    }

    public static String getWallpaperPreference(Context context){
        initPreferences(context);
        return preferences.getString(context.getString(R.string.pref_key_lockscreen_wallpaper), "system");
    }

    public static void setLatestVersionInfo(AppUpdateManager.AppInfo appInfo, Context context){
        initPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_VERSION_CODE, appInfo.versionCode);
        editor.putString(KEY_VERSION_NAME, appInfo.versionName);
        editor.putString(KEY_CHANGE_LOG_LINK, appInfo.changeLogUrl.toString());
        editor.putString(KEY_DOWNLOAD_LINK, appInfo.downloadUrl.toString());
        editor.apply();
        preferences = null;
    }

    public static AppUpdateManager.AppInfo getLatestVersionInfo(Context context){
        initPreferences(context);
        try {
            return new AppUpdateManager.AppInfo(
                    preferences.getInt(KEY_VERSION_CODE, 0),
                    preferences.getString(KEY_VERSION_NAME, null),
                    new URL(preferences.getString(KEY_VERSION_NAME, null)),
                    new URL(preferences.getString(KEY_VERSION_NAME, null))
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean shouldHidePersistentNotifications(Context context){
        initPreferences(context);
        return preferences.getBoolean(GeneralSettingsActivity.PREF_KEY_HIDE_PERSISTENT_NOTIFICATIONS, false);
    }

    public static boolean shouldHideLowPriorityNotifications(Context context){
        initPreferences(context);
        return preferences.getBoolean(GeneralSettingsActivity.PREF_KEY_HIDE_LOW_PRIORITY_NOTIFICATIONS, false);
    }

    public static boolean isLockscreenNotificationsShown(Context context){
        initPreferences(context);
        return preferences.getBoolean(KEY_SHOW_LOCKSCREEN_NOTIFICATIONS, true);
    }

    public static String getPatternType(Context context) {
        initPreferences(context);
        return preferences.getString(KEY_PATTERN_TYPE, "inbuilt");
    }

    public static boolean isPatternVisible(Context context){
        initPreferences(context);
        return preferences.getBoolean(KEY_PATTERN_VISIBLE, true);
    }
}