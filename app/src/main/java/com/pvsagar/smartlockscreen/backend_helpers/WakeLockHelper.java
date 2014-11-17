package com.pvsagar.smartlockscreen.backend_helpers;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by aravind on 18/9/14.
 * A helper class for managing wake locks in the app
 */
public class WakeLockHelper {
    private static final String LOG_TAG = WakeLockHelper.class.getSimpleName();
    private static HashMap<String, WakeLock> wakeLocks = new HashMap<String, PowerManager.WakeLock>();

    public static void acquireWakeLock(final String TAG, int levelAndFlags, Context context){
        WakeLock wakeLock = wakeLocks.get(TAG);
        if(wakeLock == null){
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(levelAndFlags, TAG);
            wakeLocks.put(TAG, wakeLock);
            wakeLock.acquire();
        } else if(!wakeLock.isHeld()){
            wakeLock.acquire();
        } else {
            Log.v(LOG_TAG, "Wakelock already acquired:" + TAG);
        }
    }

    public static void releaseWakeLock(final String TAG){
        WakeLock wakeLock = wakeLocks.get(TAG);
        if(!Utility.checkForNullAndWarn(wakeLock, LOG_TAG)){
            if(wakeLock.isHeld()) wakeLock.release();
            //wakeLocks.remove(TAG); Not sure about this; might be good to keep the wakeLock object.
        }
    }
}
