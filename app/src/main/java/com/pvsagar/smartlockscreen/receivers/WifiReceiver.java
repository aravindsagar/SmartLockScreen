package com.pvsagar.smartlockscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.pvsagar.smartlockscreen.applogic_objects.WiFiEnvironmentVariable;

import java.util.List;

/**
 * Created by aravind on 4/9/14.
 */
public class WifiReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = WifiReceiver.class.getSimpleName();

    private static WiFiEnvironmentVariable currentWifiNetwork;

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiInfo wifiInfo = (WifiInfo) intent.getExtras().get(WifiManager.EXTRA_WIFI_INFO);
        String wifiEncryptionType = new String();
        if(wifiInfo != null) {
            List<WifiConfiguration> wifiConfigurations = WiFiEnvironmentVariable.getConfiguredWiFiConnections(context);
            for(WifiConfiguration configuration: wifiConfigurations){
                if(configuration.SSID.equals(wifiInfo.getSSID())) {
                    wifiEncryptionType = WiFiEnvironmentVariable.getSecurity(configuration);
                    break;
                }
            }
            currentWifiNetwork = WiFiEnvironmentVariable.getWifiEnvironmentVariableFromDatabase(
                    context, wifiInfo.getSSID(), wifiEncryptionType);
        } else {
            currentWifiNetwork = null;
        }
    }

    public WiFiEnvironmentVariable getCurrentWifiNetwork(){
        return currentWifiNetwork;
    }
}
