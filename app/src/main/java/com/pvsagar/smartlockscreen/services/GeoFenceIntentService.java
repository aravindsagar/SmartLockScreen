package com.pvsagar.smartlockscreen.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.pvsagar.smartlockscreen.applogic_objects.LocationEnvironmentVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread. This service is invoked whenever a geofence transition
 * happens, and handles the transition and takes the required actions
 *
 */
public class GeoFenceIntentService extends IntentService {
    private static final String LOG_TAG = GeoFenceIntentService.class.getSimpleName();

    private static final Semaphore manageCurrentGeofencesCriticalSection = new Semaphore(1);
    private static ArrayList<LocationEnvironmentVariable> currentGeofences =
            new ArrayList<LocationEnvironmentVariable>();

    public static Intent getIntent(Context context){
        return new Intent(context, GeoFenceIntentService.class);
    }

    public GeoFenceIntentService() {
        super("GeoFenceIntentService");
    }

    public static ArrayList<LocationEnvironmentVariable> getCurrentGeofences() {
        return currentGeofences;
    }

    /**
     * Handle the intent received by the service.
     * @param intent This will be a geofence transition intent.
     */
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntentCalled.");
        // First check for errors
        if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e("ReceiveTransitionsIntentService", "Location Services error: " +
                            Integer.toString(errorCode));

        /*
         * If there's no error, get the transition type and the IDs
         * of the geofence or geofences that triggered the transition
         */
        } else {
            manageCurrentGeofencesCriticalSection.acquireUninterruptibly();
            // Get the type of transition (entry or exit)
            int transitionType = LocationClient.getGeofenceTransition(intent);
            // Test that a valid transition was reported
            if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
                            || (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)) {
                List <Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);
                Location triggerLocation = LocationClient.getTriggeringLocation(intent);

                if(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER){
                    addToCurrentGeofences(triggerList);
                } else {
                    removeFromCurrentGeofences(triggerList);
                }
                String currentGeofenceNames = "Current Geofences:";
                for(LocationEnvironmentVariable v: currentGeofences)
                    currentGeofenceNames += v.getLocationName() + "; ";
                manageCurrentGeofencesCriticalSection.release();
                startService(BaseService.getServiceIntent(this, null,
                        BaseService.ACTION_DETECT_ENVIRONMENT));
            } else {
                // An invalid transition was reported
                Log.e("ReceiveTransitionsIntentService", "Geofence transition error: " +
                                Integer.toString(transitionType));
            }
        }
    }

    private void addToCurrentGeofences(List<Geofence> triggerList){
        for(Geofence geofence: triggerList){
            LocationEnvironmentVariable variable = LocationEnvironmentVariable.
                    getLocationEnvironmentVariableFromAndroidGeofence(this, geofence);
            if(!currentGeofences.contains(variable)){
                currentGeofences.add(variable);
            }
        }
    }

    private void removeFromCurrentGeofences(List<Geofence> triggerList){
        for (Geofence geofence: triggerList){
            LocationEnvironmentVariable variable = LocationEnvironmentVariable.
                    getLocationEnvironmentVariableFromAndroidGeofence(this, geofence);
            currentGeofences.remove(variable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
