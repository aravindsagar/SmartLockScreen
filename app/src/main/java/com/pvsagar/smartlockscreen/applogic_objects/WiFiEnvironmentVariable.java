package com.pvsagar.smartlockscreen.applogic_objects;

import android.content.ContentValues;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.WiFiNetworksEntry;

import java.util.ArrayList;

/**
 * Created by aravind on 10/8/14.
 */
public class WiFiEnvironmentVariable extends EnvironmentVariable {
    private static final String LOG_TAG = WiFiEnvironmentVariable.class.getSimpleName();

    /* Encryption type */
    public static final String SECURITY_PSK = "WPA_PSK";
    public static final String SECURITY_EAP = "WPA_EAP";
    public static final String SECURITY_NONE = "NONE";
    public static final String SECURITY_WEP = "WEP";
    /* End of Encryption type */

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
        return true;
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

    @Override
    public ContentValues getContentValues() {
        ContentValues wifiValues = new ContentValues();
        wifiValues.put(WiFiNetworksEntry.COLUMN_SSID, getSSID());
        wifiValues.put(WiFiNetworksEntry.COLUMN_ENCRYPTION_TYPE, getEncryptionType());
        return wifiValues;
    }

    public static boolean enableWifi(Context context){
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        if(wifiManager == null){
            Log.e(LOG_TAG,"WiFi Hardware not available");
            return false;
        }
        if(!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            return true;
        }
        return true;
    }

    public static ArrayList<WifiConfiguration> getConfiguredWiFiConnections(Context context){

        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        if(wifiManager == null){
            Log.e(LOG_TAG,"WiFi Hardware not available");
            return null;
        }
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        ArrayList<WifiConfiguration> wifiConfigurations = new ArrayList<WifiConfiguration>();
        for (WifiConfiguration wifiConfiguration : wifiManager.getConfiguredNetworks()) {
            wifiConfigurations.add(wifiConfiguration);
        }
        return wifiConfigurations;
    }

    public static String getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }
}
