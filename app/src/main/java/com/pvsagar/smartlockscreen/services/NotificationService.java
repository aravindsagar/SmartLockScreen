package com.pvsagar.smartlockscreen.services;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v7.widget.CardView;

import com.pvsagar.smartlockscreen.applogic_objects.LockScreenNotification;

import java.util.ArrayList;

/**
 * Created by PV on 10/7/2014.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationService extends NotificationListenerService{

    private static NotificationService notificationService = null;

    private static String LOG_TAG = NotificationService.class.getSimpleName();
    private static String PACKAGE_NAME = NotificationService.class.getPackage().getName();
    public static String EXTRAS_LOCK_SCREEN_NOTIFICATION = ".LockScreenNotification";
    public static String EXTRAS_LOCK_SCREEN_NOTIFICATION_ID = ".LockScreenNotification.id";
    public static ArrayList<StatusBarNotification> currentSBN = new ArrayList<StatusBarNotification>();
    public static ArrayList<LockScreenNotification> currentNotifications;
    public static ArrayList<LockScreenNotification> removedNotifications = new ArrayList<LockScreenNotification>();

    //Actions
    public static String ACTION_CANCEL_NOTIFICATION = PACKAGE_NAME + ".cancel_notification";
    public static String ACTION_GET_CURRENT_NOTIFICATION = PACKAGE_NAME + ".get_current_notification";
    public static String ACTION_GET_CURRENT_NOTIFICATION_CLEAR_PREVIOOUS = PACKAGE_NAME + ".get_current_notification_clear_previous";

    //Extras
    public static String EXTRAS_CANCEL_NOTIFICATION_KEY = PACKAGE_NAME + ".cancel_notification_key";
    public static String EXTRAS_CANCEL_NOTIFICATION_PACKAGE = PACKAGE_NAME + ".cancel_notification_package";
    public static String EXTRAS_CANCEL_NOTIFICATION_TAG = PACKAGE_NAME + ".cancel_notification_tag";
    public static String EXTRAS_CANCEL_NOTIFICATION_ID = PACKAGE_NAME + ".cancel_notification_id";
    public static String EXTRAS_CANCEL_NOTIFICATION_BUNDLE = PACKAGE_NAME + ".cancel_notification_bundle";


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action;
        if(intent != null) {
            action = intent.getAction();
            if (action != null && !action.isEmpty()) {
                if (action.equals(ACTION_CANCEL_NOTIFICATION)) {
                    //cancelNotification(key);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                            Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                        //Cancel notification with package and stuff
                        Bundle bundle = intent.getBundleExtra(EXTRAS_CANCEL_NOTIFICATION_BUNDLE);
                        String pkg = bundle.getString(EXTRAS_CANCEL_NOTIFICATION_PACKAGE);
                        String tag = bundle.getString(EXTRAS_CANCEL_NOTIFICATION_TAG);
                        int id = bundle.getInt(EXTRAS_CANCEL_NOTIFICATION_ID);
                        cancelNotification(pkg, tag, id);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        String key = (String) intent.getCharSequenceExtra(EXTRAS_CANCEL_NOTIFICATION_KEY);
//                        Log.d(LOG_TAG, key);
                    }
                } else if (action.equals(ACTION_GET_CURRENT_NOTIFICATION)) {
//                    Log.d(LOG_TAG,"Get current notifications");
                    StatusBarNotification[] sbns = getActiveNotifications();
                    if(sbns != null){
//                        Log.d(LOG_TAG,"Get current notifications not null");
                        boolean flag;
                        for (StatusBarNotification sbn : sbns) {
                            flag = false;
                            for (LockScreenNotification currentNotification : currentNotifications) {
                                if (currentNotification.getPackageName().equals(sbn.getPackageName()) &&
                                        currentNotification.getId() == sbn.getId()) {
                                    flag = true;
                                    break;
                                }
                            }
                            if(!flag) {
                                currentNotifications.add(new LockScreenNotification(sbn));
                                currentSBN.add(sbn);
                            }
                        }
                        Intent baseIntent = new Intent(this,BaseService.class);
                        baseIntent.setAction(BaseService.ACTION_NOTIFICATION_CHANGED);
                        startService(baseIntent);
                    }
                } else if(action.equals(ACTION_GET_CURRENT_NOTIFICATION_CLEAR_PREVIOOUS)) {
                    StatusBarNotification[] sbns = getActiveNotifications();
                    if (currentNotifications != null){
                        currentNotifications.clear();
                    }
                    if(currentSBN != null){
                        currentSBN.clear();
                    }
                    if(removedNotifications != null){
                        removedNotifications.clear();
                    }
                    if(sbns != null){
                        for (StatusBarNotification sbn : sbns) {
                            LockScreenNotification lsn = new LockScreenNotification(sbn);
                            currentNotifications.add(lsn);
                            currentSBN.add(sbn);
                        }
                        Intent baseIntent = new Intent(this,BaseService.class);
                        baseIntent.setAction(BaseService.ACTION_NOTIFICATION_CHANGED);
                        startService(baseIntent);
                    }
                }
            }
        }
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public static boolean isInstanceCreated(){
        return notificationService != null;
    }

    @Override
    public void onCreate() {
        notificationService = this;
        currentNotifications = new ArrayList<LockScreenNotification>();
        super.onCreate();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        LockScreenNotification lsn = new LockScreenNotification(sbn);
        CardView currentCardView = null;
        for(int i = 0; i < currentNotifications.size(); i++){
            if(currentNotifications.get(i).getPackageName().equals(sbn.getPackageName()) &&
                    currentNotifications.get(i).getId() == sbn.getId() ){
                //Log.d(LOG_TAG,"Removing for adding: \n"+NotificationListAdapter.currentNotifications.get(i).getNotification().toString());
                currentCardView = currentNotifications.get(i).getCardView();
                currentNotifications.remove(i);
                currentSBN.remove(i);
                break;
            }
        }
        if(currentCardView != null){
            lsn.setCardView(currentCardView);
        }
        currentNotifications.add(lsn);
        currentSBN.add(sbn);
        Intent intent = new Intent(this,BaseService.class);
        intent.setAction(BaseService.ACTION_NOTIFICATION_POSTED);
        startService(intent);

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //Log.d(LOG_TAG,"Removed:\n"+sbn.toString());
        //Log.d(LOG_TAG,"Extras: \n"+sbn.getNotification().extras.get("android.title"));

        int id = sbn.getId();
        for(int i = 0; i < currentNotifications.size(); i++){
            if(currentNotifications.get(i).getPackageName().equals(sbn.getPackageName()) &&
                    currentNotifications.get(i).getId() == id ){
                //Log.d(LOG_TAG,"Removing: \n"+NotificationListAdapter.currentNotifications.get(i).getNotification().toString());
                removedNotifications.add(currentNotifications.get(i));
                currentNotifications.remove(i);
                currentSBN.remove(i);
                break;
            }
        }
        Intent intent = new Intent(this,BaseService.class);
        intent.setAction(BaseService.ACTION_NOTIFICATION_REMOVED);
        startService(intent);
    }
}
