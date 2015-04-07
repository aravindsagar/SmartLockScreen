package com.pvsagar.smartlockscreen.services;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.view.WindowManager;

import com.pvsagar.smartlockscreen.applogic.AppLocker;
import com.pvsagar.smartlockscreen.baseclasses.OnForegroundAppChangedListener;
import com.pvsagar.smartlockscreen.services.window_helpers.AppLockScreenOverlay;

import java.util.List;

public class AppLockService extends Service {
    private static final String PACKAGE_NAME = AppLockService.class.getPackage().getName();
    private static final String LOG_TAG = AppLockService.class.getSimpleName();

    public static final String ACTION_CLEAR_PREVIOUS_PACKAGE = PACKAGE_NAME + ".CLEAR_PREVIOUS_PACKAGE";
    public static final String ACTION_START_SERVICE = PACKAGE_NAME + ".START_SERVICE";
    public static final String ACTION_STOP_SERVICE = PACKAGE_NAME + ".STOP_SERVICE";
    public static final String ACTION_START_APP_LOCK_OVERLAY = PACKAGE_NAME + ".START_APP_LOCK_OVERLAY";
    public static final String ACTION_CLEAR_APP_LOCK_OVERLAY = PACKAGE_NAME + ".CLEAR_APP_LOCK_OVERLAY";

    private String previousPackageName = "";

    private OnForegroundAppChangedListener mOnForegroundAppChangedListener;

    private AppLockThread mAppLockThread;

    private AppLockScreenOverlay mAppLockScreenOverlay;

    ActivityManager mActivityManager;

    public AppLockService() {
    }

    public static Intent getServiceIntent(Context context, final String action){
        Intent intent = new Intent(context, AppLockService.class);
        if(action != null && !action.isEmpty()) {
            intent.setAction(action);
        }
        return intent;
    }

    @Override
    public void onCreate() {
        startThread();
        mOnForegroundAppChangedListener = new AppLocker(this);
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mAppLockScreenOverlay = new AppLockScreenOverlay(this, windowManager);
        mActivityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            if (action != null && !action.isEmpty()) {
                if (action.equals(ACTION_CLEAR_PREVIOUS_PACKAGE)) {
                    previousPackageName = "";
                    mAppLockScreenOverlay.remove();
                } else if (action.equals(ACTION_START_SERVICE)) {
                    startThread();
                } else if (action.equals(ACTION_STOP_SERVICE)) {
                    stopThread();
                } else if (action.equals(ACTION_START_APP_LOCK_OVERLAY)) {
                    mAppLockScreenOverlay.execute();
                } else if (action.equals(ACTION_CLEAR_APP_LOCK_OVERLAY)) {
                    mAppLockScreenOverlay.remove();
                }
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

                String foregroundTaskPackageName, foregroundTaskActivityName;
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    RunningTaskInfo foregroundTaskInfo = mActivityManager.getRunningTasks(1).get(0);

                    foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
                    foregroundTaskActivityName = foregroundTaskInfo.topActivity.getShortClassName();
                } else {
                    ActivityManager manager = (ActivityManager)AppLockService.this.getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningAppProcessInfo> tasks = manager.getRunningAppProcesses();
                    foregroundTaskPackageName = tasks.get(0).processName;
                    foregroundTaskActivityName = "";
                }
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
