package com.pvsagar.smartlockscreen.applogic;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pvsagar.smartlockscreen.applogic_objects.BluetoothEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.LocationEnvironmentVariable;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.receivers.BluetoothReceiver;
import com.pvsagar.smartlockscreen.receivers.WifiReceiver;
import com.pvsagar.smartlockscreen.services.GeoFenceIntentService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by aravind on 6/9/14.
 * Contains static functions related to detecting the current environment
 */
public class EnvironmentDetector {
    private static final String LOG_TAG = EnvironmentDetector.class.getSimpleName();

    /**
     * The implementation of EnvironmentDetectedCallback to be called when the environment
     * detection is complete
     */
    private EnvironmentDetectedCallback callback;

    /**
     * Semaphore to make sure that only 1 instance of the async task of updating the current
     * environment runs at a time. Async tasks can be run in parallel in Android versions 1.6 to <3.0,
     * but are sequential in other Android versions.
     */
    private static final Semaphore manageEnvironmentDetectionCriticalSection = new Semaphore(1);

    /**
     * Detects the current environment based on the current values of the environment variables.
     * @param context Activity/service context
     *
     */
    public void detectCurrentEnvironment(Context context, EnvironmentDetectedCallback callback){
        Utility.checkForNullAndThrowException(context);
        Utility.checkForNullAndThrowException(callback);
        this.callback = callback;
        new EnvironmentDetectorAsyncTask().execute(context);
    }

    /**
     * This class runs the actual environment detection task in a separate thread. Used internally
     * by EnvironmentDetector class
     */
    private class EnvironmentDetectorAsyncTask extends AsyncTask<Context, Void, Environment>{

        /**
         * The function which is run in background. Checks for the stored environment which matches
         * the current variable values
         * @param params Activity/service context
         * @return The detected environment
         */
        @Override
        protected Environment doInBackground(Context... params) {
            if(params.length == 0 || params[0] == null){
                throw new IllegalArgumentException
                        ("The current activity/service context should be passed as argument.");
            }
            Context context = params[0];

            String logText = "Current Location: ";
            if(GeoFenceIntentService.getCurrentGeofences() != null && !GeoFenceIntentService.
                    getCurrentGeofences().isEmpty()){
                for (LocationEnvironmentVariable variable : GeoFenceIntentService.getCurrentGeofences()) {
                    logText += variable.getLocationName() + ", ";
                }
            } else {
                logText += "Unknown; ";
            }
            logText += "Current Bluetooth Devices: ";
            if(BluetoothReceiver.getCurrentlyConnectedBluetoothDevices() != null &&
                    !BluetoothReceiver.getCurrentlyConnectedBluetoothDevices().isEmpty()){
                for (BluetoothEnvironmentVariable variable : BluetoothReceiver.
                        getCurrentlyConnectedBluetoothDevices()) {
                    logText += variable.getDeviceName() + ", ";
                }
            } else {
                logText += "Unknown; ";
            }
            logText += "Current Wifi Network: ";
            if(WifiReceiver.getCurrentWifiNetwork() != null){
                logText += WifiReceiver.getCurrentWifiNetwork().getSSID();
            } else {
                logText += "Unknown";
            }
            Log.d(LOG_TAG, logText);

            List<Environment> currentEnvironments = new ArrayList<Environment>();
            manageEnvironmentDetectionCriticalSection.acquireUninterruptibly();
            for(LocationEnvironmentVariable location: GeoFenceIntentService.getCurrentGeofences()){
                List<Environment> potentialEnvironments = Environment.
                        getAllEnvironmentBarebonesForLocation(context, location);
                if(potentialEnvironments != null && !potentialEnvironments.isEmpty()) {
                    currentEnvironments.addAll(checkWifiAndBluetoothOfPotentialEnvironments(context,
                            potentialEnvironments));
                }
            }
            //Now checking for environments without geofence
            List<Environment> potentialEnvironments = Environment.
                    getAllEnvironmentBarebonesWithoutLocation(context);
            if(potentialEnvironments != null && !potentialEnvironments.isEmpty()) {
                currentEnvironments.addAll(checkWifiAndBluetoothOfPotentialEnvironments(context,
                        potentialEnvironments));
            }
            manageEnvironmentDetectionCriticalSection.release();
            if(currentEnvironments.size() > 1){
                Log.w(LOG_TAG, "Environment Conflict, " + currentEnvironments.toString());
            }
            if(currentEnvironments.size() == 0) {
                Log.v(LOG_TAG, "No stored environment matched current environment.");
                return null;
            }
            return currentEnvironments.get(0);
        }

        /**
         * This function is executed in the UI thread when doInBackground is finished. It calls the
         * call back function passed, and passes the detected environment to it
         * @param environment Result of doInBackground
         */
        @Override
        protected void onPostExecute(Environment environment) {
            callback.onEnvironmentDetected(environment);
            super.onPostExecute(environment);
        }

        /**
         * Checks whether the passed environments match the current bluetooth and wifi variables
         * @param context Activity/service context
         * @param potentialEnvironments The environments for which matching is to be done
         * @return the list of environments whose Bluetooth/Wifi variables match the current variables
         */
        private List<Environment> checkWifiAndBluetoothOfPotentialEnvironments
                (Context context, List<Environment> potentialEnvironments){
            ArrayList<Environment> currentEnvironments = new ArrayList<Environment>();
            for(Environment e: potentialEnvironments){
                Environment environment = Environment.getFullEnvironment(context, e.getName());
                if(Utility.checkForNullAndWarn(environment, LOG_TAG)){
                    continue;
                }
                if(environment.hasBluetoothDevices &&
                        environment.getBluetoothEnvironmentVariables() != null){
                    if(environment.isBluetoothAllOrAny()){
                        if(!checkForAllBluetoothDevices(
                                environment.getBluetoothEnvironmentVariables())){
                            continue;
                        }
                    } else {
                        if(!checkForAnyBluetoothDevices(
                                environment.getBluetoothEnvironmentVariables())){
                            continue;
                        }
                    }
                }
                if(environment.hasWiFiNetwork && environment.getWiFiEnvironmentVariable() != null){
                    if(WifiReceiver.getCurrentWifiNetwork() == null ||
                            !WifiReceiver.getCurrentWifiNetwork().
                                    equals(environment.getWiFiEnvironmentVariable())){
                        continue;
                    }
                }
                Log.d(LOG_TAG, "Adding environment to currentEnvironments: " + e.getName());
                currentEnvironments.add(e);
            }
            return currentEnvironments;
        }

        private boolean checkForAllBluetoothDevices(List<BluetoothEnvironmentVariable> variables){
            if(Utility.checkForNullAndWarn(BluetoothReceiver.getCurrentlyConnectedBluetoothDevices()
                    , LOG_TAG)){
                return false;
            }
            for (BluetoothEnvironmentVariable variable : variables) {
                if(Utility.checkForNullAndWarn(variable, LOG_TAG)){
                    continue;
                }
                if(!BluetoothReceiver.getCurrentlyConnectedBluetoothDevices().contains(variable)){
                    return false;
                }
            }
            return true;
        }

        private boolean checkForAnyBluetoothDevices(List<BluetoothEnvironmentVariable> variables){
            if(Utility.checkForNullAndWarn(BluetoothReceiver.getCurrentlyConnectedBluetoothDevices()
                    , LOG_TAG)){
                return false;
            }
            for (BluetoothEnvironmentVariable variable : variables) {
                if(Utility.checkForNullAndWarn(variable, LOG_TAG)){
                    continue;
                }
                if(BluetoothReceiver.getCurrentlyConnectedBluetoothDevices().contains(variable)){
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Interface which should be extended to receive callback when environment detection background
     * task is over. The implementation registered via EnvironmentDetector.detectCurrentEnvironment
     * will receive the current environment detected; it can take further actions based on that.
     */
    public interface EnvironmentDetectedCallback{
        public abstract void onEnvironmentDetected(Environment current);
    }
}
