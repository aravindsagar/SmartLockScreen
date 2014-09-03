package com.pvsagar.smartlockscreen;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;


public class SelectLocation extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    private String LOG_TAG = SelectLocation.class.getSimpleName();
    private GoogleMap googleMap;
    private static final int MAP_DFAULT_ZOOM_LEVEL = 14;
    private Location selectedLocation;
    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        /* Adding Contextual Action Bar with Done and Cancel Button */
        final LayoutInflater layoutInflater = (LayoutInflater) getActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = layoutInflater.inflate(R.layout.actionbar_custom_view_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // "Done"
                        onDoneButtonClick();
                    }
                });

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
            /* End of Action Bar Code */


        googleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map_select_location)).getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                selectedLocation = googleMap.getMyLocation();
                if(selectedLocation != null){
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(new LatLng(selectedLocation.getLatitude(),selectedLocation.getLongitude())));
                }else{
                    Toast.makeText(getBaseContext(),"Unable to get Location",Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        mLocationClient = new LocationClient(this,this,this);
        initGoogleMap();
    }

    private void onDoneButtonClick(){
        if(selectedLocation != null){
            Intent _resultIntent = new Intent();
            _resultIntent.putExtra(AddEnvironment.INTENT_EXTRA_SELECTED_LOCATION,selectedLocation);
            setResult(Activity.RESULT_OK, _resultIntent);
            finish();
        }
        else{
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }

    private void initGoogleMap(){
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                selectedLocation = new Location("selectedLocation");
                selectedLocation.setTime(new Date().getTime());
                selectedLocation.setLatitude(latLng.latitude);
                selectedLocation.setLongitude(latLng.longitude);
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(latLng));
            }
        });
    }

    private boolean checkServicesConnected(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(ConnectionResult.SUCCESS == resultCode){
            Log.d(LOG_TAG,"Location Update: Google Play services available");
            return true;
        } else{
            Log.e(LOG_TAG,"Location update: failure\nResult Code: "+resultCode);
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        selectedLocation = mLocationClient.getLastLocation();
        LatLng latLng = new LatLng(selectedLocation.getLatitude(),selectedLocation.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,MAP_DFAULT_ZOOM_LEVEL));
        mLocationClient.disconnect();
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG,"Cannot connect to Google Play Services");
        Toast.makeText(this,"Cannot connect to Google Play Services",Toast.LENGTH_SHORT).show();
    }
}
