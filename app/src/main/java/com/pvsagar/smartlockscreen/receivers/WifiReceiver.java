package com.pvsagar.smartlockscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

/**
 * Created by aravind on 4/9/14.
 */
public class WifiReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = WifiReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        WifiInfo wifiInfo = (WifiInfo) intent.getExtras().get(WifiManager.EXTRA_WIFI_INFO);
        String notificationText =  "Wifi State changed.";
        if(wifiInfo != null) {
            notificationText += "WiFi info: " + wifiInfo;
        }
        //context.startService(BaseService.getServiceIntent(context, notificationText));
        Toast.makeText(context, notificationText, Toast.LENGTH_SHORT).show();
    }
}
