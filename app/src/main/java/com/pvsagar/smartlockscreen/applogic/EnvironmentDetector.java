package com.pvsagar.smartlockscreen.applogic;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pvsagar.smartlockscreen.applogic_objects.BluetoothEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.LocationEnvironmentVariable;
import com.pvsagar.smartlockscreen.receivers.BluetoothReceiver;
import com.pvsagar.smartlockscreen.receivers.WifiReceiver;
import com.pvsagar.smartlockscreen.services.GeoFenceIntentService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

/**
 * Created by aravind on 6/9/14.
 * Contains static functions related to detecting the current environment
 */
public class EnvironmentDetector {
    private static final String LOG_TAG = EnvironmentDetector.class.getSimpleName();
    private static final Semaphore manageEnvironmentDetectionCriticalSection = new Semaphore(1);

    public static Environment detectCurrentEnvironment(Context context){
        try {
            return (new EnvironmentDetectorAsyncTask()).execute(context).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class EnvironmentDetectorAsyncTask extends AsyncTask<Context, Void, Environment>{

        @Override
        protected Environment doInBackground(Context... params) {
            if(params.length == 0 || params[0] == null){
                throw new IllegalArgumentException
                        ("The current activity/service context should be passed as argument.");
            }
            Context context = params[0];
            List<Environment> currentEnvironments = new ArrayList<Environment>();
            manageEnvironmentDetectionCriticalSection.acquireUninterruptibly();
            for(LocationEnvironmentVariable location: GeoFenceIntentService.getCurrentGeofences()){
                List<Environment> potentialEnvironments = Environment.
                        getAllEnvironmentBarebonesForLocation(context, location);
                currentEnvironments.addAll(checkWifiAndBluetoothOfPotentialEnvironments(context,
                        potentialEnvironments));
                Log.d(LOG_TAG, potentialEnvironments.size() + " potential environments.");

            }
            //Now checking for environments without geofence
            List<Environment> potentialEnvironments = Environment.
                    getAllnvironmentBarebonesWithoutLocation(context);
            currentEnvironments.addAll(checkWifiAndBluetoothOfPotentialEnvironments(context,
                    potentialEnvironments));
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

        private List<Environment> checkWifiAndBluetoothOfPotentialEnvironments
                (Context context, List<Environment> potentialEnvironments){
            ArrayList<Environment> currentEnvironments = new ArrayList<Environment>();
            for(Environment e: potentialEnvironments){
                Environment environment = Environment.getFullEnvironment(context, e.getName());
                if(environment.hasBluetoothDevices &&
                        environment.getBluetoothEnvironmentVariables() != null){
                    if(environment.isBluetoothAllOrAny()){
                        if(!checkForAllBluetoothDevices(
                                environment.getBluetoothEnvironmentVariables())){
                            Log.d(LOG_TAG, "All Bluetooth match didn't happen for " + e.getName());
                            continue;
                        }
                    } else {
                        if(!checkForAnyBluetoothDevices(
                                environment.getBluetoothEnvironmentVariables())){
                            Log.d(LOG_TAG, "Any Bluetooth match didn't happen for " + e.getName());
                            continue;
                        }
                    }
                }
                if(environment.hasWiFiNetwork){
                    if(WifiReceiver.getCurrentWifiNetwork() == null ||
                            !WifiReceiver.getCurrentWifiNetwork().
                                    equals(environment.getWiFiEnvironmentVariable())){
                        Log.d(LOG_TAG, "Wifi matching didn't happen for " + e.getName());
                        if(WifiReceiver.getCurrentWifiNetwork() != null)
                            Log.d(LOG_TAG,
                                    "Current: " + WifiReceiver.getCurrentWifiNetwork().getSSID() +
                                            ":" + WifiReceiver.getCurrentWifiNetwork().getEncryptionType());
                        Log.d(LOG_TAG, "Needed: " + environment.getWiFiEnvironmentVariable().getSSID() + ":"
                                + environment.getWiFiEnvironmentVariable().getEncryptionType());

                        continue;
                    }
                }
                Log.d(LOG_TAG, "Adding environment to currentEnvironments: " + e.getName());
                currentEnvironments.add(e);
            }
            return currentEnvironments;
        }

        private boolean checkForAllBluetoothDevices(List<BluetoothEnvironmentVariable> variables){
            for (BluetoothEnvironmentVariable variable : variables) {
                if(!BluetoothReceiver.getCurrentlyConnectedBluetoothDevices().contains(variable)){
                    return false;
                }
            }
            return true;
        }

        private boolean checkForAnyBluetoothDevices(List<BluetoothEnvironmentVariable> variables){
            for (BluetoothEnvironmentVariable variable : variables) {
                Log.d(LOG_TAG, "Checking for variable " + variable.getDeviceName() + ":" + variable.getDeviceAddress());
                if(BluetoothReceiver.getCurrentlyConnectedBluetoothDevices().contains(variable)){
                    return true;
                }
            }
            String logMessage = "Currently connected devices: ";
            for (BluetoothEnvironmentVariable variable : BluetoothReceiver.getCurrentlyConnectedBluetoothDevices()) {
                logMessage += variable.getDeviceAddress() + ":" + variable.getDeviceName() + "; ";
            }
            Log.d(LOG_TAG, logMessage);

            return false;
        }
    }
}
