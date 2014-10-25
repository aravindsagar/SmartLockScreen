package com.pvsagar.smartlockscreen.frontend_helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.MediaStore;

/**
 * Created by PV on 10/25/2014.
 */
public class ExternalIntents {

    public static void launchDialer(Context context){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void launchCamera(Context context){
        Intent intent;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
        } else {
            intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
