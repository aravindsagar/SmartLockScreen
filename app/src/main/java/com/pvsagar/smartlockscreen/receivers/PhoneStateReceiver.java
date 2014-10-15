package com.pvsagar.smartlockscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.services.BaseService;

/**
 * Created by aravind on 25/9/14.
 * A broadcast receiver which dismisses the overlay when phone call/alarm comes.
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = PhoneStateReceiver.class.getSimpleName();
    public static final String ALARM_ALERT_ACTION = "com.android.deskclock.ALARM_ALERT";
    public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";
    public static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";
    public static final String ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE";

    private static final String PACKAGE_NAME = PhoneStateReceiver.class.getPackage().getName();

    public static final String EXTRA_IS_IN_CALL = PACKAGE_NAME + ".EXTRA_IS_IN_CALL";

    private static boolean isInCall = false, isAlarmRinging = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
            String callState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if(callState.equals(TelephonyManager.EXTRA_STATE_RINGING) || callState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                isInCall = true;
            } else if(callState.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                isInCall = false;
            }
        } else if(intent.getAction().equals(ALARM_ALERT_ACTION)){
            isAlarmRinging = true;
        } else if(intent.getAction().equals(ALARM_SNOOZE_ACTION) || intent.getAction().equals(ALARM_DISMISS_ACTION)
                || intent.getAction().equals(ALARM_DONE_ACTION)) {
            isAlarmRinging = false;
        }

        if(isInCall || isAlarmRinging){
            if (AdminActions.getCurrentPassphraseType().equals(Passphrase.TYPE_PATTERN)) {
                Intent serviceIntent = BaseService.getServiceIntent(context, null, BaseService.ACTION_DISMISS_PATTERN_OVERLAY);
                serviceIntent.putExtra(EXTRA_IS_IN_CALL, true);
                context.startService(serviceIntent);
            } else {
                Intent serviceIntent = BaseService.getServiceIntent(context, null, BaseService.ACTION_DISMISS_LOCKSCREEN_OVERLAY);
                serviceIntent.putExtra(EXTRA_IS_IN_CALL, true);
                context.startService(serviceIntent);
            }
        } else {
            Intent serviceIntent = BaseService.getServiceIntent(context, null, BaseService.ACTION_START_LOCKSCREEN_OVERLAY);
            serviceIntent.putExtra(EXTRA_IS_IN_CALL, false);
            context.startService(serviceIntent);
        }
    }
}
