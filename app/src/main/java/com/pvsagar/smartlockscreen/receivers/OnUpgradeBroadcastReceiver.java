package com.pvsagar.smartlockscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.pvsagar.smartlockscreen.services.BaseService;

/**
 * Created by aravind on 17/9/14.
 * Receiver which retsarts the services when the package is reinstalled (for eg, during app update)
 */
public class OnUpgradeBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_ADD_GEOFENCES));
        context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_DETECT_WIFI));
        context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_DETECT_BLUETOOTH));

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if(!powerManager.isScreenOn()){
            context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_START_LOCKSCREEN_ACTIVITY));
        }
    }
}
