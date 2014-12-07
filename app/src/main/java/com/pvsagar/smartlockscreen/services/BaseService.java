package com.pvsagar.smartlockscreen.services;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;
import com.pvsagar.smartlockscreen.DismissKeyguardActivity;
import com.pvsagar.smartlockscreen.applogic.EnvironmentDetector;
import com.pvsagar.smartlockscreen.applogic_objects.environment_variables.BluetoothEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.environment_variables.LocationEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.applogic_objects.environment_variables.WiFiEnvironmentVariable;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.backend_helpers.WakeLockHelper;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.frontend_helpers.NotificationHelper;
import com.pvsagar.smartlockscreen.frontend_helpers.WallpaperHelper;
import com.pvsagar.smartlockscreen.receivers.AdminActions;
import com.pvsagar.smartlockscreen.receivers.BluetoothReceiver;
import com.pvsagar.smartlockscreen.receivers.PhoneStateReceiver;
import com.pvsagar.smartlockscreen.receivers.ScreenReceiver;
import com.pvsagar.smartlockscreen.receivers.WifiReceiver;
import com.pvsagar.smartlockscreen.services.window_helpers.LockScreenOverlayHelper;
import com.pvsagar.smartlockscreen.services.window_helpers.PatternLockOverlay;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The main service which acts as the app backbone. This service should be notified of all
 * the relevant events, and will take required actions related to the events
 */
public class BaseService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        OnAddGeofencesResultListener,
        OnRemoveGeofencesResultListener,
        EnvironmentDetector.EnvironmentDetectedCallback{
    private static final String LOG_TAG = BaseService.class.getSimpleName();

    public static final int ONGOING_NOTIFICATION_ID = 1;
    public static final int GEOFENCE_SERVICE_REQUEST_CODE = 2;

    private static final String PACKAGE_NAME = BaseService.class.getPackage().getName();
    public static final String ACTION_DETECT_ENVIRONMENT = PACKAGE_NAME + ".DETECT_ENVIRONMENT";
    public static final String ACTION_DETECT_ENVIRONMENT_SWITCH_USER = PACKAGE_NAME + ".DETECT_ENVIRONMENT_SWITCH_USER";
    public static final String ACTION_ADD_GEOFENCES = PACKAGE_NAME + ".ADD_GEOFENCES";
    public static final String ACTION_REMOVE_GEOFENCES = PACKAGE_NAME + ".REMOVE_GEOFENCES";
    public static final String ACTION_DETECT_WIFI = PACKAGE_NAME + ".DETECT_WIFI";
    public static final String ACTION_DETECT_BLUETOOTH = PACKAGE_NAME + ".DETECT_BLUETOOTH";
    public static final String ACTION_START_LOCKSCREEN_OVERLAY = PACKAGE_NAME + ".START_LOCKSCREEN_OVERLAY";
    public static final String ACTION_START_PATTERN_OVERLAY = PACKAGE_NAME + ".START_PATTERN_OVERLAY";
    public static final String ACTION_DISMISS_LOCKSCREEN_OVERLAY = PACKAGE_NAME + ".DISMISS_LOCKSCREEN_OVERLAY";
    public static final String ACTION_DISMISS_PATTERN_OVERLAY = PACKAGE_NAME + ".DISMISS_PATTERN_OVERLAY";
    public static final String ACTION_DISMISS_PATTERN_OVERLAY_ONLY = PACKAGE_NAME + ".DISMISS_PATTERN_OVERLAY_ONLY";
    public static final String ACTION_UNLOCK = PACKAGE_NAME + ".UNLOCK";
    public static final String ACTION_NOTIFICATION_CHANGED = PACKAGE_NAME + ".NOTIFICATION_CHANGED";
    public static final String ACTION_NOTIFICATION_POSTED = PACKAGE_NAME + ".NOTIFICATION_POSTED";
    public static final String ACTION_NOTIFICATION_REMOVED = PACKAGE_NAME + ".NOTIFICATION_REMOVED";
    public static final String ACTION_REMOVE_PERSISTENT_NOTIFICATION = PACKAGE_NAME + ".REMOVE_PERSISTENT_NOTIFICATION";

    public static final String EXTRA_GEOFENCE_IDS_TO_REMOVE = PACKAGE_NAME + ".EXTRA_GEOFENCE_IDS_TO_REMOVE";

    private static List<Environment> currentEnvironments;

    private LocationClient mLocationClient;

    public static List<Environment> getCurrentEnvironments() {
        return currentEnvironments;
    }

    // Defines the allowable request types.
    public enum REQUEST_TYPE {ADD_GEOFENCES, REMOVE_GEOFENCES}
    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    //List of geofence ids to delete when calling removeGeofences
    private List<String> mGeofencesToRemove;

    private LockScreenOverlayHelper mLockScreenOverlayHelper;

    private PatternLockOverlay mPatternLockOverlay;

    private boolean mIsInCall = false, switchUser = false;

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
        User.setCurrentUser(User.getDefaultUser(this));
        if(SharedPreferencesHelper.isNotificationEnabled(this)) {
            startForeground(ONGOING_NOTIFICATION_ID, NotificationHelper.getAppNotification(this, null));
        }
        AdminActions.initializeAdminObjects(this);
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            startService(new Intent(this, AppLockService.class));
        }
        mInProgress = false;
        mLocationClient = new LocationClient(this, this, this);
        requestAddGeofences();
        new DetermineConnectedWifiNetwork().execute();
        new BluetoothDeviceSearch().execute();
        ScreenReceiver.registerScreenReceiver(this);
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLockScreenOverlayHelper = new LockScreenOverlayHelper(this, windowManager);
        mPatternLockOverlay = new PatternLockOverlay(this, windowManager);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            Intent intent = new Intent(this,NotificationService.class);
            startService(intent);
        }

        WallpaperHelper.onWallpaperChanged(this);

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
//                Log.d(LOG_TAG, "Uri: " + uri);
                startForeground(ONGOING_NOTIFICATION_ID, NotificationHelper.getAppNotification(this,
                        uri.toString()));
            }
            action = intent.getAction();
            if(action != null && !action.isEmpty()) {
                if (action.equals(ACTION_DETECT_ENVIRONMENT)) {
                    new EnvironmentDetector().detectCurrentEnvironment(this, this);
                } else if (action.equals(ACTION_DETECT_ENVIRONMENT_SWITCH_USER)){
                    switchUser = true;
                    new EnvironmentDetector().detectCurrentEnvironment(this, this);
                } else if(action.equals(ACTION_ADD_GEOFENCES)) {
                    requestAddGeofences();
                } else if(action.equals(ACTION_REMOVE_GEOFENCES)){
                    try {
                        mGeofencesToRemove = (List<String>) intent.
                                getSerializableExtra(EXTRA_GEOFENCE_IDS_TO_REMOVE);
                        if(mGeofencesToRemove == null || mGeofencesToRemove.isEmpty()){
                            throw new IllegalArgumentException();
                        }
                        requestRemoveGeofences();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new IllegalArgumentException("Intent should specify geofences to remove," +
                                "if action is ACTION_REMOVE_GEOFENCES.");
                    }
                } else if(action.equals(ACTION_DETECT_WIFI)){
                    new DetermineConnectedWifiNetwork().execute();
                } else if(action.equals(ACTION_DETECT_BLUETOOTH)){
                    new BluetoothDeviceSearch().execute();
                } else if(action.equals(ACTION_START_LOCKSCREEN_OVERLAY)){
                    mLockScreenOverlayHelper.remove();
                    mPatternLockOverlay.remove();
                    if(!intent.getBooleanExtra(PhoneStateReceiver.EXTRA_IS_IN_CALL, true)){
                        mIsInCall = false;
                    }
                    if(!mIsInCall){
                        mLockScreenOverlayHelper.execute();
                    }
                    WakeLockHelper.releaseWakeLock(ScreenReceiver.WAKE_LOCK_TAG);
                } else if(action.equals(ACTION_DISMISS_LOCKSCREEN_OVERLAY)){
                    mLockScreenOverlayHelper.remove();
                    if(intent.getBooleanExtra(PhoneStateReceiver.EXTRA_IS_IN_CALL, false)){
                        mIsInCall = true;
                    }
                } else if(action.equals(ACTION_START_PATTERN_OVERLAY)){
                    mPatternLockOverlay.execute();
                } else if(action.equals(ACTION_UNLOCK)){
                    EnvironmentDetector.manageEnvironmentDetectionCriticalSection.acquireUninterruptibly();
                    AdminActions.changePassword("", Passphrase.TYPE_NONE, null);
                    Intent dismissIntent = new Intent(this, DismissKeyguardActivity.class);
                    dismissIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    this.getApplicationContext().startActivity(dismissIntent);
                } else if(action.equals(ACTION_DISMISS_PATTERN_OVERLAY)){
                    mPatternLockOverlay.remove();
                    mLockScreenOverlayHelper.remove();
                    if(intent.getBooleanExtra(PhoneStateReceiver.EXTRA_IS_IN_CALL, false)){
                        mIsInCall = true;
                    }
                } else if(action.equals(ACTION_DISMISS_PATTERN_OVERLAY_ONLY)) {
                    mPatternLockOverlay.remove();
                    mLockScreenOverlayHelper.execute();
                } else if (action.equals(ACTION_NOTIFICATION_CHANGED)) {
                    // Add to the list view
                    //Bundle extras = intent.getExtras();
                    //LockScreenNotification lsn = (LockScreenNotification)
                    //        extras.getParcelable(NotificationService.EXTRAS_LOCK_SCREEN_NOTIFICATION);
                    mLockScreenOverlayHelper.initNotification();
                } else if(action.equals(ACTION_NOTIFICATION_POSTED)){
                    mLockScreenOverlayHelper.notificationPosted();
                } else if(action.equals(ACTION_NOTIFICATION_REMOVED)) {
                    mLockScreenOverlayHelper.notificationRemoved();
                } else if(action.equals(ACTION_REMOVE_PERSISTENT_NOTIFICATION)){
                    stopForeground(true);
                }
                //Additional action handling to be done here when more actions are added
            }
        }

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
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
                } else {
                    mInProgress = false;
                    mLocationClient.disconnect();
                }
                break;

            case REMOVE_GEOFENCES:
                mLocationClient.removeGeofences(mGeofencesToRemove, this);
        }
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {
        switch (i){
            case LocationStatusCodes.SUCCESS:
                String geofences = "";
                for (String string : strings) {
                    geofences += string + ",";
                }
                break;
            default:
                Log.e(LOG_TAG, "Error adding Geofences. Status code: " + i);
                Toast.makeText(this, "Error adding geofences.", Toast.LENGTH_SHORT).show();
        }

        mInProgress = false;
        mLocationClient.disconnect();

    }

    @Override
    public void onRemoveGeofencesByRequestIdsResult(int i, String[] strings) {
        switch (i){
            case LocationStatusCodes.SUCCESS:
                String geofences = "";
                for (String string : strings) {
                    geofences += string + ",";
                }
                break;
            default:
                Log.e(LOG_TAG, "Error removing Geofences. Status code: " + i);
                Toast.makeText(this, "Error removing geofences.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int i, PendingIntent pendingIntent) {

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
            startLocationRequestReset();
            requestAddGeofences();
        }
    }

    private void requestRemoveGeofences(){
        mRequestType = REQUEST_TYPE.REMOVE_GEOFENCES;
        if(!mInProgress){
            mInProgress = true;
            mLocationClient.connect();
        } else {
            Log.w(LOG_TAG, "A request already in progress.");
            startLocationRequestReset();
            requestRemoveGeofences();
        }
    }

    private WiFiEnvironmentVariable getConnectedWifiNetwork(){
        WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);

        if(wifiManager == null){
            Log.e(LOG_TAG,"WiFi Hardware not available");
            return null;
        }
        if(!wifiManager.isWifiEnabled()){
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if(wifiInfo != null) {
            List<WifiConfiguration> wifiConfigurations = WiFiEnvironmentVariable.getConfiguredWiFiConnections(this);
            for(WifiConfiguration configuration: wifiConfigurations){
                if(configuration.SSID.equals(wifiInfo.getSSID())) {
                    String wifiEncryptionType = WiFiEnvironmentVariable.getSecurity(configuration);
                    return new WiFiEnvironmentVariable(wifiInfo.getSSID(), wifiEncryptionType);
                    /*WiFiEnvironmentVariable.getWifiEnvironmentVariableFromDatabase(
                            this, wifiInfo.getSSID(), wifiEncryptionType);*/

                }
            }
        }
        return null;
    }

    private class BluetoothDeviceSearch extends AsyncTask<Void, Void, List<BluetoothEnvironmentVariable>>{
        @Override
        protected List<BluetoothEnvironmentVariable> doInBackground(Void... params) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(mBluetoothAdapter.isEnabled()){
                Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
                List<BluetoothEnvironmentVariable> connectedDevices = new ArrayList<BluetoothEnvironmentVariable>();
                for(BluetoothDevice device: bondedDevices){
                    try {
                        Method method = device.getClass().getMethod("getUuids"); /// get all services
                        ParcelUuid[] parcelUuids = (ParcelUuid[]) method.invoke(device); /// get all services

                        BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord
                                (parcelUuids[0].getUuid()); ///pick one at random

                        socket.connect();
                        socket.close();
                        connectedDevices.add(new BluetoothEnvironmentVariable(device.getName(),
                                device.getAddress()));
                        Log.d(LOG_TAG, device.getName() + " added.");
                    } catch (Exception e) {
                        Log.d("BluetoothPlugin", device.getName() + "Device is not in range");
                    }
                }
                return connectedDevices;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<BluetoothEnvironmentVariable> variables) {
            Log.d(LOG_TAG, "bluetooth device search over.");
            if(!Utility.checkForNullAndWarn(variables, LOG_TAG)) {
                for (BluetoothEnvironmentVariable device : variables) {
                    BluetoothReceiver.addBluetoothDeviceToConnectedDevices(device);
                }
            }
            startService(BaseService.getServiceIntent(getBaseContext(), null, ACTION_DETECT_ENVIRONMENT));
            super.onPostExecute(variables);
        }
    }

    private class DetermineConnectedWifiNetwork extends AsyncTask<Void, Void, WiFiEnvironmentVariable>{
        @Override
        protected WiFiEnvironmentVariable doInBackground(Void... params) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getConnectedWifiNetwork();
        }

        @Override
        protected void onPostExecute(WiFiEnvironmentVariable variable) {
            if(variable != null){
                WifiReceiver.setCurrentWifiNetwork(variable);
            }
            super.onPostExecute(variable);
        }
    }

    private void startLocationRequestReset(){
        mLocationClient.disconnect();
        mInProgress = false;
    }

    @Override
    public void onEnvironmentDetected(List<Environment> currentList) {
        currentEnvironments = currentList;
        if (currentList == null || currentList.size() == 0) {
            if(SharedPreferencesHelper.isNotificationEnabled(this)) {
                startForeground(ONGOING_NOTIFICATION_ID, NotificationHelper.getAppNotification(this,
                        "Unknown Environment"));
            } else {
                stopForeground(true);
            }
            Passphrase unknownPassphrase = User.getCurrentUser(this).getPassphraseForUnknownEnvironment(this);
            if(unknownPassphrase == null) {
                unknownPassphrase = Passphrase.getMasterPassword(this);
            }
            if(!unknownPassphrase.setAsCurrentPassword(this)){
                AdminActions.initializeAdminObjects(this);
                if(!unknownPassphrase.setAsCurrentPassword(this)){
                    startService(BaseService.getServiceIntent(this,
                            "Please enable administrator for the app.", null));
                }
            }
        } else {
            Environment current = currentList.get(0);
            User user = User.getCurrentUser(this);
            if(user != null) {
                Passphrase passphrase = user.getPassphraseForEnvironment(this, current);
                if(passphrase == null){
                    passphrase = user.getPassphraseForUnknownEnvironment(this);
                }
                if(passphrase != null){
                    if(!passphrase.setAsCurrentPassword(this)){
                        AdminActions.initializeAdminObjects(this);
                        if(!passphrase.setAsCurrentPassword(this)){
                            startService(BaseService.getServiceIntent(this,
                                    "Please enable administrator for the app.", null));
                        }
                    }
                } else {
                    Log.w(LOG_TAG, "Passphrase null for current user in current environment.");
                }
            } else {
                Log.e(LOG_TAG, "Current user null!");
            }
            if(SharedPreferencesHelper.isNotificationEnabled(this)) {
                startForeground(ONGOING_NOTIFICATION_ID, NotificationHelper.getAppNotification(this,
                        "Environment: " + current.getName()));
            } else {
                stopForeground(true);
            }
        }
        if(switchUser){
            switchUser = false;
            ScreenReceiver.turnScreenOff(this);
            ScreenReceiver.turnScreenOn(this);
        }
        if(mLockScreenOverlayHelper != null){
            mLockScreenOverlayHelper.setUpEnvironmentOptions();
        }
    }
}
