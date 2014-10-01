package com.pvsagar.smartlockscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.services.BaseService;

/**
 * Created by aravind on 25/9/14.
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = PhoneStateReceiver.class.getSimpleName();
    private static final String PACKAGE_NAME = PhoneStateReceiver.class.getPackage().getName();

    public static final String EXTRA_IS_IN_CALL = PACKAGE_NAME + ".EXTRA_IS_IN_CALL";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
            String callState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d(LOG_TAG, "call state: " + callState + ", EXTRA_STATE_RINGING:" +
                    TelephonyManager.EXTRA_STATE_RINGING + ", EXTRA_STATE_IDLE: " + TelephonyManager.EXTRA_STATE_IDLE);
            if(callState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                if (AdminActions.getCurrentPassphraseType().equals(Passphrase.TYPE_PATTERN)) {
                    Intent serviceIntent = BaseService.getServiceIntent(context, null, BaseService.ACTION_DISMISS_PATTERN_OVERLAY);
                    serviceIntent.putExtra(EXTRA_IS_IN_CALL, true);
                    context.startService(serviceIntent);
                } else {
                    Intent serviceIntent = BaseService.getServiceIntent(context, null, BaseService.ACTION_DISMISS_LOCKSCREEN_OVERLAY);
                    serviceIntent.putExtra(EXTRA_IS_IN_CALL, true);
                    context.startService(serviceIntent);
                }
            } else if(callState.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                Intent serviceIntent = BaseService.getServiceIntent(context, null, BaseService.ACTION_START_LOCKSCREEN_OVERLAY);
                serviceIntent.putExtra(EXTRA_IS_IN_CALL, false);
                context.startService(serviceIntent);
            }
        }
    }
}
