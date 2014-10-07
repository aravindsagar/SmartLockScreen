package com.pvsagar.smartlockscreen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.Date;


public class SelectLocation extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    private final String LOG_TAG = SelectLocation.class.getSimpleName();
    public static final String INTENT_EXTRA_SELECTED_LATITUDE = "selectedLatitude";
    public static final String INTENT_EXTRA_SELECTED_LONGITUDE = "selectedLongitude";
    public static final int REQUEST_SEARCH_LOCATION = 93;
    private static final int MAP_DFAULT_ZOOM_LEVEL = 14;
    private static final double DEFAULT_LATITUDE = 22;
    private static final double DEFAULT_LONGITUDE = 75;

    private GoogleMap googleMap;
    private Location selectedLocation;
    private LocationClient mLocationClient;

    private int paddingTop = 0, paddingBottom = 0;

    //Intent
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        //Intent
        intent = getIntent();
        //Init
        setUpActionBar();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.action_bar_location));
            paddingBottom = tintManager.getConfig().getNavigationBarHeight();
            paddingTop = tintManager.getConfig().getPixelInsetTop(true);
        }
        setUpGoogleMap();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_SEARCH_LOCATION){
            if(resultCode == RESULT_OK){
                Bundle bundle = data.getExtras();
                double latitude = bundle.getDouble(INTENT_EXTRA_SELECTED_LATITUDE);
                double longitude = bundle.getDouble(INTENT_EXTRA_SELECTED_LONGITUDE);
                selectedLocation = new Location("Selected Location");
                selectedLocation.setLatitude(latitude);
                selectedLocation.setLongitude(longitude);
                Log.v(LOG_TAG,"Activity success");
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),MAP_DFAULT_ZOOM_LEVEL));
            }
        }
    }

    private void setUpActionBar(){
        /* Adding Contextual Action Bar with Done and Cancel Button */
        final LayoutInflater layoutInflater = (LayoutInflater) getSupportActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = layoutInflater.inflate(R.layout.actionbar_custom_view_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // "Done"
                        onDoneButtonClick();
                    }
                });

        customActionBarView.findViewById(R.id.actionbar_search).setOnClickListener(
                new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //On search button click
                    Intent searchActivityIntent = new Intent(getBaseContext(),SearchLocation.class);
//                    searchActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivityForResult(searchActivityIntent, REQUEST_SEARCH_LOCATION);
                    overridePendingTransition(R.anim.abc_fade_in, 0);
                }
        });

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(
                R.color.action_bar_location)));
            /* End of Action Bar Code */
    }

    private void setUpGoogleMap(){
        googleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map_select_location)).getMap();
        googleMap.setPadding(0, paddingTop, 0, paddingBottom);
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
        Bundle intentBundle = intent.getExtras();
        double latitude = -1;
        double longitude = -1;
        if(intentBundle != null && intentBundle.containsKey(INTENT_EXTRA_SELECTED_LATITUDE) && intentBundle.containsKey(INTENT_EXTRA_SELECTED_LONGITUDE)) {
            latitude = intentBundle.getDouble(INTENT_EXTRA_SELECTED_LATITUDE, -1);
            longitude = intentBundle.getDouble(INTENT_EXTRA_SELECTED_LONGITUDE, -1);
        }
        if(selectedLocation == null) {
            if (latitude != -1 && longitude != -1) {
                selectedLocation = new Location("Selected Location");
                selectedLocation.setLatitude(latitude);
                selectedLocation.setLongitude(longitude);
                LatLng latLng = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions().position(latLng));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_DFAULT_ZOOM_LEVEL));

            } else {
                selectedLocation = mLocationClient.getLastLocation();
                if (selectedLocation == null) {
                    selectedLocation = new Location("Default location");
                    selectedLocation.setLatitude(DEFAULT_LATITUDE);
                    selectedLocation.setLongitude(DEFAULT_LONGITUDE);
                }
                LatLng latLng = new LatLng(selectedLocation.getLatitude(), selectedLocation.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(latLng));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_DFAULT_ZOOM_LEVEL));
                mLocationClient.disconnect();
            }
        }

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
