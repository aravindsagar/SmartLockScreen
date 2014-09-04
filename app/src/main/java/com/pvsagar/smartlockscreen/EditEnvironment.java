package com.pvsagar.smartlockscreen;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pvsagar.smartlockscreen.applogic_objects.BluetoothEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.LocationEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.WiFiEnvironmentVariable;
import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;

import java.util.ArrayList;
import java.util.Vector;


public class EditEnvironment extends ActionBarActivity {

    private static final String LOG_TAG = EditEnvironment.class.getSimpleName();
    public static final int REQUEST_LOCATION_SELECT = 31;
    public static final String INTENT_EXTRA_ENVIRONMENT = "environmentName";

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
                placeholderFragment.setUpBluetoothElements();
            } else{
                Toast.makeText(this,"Bluetooth not available. Exiting edit environment",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_environment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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


            setUpActionBar();


            /* Initialization */
            setUpEditEnvironment();
            /* End of Initialization */

            return rootView;
        }

        private void setUpActionBar(){
            /* Adding Contextual Action Bar with Done and Cancel Button */
            final LayoutInflater layoutInflater = (LayoutInflater) getActivity().getActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
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

            final ActionBar actionBar = getActivity().getActionBar();
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
            /* Bluetooth Details */
            if(environment.hasBluetoothDevices){
                BluetoothEnvironmentVariable.enableBluetooth(getActivity());
            }
            /* Wi-Fi Details */
            if(environment.hasWiFiNetwork){
                if(!WiFiEnvironmentVariable.enableWifi(getActivity())){
                    Toast.makeText(getActivity(),"Couldn't start Wi-Fi",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                } else{
                    setUpWiFiElements();
                }
            }

            if (environment.hasLocation){
                setUpLocationElements();
            }

        }

        private void setUpBluetoothElements(){
            /* Initialization */
            bluetoothDevices = new ArrayList<BluetoothDevice>();
            mSelectedBluetoothItems = new ArrayList<Integer>();
            mSelectedBluetoothDevices = new ArrayList<BluetoothDevice>();
            setBluetoothItemsEnabled(true);
            enableBluetoothCheckBox.setChecked(true);
            bluetoothAllCheckbox.setChecked(false);

            //Todo: setallorany
            if(environment.isBluetoothAllOrAny()){
                enableBluetoothCheckBox.setChecked(true);
            }

            /* Populating bluetooth list */
            bluetoothDevices = new BluetoothEnvironmentVariable().getPairedBluetoothDevices(getActivity());
            Vector<BluetoothEnvironmentVariable> bluetoothVariables =  environment.getBluetoothEnvironmentVariables();
            for(int i = 0; i < bluetoothDevices.size(); i++){
                for(int j = 0; j < bluetoothVariables.size(); j++){
                    if(bluetoothVariables.get(j).getDeviceAddress().equals(bluetoothDevices.get(i).getAddress())){
                        mSelectedBluetoothDevices.add(bluetoothDevices.get(i));
                        mSelectedBluetoothItems.add(i);
                    }
                }
            }

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
                    String[] bluetoothDevicesName = new String[bluetoothDevices.size()];

                    for(int i=0;i<bluetoothDevices.size();i++){
                        bluetoothDevicesName[i] = bluetoothDevices.get(i).getName();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.dialog_pick_bluetooth_devices);
                    //Set boolean array for previously checked items
                    boolean[] checkedItems = new boolean[bluetoothDevices.size()];
                    for (Integer mSelectedItem : mSelectedBluetoothItems) {
                        checkedItems[mSelectedItem] = true;
                    }

                    builder.setMultiChoiceItems(bluetoothDevicesName,checkedItems,new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if(isChecked){
                                mSelectedBluetoothItems.add(which);
                            }
                            else{
                                mSelectedBluetoothItems.remove(Integer.valueOf(which));
                            }
                        }
                    });
                    builder.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
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
            setLocationItemsEnabled(true);
            enableLocationCheckBox.setChecked(true);

            //setting current location
            LocationEnvironmentVariable locationVariable = environment.getLocationEnvironmentVariable();
            nameLocationEditText.setText(locationVariable.getLocationName());
            latLocationEditText.setText(""+locationVariable.getLatitude());
            lonLocationEditText.setText(""+locationVariable.getLongitude());
            radLocationEditText.setText(""+locationVariable.getRadius());


            enableLocationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        setLocationItemsEnabled(true);
                    } else {
                        setLocationItemsEnabled(false);
                    }
                }
            });

            selectLocationTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(LOG_TAG, "Starting select location");
                    Intent intent = new Intent(getActivity(),SelectLocation.class);
                    LocationEnvironmentVariable location = environment.getLocationEnvironmentVariable();
                    Bundle bundle = new Bundle();
                    bundle.putDouble(SelectLocation.INTENT_EXTRA_SELECTED_LATITUDE,location.getLatitude());
                    bundle.putDouble(SelectLocation.INTENT_EXTRA_SELECTED_LONGITUDE,location.getLongitude());
                    intent.putExtras(bundle);
                    Toast.makeText(getActivity(),"Select Location edit",Toast.LENGTH_SHORT).show();
                    getActivity().startActivityForResult(intent,REQUEST_LOCATION_SELECT);
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
                //Todo: check for unique environment name
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

                environmentVariables.add(new LocationEnvironmentVariable((float)latLocation,(float)lonLocation,(int)radLocation,locationName));
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
            //Todo: Update the database

            //environment.insertIntoDatabase(getActivity());
            getActivity().finish();

        }
        public void onCancelButtonClick(){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.alert_cancel_add_environment_title).setMessage(R.string.alert_cancel_add_environment_message);
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
