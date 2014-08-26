package com.pvsagar.smartlockscreen.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.pvsagar.smartlockscreen.frontend_helpers.NotificationHelper;

public class BaseService extends Service implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        OnAddGeofencesResultListener{
    private static final String LOG_TAG = BaseService.class.getSimpleName();
    public static final int ONGOING_NOTIFICATION_ID = 1;

    private LocationClient mLocationClient;

    public static Intent getServiceIntent(Context context, String extraText){
        Intent serviceIntent  = new Intent();
        serviceIntent.setClass(context, com.pvsagar.smartlockscreen.services.BaseService.class);
        if(extraText != null && !extraText.isEmpty()){
            serviceIntent.setData(Uri.parse(extraText));
        }
        return serviceIntent;
    }
    public BaseService() {
    }

    @Override
    public void onCreate() {
        startForeground(ONGOING_NOTIFICATION_ID, NotificationHelper.getAppNotification(this, null));
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri uri = intent.getData();
        if(uri != null){
            startForeground(ONGOING_NOTIFICATION_ID, NotificationHelper.getAppNotification(this,
                    uri.toString()));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
