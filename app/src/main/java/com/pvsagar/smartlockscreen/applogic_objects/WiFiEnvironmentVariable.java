package com.pvsagar.smartlockscreen.applogic_objects;

import android.util.Log;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;

/**
 * Created by aravind on 10/8/14.
 */
public class WiFiEnvironmentVariable extends EnvironmentVariable {
    private static final String LOG_TAG = WiFiEnvironmentVariable.class.getSimpleName();

    private static final int NUMBER_OF_STRING_VALUES = 2;
    private static final int INDEX_SSID = 0;
    private static final int INDEX_ENCRYPTION_TYPE = 1;

    public WiFiEnvironmentVariable() {
        super(EnvironmentVariable.TYPE_WIFI_NETWORKS, 0, NUMBER_OF_STRING_VALUES);
    }

    public WiFiEnvironmentVariable(String ssid, String encryptionType){
        super(EnvironmentVariable.TYPE_WIFI_NETWORKS, null, new String[]{ssid, encryptionType});
    }

    @Override
    public boolean isStringValuesSupported() {
        return false;
    }

    @Override
    public boolean isFloatValuesSupported() {
        return false;
    }

    public String getSSID(){
        try {
            return getStringValue(INDEX_SSID);
        } catch (Exception e){
            Log.e(LOG_TAG, "Internal application error, please file a bug report to developer."
                    + e.getMessage());
            return null;
        }
    }

    public String getEncryptionType(){
        try {
            return getStringValue(INDEX_ENCRYPTION_TYPE);
        } catch (Exception e){
            Log.e(LOG_TAG, "Internal application error, please file a bug report to developer."
                    + e.getMessage());
            return null;
        }
    }

    public void setSSID(String ssid){
        setStringValue(ssid, INDEX_SSID);
    }

    public void setEncryptionType(String encryptionType){
        setStringValue(encryptionType, INDEX_ENCRYPTION_TYPE);
    }
}
