package com.pvsagar.smartlockscreen.frontend_helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import java.util.List;

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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                isIntentAvailable(context, MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE)) {
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

    /**
     * Indicates whether the specified action can be used as an intent. This
     * method queries the package manager for installed packages that can
     * respond to an intent with the specified action. If no suitable package is
     * found, this method returns false.
     *
     * @param context The application's environment.
     * @param action The Intent action to check for availability.
     *
     * @return True if an Intent with the specified action can be sent and
     *         responded to, false otherwise.
     */
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
