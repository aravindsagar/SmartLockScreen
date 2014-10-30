package com.pvsagar.smartlockscreen.frontend_helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

/**
 * Created by PV on 10/25/2014.
 */
public class ExternalIntents {

    public static void launchDialer(Context context){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e2){
            Toast.makeText(context, "Cannot start dialer.", Toast.LENGTH_SHORT).show();
            e2.printStackTrace();
        }

    }

    public static void launchCamera(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                context.startActivity(getSecureCameraIntent());
            } catch (Exception e){
                try {
                    context.startActivity(getNormalCameraIntent());
                } catch (Exception e2){
                    Toast.makeText(context, "Cannot start camera.", Toast.LENGTH_SHORT).show();
                    e2.printStackTrace();
                }
            }
        } else {
            try {
                context.startActivity(getNormalCameraIntent());
            } catch (Exception e2){
                Toast.makeText(context, "Cannot start camera.", Toast.LENGTH_SHORT).show();
                e2.printStackTrace();
            }
        }
    }

    private static Intent getSecureCameraIntent(){
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
    private static Intent getNormalCameraIntent(){
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

}
