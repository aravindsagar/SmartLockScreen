package com.pvsagar.smartlockscreen.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AppLockService extends Service {
    private static final String LOG_TAG = AppLockService.class.getSimpleName();

    public AppLockService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new AppLockThread().start();
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private static class AppLockThread extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(LOG_TAG, "Run");
            }
        }
    }
}
