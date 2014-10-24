package com.pvsagar.smartlockscreen.frontend_helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.SmartLockScreenSettings;

/**
 * Created by aravind on 25/8/14.
 */
public class NotificationHelper {
    private static final String NOTIFICATION_TITLE = "Smart Lockscreen";
    public static Notification getAppNotification(Context context, String text){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setPriority(Notification.PRIORITY_MIN);
        Intent notificationIntent = new Intent(context, SmartLockScreenSettings.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(SmartLockScreenSettings.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setContentTitle(NOTIFICATION_TITLE);
        if(text == null || text.isEmpty()){
            text = "Service Started.";
        }
        notificationBuilder.setContentText(text);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
        return notificationBuilder.build();
    }
}
