package com.pvsagar.smartlockscreen;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SelectLocation extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    private final String LOG_TAG = SelectLocation.class.getSimpleName();
    public static final String INTENT_EXTRA_SELECTED_LATITUDE = "selectedLatitude";
    public static final String INTENT_EXTRA_SELECTED_LONGITUDE = "selectedLongitude";
    public static final int GEOCODER_MAX_RESULTS_COUNT = 5;
    public static final int AUTO_COMPLETE_THRESHOLD = 1;

    private GoogleMap googleMap;
    private static final int MAP_DFAULT_ZOOM_LEVEL = 14;
    private Location selectedLocation;
    private LocationClient mLocationClient;

    //UI elements
    private static Geocoder geocoder;
    private static Button addressButton;
    private static AutoCompleteTextView addressAutoCompleteTextView;
    private static ArrayAdapter<String> addressAutoCompleteAdapter;
    private static ArrayList<String> autoCompleteStrings;
    private static LocationSuggestTask locationSuggestTask;

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
        setUpGoogleMap();

        //Geocoder
        locationSuggestTask = new LocationSuggestTask();
        setUpGeocoder();


    }

    class LocationSuggestTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            if(autoCompleteStrings == null){
                autoCompleteStrings = new ArrayList<String>();
            } else {
                autoCompleteStrings.clear();
            }
            try{
                List<Address> addresses =  geocoder.getFromLocationName(params[0].toString(),GEOCODER_MAX_RESULTS_COUNT);
                addressAutoCompleteAdapter.clear();
                for (Address address : addresses) {
                    if(address.getFeatureName() != null){
                        autoCompleteStrings.add(address.getFeatureName()+", "+address.getCountryName());
                    } else {
                        autoCompleteStrings.add(getReadableAddress(address));
                    }
                }

            } catch (Exception e){
                Log.e(LOG_TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            addressAutoCompleteAdapter.clear();
            for (String autoCompleteString : autoCompleteStrings) {
                addressAutoCompleteAdapter.add(autoCompleteString);
                Log.v(LOG_TAG,autoCompleteString);
            }
        }
    }

    private void setUpGeocoder(){
        geocoder = new Geocoder(this);
        addressButton = (Button)findViewById(R.id.button_address);
        addressAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.auto_complete_text_view_address);
        autoCompleteStrings = new ArrayList<String>();
        addressAutoCompleteAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,autoCompleteStrings);
        addressAutoCompleteTextView.setAdapter(addressAutoCompleteAdapter);
        addressAutoCompleteTextView.setThreshold(AUTO_COMPLETE_THRESHOLD);
        addressAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*if(locationSuggestTask != null && !(locationSuggestTask.getStatus() == AsyncTask.Status.FINISHED)){
                    locationSuggestTask.cancel(true);
                }
                locationSuggestTask = new LocationSuggestTask();
                locationSuggestTask.execute(s.toString());*/

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    List<Address> addresses =  geocoder.getFromLocationName(s.toString(),GEOCODER_MAX_RESULTS_COUNT);
                    addressAutoCompleteAdapter.clear();
                    Toast.makeText(getBaseContext(),""+addresses.size(),Toast.LENGTH_SHORT).show();
                    for (Address address : addresses) {
                        addressAutoCompleteAdapter.add(getReadableAddress(address));
                        Log.v(LOG_TAG,getReadableAddress(address));
                    }
                } catch (Exception e){
                    Log.e(LOG_TAG, e.toString());
                }
            }
        });
        addressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (Address address : geocoder.getFromLocationName(addressAutoCompleteTextView.getText().toString(),
                            GEOCODER_MAX_RESULTS_COUNT)) {
                        Log.v(LOG_TAG,address.toString());
                    }
                    ;

                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }
        });
    }

    public String getReadableAddress(Address address){
        String readableAddress = "";
        if(address.getFeatureName() != null){
            readableAddress = address.getFeatureName();
        }
        for(int i=0;i<address.getMaxAddressLineIndex();i++){
            if(!address.getAddressLine(i).equals("")) {
                readableAddress = readableAddress + ", " + address.getAddressLine(i);
            }
        }
        readableAddress = readableAddress+", "+address.getCountryName();
        return readableAddress;
    }

    private void setUpActionBar(){
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

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
            /* End of Action Bar Code */
    }

    private void setUpGoogleMap(){
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
        else{
            latitude = -1;
            longitude = -1;
        }
        if(latitude != -1 && longitude != -1){
            selectedLocation = new Location("Selected Location");
            selectedLocation.setLatitude(latitude);
            selectedLocation.setLongitude(longitude);
            LatLng latLng = new LatLng(latitude,longitude);
            googleMap.addMarker(new MarkerOptions().position(latLng));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_DFAULT_ZOOM_LEVEL));

        } else {
            selectedLocation = mLocationClient.getLastLocation();
            LatLng latLng = new LatLng(selectedLocation.getLatitude(),selectedLocation.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(latLng));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_DFAULT_ZOOM_LEVEL));
            mLocationClient.disconnect();
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
