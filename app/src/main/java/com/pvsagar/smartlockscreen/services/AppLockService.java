package com.pvsagar.smartlockscreen.services;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;

import com.pvsagar.smartlockscreen.applogic.AppLocker;
import com.pvsagar.smartlockscreen.baseclasses.OnForegroundAppChangedListener;

public class AppLockService extends Service {
    private static final String PACKAGE_NAME = AppLockService.class.getPackage().getName();
    private static final String LOG_TAG = AppLockService.class.getSimpleName();

    public static final String ACTION_CLEAR_PREVIOUS_PACKAGE = PACKAGE_NAME + ".CLEAR_PREVIOUS_PACKAGE";
    public static final String ACTION_START_SERVICE = PACKAGE_NAME + ".START_SERVICE";
    public static final String ACTION_STOP_SERVICE = PACKAGE_NAME + ".STOP_SERVICE";

    private String previousPackageName = "";

    private OnForegroundAppChangedListener mOnForegroundAppChangedListener;

    private AppLockThread mAppLockThread;

    public AppLockService() {
    }

    @Override
    public void onCreate() {
        startThread();
        mOnForegroundAppChangedListener = new AppLocker(this);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if(action != null && !action.isEmpty()){
            if(action.equals(ACTION_CLEAR_PREVIOUS_PACKAGE)){
                previousPackageName = "";
            } else if(action.equals(ACTION_START_SERVICE)){
                startThread();
            } else if(action.equals(ACTION_STOP_SERVICE)){
                stopThread();
            }
        }
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void stopThread(){
        if(mAppLockThread != null) {
            mAppLockThread.interrupt();
            mAppLockThread = null;
        }
    }

    private void startThread(){
        stopThread();
        mAppLockThread = new AppLockThread();
        mAppLockThread.start();
    }

    private class AppLockThread extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ActivityManager mActivityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
                RunningTaskInfo foregroundTaskInfo = mActivityManager.getRunningTasks(1).get(0);

                String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
                PackageManager pm = getBaseContext().getPackageManager();
                String foregroundTaskActivityName = foregroundTaskInfo.topActivity.getShortClassName();

                if(foregroundTaskPackageName.equals(previousPackageName)){
                    continue;
                } else {
                    previousPackageName = foregroundTaskPackageName;
                }
                if(mOnForegroundAppChangedListener != null){
                    mOnForegroundAppChangedListener.onForegroundAppChanged(
                            foregroundTaskPackageName, foregroundTaskActivityName);
                }
            }
        }
    }
}
