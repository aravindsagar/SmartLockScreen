package com.pvsagar.smartlockscreen.applogic_objects;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.pvsagar.smartlockscreen.services.NotificationService;

/**
 * Created by PV on 10/7/2014.
 */
public class LockScreenNotification {

    private int notification_id;
    private Notification mNotification;
    private String packageName;
    private String tag;
    private boolean isClearable;
    private String key;

    private static String LOG_TAG = LockScreenNotification.class.getSimpleName();


    public LockScreenNotification(int notification_id, Notification mNotification,String packageName, boolean isClearable, String tag, String key){
        this.notification_id = notification_id;
        this.mNotification = mNotification;
        this.packageName = packageName;
        this.isClearable = isClearable;
        this.tag = tag;
        this.key = key;
    }
    public LockScreenNotification(StatusBarNotification sbn){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            this.notification_id = sbn.getId();
            this.mNotification = sbn.getNotification();
            this.packageName = sbn.getPackageName();
            this.isClearable = sbn.isClearable();
            this.tag = sbn.getTag();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                this.key = sbn.getKey();
            }
        }
    }

    public String getKey(){
        return this.key;
    }

    public int getId(){
        return notification_id;
    }

    public Notification getNotification(){
        return mNotification;
    }

    public String getPackageName(){
        return packageName;
    }

    public boolean isClearable(){
        return isClearable;
    }

    public String getTag(){
        return tag;
    }

    public void dismiss(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            /*for(int i=0; i< NotificationService.currentNotifications.size(); i++){
                if(this.packageName.equals(NotificationService.currentNotifications.get(i)) &&
                        this.notification_id == NotificationService.currentNotifications.get(i).getId()){
                    
                }
            }*/
            Intent intent = new Intent(context, NotificationService.class);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.putExtra(NotificationService.EXTRAS_CANCEL_NOTIFICATION_KEY, getKey());
//                Log.d(LOG_TAG,getKey());
            } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
                Bundle bundle = new Bundle();
                bundle.putString(NotificationService.EXTRAS_CANCEL_NOTIFICATION_PACKAGE,packageName);
                bundle.putString(NotificationService.EXTRAS_CANCEL_NOTIFICATION_TAG,tag);
                bundle.putInt(NotificationService.EXTRAS_CANCEL_NOTIFICATION_ID,notification_id);
                intent.putExtra(NotificationService.EXTRAS_CANCEL_NOTIFICATION_BUNDLE,bundle);
            }
            intent.setAction(NotificationService.ACTION_CANCEL_NOTIFICATION);
            Log.d(LOG_TAG,"Starting Service");
            context.startService(intent);
        }
    }
}
