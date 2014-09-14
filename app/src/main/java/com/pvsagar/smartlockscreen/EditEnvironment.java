package com.pvsagar.smartlockscreen;

import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pvsagar.smartlockscreen.applogic_objects.BluetoothEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.LocationEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.applogic_objects.WiFiEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Password;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Pin;
import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.services.BaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

//TODO see whether the location name entered exists for other lat/long/radius entries, and take appropriate actions

public class EditEnvironment extends ActionBarActivity {

    private static final String LOG_TAG = EditEnvironment.class.getSimpleName();
    public static final int REQUEST_LOCATION_SELECT = 31;
    public static final String INTENT_EXTRA_ENVIRONMENT = "environmentName";

    /* These variables hold the data of the present environment being edited */
    /* Bluetooth */
    private static ArrayList<BluetoothDevice> bluetoothDevices;
    private static ArrayList<BluetoothDevice> mSelectedBluetoothDevices;
    private static ArrayList<Integer> mSelectedBluetoothItems;

    /* WiFi */
    private static ArrayList<WifiConfiguration> wifiConfigurations;
    private static WifiConfiguration mSelectedWifiConfiguration;
    private static int mSelectedWiFiItem;

    /* Location */
    private static double latLocation;
    private static double lonLocation;
    private static double radLocation;
    private static List<EnvironmentVariable> storedLocations;
    private static int mSelectedLocationItem;
    private static LocationEnvironmentVariable mSelectedLocation;

    /* Passphrase */
    private static ArrayAdapter<String> passphraseAdapter;
    private static int selectedPassphrasetype;

    PlaceholderFragment placeholderFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_environment);
        if (savedInstanceState == null) {
            placeholderFragment = new PlaceholderFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, placeholderFragment)
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BluetoothEnvironmentVariable.REQUEST_BLUETOOTH_ENABLE){
            if(resultCode == RESULT_OK){
                //Bluetooth Enabled
                //Get the list of paired devices, populate the list, set the adapter
                Toast.makeText(getBaseContext(),"Bluetooth switched on",Toast.LENGTH_SHORT).show();
                placeholderFragment.enableBluetoothCheckBox.setChecked(true);
                ArrayList<BluetoothDevice> bluetoothDevices = BluetoothEnvironmentVariable.getPairedBluetoothDevices(this);
                ArrayList<String> deviceNamesArrayList = new ArrayList<String>();
                if(bluetoothDevices != null) {
                    //Populate the String list and Set the adapter for the list view
                    for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
                        deviceNamesArrayList.add(bluetoothDevice.getName());
                    }
                    EditEnvironment.bluetoothDevices = bluetoothDevices;
                    placeholderFragment.setBluetoothItemsEnabled(true);
                }
                if(placeholderFragment.environment.hasBluetoothDevices){
                    placeholderFragment.initBluetoothDispItems();
                }
            } else{

                if(placeholderFragment.environment.hasBluetoothDevices){
                    Toast.makeText(this,"Bluetooth not available. Exiting edit environment",Toast.LENGTH_SHORT).show();
                    finish();
                } else{
                    placeholderFragment.enableBluetoothCheckBox.setChecked(false);
                    placeholderFragment.setBluetoothItemsEnabled(false);
                }
            }
        } else if(requestCode == REQUEST_LOCATION_SELECT){
            Bundle bundle = data.getExtras();
            Location location = (Location)bundle.get(AddEnvironment.INTENT_EXTRA_SELECTED_LOCATION);
            placeholderFragment.latLocationEditText.setText(""+location.getLatitude());
            placeholderFragment.lonLocationEditText.setText(""+location.getLongitude());
        }
    }

    @Override
    public void onBackPressed() {
        placeholderFragment.onCancelButtonClick();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        Environment environment;
        String environmentName;

        /* Environment Details */
        private EditText environmentNameEditText;
        private EditText environmentHintEditText;
        /* Bluetooth */
        private CheckBox enableBluetoothCheckBox;
        private TextView selectBluetoothDevicesTextView;
        private CheckBox bluetoothAllCheckbox;
        /* WiFi */
        private CheckBox enableWiFiCheckBox;
        private TextView selectWiFiConnectionTextView;
        /* Location */
        private CheckBox enableLocationCheckBox;
        private EditText nameLocationEditText;
        private EditText latLocationEditText;
        private EditText lonLocationEditText;
        private EditText radLocationEditText;
        private TextView selectLocationTextView;
        private TextView selectStoredLocationTextView;

        /* Passphrase */
        private Spinner passphraseTypeSpinner;
        private EditText passphraseEditText;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_add_environment, container, false);

            //Get the environment
            Intent passedIntent = getActivity().getIntent();
            environmentName = passedIntent.getStringExtra(EditEnvironment.INTENT_EXTRA_ENVIRONMENT);
            environment = Environment.getFullEnvironment(getActivity(),environmentName);

            /* Variable Initialization */
            //Environment details
            environmentNameEditText = (EditText) rootView.findViewById(R.id.edit_text_environment_name);
            environmentHintEditText = (EditText) rootView.findViewById(R.id.edit_text_environment_hint);
            //Bluetooth
            enableBluetoothCheckBox = (CheckBox)rootView.findViewById(R.id.checkbox_enable_bluetooth);
            selectBluetoothDevicesTextView = (TextView)rootView.findViewById(R.id.text_view_bluetooth_devices_select);
            bluetoothAllCheckbox = (CheckBox)rootView.findViewById(R.id.checkbox_bluetooth_all);
            //WiFi
            enableWiFiCheckBox = (CheckBox)rootView.findViewById(R.id.checkbox_enable_wifi);
            selectWiFiConnectionTextView = (TextView)rootView.findViewById(R.id.text_view_wifi_connection_select);
            //Location
            enableLocationCheckBox = (CheckBox)rootView.findViewById(R.id.checkbox_enable_location);
            nameLocationEditText = (EditText)rootView.findViewById(R.id.edit_text_location_name);
            latLocationEditText = (EditText)rootView.findViewById(R.id.edit_text_location_lat);
            lonLocationEditText = (EditText)rootView.findViewById(R.id.edit_text_location_lon);
            radLocationEditText = (EditText)rootView.findViewById(R.id.edit_text_location_rad);
            selectLocationTextView = (TextView)rootView.findViewById(R.id.text_view_select_location);
            selectStoredLocationTextView = (TextView) rootView.findViewById(R.id.text_view_select_stored_location);

            //Passphrase
            passphraseTypeSpinner = (Spinner) rootView.findViewById(R.id.spinner_passphrase_type);
            passphraseEditText = (EditText) rootView.findViewById(R.id.edit_text_passphrase);

            setUpActionBar();


            /* Initialization */
            setUpEditEnvironment();
            /* End of Initialization */

            return rootView;
        }

        private void setUpActionBar(){
            /* Adding Contextual Action Bar with Done and Cancel Button */
            final LayoutInflater layoutInflater = (LayoutInflater) ((ActionBarActivity)getActivity()).getSupportActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            final View customActionBarView = layoutInflater.inflate(R.layout.actionbar_custom_view_done_cancel, null);
            customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // "Done"
                            onDoneButtonClick();
                        }
                    });
            customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // "Cancel"
                    onCancelButtonClick();
                }
            });

            final ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
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

        private void setUpEditEnvironment(){
            /* Environment Details */
            environmentNameEditText.setText(environment.getName());
            environmentHintEditText.setText(environment.getHint());
            /* setting up */
            setUpBluetoothElements();
            setUpWiFiElements();
            setUpLocationElements();
            setUpPassphraseElements();
            initPassphraseElements();
            setBluetoothItemsEnabled(false);
            setWiFiItemsEnabled(false);
            setLocationItemsEnabled(false);
            /* Bluetooth Details */
            if(environment.hasBluetoothDevices){
                bluetoothDevices = BluetoothEnvironmentVariable.getPairedBluetoothDevices(getActivity());
                if(bluetoothDevices != null){
                    initBluetoothDispItems();
                }
            }
            /* Wi-Fi Details */
            if(environment.hasWiFiNetwork){
                if(!WiFiEnvironmentVariable.enableWifi(getActivity())){
                    Toast.makeText(getActivity(),"Couldn't start Wi-Fi",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                } else{
                    initWiFiDispItems();
                }
            }

            if (environment.hasLocation){
                initLocationDispItems();
            }

        }

        private void initBluetoothDispItems(){
            /* Initialization */
            mSelectedBluetoothItems = new ArrayList<Integer>();
            mSelectedBluetoothDevices = new ArrayList<BluetoothDevice>();
            setBluetoothItemsEnabled(true);
            enableBluetoothCheckBox.setChecked(true);
            bluetoothAllCheckbox.setChecked(false);

            if(environment.isBluetoothAllOrAny()){
                bluetoothAllCheckbox.setChecked(true);
            }

            /* Populating bluetooth list */
            bluetoothDevices = BluetoothEnvironmentVariable.getPairedBluetoothDevices(getActivity());
            Vector<BluetoothEnvironmentVariable> bluetoothVariables =  environment.getBluetoothEnvironmentVariables();
            for(int i = 0; i < bluetoothDevices.size(); i++){
                for(int j = 0; j < bluetoothVariables.size(); j++){
                    if(bluetoothVariables.get(j).getDeviceAddress().equals(bluetoothDevices.get(i).getAddress())){
                        mSelectedBluetoothDevices.add(bluetoothDevices.get(i));
                        mSelectedBluetoothItems.add(i);
                    }
                }
            }
        }

        private void initWiFiDispItems(){
            enableWiFiCheckBox.setChecked(true);
            wifiConfigurations = WiFiEnvironmentVariable.getConfiguredWiFiConnections(getActivity());
            String wifiSSIDSelected = environment.getWiFiEnvironmentVariable().getSSID();
            mSelectedWiFiItem = -1;
            //Finding selected Wi-Fi connection
            for (int i = 0; i < wifiConfigurations.size(); i++) {
                if(wifiSSIDSelected.equals(wifiConfigurations.get(i).SSID)){
                    mSelectedWifiConfiguration = wifiConfigurations.get(i);
                    mSelectedWiFiItem = i;
                    break;
                }
            }
            setWiFiItemsEnabled(true);
        }

        private void initLocationDispItems(){
            setLocationItemsEnabled(true);
            enableLocationCheckBox.setChecked(true);
            //setting current location
            LocationEnvironmentVariable locationVariable = environment.getLocationEnvironmentVariable();
            nameLocationEditText.setText(locationVariable.getLocationName());
            latLocationEditText.setText("" + locationVariable.getLatitude());
            lonLocationEditText.setText(""+locationVariable.getLongitude());
            radLocationEditText.setText(""+locationVariable.getRadius());
        }

        private void initPassphraseElements(){
            // Todo: do initialization form database
            passphraseEditText.setHint("(Unchanged)");
            passphraseTypeSpinner.setSelection(Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD);
            selectedPassphrasetype = Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD;
        }

        private void setUpBluetoothElements(){

            /* CheckBox CheckedChange Listener */
            enableBluetoothCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        ArrayList<BluetoothDevice> bluetoothDevices = new BluetoothEnvironmentVariable().getPairedBluetoothDevices(getActivity());
                        ArrayList<String> deviceNamesArrayList = new ArrayList<String>();
                        if (bluetoothDevices != null) {
                            //Populate the String list and Set the adapter for the list view
                            for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
                                deviceNamesArrayList.add(bluetoothDevice.getName());
                            }
                            EditEnvironment.bluetoothDevices = bluetoothDevices;
                            setBluetoothItemsEnabled(true);


                        } else {
                            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (mBluetoothAdapter == null) {
                                enableBluetoothCheckBox.setChecked(false);
                                //Disable the list
                                EditEnvironment.bluetoothDevices = new ArrayList<BluetoothDevice>();
                                setBluetoothItemsEnabled(false);
                            }
                        }
                    } else {
                        //Disable the list
                        EditEnvironment.bluetoothDevices = new ArrayList<BluetoothDevice>();
                        setBluetoothItemsEnabled(false);
                    }
                }
            });

            /* Text View onClickListener */
            selectBluetoothDevicesTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bluetoothDevices == null) {
                        bluetoothDevices = new ArrayList<BluetoothDevice>();
                    }
                    String[] bluetoothDevicesName = new String[bluetoothDevices.size()];
                    for (int i = 0; i < bluetoothDevices.size(); i++) {
                        bluetoothDevicesName[i] = bluetoothDevices.get(i).getName();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.dialog_pick_bluetooth_devices);
                    //Set boolean array for previously checked items
                    boolean[] checkedItems = new boolean[bluetoothDevices.size()];
                    if (mSelectedBluetoothDevices == null || mSelectedBluetoothItems == null) {
                        mSelectedBluetoothDevices = new ArrayList<BluetoothDevice>();
                        mSelectedBluetoothItems = new ArrayList<Integer>();
                    }
                    for (Integer mSelectedItem : mSelectedBluetoothItems) {
                        checkedItems[mSelectedItem] = true;
                    }

                    builder.setMultiChoiceItems(bluetoothDevicesName, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (isChecked) {
                                mSelectedBluetoothItems.add(which);
                            } else {
                                mSelectedBluetoothItems.remove(Integer.valueOf(which));
                            }
                        }
                    });
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (Integer mSelectedItem : mSelectedBluetoothItems) {
                                mSelectedBluetoothDevices.add(bluetoothDevices.get(mSelectedItem));
                            }
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }

        private void setUpWiFiElements(){

            /* Check Box listener */
            enableWiFiCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        //WiFi is enabled
                        wifiConfigurations = WiFiEnvironmentVariable.getConfiguredWiFiConnections(getActivity());
                        setWiFiItemsEnabled(true);
                        if(wifiConfigurations == null){
                            //No WiFi adapter Found
                            enableWiFiCheckBox.setChecked(false);
                            setWiFiItemsEnabled(false);
                        }
                    }
                    else{
                        setWiFiItemsEnabled(false);
                    }
                }
            });

            selectWiFiConnectionTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] wifiConnectionNames = new String[wifiConfigurations.size()];
                    for(int i=0; i<wifiConfigurations.size();i++){
                        wifiConnectionNames[i] = wifiConfigurations.get(i).SSID;
                    }
                    if(mSelectedWiFiItem == -1){
                        mSelectedWifiConfiguration = null;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.dialog_pick_wifi_connection);
                    builder.setSingleChoiceItems(wifiConnectionNames,mSelectedWiFiItem,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSelectedWiFiItem = which;
                            mSelectedWifiConfiguration = wifiConfigurations.get(which);
                        }
                    });
                    builder.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();

                }
            });
        }

        private void setUpLocationElements(){

            enableLocationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        setLocationItemsEnabled(true);
                        storedLocations = LocationEnvironmentVariable.
                                getLocationEnvironmentVariables(getActivity());
                        mSelectedLocationItem = -1;
                    } else {
                        setLocationItemsEnabled(false);
                    }
                }
            });

            selectLocationTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(LOG_TAG, "Starting select location");
                    Intent intent = new Intent(getActivity(), SelectLocation.class);
                    if(environment.hasLocation) {
                        LocationEnvironmentVariable location = environment.getLocationEnvironmentVariable();
                        Bundle bundle = new Bundle();
                        bundle.putDouble(SelectLocation.INTENT_EXTRA_SELECTED_LATITUDE, location.getLatitude());
                        bundle.putDouble(SelectLocation.INTENT_EXTRA_SELECTED_LONGITUDE, location.getLongitude());
                        intent.putExtras(bundle);
                    }
                    Toast.makeText(getActivity(), "Select Location edit", Toast.LENGTH_SHORT).show();
                    getActivity().startActivityForResult(intent, REQUEST_LOCATION_SELECT);
                }
            });

            /* Select stored location */
            selectStoredLocationTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] locationNames = new String[storedLocations.size()];
                    for (int i=0; i<storedLocations.size();i++) {
                        locationNames[i] = ((LocationEnvironmentVariable)storedLocations.get(i)).getLocationName();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.dialog_pick_wifi_connection);
                    builder.setSingleChoiceItems(locationNames, mSelectedLocationItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSelectedLocationItem = which;
                            mSelectedLocation = (LocationEnvironmentVariable) storedLocations.get(which);
                        }
                    });
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mSelectedLocationItem != -1) {
                                nameLocationEditText.setText(mSelectedLocation.getLocationName());
                                latLocationEditText.setText("" + mSelectedLocation.getLatitude());
                                lonLocationEditText.setText("" + mSelectedLocation.getLongitude());
                                radLocationEditText.setText("" + mSelectedLocation.getRadius());
                            }
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();

                }
            });
        }

        public void setUpPassphraseElements(){
            //Adapter for spinner
            passphraseAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, Passphrase.passphraseTypes);
            passphraseTypeSpinner.setAdapter(passphraseAdapter);
            passphraseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedPassphrasetype = position;
                    if (position == Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD) {
                        passphraseEditText.setText("");
                        passphraseEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passphraseEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

                    } else if (position == Passphrase.INDEX_PASSPHRASE_TYPE_PIN) {
                        passphraseEditText.setText("");
                        passphraseEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
                        passphraseEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    passphraseTypeSpinner.setSelection(Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD);
                    selectedPassphrasetype = Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD;
                }
            });
        }

        public void setBluetoothItemsEnabled(boolean flag){
            selectBluetoothDevicesTextView.setEnabled(flag);
            bluetoothAllCheckbox.setEnabled(flag);
        }

        public void setWiFiItemsEnabled(boolean flag){
            selectWiFiConnectionTextView.setEnabled(flag);
        }

        public void setLocationItemsEnabled(boolean flag){
            nameLocationEditText.setEnabled(flag);
            latLocationEditText.setEnabled(flag);
            lonLocationEditText.setEnabled(flag);
            radLocationEditText.setEnabled(flag);
            selectLocationTextView.setEnabled(flag);
            selectStoredLocationTextView.setEnabled(flag);
        }

        public void onDoneButtonClick(){
            //Environment Details
            String environmentName = environmentNameEditText.getText().toString();
            String environmentHint = environmentHintEditText.getText().toString();
            //Bluetooth details
            boolean bluetoothFlag = enableBluetoothCheckBox.isChecked();
            boolean bluetoothAllFlag = false;
            //Wifi Details
            boolean wifiFlag = enableWiFiCheckBox.isChecked();
            //Location
            boolean locationFlag = enableLocationCheckBox.isChecked();
            String locationName;
            double latLocation;
            double lonLocation;
            double radLocation;

            //List to store all the environment variables
            ArrayList<EnvironmentVariable> environmentVariables = new ArrayList<EnvironmentVariable>();


            /* Parsing the Data */
            if(environmentName.equals("")){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.alert_environment_name_title).setMessage(R.string.alert_environment_name_message);
                builder.setPositiveButton(R.string.ok,null);
                builder.create().show();
                return;
            } else{
                if(!environmentName.equals(this.environmentName)) {
                    Toast.makeText(getActivity(),"Name Check",Toast.LENGTH_SHORT).show();
                    for (String s : Environment.getAllEnvironmentNames(getActivity())) {
                        if (s.equals(environmentName)){
                            //Error
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(R.string.alert_environment_name_unique_title).setMessage(R.string.alert_environment_name_unique_message);
                            builder.setPositiveButton(R.string.ok,null);
                            builder.create().show();
                            return;
                        }
                    }
                }

            }

            if(environmentHint.equals("")){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.alert_environment_hint_title).setMessage(R.string.alert_environment_hint_message);
                builder.setPositiveButton(R.string.ok,null);
                builder.create().show();
                return;
            }

            if(bluetoothFlag){
                if(mSelectedBluetoothDevices != null && mSelectedBluetoothDevices.size() != 0){
                    for (BluetoothDevice bluetoothDevice : mSelectedBluetoothDevices) {
                        environmentVariables.add(new BluetoothEnvironmentVariable(bluetoothDevice.getName(),bluetoothDevice.getAddress()));
                        if(bluetoothDevice.getName() == null) Log.w(LOG_TAG, "Bluetooth deviceName null.");
                        if(bluetoothDevice.getAddress() == null) Log.w(LOG_TAG, "Bluetooth deviceAddress null.");
                    }
                } else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.alert_bluetooth_devices_selection_title).setMessage(R.string.alert_bluetooth_devices_selection_message);
                    builder.setPositiveButton(R.string.ok, null);
                    builder.create().show();
                    return;
                }
                bluetoothAllFlag = bluetoothAllCheckbox.isChecked();
            }
            if(wifiFlag){
                if(mSelectedWifiConfiguration != null){
                    String ssid = mSelectedWifiConfiguration.SSID;
                    String encryptionType = WiFiEnvironmentVariable.getSecurity(mSelectedWifiConfiguration);
                    Log.v(LOG_TAG,ssid);
                    //Toast.makeText(getActivity(),ssid,Toast.LENGTH_SHORT).show();
                    environmentVariables.add(new WiFiEnvironmentVariable(ssid,encryptionType));
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.alert_wifi_connection_selection_title).setMessage(R.string.alert_wifi_connection_selection_message);
                    builder.setPositiveButton(R.string.ok,null);
                    builder.create().show();
                    return;
                }
            }
            if(locationFlag){
                locationName = nameLocationEditText.getText().toString();
                if(locationName.equals("")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.alert_name_location_title).setMessage(R.string.alert_environment_name_message);
                    builder.setPositiveButton(R.string.ok,null);
                    builder.create().show();
                    return;
                }

                //Lat Check
                try{
                    latLocation = Double.parseDouble(latLocationEditText.getText().toString());
                } catch (NumberFormatException e){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.alert_lat_location_title).setMessage(R.string.alert_lat_location_selection_message);
                    builder.setPositiveButton(R.string.ok,null);
                    builder.create().show();
                    return;
                }
                try{
                    lonLocation = Double.parseDouble(lonLocationEditText.getText().toString());
                } catch (NumberFormatException e){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.alert_lon_location_title).setMessage(R.string.alert_lon_location_selection_message);
                    builder.setPositiveButton(R.string.ok,null);
                    builder.create().show();
                    return;
                }
                try{
                    radLocation = Double.parseDouble(radLocationEditText.getText().toString());
                } catch (NumberFormatException e){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.alert_rad_location_title).setMessage(R.string.alert_rad_location_selection_message);
                    builder.setPositiveButton(R.string.ok,null);
                    builder.create().show();
                    return;
                }

                environmentVariables.add(new LocationEnvironmentVariable(latLocation,lonLocation,(int)radLocation,locationName));
            }

            if(!bluetoothFlag && !wifiFlag && !locationFlag){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.alert_no_variables_title).setMessage(R.string.alert_no_variables_message);
                builder.setPositiveButton(R.string.ok,null);
                builder.create().show();
                return;
            }
            /* Data Parsed */

            /* Creating Environment */
            Environment newEnvironment = new Environment(environmentName,environmentVariables);
            if(bluetoothFlag){
                newEnvironment.setBluetoothAllOrAny(bluetoothAllFlag);
            }
            newEnvironment.setHint(environmentHint);
            newEnvironment.updateInDatabase(getActivity(),environmentName);

            /* Updating passphrase */
            if(!passphraseEditText.getText().toString().equals("")){
                //Password changed
                Log.d(LOG_TAG, "Password changed. Updating in db.");
                if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD){
                    Password password = new Password(passphraseEditText.getText().toString());
                    User.getDefaultUser(getActivity()).setPassphraseForEnvironment(getActivity(),password,newEnvironment);
                }
                else if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_PIN){
                    Pin pin = new Pin(passphraseEditText.getText().toString());
                    User.getDefaultUser(getActivity()).setPassphraseForEnvironment(getActivity(),pin,newEnvironment);
                }
            }
            /* done with updating passphrase */
            getActivity().startService(BaseService.getServiceIntent(getActivity(), null,
                    BaseService.ACTION_ADD_GEOFENCES));
            getActivity().startService(BaseService.getServiceIntent(getActivity(), null,
                    BaseService.ACTION_DETECT_ENVIRONMENT));
            getActivity().finish();

        }
        public void onCancelButtonClick(){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.alert_cancel_edit_environment_title).setMessage(R.string.alert_cancel_edit_environment_message);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                }
            });
            builder.setNegativeButton(R.string.cancel,null);
            builder.create().show();
        }
    }
}
