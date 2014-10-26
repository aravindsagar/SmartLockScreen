package com.pvsagar.smartlockscreen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;


public class SearchLocation extends ActionBarActivity {

    public static final int GEOCODER_MAX_RESULTS_COUNT = 5;
    private final String LOG_TAG = SearchLocation.class.getSimpleName();

    private static ArrayAdapter<String> addressAutoCompleteAdapter;
    private static ArrayList<String> autoCompleteStrings;
    private static List<Address> searchAddresses;
    private static LocationSuggestTask locationSuggestTask;
    private static EditText searchBarEditText;
    private static Geocoder geocoder;

    private static ListView searchLocationListView;

    class LocationSuggestTask extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {
            if(autoCompleteStrings == null){
                autoCompleteStrings = new ArrayList<String>();
            } else {
                autoCompleteStrings.clear();
            }
            try{
                searchAddresses =  geocoder.getFromLocationName(params[0],GEOCODER_MAX_RESULTS_COUNT);
                for (Address address : searchAddresses) {
                    if(address.getFeatureName() != null){
                        autoCompleteStrings.add(address.getFeatureName()+", "+address.getCountryName());
                    } else {
                        autoCompleteStrings.add(getReadableAddress(address));
                    }
                }
                Log.v(LOG_TAG, "background work complete " + autoCompleteStrings.size());

            } catch (Exception e){
                Log.e(LOG_TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            addressAutoCompleteAdapter.clear();
            Log.v(LOG_TAG,"On post execute started "+autoCompleteStrings.size());
            for (String autoCompleteString : autoCompleteStrings) {
                addressAutoCompleteAdapter.add(autoCompleteString);
                //Log.v(LOG_TAG,autoCompleteString);
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        /* Initialization */
        init();
        setUpActionBar();
        this.setFinishOnTouchOutside(true);
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.linear_layout_search_location);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS  | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.action_bar_location_search));
        }
    }

    private void setUpActionBar(){
        /* Adding Contextual Action Bar with Done and Cancel Button */
        final LayoutInflater layoutInflater = (LayoutInflater) getSupportActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = layoutInflater.inflate(R.layout.actionbar_custom_search, null);
        customActionBarView.getBackground().setAlpha(100);
        searchBarEditText = (EditText) customActionBarView.findViewById(R.id.edit_text_searchbar);
        searchBarEditText.setHintTextColor(Color.LTGRAY);
        searchBarEditText.setTextColor(Color.WHITE);
        searchBarEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(locationSuggestTask != null && locationSuggestTask.getStatus() != AsyncTask.Status.FINISHED){
                    locationSuggestTask.cancel(true);
                }
                locationSuggestTask = new LocationSuggestTask();
                locationSuggestTask.execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        FrameLayout cancelButton = (FrameLayout)customActionBarView.findViewById(R.id.actionbar_cancel_icon);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchBarEditText.getText().toString().equals("")){
                    onBackPressed();
                }else {
                    searchBarEditText.setText("");
                }
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
                R.color.action_bar_location_search)));
            /* End of Action Bar Code */
    }

    private void init(){
        geocoder = new Geocoder(this);
        autoCompleteStrings = new ArrayList<String>();
        addressAutoCompleteAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,new ArrayList<String>());
        searchLocationListView = (ListView)findViewById(R.id.list_view_location_search);
        searchLocationListView.setAdapter(addressAutoCompleteAdapter);
        searchLocationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent resultIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putDouble(SelectLocation.INTENT_EXTRA_SELECTED_LATITUDE,searchAddresses.get(position).getLatitude());
                bundle.putDouble(SelectLocation.INTENT_EXTRA_SELECTED_LONGITUDE,searchAddresses.get(position).getLongitude());
                resultIntent.putExtras(bundle);
                Log.v(LOG_TAG,""+resultIntent.getExtras().getDouble(SelectLocation.INTENT_EXTRA_SELECTED_LATITUDE));
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                overridePendingTransition(0, R.anim.abc_fade_out);
            }
        });
    }

    public static String getReadableAddress(Address address){
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

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        setResult(RESULT_CANCELED,resultIntent);
        finish();
        overridePendingTransition(0, R.anim.abc_fade_out);
    }
}
