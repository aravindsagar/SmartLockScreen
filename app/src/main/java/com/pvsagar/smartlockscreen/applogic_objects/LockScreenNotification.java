package com.pvsagar.smartlockscreen.applogic_objects;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

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


    public LockScreenNotification(int notification_id, Notification mNotification,String packageName, boolean isClearable, String tag){
        this.notification_id = notification_id;
        this.mNotification = mNotification;
        this.packageName = packageName;
        this.isClearable = isClearable;
        this.tag = tag;
    }
    public LockScreenNotification(StatusBarNotification sbn){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            this.notification_id = sbn.getId();
            this.mNotification = sbn.getNotification();
            this.packageName = sbn.getPackageName();
            this.isClearable = sbn.isClearable();
            this.tag = sbn.getTag();
        }
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
            for(int i=0; i< NotificationService.currentNotifications.size(); i++){
                if(this.packageName.equals(NotificationService.currentNotifications.get(i)) &&
                        this.notification_id == NotificationService.currentNotifications.get(i).getId()){
                    
                }
            }
        }
    }
}
