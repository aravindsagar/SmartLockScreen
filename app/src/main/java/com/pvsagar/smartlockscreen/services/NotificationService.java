package com.pvsagar.smartlockscreen.services;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.pvsagar.smartlockscreen.adapters.NotificationListAdapter;
import com.pvsagar.smartlockscreen.applogic_objects.LockScreenNotification;

import java.util.ArrayList;

/**
 * Created by PV on 10/7/2014.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationService extends NotificationListenerService{

    private static String LOG_TAG = NotificationService.class.getSimpleName();
    public static String EXTRAS_LOCK_SCREEN_NOTIFICATION = ".LockScreenNotification";
    public static String EXTRAS_LOCK_SCREEN_NOTIFICATION_ID = ".LockScreenNotification.id";
    public static ArrayList<StatusBarNotification> currentSBN = new ArrayList<StatusBarNotification>();
    public static ArrayList<LockScreenNotification> currentNotifications =
            new ArrayList<LockScreenNotification>();

    @Override
    public void onCreate() {
        currentNotifications = new ArrayList<LockScreenNotification>();
        super.onCreate();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(LOG_TAG,"Added:\n"+sbn.toString());
        //Log.d(LOG_TAG,"Extras: \n"+sbn.getNotification().extras.get(KEY_NOTIFICATION_TITLE));
        /*LockScreenNotification lsn = new LockScreenNotification(sbn.getId(),sbn.getNotification(),
                sbn.getPackageName(), sbn.isClearable(), sbn.getTag()); */
        LockScreenNotification lsn = new LockScreenNotification(sbn);
        currentSBN.add(sbn);

        //
        for(int i = 0; i < currentNotifications.size(); i++){
            if(currentNotifications.get(i).getPackageName().equals(sbn.getPackageName()) &&
                    currentNotifications.get(i).getId() == sbn.getId() ){
                //Log.d(LOG_TAG,"Removing for adding: \n"+NotificationListAdapter.currentNotifications.get(i).getNotification().toString());
                //currentNotifications.remove(i);
                NotificationListAdapter.deleteItem(i);
                currentSBN.remove(i);
                break;
            }
        }
        //NotificationListAdapter.currentNotifications.add(lsn);
        NotificationListAdapter.addItem(lsn);
        Intent intent = new Intent(this,BaseService.class);
        intent.setAction(BaseService.ACTION_NOTIFICATION_CHANGED);
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
                //NotificationListAdapter.currentNotifications.remove(i);
                NotificationListAdapter.deleteItem(i);
                currentSBN.remove(i);
                break;
            }
        }
        Intent intent = new Intent(this,BaseService.class);
        intent.setAction(BaseService.ACTION_NOTIFICATION_CHANGED);
        startService(intent);
    }
}
