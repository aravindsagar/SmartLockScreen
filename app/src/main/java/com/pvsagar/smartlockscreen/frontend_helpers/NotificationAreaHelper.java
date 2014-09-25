package com.pvsagar.smartlockscreen.frontend_helpers;

import android.content.Context;
import android.os.Build;

import java.lang.reflect.Method;

/**
 * Created by aravind on 24/9/14.
 */
public class NotificationAreaHelper {
    public static void expand(Context context){
        try {
            //noinspection ResourceType
            Object sbservice = context.getSystemService("statusbar");
            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            Method showsb;
            if (Build.VERSION.SDK_INT >= 17) {
                showsb = statusbarManager.getMethod("expandNotificationsPanel");
            } else {
                showsb = statusbarManager.getMethod("expand");
            }
            showsb.invoke(sbservice);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
