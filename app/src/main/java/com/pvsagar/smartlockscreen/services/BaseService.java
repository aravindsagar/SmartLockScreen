package com.pvsagar.smartlockscreen.services;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.pvsagar.smartlockscreen.applogic.EnvironmentDetector;
import com.pvsagar.smartlockscreen.applogic_objects.BluetoothEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.LocationEnvironmentVariable;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDbHelper;
import com.pvsagar.smartlockscreen.frontend_helpers.NotificationHelper;
import com.pvsagar.smartlockscreen.receivers.BluetoothReceiver;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class BaseService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        OnAddGeofencesResultListener{
    private static final String LOG_TAG = BaseService.class.getSimpleName();

    public static final int ONGOING_NOTIFICATION_ID = 1;
    public static final int GEOFENCE_SERVICE_REQUEST_CODE = 2;

    private static final String PACKAGE_NAME = "com.pvsagar.smartlockscreen.services";
    public static final String ACTION_DETECT_ENVIRONMENT = PACKAGE_NAME + ".DETECT_ENVIRONMENT";
    public static final String ACTION_ADD_GEOFENCES = PACKAGE_NAME + ".ADD_GEOFENCES";

    private LocationClient mLocationClient;
    // Defines the allowable request types.
    public enum REQUEST_TYPE {ADD_GEOFENCES}
    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    public static Intent getServiceIntent(Context context, String extraText, String action){
        Intent serviceIntent  = new Intent();
        serviceIntent.setClass(context, com.pvsagar.smartlockscreen.services.BaseService.class);
        if(extraText != null && !extraText.isEmpty()){
            serviceIntent.setData(Uri.parse(extraText));
        }
        serviceIntent.setAction(action);
        return serviceIntent;
    }

    public BaseService() {
    }

    @Override
    public void onCreate() {
        EnvironmentDbHelper.insertDefaultUser(this);
        startForeground(ONGOING_NOTIFICATION_ID, NotificationHelper.getAppNotification(this, null));

        mInProgress = false;
        mLocationClient = new LocationClient(this, this, this);
        requestAddGeofences();
        new BluetoothDeviceSearch().execute();
        //TODO: populate current wifi network in WifiReceiver
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action;
        if(intent != null) {
            Uri uri = intent.getData();

            if (uri != null) {
                Log.d(LOG_TAG, "Uri: " + uri);
                startForeground(ONGOING_NOTIFICATION_ID, NotificationHelper.getAppNotification(this,
                        uri.toString()));
            }
            action = intent.getAction();
            if(action != null && !action.isEmpty()) {
                if (action.equals(ACTION_DETECT_ENVIRONMENT)) {
                    Environment current = EnvironmentDetector.detectCurrentEnvironment(this);
                    if (current == null) {
                        startForeground(ONGOING_NOTIFICATION_ID, NotificationHelper.getAppNotification(this,
                                "Unknown Environment"));
                    } else {
                        startForeground(ONGOING_NOTIFICATION_ID, NotificationHelper.getAppNotification(this,
                                "Environment: " + current.getName()));
                    }
                } else if(action.equals(ACTION_ADD_GEOFENCES)) {
                    requestAddGeofences();
                }
                //Additional action handling to be done here when more actions are added
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        switch (mRequestType){
            case ADD_GEOFENCES:
                PendingIntent geofenceIntent = PendingIntent.getService(this,
                        GEOFENCE_SERVICE_REQUEST_CODE, GeoFenceIntentService.getIntent(this), 0);
                List<Geofence> geofenceList = LocationEnvironmentVariable.getAndroidGeofences(this);
                if(geofenceList != null && !geofenceList.isEmpty()) {
                    mLocationClient.addGeofences(geofenceList, geofenceIntent, this);
                }
        }
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {
        Toast.makeText(this, "Geofence added: " + strings[0], Toast.LENGTH_SHORT).show();
        mInProgress = false;
        mLocationClient.disconnect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void requestAddGeofences(){
        mRequestType = REQUEST_TYPE.ADD_GEOFENCES;
        if(!mInProgress){
            mInProgress = true;
            mLocationClient.connect();
        } else {
            Log.w(LOG_TAG, "A request already in progress.");
        }
    }

    private class BluetoothDeviceSearch extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(mBluetoothAdapter.isEnabled()){
                Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
                for(BluetoothDevice device: bondedDevices){
                    try {
                        Method method = device.getClass().getMethod("getUuids"); /// get all services
                        ParcelUuid[] parcelUuids = (ParcelUuid[]) method.invoke(device); /// get all services

                        BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord
                                (parcelUuids[0].getUuid()); ///pick one at random

                        socket.connect();
                        socket.close();
                        BluetoothReceiver.addBluetoothDeviceToConnectedDevices(new
                                BluetoothEnvironmentVariable(device.getName(), device.getAddress()));
                        Log.d(LOG_TAG, device.getName() + " added.");
                    } catch (Exception e) {
                        Log.d("BluetoothPlugin", device.getName() + "Device is not in range");
                    }
                }
            }
            return null;
        }
    }
}
