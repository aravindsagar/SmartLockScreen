package com.pvsagar.smartlockscreen.backend_helpers;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.util.Log;

import com.pvsagar.smartlockscreen.baseclasses.OnForegroundAppChangedListener;
import com.pvsagar.smartlockscreen.services.AppLockService;
import com.pvsagar.smartlockscreen.services.BaseService;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by aravind on 9/1/15.
 */
public class RootHelper {
    private static final String GESTURE_KEY_PATH = "/data/system/";
    private static final String GESTURE_KEY_FILE_NAME = "gesture.key";
    private static final String GESTURE_KEY_FULL_PATH = GESTURE_KEY_PATH + GESTURE_KEY_FILE_NAME;
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final String PATTERN_ACTIVITY_NAME = "ChooseLockPattern";
    private static final String GESTURE_KEY_TEMP_NAME = "temp";

    private static final int TIME_FOR_CHOOSE_PATTERN_START = 20000;

    private static boolean hasCapturedPattern = false;
    private static String mEnvironmentName;
    private static final String LOG_TAG = RootHelper.class.getName();

    public static boolean hasRootAccess(){
        return Shell.SU.available();
    }

    private static String getCopyCurrentPatternCommand(Context context, String name) {
        return "cp " + GESTURE_KEY_FULL_PATH + " " + context.getFilesDir() + "/" + name + ".key";
    }

    private static String getSetCurrentPatternCommand(Context context, String name) {
        return "cp " + context.getFilesDir() + "/" + name + ".key" + " " + GESTURE_KEY_FULL_PATH;
    }

    private static String getClearCurrentGestureKeyCommand() {
        return "rm " + GESTURE_KEY_FULL_PATH;
    }

    private static String getStartPatternActivityCommand() {
        return "am start -n " + SETTINGS_PACKAGE_NAME + "/." + PATTERN_ACTIVITY_NAME + " --ez confirm_credentials false --activity-exclude-from-recents";
    }

    private static boolean isGestureKeyPresent(){
        List<String> output = Shell.SU.run("ls /data/system | grep " + GESTURE_KEY_FILE_NAME);
        return !(output.isEmpty() || !output.get(0).contains(GESTURE_KEY_FILE_NAME));
    }

    public static boolean setCurrentPattern(Context context){
        if(!hasRootAccess()) return false;
        String fileName;
        if(BaseService.getCurrentEnvironments() == null || BaseService.getCurrentEnvironments().isEmpty()) {
            fileName = "unknown_environment";
        } else {
            fileName = "" + BaseService.getCurrentEnvironments().get(0);
        }
        // TODO check whether file exists before setting db values
        List<String> output = Shell.SU.run(new String[]{"cp " + context.getFilesDir() + "/" + fileName + ".key " + GESTURE_KEY_FULL_PATH
                , "sqlite3 /data/system/locksettings.db " + "\"update locksettings set value = " + DevicePolicyManager.PASSWORD_QUALITY_SOMETHING + " where name='lockscreen.password_type'\""
                , "chmod 777 " + GESTURE_KEY_FULL_PATH
                , "chown 0,0 " + GESTURE_KEY_FULL_PATH
        });

        for (String o:output){
            Log.d(LOG_TAG, o);
        }

        Shell.SU.run(new String[]{"sqlite3 /data/system/locksettings.db " + "\"update locksettings set value = 1 where name='lock_pattern_autolock'\""});
        return true;
    }

    public static boolean getPattern(Context context, String environmentName) {
        Log.d(LOG_TAG, "Pattern capture started");
        hasCapturedPattern = false;
        mEnvironmentName = environmentName;
        if(!hasRootAccess()) return false;
        context.startService(AppLockService.getServiceIntent(context, AppLockService.ACTION_REGISTER_LISTENER_ROOT_HELPER));
        Shell.SU.run(new String[]{
                getCopyCurrentPatternCommand(context, GESTURE_KEY_TEMP_NAME),
                getClearCurrentGestureKeyCommand(),
                getStartPatternActivityCommand()
        });
        return true;
    }

    public static boolean isHasCapturedPattern() {
        return hasCapturedPattern;
    }

    public static void renameGestureKeyFile(Context context, String currentName, String newName){
        Shell.SU.run(new String[]{
                "mv " + context.getFilesDir() + "/" + currentName + ".key " + context.getFilesDir() + "/" + newName + ".key"
        });
    }

    public static class SettingsInAndOutDetector implements OnForegroundAppChangedListener {
        private boolean hasEnteredSettings = false;
        private Context mContext;
        private String mGestureKeyName;

        public SettingsInAndOutDetector(Context context, String gestureKeyName){
            mContext = context;
            mGestureKeyName = gestureKeyName;
        }

        @Override
        public void onForegroundAppChanged(String packageName, String activityName, int timeSinceRegistered) {
            if(packageName.equals(SETTINGS_PACKAGE_NAME)){
                hasEnteredSettings = true;
                Log.d(LOG_TAG, "Entered settings");
                if(!activityName.equals("." + PATTERN_ACTIVITY_NAME)){
                    mContext.startService(AppLockService.getServiceIntent(mContext, AppLockService.ACTION_UNREGISTER_LISTENER_ROOT_HELPER));
                    Log.d(LOG_TAG, "But not in Pattern screen. Unregistering. activity: " + activityName);
                }
                return;
            }
            if(timeSinceRegistered > TIME_FOR_CHOOSE_PATTERN_START && !hasEnteredSettings) {
                mContext.startService(AppLockService.getServiceIntent(mContext, AppLockService.ACTION_UNREGISTER_LISTENER_ROOT_HELPER));
                Log.d(LOG_TAG, "Pattern screen time out. Unregistering. time: " + timeSinceRegistered);
                return;
            }
            if(hasEnteredSettings){
                if(isGestureKeyPresent()){
                    //Pattern entered by the user successfully
                    Log.d(LOG_TAG, "Pattern capture successful");
                    hasCapturedPattern = true;
                    Shell.SU.run(new String[]{
                            getCopyCurrentPatternCommand(mContext, mGestureKeyName),
                            getClearCurrentGestureKeyCommand(),
                            getSetCurrentPatternCommand(mContext, GESTURE_KEY_TEMP_NAME)
                    });
                } else {
                    //Pattern entry was cancelled
                    Log.d(LOG_TAG, "Pattern capture cancelled");
                    hasCapturedPattern = false;
                    Shell.SU.run(getSetCurrentPatternCommand(mContext, GESTURE_KEY_TEMP_NAME));
                }
                mContext.startService(AppLockService.getServiceIntent(mContext, AppLockService.ACTION_UNREGISTER_LISTENER_ROOT_HELPER));
                mContext.startService(BaseService.getServiceIntent(mContext, null, BaseService.ACTION_DETECT_ENVIRONMENT));
            }
        }
    }

    public static OnForegroundAppChangedListener getListener(Context context){
        return new SettingsInAndOutDetector(context, mEnvironmentName);
    }
}
