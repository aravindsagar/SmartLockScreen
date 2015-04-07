package com.pvsagar.smartlockscreen.backend_helpers;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Pattern;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by aravind on 9/1/15.
 */
public class RootHelper {
    //Contains just static methods. No need to instantiate an object
    private RootHelper(){}

    private final static String PATTERN_EVER_CHOSEN_KEY = "lockscreen.patterneverchosen";
    private final static String PASSWORD_TYPE_KEY = "lockscreen.password_type";
    private final static String PATTERN_ENABLED_KEY = "lock_pattern_autolock";
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String LOCK_PATTERN_FILE_NAME = "gesture.key";
    private static final String LOCK_SETTINGS_DB_FILE_NAME = "locksettings.db";
    private static final String LOCK_SETTINGS_KEY_PASSWORD_TYPE = "lockscreen.password_type";
    private final static String LOCKSETTINGS_DB_FILE = android.os.Environment.getDataDirectory().
            getAbsolutePath() + SYSTEM_DIRECTORY + LOCK_SETTINGS_DB_FILE_NAME;
    private final static String LOCKPATTERN_FILE = android.os.Environment.getDataDirectory().
            getAbsolutePath() + SYSTEM_DIRECTORY + LOCK_PATTERN_FILE_NAME;
    private static final String LOG_TAG = RootHelper.class.getName();

    public static boolean hasRootAccess(){
        return Shell.SU.available();
    }

    public static boolean setCurrentPattern(Context context, Pattern pattern){
        if(!hasRootAccess()) return false;
        ServiceHelper serviceHelper = new ServiceHelper();

        List<String> outputs = Shell.SU.run(new String[]{
                serviceHelper.setPattern(pattern),
                serviceHelper.setBoolean(PATTERN_EVER_CHOSEN_KEY, true),
                serviceHelper.setLong(PASSWORD_TYPE_KEY, DevicePolicyManager.PASSWORD_QUALITY_SOMETHING),
                serviceHelper.setActivePasswordState(pattern),
                serviceHelper.setBoolean(PATTERN_ENABLED_KEY, true)
        });
        Log.d(LOG_TAG, "Su commands over. Output:");
        for (String output:outputs){
            Log.d(LOG_TAG, output);
        }

        if(!hasSetPattern(context)){
            //Trying a different approach
            writePattenFile(context, pattern);
            outputs = Shell.SU.run(new String[]{
                    getSetCurrentPatternCommand(context, LOCK_PATTERN_FILE_NAME)
                    , getSqlite3BinaryFile(context) + " " + LOCKSETTINGS_DB_FILE + " \"update locksettings set value = "
                    + DevicePolicyManager.PASSWORD_QUALITY_SOMETHING + " where name='"
                    + LOCK_SETTINGS_KEY_PASSWORD_TYPE + "\'\""
                    , "chmod 777 " + LOCKPATTERN_FILE
                    , "chown 0.0 " + LOCKPATTERN_FILE
            });
            Log.d(LOG_TAG, "Alternate method. Output:");
            for (String output:outputs){
                Log.d(LOG_TAG, output);
            }
            Shell.SU.run(new String[]{getSqlite3BinaryFile(context) + " " + LOCKSETTINGS_DB_FILE +
                    " \"update locksettings set value = 1 where name='lock_pattern_autolock'\""});
        }

        boolean result = hasSetPattern(context);
        if(result){
            Log.d(LOG_TAG, "Set pattern");
        } else {
            Log.d(LOG_TAG, "Couldn't set pattern");
        }
        return result;
    }

    private static boolean hasSetPattern(Context context) {
        try {
            long passwordType = Long.parseLong(Shell.SU.run(getSqlite3BinaryFile(context) + " "
                    + LOCKSETTINGS_DB_FILE + " \"select value from locksettings where name=\'"
                    + LOCK_SETTINGS_KEY_PASSWORD_TYPE + "\'\"").get(0));
            return passwordType == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING;
        } catch (NumberFormatException e){
            Log.e(LOG_TAG, "Cannot parse current lock password type");
            return false;
        } catch (IndexOutOfBoundsException e){
            Log.e(LOG_TAG, "Probably sqlite3 isn't present, pattern might be set anyway.");
            return true;
        }
    }

    private static void writePattenFile(Context context, Pattern pattern){
        try {
            // Write the hash to file
            RandomAccessFile raf = new RandomAccessFile(context.getFilesDir() + "/" + LOCK_PATTERN_FILE_NAME, "rw");
            // Truncate the file if pattern is null, to clear the lock
            byte[] hash = pattern.toSystemHash();
            if (hash == null || hash.length == 0) {
                raf.setLength(0);
            } else {
                raf.write(hash, 0, hash.length);
            }
            raf.close();
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Error writing to file " + ioe);
        }
    }

    private static String getSetCurrentPatternCommand(Context context, String name) {
        return "cp " + context.getFilesDir() + "/" + name + ".key" + " " + LOCKPATTERN_FILE;
    }

    private static final String SQLITE3_BINARY_FILE_NAME = "sqlite3";
    private static String getSqlite3BinaryFile(Context context){
        copyBinaryFileFromAssets(context);
        return context.getFilesDir().getAbsolutePath() + "/" + SQLITE3_BINARY_FILE_NAME;
    }

    private static void copyBinaryFileFromAssets(Context context) {
        List<String> shOutput = Shell.SH.run("ls " + context.getFilesDir().getAbsolutePath()
                + " | grep " + SQLITE3_BINARY_FILE_NAME);
        if(!shOutput.isEmpty()) return;
        InputStream sqlite3IpStream = null;
        String[] abis;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            abis = Build.SUPPORTED_ABIS;
        } else {
            abis = new String[2];
            abis[0] = Build.CPU_ABI;
            abis[1] = Build.CPU_ABI2;
        }
        AssetManager assetManager = context.getAssets();
        try {
            for (String abi : abis) {
                if (abi.startsWith("armeabi")) {
                    sqlite3IpStream = assetManager.open("sqlite3.armeabi.pie");
                    break;
                } else if(abi.startsWith("x86") || abi.startsWith("X86")){
                    sqlite3IpStream = assetManager.open("sqlite3.x86.pie");
                    break;
                } else if(abi.startsWith("mips")){
                    sqlite3IpStream = assetManager.open("sqlite3.mips.pie");
                    break;
                }
            }

            if(sqlite3IpStream == null) {
                Log.e(LOG_TAG, "sqlite3 for CPU ABI is not included.");
                return;
            }

            final File file = new File(context.getFilesDir(), SQLITE3_BINARY_FILE_NAME);
            OutputStream output = null;
            try {
                output = new FileOutputStream(file);
                final byte[] buffer = new byte[1024];
                int read;

                while ((read = sqlite3IpStream.read(buffer)) != -1)
                    output.write(buffer, 0, read);

                output.flush();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(output != null) {
                    output.close();
                }
                Shell.SH.run("chmod 777 " + context.getFilesDir() + "/" + SQLITE3_BINARY_FILE_NAME);
            }

        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(sqlite3IpStream != null) {
                try {
                    sqlite3IpStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ServiceHelper {
        private interface Constants{
            String LOCK_SETTINGS_SERVICE();
            String DEVICE_POLICY_SERVICE();
            int SET_PATTERN_METHOD();
            int SET_BOOLEAN_METHOD();
            int SET_LONG_METHOD();
            int SET_ACTIVE_PASSWORD_STATE_METHOD();
        }

        private class Lollipop_5_0 implements Constants{

            public static final String LOCK_SETTINGS_SERVICE = "lock_settings";
            public static final String DEVICE_POLICY_SERVICE = "device_policy";
            public static final int SET_PATTERN_METHOD = 7;
            public static final int SET_BOOLEAN_METHOD = 1;
            public static final int SET_LONG_METHOD = 2;
            public static final int SET_ACTIVE_PASSWORD_STATE_METHOD = 50;

            @Override
            public String LOCK_SETTINGS_SERVICE() {
                return LOCK_SETTINGS_SERVICE;
            }

            @Override
            public String DEVICE_POLICY_SERVICE() {
                return DEVICE_POLICY_SERVICE;
            }

            @Override
            public int SET_PATTERN_METHOD() {
                return SET_PATTERN_METHOD;
            }

            @Override
            public int SET_BOOLEAN_METHOD() {
                return SET_BOOLEAN_METHOD;
            }

            @Override
            public int SET_LONG_METHOD() {
                return SET_LONG_METHOD;
            }

            @Override
            public int SET_ACTIVE_PASSWORD_STATE_METHOD() {
                return SET_ACTIVE_PASSWORD_STATE_METHOD;
            }
        }

        private class Lollipop_5_1 implements Constants{

            public static final String LOCK_SETTINGS_SERVICE = "lock_settings";
            public static final String DEVICE_POLICY_SERVICE = "device_policy";
            public static final int SET_PATTERN_METHOD = 7;
            public static final int SET_BOOLEAN_METHOD = 1;
            public static final int SET_LONG_METHOD = 2;
            public static final int SET_ACTIVE_PASSWORD_STATE_METHOD = 51;

            @Override
            public String LOCK_SETTINGS_SERVICE() {
                return LOCK_SETTINGS_SERVICE;
            }

            @Override
            public String DEVICE_POLICY_SERVICE() {
                return DEVICE_POLICY_SERVICE;
            }

            @Override
            public int SET_PATTERN_METHOD() {
                return SET_PATTERN_METHOD;
            }

            @Override
            public int SET_BOOLEAN_METHOD() {
                return SET_BOOLEAN_METHOD;
            }

            @Override
            public int SET_LONG_METHOD() {
                return SET_LONG_METHOD;
            }

            @Override
            public int SET_ACTIVE_PASSWORD_STATE_METHOD() {
                return SET_ACTIVE_PASSWORD_STATE_METHOD;
            }
        }

        private class KitKat_4_4 implements Constants{

            public static final String LOCK_SETTINGS_SERVICE = "lock_settings";
            public static final String DEVICE_POLICY_SERVICE = "device_policy";
            public static final int SET_PATTERN_METHOD = 7;
            public static final int SET_BOOLEAN_METHOD = 1;
            public static final int SET_LONG_METHOD = 2;
            public static final int SET_ACTIVE_PASSWORD_STATE_METHOD = 47;

            @Override
            public String LOCK_SETTINGS_SERVICE() {
                return LOCK_SETTINGS_SERVICE;
            }

            @Override
            public String DEVICE_POLICY_SERVICE() {
                return DEVICE_POLICY_SERVICE;
            }

            @Override
            public int SET_PATTERN_METHOD() {
                return SET_PATTERN_METHOD;
            }

            @Override
            public int SET_BOOLEAN_METHOD() {
                return SET_BOOLEAN_METHOD;
            }

            @Override
            public int SET_LONG_METHOD() {
                return SET_LONG_METHOD;
            }

            @Override
            public int SET_ACTIVE_PASSWORD_STATE_METHOD() {
                return SET_ACTIVE_PASSWORD_STATE_METHOD;
            }
        }

        private class Jellybean_4_3 implements Constants{

            public static final String LOCK_SETTINGS_SERVICE = "lock_settings";
            public static final String DEVICE_POLICY_SERVICE = "device_policy";
            public static final int SET_PATTERN_METHOD = 7;
            public static final int SET_BOOLEAN_METHOD = 1;
            public static final int SET_LONG_METHOD = 2;
            public static final int SET_ACTIVE_PASSWORD_STATE_METHOD = 47;

            @Override
            public String LOCK_SETTINGS_SERVICE() {
                return LOCK_SETTINGS_SERVICE;
            }

            @Override
            public String DEVICE_POLICY_SERVICE() {
                return DEVICE_POLICY_SERVICE;
            }

            @Override
            public int SET_PATTERN_METHOD() {
                return SET_PATTERN_METHOD;
            }

            @Override
            public int SET_BOOLEAN_METHOD() {
                return SET_BOOLEAN_METHOD;
            }

            @Override
            public int SET_LONG_METHOD() {
                return SET_LONG_METHOD;
            }

            @Override
            public int SET_ACTIVE_PASSWORD_STATE_METHOD() {
                return SET_ACTIVE_PASSWORD_STATE_METHOD;
            }
        }

        private class Jellybean_4_2 implements Constants{

            public static final String LOCK_SETTINGS_SERVICE = "lock_settings";
            public static final String DEVICE_POLICY_SERVICE = "device_policy";
            public static final int SET_PATTERN_METHOD = 7;
            public static final int SET_BOOLEAN_METHOD = 1;
            public static final int SET_LONG_METHOD = 2;
            public static final int SET_ACTIVE_PASSWORD_STATE_METHOD = 47;

            @Override
            public String LOCK_SETTINGS_SERVICE() {
                return LOCK_SETTINGS_SERVICE;
            }

            @Override
            public String DEVICE_POLICY_SERVICE() {
                return DEVICE_POLICY_SERVICE;
            }

            @Override
            public int SET_PATTERN_METHOD() {
                return SET_PATTERN_METHOD;
            }

            @Override
            public int SET_BOOLEAN_METHOD() {
                return SET_BOOLEAN_METHOD;
            }

            @Override
            public int SET_LONG_METHOD() {
                return SET_LONG_METHOD;
            }

            @Override
            public int SET_ACTIVE_PASSWORD_STATE_METHOD() {
                return SET_ACTIVE_PASSWORD_STATE_METHOD;
            }
        }

        private class Jellybean_4_1 implements Constants{

            public static final String LOCK_SETTINGS_SERVICE = "lock_settings";
            public static final String DEVICE_POLICY_SERVICE = "device_policy";
            public static final int SET_PATTERN_METHOD = 7;
            public static final int SET_BOOLEAN_METHOD = 1;
            public static final int SET_LONG_METHOD = 2;
            public static final int SET_ACTIVE_PASSWORD_STATE_METHOD = 45;

            @Override
            public String LOCK_SETTINGS_SERVICE() {
                return LOCK_SETTINGS_SERVICE;
            }

            @Override
            public String DEVICE_POLICY_SERVICE() {
                return DEVICE_POLICY_SERVICE;
            }

            @Override
            public int SET_PATTERN_METHOD() {
                return SET_PATTERN_METHOD;
            }

            @Override
            public int SET_BOOLEAN_METHOD() {
                return SET_BOOLEAN_METHOD;
            }

            @Override
            public int SET_LONG_METHOD() {
                return SET_LONG_METHOD;
            }

            @Override
            public int SET_ACTIVE_PASSWORD_STATE_METHOD() {
                return SET_ACTIVE_PASSWORD_STATE_METHOD;
            }
        }

        private Constants getConstants(){
            switch (Build.VERSION.SDK_INT) {
                case 22: //TODO change 22 to constant after changing compile and target sdk version
                    return new Lollipop_5_1();
                case Build.VERSION_CODES.LOLLIPOP:
                    return new Lollipop_5_0();
                case Build.VERSION_CODES.KITKAT:
                    return new KitKat_4_4();
                case Build.VERSION_CODES.JELLY_BEAN_MR2:
                    return new Jellybean_4_3();
                case Build.VERSION_CODES.JELLY_BEAN_MR1:
                    return new Jellybean_4_2();
                case Build.VERSION_CODES.JELLY_BEAN:
                    return new Jellybean_4_1();
            }

            return null;
        }

        private Constants constants;
        private static final String SERVICE_CALL = "service call";
        private static final String STRING_ARG = "s16";
        private static final String INT_ARG = "i32";
        private static final String SPACE = " ";

        public ServiceHelper(){
            constants = getConstants();
        }

        public String setPattern(Pattern pattern){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return SERVICE_CALL + SPACE + constants.LOCK_SETTINGS_SERVICE() + SPACE
                        + constants.SET_PATTERN_METHOD() + SPACE + STRING_ARG + SPACE + pattern.toSystemString()
                        + SPACE + INT_ARG + " 0";
            } else {
                Log.d(LOG_TAG, pattern.toSystemHash().toString());
                return SERVICE_CALL + SPACE + constants.LOCK_SETTINGS_SERVICE() + SPACE
                        + constants.SET_PATTERN_METHOD() + SPACE + STRING_ARG + SPACE + pattern.toSystemHash()
                        + SPACE + INT_ARG + " 0";
            }
        }

        public String setBoolean(final String KEY, boolean value){
            return SERVICE_CALL + SPACE + constants.LOCK_SETTINGS_SERVICE() + SPACE
                    + constants.SET_BOOLEAN_METHOD() + SPACE + STRING_ARG + SPACE + KEY
                    + SPACE + INT_ARG + SPACE + (value?1:0) + SPACE + INT_ARG + " 0";
        }

        public String setLong(final String KEY, int value){
            return SERVICE_CALL + SPACE + constants.LOCK_SETTINGS_SERVICE() + SPACE
                    + constants.SET_LONG_METHOD() + SPACE + STRING_ARG + SPACE + KEY
                    + SPACE + INT_ARG + SPACE + value + SPACE + INT_ARG + " 0";
        }

        public String setActivePasswordState(Pattern pattern){
            int patternSize = pattern.getPassphraseRepresentation().size();
            return SERVICE_CALL + SPACE + constants.DEVICE_POLICY_SERVICE() + SPACE
                    + constants.SET_ACTIVE_PASSWORD_STATE_METHOD()
                    + SPACE + INT_ARG + SPACE + DevicePolicyManager.PASSWORD_QUALITY_SOMETHING
                    + SPACE + INT_ARG + SPACE + patternSize
                    + SPACE + INT_ARG + " 0"
                    + SPACE + INT_ARG + " 0"
                    + SPACE + INT_ARG + " 0"
                    + SPACE + INT_ARG + " 0"
                    + SPACE + INT_ARG + " 0"
                    + SPACE + INT_ARG + " 0"
                    + SPACE + INT_ARG + " 0";
        }
    }

    public interface RootAccessCheckedListener {
        void onRootAccessChecked(boolean hasRootAccess);
    }

    public static void hasRootAccessAsync(RootAccessCheckedListener listener){
        new CheckForRootAccessAsyncTask().execute(listener);
    }

    private static class CheckForRootAccessAsyncTask extends AsyncTask<RootAccessCheckedListener, Void, Boolean>{
        RootAccessCheckedListener mRootAccessCheckedListener;
        @Override
        protected Boolean doInBackground(RootAccessCheckedListener... params) {
            mRootAccessCheckedListener = params[0];
            return RootHelper.hasRootAccess();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(mRootAccessCheckedListener != null){
                mRootAccessCheckedListener.onRootAccessChecked(aBoolean);
            }
        }
    }
}
