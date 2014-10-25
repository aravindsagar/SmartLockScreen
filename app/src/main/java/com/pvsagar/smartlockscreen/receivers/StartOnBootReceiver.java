package com.pvsagar.smartlockscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pvsagar.smartlockscreen.services.BaseService;

/**
 * Created by aravind on 15/9/14.
 * Starts the app service automatically after boot.
 */
public class StartOnBootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            context.startService(BaseService.getServiceIntent(context, null, null));
        }
    }
}
