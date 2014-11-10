package com.pvsagar.smartlockscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.backend_helpers.WakeLockHelper;
import com.pvsagar.smartlockscreen.services.AppLockService;
import com.pvsagar.smartlockscreen.services.BaseService;

/**
 * Created by aravind on 15/9/14.
 * Receives screen on and off events so that lockscreen overlay can be started appropriately.
 */
public class ScreenReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = ScreenReceiver.class.getSimpleName();
    private static final String HANDLER_THREAD_NAME = "screen_receiver_thread";
    public static final String WAKELOCK_TAG = "lockscreen_overlay_create_wakelock";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            WakeLockHelper.acquireWakeLock(WAKELOCK_TAG, context);
            Log.d(LOG_TAG, "Screen off");
            context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_START_LOCKSCREEN_OVERLAY));
            context.startService(AppLockService.getServiceIntent(context, AppLockService.ACTION_CLEAR_PREVIOUS_PACKAGE));
        } else {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                //Might be useful later, while adding notifications etc
                //TODO remove this and the action in intent filter if not required
                Log.d(LOG_TAG, "Screen on");
            }
        }
    }

    public static void registerScreenReceiver(Context context){
        new BroadcastReceiverRegistration().execute(context);
    }

    private static class BroadcastReceiverRegistration extends AsyncTask<Context, Void, Void>{
        @Override
        protected Void doInBackground(Context... params) {
            Context context = params[0];
            if(Utility.checkForNullAndWarn(context, LOG_TAG)){
                return null;
            }
            HandlerThread handlerThread = new HandlerThread(HANDLER_THREAD_NAME);
            handlerThread.start();
            Looper looper = handlerThread.getLooper();
            Handler handler = new Handler(looper);

            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            BroadcastReceiver mReceiver = new ScreenReceiver();

            context.registerReceiver(mReceiver, filter, null, handler);
            return null;
        }
    }
}
