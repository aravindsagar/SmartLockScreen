package com.pvsagar.smartlockscreen.frontend_helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.pvsagar.smartlockscreen.LockScreenActivity;
import com.pvsagar.smartlockscreen.R;

/**
 * Created by aravind on 25/8/14.
 */
public class NotificationHelper {
    private static final String NOTIFICATION_TITLE = "Smart Lockscreen";
    public static Notification getAppNotification(Context context, String text){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setPriority(Notification.PRIORITY_MIN);
        Intent notificationIntent = new Intent(context, LockScreenActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setContentTitle(NOTIFICATION_TITLE);
        if(text == null || text.isEmpty()){
            text = "Service Started.";
        }
        notificationBuilder.setContentText(text);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
        Notification notification = notificationBuilder.build();
        return notification;
    }
}
