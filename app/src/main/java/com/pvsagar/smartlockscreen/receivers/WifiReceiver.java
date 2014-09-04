package com.pvsagar.smartlockscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.pvsagar.smartlockscreen.services.BaseService;

/**
 * Created by aravind on 4/9/14.
 */
public class WifiReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = WifiReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        String bssid = intent.getStringExtra(WifiManager.EXTRA_BSSID);
        WifiInfo wifiInfo = (WifiInfo) intent.getExtras().get(WifiManager.EXTRA_WIFI_INFO);
        String notificationText =  "Wifi State changed.";
        if(bssid != null && wifiInfo != null) {
            notificationText += " BSSID: " + bssid + "; WiFi info: " + wifiInfo;
            Log.d(LOG_TAG, "bssid and wifiInfo not null. BSSID: "
                    + bssid + "; WiFi info: " + wifiInfo);
        } else {
            Log.d(LOG_TAG, "bssid and wifiInfo null.");
        }
        context.startService(BaseService.getServiceIntent(context, notificationText));
    }
}
