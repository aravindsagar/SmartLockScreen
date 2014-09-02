package com.pvsagar.smartlockscreen.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 */
public class GeoFenceIntentService extends IntentService {
    private static final String LOG_TAG = GeoFenceIntentService.class.getSimpleName();

    public static Intent getIntent(Context context){
        Intent intent = new Intent(context, GeoFenceIntentService.class);
        return intent;
    }

    public GeoFenceIntentService() {
        super("GeoFenceIntentService");
    }


    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntentCalled.");
        // First check for errors
        if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e("ReceiveTransitionsIntentService",
                    "Location Services error: " +
                            Integer.toString(errorCode));

        /*
         * If there's no error, get the transition type and the IDs
         * of the geofence or geofences that triggered the transition
         */
        } else {
            // Get the type of transition (entry or exit)
            int transitionType =
                    LocationClient.getGeofenceTransition(intent);
            // Test that a valid transition was reported
            if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
                            || (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)) {
                List <Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);
                Location triggerLocation = LocationClient.getTriggeringLocation(intent);
                Intent intentToBaseService = new Intent(this, BaseService.class);
                intentToBaseService.setData(Uri.parse("Geofence transition at " +
                        triggerLocation.getLatitude() + ", " + triggerLocation.getLongitude()));
                startService(intentToBaseService);
            } else {
                // An invalid transition was reported
                Log.e("ReceiveTransitionsIntentService",
                        "Geofence transition error: " +
                                Integer.toString(transitionType));
            }
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
