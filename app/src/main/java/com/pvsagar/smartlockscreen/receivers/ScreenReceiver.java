package com.pvsagar.smartlockscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.pvsagar.smartlockscreen.LockScreenActivity;

/**
 * Created by aravind on 15/9/14.
 */
public class ScreenReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = ScreenReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Intent lockscreenIntent = new Intent(context, LockScreenActivity.class);
            lockscreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(lockscreenIntent);
        } else {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                //Might be useful later, while adding notifications etc
                //TODO remove this and the action in intent filter if not required
            }
        }
    }

    public static void registerScreenReceiver(Context context){
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        context.registerReceiver(mReceiver, filter);
    }
}
