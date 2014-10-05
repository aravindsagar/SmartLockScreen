package com.pvsagar.smartlockscreen;

import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pvsagar.smartlockscreen.applogic_objects.BluetoothEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.LocationEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.applogic_objects.WiFiEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.NoSecurity;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Password;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Pattern;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Pin;
import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.cards.EnableDisableCardHeader;
import com.pvsagar.smartlockscreen.cards.EnvironmentCardHeader;
import com.pvsagar.smartlockscreen.cards.PassphraseCardHeader;
import com.pvsagar.smartlockscreen.services.BaseService;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardView;

//TODO see whether the location name entered exists for other lat/long/radius entries, and take appropriate actions

public class EditEnvironment extends ActionBarActivity {

    private static final String LOG_TAG = EditEnvironment.class.getSimpleName();
    private static final int REQUEST_CREATE_PATTERN = 32;
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
    private static List<EnvironmentVariable> storedLocations;
    private static int mSelectedLocationItem;
    private static LocationEnvironmentVariable mSelectedLocation;

    /* Passphrase */
    private static ArrayAdapter<String> passphraseAdapter;
    private static int selectedPassphrasetype;

    private static List<Integer> pattern;

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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.action_bar_edit_environment));
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

    public static class PlaceholderFragment extends Fragment {

        Environment environment;
        String environmentName;

        /* Variables containing the UI elements */
        /* Environment Details */
        private CardView environmentCardView;
        private EditText environmentNameEditText;
        private EditText environmentHintEditText;
        /* Bluetooth */
        private CardView bluetoothCardView;
        private CheckBox enableBluetoothCheckBox;
        private TextView selectBluetoothDevicesTextView;
        private CheckBox bluetoothAllCheckbox;
        /* WiFi */
        private CardView wifiCardView;
        private CheckBox enableWiFiCheckBox;
        private TextView selectWiFiConnectionTextView;
        /* Location */
        private CardView locationCardView;
        private CheckBox enableLocationCheckBox;
        private EditText nameLocationEditText;
        private EditText latLocationEditText;
        private EditText lonLocationEditText;
        private EditText radLocationEditText;
        private TextView selectLocationTextView;
        private TextView selectStoredLocationTextView;

        /* Passphrase */
        private CardView passphraseCardView;
        private Spinner passphraseTypeSpinner;
        private EditText passphraseEditText;
        private EditText passphraseConfirmationEditText;
        private TextView passphraseEnterPatternTextView;

        /* Misc */
        int listPreferredItemHeight;
        int textViewTouchedColor, textViewNormalColor;
        LinearLayout.LayoutParams marginTopLayoutParams;

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
            environmentCardView = (CardView) rootView.findViewById(R.id.card_environment_basic);
            //Bluetooth
            bluetoothCardView = (CardView) rootView.findViewById(R.id.card_bluetooth);
            //WiFi
            wifiCardView = (CardView) rootView.findViewById(R.id.card_wifi);
            //Location
            locationCardView = (CardView) rootView.findViewById(R.id.card_location);
            //Passphrase
            passphraseCardView = (CardView) rootView.findViewById(R.id.card_passphrase);
            //Misc
            listPreferredItemHeight = (int) getListPreferredItemHeight();
            textViewNormalColor = Color.argb(0, 0, 0, 0);
            textViewTouchedColor = getResources().getColor(R.color.text_view_touched);
            marginTopLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            marginTopLayoutParams.topMargin = convertDipToPx(8);

            /* Initialization */
            setUpActionBar();
            setUpEditEnvironment();
            /* End of Initialization */

            return rootView;
        }

        private float getListPreferredItemHeight(){
            android.util.TypedValue value = new android.util.TypedValue();
            boolean b = getActivity().getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
            String s = TypedValue.coerceToString(value.type, value.data);
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            return value.getDimension(metrics);
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
            actionBar.setBackgroundDrawable(new ColorDrawable(
                    getResources().getColor(R.color.action_bar_edit_environment)));
            /* End of Action Bar Code */
        }

        private void setUpEditEnvironment(){
            /* Environment Details */
            setupEnvironmentBasic();
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
                        Log.d(LOG_TAG, "Adding " + bluetoothDevices.get(i).getName() + " to mSelectedBluetoothDevices.");
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
            Passphrase passphrase = User.getCurrentUser(getActivity()).getPassphraseForEnvironment(getActivity(), environment);
            passphraseEditText.setHint("(Unchanged)");
            if(passphrase.getPassphraseType().equals(Passphrase.TYPE_PASSWORD)) {
                passphraseTypeSpinner.setSelection(Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD);
                selectedPassphrasetype = Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD;
            } else if(passphrase.getPassphraseType().equals(Passphrase.TYPE_PATTERN)){
                passphraseTypeSpinner.setSelection(Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN);
                selectedPassphrasetype = Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN;
                passphraseEnterPatternTextView.setText(getString(R.string.text_view_enter_pattern_after_entry));
            } else if(passphrase.getPassphraseType().equals(Passphrase.TYPE_PIN)){
                passphraseTypeSpinner.setSelection(Passphrase.INDEX_PASSPHRASE_TYPE_PIN);
                selectedPassphrasetype = Passphrase.INDEX_PASSPHRASE_TYPE_PIN;
            } else if(passphrase.getPassphraseType().equals(Passphrase.TYPE_NONE)){
                passphraseTypeSpinner.setSelection(Passphrase.INDEX_PASSPHRASE_TYPE_NONE);
                selectedPassphrasetype = Passphrase.INDEX_PASSPHRASE_TYPE_NONE;
            }
        }

        private void setupEnvironmentBasic(){

            environmentHintEditText = new EditText(getActivity());
            environmentHintEditText.setHint(getString(R.string.edit_text_environment_hint));
            environmentHintEditText.setSingleLine(true);
            environmentHintEditText.setLayoutParams(marginTopLayoutParams);

            environmentNameEditText = new EditText(getActivity());
            environmentNameEditText.setHint(getString(R.string.edit_text_environment_name));
            environmentNameEditText.setSingleLine(true);
            environmentNameEditText.setLayoutParams(marginTopLayoutParams);

            Card environmentCard = new Card(getActivity());
            ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().enableForExpandAction();
            environmentCard.setViewToClickToExpand(viewToClickToExpand);

            CardHeader environmentCardHeader = new EnvironmentCardHeader(getActivity(),
                    new EnvironmentCardHeader.InnerViewElementsSetUpListener() {
                        @Override
                        public void onInnerViewElementsSetUp(EnvironmentCardHeader header) {
                            header.setTitle("Environment");
                        }
                    });

            environmentCard.addCardHeader(environmentCardHeader);
            CardExpand environmentCardExpand = new CardEnvironmentExpand(getActivity());
            environmentCard.addCardExpand(environmentCardExpand);

            environmentCardView.setCard(environmentCard);
            environmentCard.doExpand();
        }

        /**
         * Sets up the bluetooth UI elements including action listeners.
         */
        public void setUpBluetoothElements(){

            /* Initialization */
            bluetoothDevices = new ArrayList<BluetoothDevice>();
            mSelectedBluetoothItems = new ArrayList<Integer>();
            mSelectedBluetoothDevices = new ArrayList<BluetoothDevice>();

            /* Text View */
            selectBluetoothDevicesTextView = new TextView(getActivity());
            selectBluetoothDevicesTextView.setText(getString(R.string.text_view_bluetooth_devices_select));
            selectBluetoothDevicesTextView.setMinHeight(listPreferredItemHeight);
            selectBluetoothDevicesTextView.setGravity(Gravity.CENTER_VERTICAL);
            selectBluetoothDevicesTextView.setPadding((int) getResources().getDimension(R.dimen.activity_vertical_margin), 0, 0, 0);
            selectBluetoothDevicesTextView.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Medium);
            selectBluetoothDevicesTextView.setOnTouchListener(new TextViewTouchListener());
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
                    builder.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSelectedBluetoothDevices.clear();
                            for (Integer mSelectedItem : mSelectedBluetoothItems) {
                                mSelectedBluetoothDevices.add(bluetoothDevices.get(mSelectedItem));
                            }
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });

            bluetoothAllCheckbox = new CheckBox(getActivity());
            bluetoothAllCheckbox.setText(getString(R.string.checkbox_bluetooth_all));

            final Card bluetoothCard = new Card(getActivity());
            ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().enableForExpandAction();
            bluetoothCard.setViewToClickToExpand(viewToClickToExpand);

            EnableDisableCardHeader bluetoothCardHeader = new EnableDisableCardHeader(getActivity(),
                    new EnableDisableCardHeader.InnerViewElementsSetUpListener() {
                        @Override
                        public void onInnerViewElementsSetUp(EnableDisableCardHeader header) {
                            setBluetoothItemsEnabled(false);
                            bluetoothAllCheckbox.setChecked(false);

                            header.setTitle(getString(R.string.text_view_bluetooth));
                            enableBluetoothCheckBox = header.getCheckBox();

                            /* CheckBox CheckedChange Listener */
                            enableBluetoothCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {
                                        bluetoothCard.doExpand();
                                        ArrayList<BluetoothDevice> bluetoothDevices = BluetoothEnvironmentVariable.getPairedBluetoothDevices(getActivity());
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
                                        bluetoothCard.doCollapse();
                                        //Disable the list
                                        EditEnvironment.bluetoothDevices = new ArrayList<BluetoothDevice>();
                                        setBluetoothItemsEnabled(false);
                                    }
                                }
                            });
                        }
                    });

            bluetoothCard.addCardHeader(bluetoothCardHeader);
            bluetoothCard.addCardExpand(new CardBluetoothExpand(getActivity()));
            bluetoothCardView.setCard(bluetoothCard);
        }

        /**
         * Sets up the WifI UI elements including action listeners.
         */
        public void setUpWiFiElements(){
            wifiConfigurations = new ArrayList<WifiConfiguration>();
            mSelectedWiFiItem = -1;

            selectWiFiConnectionTextView = new TextView(getActivity());
            selectWiFiConnectionTextView.setText(getString(R.string.text_view_wifi_connection_select));
            selectWiFiConnectionTextView.setMinHeight(listPreferredItemHeight);
            selectWiFiConnectionTextView.setGravity(Gravity.CENTER_VERTICAL);
            selectWiFiConnectionTextView.setPadding((int) getResources().getDimension(R.dimen.activity_vertical_margin), 0, 0, 0);
            selectWiFiConnectionTextView.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Medium);
            selectWiFiConnectionTextView.setOnTouchListener(new TextViewTouchListener());
            selectWiFiConnectionTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] wifiConnectionNames = new String[wifiConfigurations.size()];
                    for (int i = 0; i < wifiConfigurations.size(); i++) {
                        wifiConnectionNames[i] = wifiConfigurations.get(i).SSID;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.dialog_pick_wifi_connection);
                    builder.setSingleChoiceItems(wifiConnectionNames, mSelectedWiFiItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSelectedWiFiItem = which;
                            mSelectedWifiConfiguration = wifiConfigurations.get(which);
                        }
                    });
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();

                }
            });

            final Card wifiCard = new Card(getActivity());
            ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().enableForExpandAction();
            wifiCard.setViewToClickToExpand(viewToClickToExpand);

            EnableDisableCardHeader wifiCardHeader = new EnableDisableCardHeader(getActivity(),
                    new EnableDisableCardHeader.InnerViewElementsSetUpListener() {
                        @Override
                        public void onInnerViewElementsSetUp(EnableDisableCardHeader header) {
                            setWiFiItemsEnabled(false);
                            enableWiFiCheckBox = header.getCheckBox();
                            /* Check Box listener */
                            enableWiFiCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {
                                        //WiFi is enabled
                                        wifiCard.doExpand();
                                        wifiConfigurations = WiFiEnvironmentVariable.getConfiguredWiFiConnections(getActivity());
                                        setWiFiItemsEnabled(true);
                                        if (wifiConfigurations == null) {
                                            //No WiFi adapter Found
                                            enableWiFiCheckBox.setChecked(false);
                                            setWiFiItemsEnabled(false);
                                        }
                                    } else {
                                        wifiCard.doCollapse();
                                        setWiFiItemsEnabled(false);
                                    }
                                }
                            });
                            header.setTitle(getString(R.string.text_view_wifi));
                        }
                    });
            wifiCard.addCardHeader(wifiCardHeader);
            wifiCard.addCardExpand(new CardWifiExpand(getActivity()));

            wifiCardView.setCard(wifiCard);
        }

        /**
         * Sets up the location UI elements including action listeners.
         */
        public void setUpLocationElements(){

            nameLocationEditText = new EditText(getActivity());
            nameLocationEditText.setHint(getString(R.string.edit_text_location_name));
            nameLocationEditText.setLayoutParams(marginTopLayoutParams);
            latLocationEditText = new EditText(getActivity());
            latLocationEditText.setHint(getString(R.string.edit_text_location_lat));
            latLocationEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            latLocationEditText.setLayoutParams(marginTopLayoutParams);
            lonLocationEditText = new EditText(getActivity());
            lonLocationEditText.setHint(getString(R.string.edit_text_location_lon));
            lonLocationEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            lonLocationEditText.setLayoutParams(marginTopLayoutParams);
            radLocationEditText = new EditText(getActivity());
            radLocationEditText.setHint(getString(R.string.edit_text_location_rad));
            radLocationEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            radLocationEditText.setLayoutParams(marginTopLayoutParams);

            /* Select location form Map */
            selectLocationTextView = new TextView(getActivity());
            selectLocationTextView.setText(getString(R.string.text_view_select_location));
            selectLocationTextView.setMinHeight(listPreferredItemHeight);
            selectLocationTextView.setGravity(Gravity.CENTER_VERTICAL);
            selectLocationTextView.setPadding((int) getResources().getDimension(R.dimen.activity_vertical_margin), 0, 0, 0);
            selectLocationTextView.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Medium);
            selectLocationTextView.setOnTouchListener(new TextViewTouchListener());
            selectLocationTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(LOG_TAG, "Starting select location");
                    Intent intent = new Intent(getActivity(), SelectLocation.class);
                    getActivity().startActivityForResult(intent, REQUEST_LOCATION_SELECT);
                }
            });

            /* Select stored location */
            selectStoredLocationTextView = new TextView(getActivity());
            selectStoredLocationTextView.setText(getString(R.string.text_view__select_stored_location));
            selectStoredLocationTextView.setMinHeight(listPreferredItemHeight);
            selectStoredLocationTextView.setGravity(Gravity.CENTER_VERTICAL);
            selectStoredLocationTextView.setPadding((int) getResources().getDimension(R.dimen.activity_vertical_margin), 0, 0, 0);
            selectStoredLocationTextView.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Medium);
            selectStoredLocationTextView.setOnTouchListener(new TextViewTouchListener());
            selectStoredLocationTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] locationNames = new String[storedLocations.size()];
                    for (int i = 0; i < storedLocations.size(); i++) {
                        locationNames[i] = ((LocationEnvironmentVariable) storedLocations.get(i)).getLocationName();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.dialog_pick_location_connection);
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

            final Card locationCard = new Card(getActivity());
            ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().enableForExpandAction();
            locationCard.setViewToClickToExpand(viewToClickToExpand);

            EnableDisableCardHeader locationCardHeader = new EnableDisableCardHeader(getActivity(),
                    new EnableDisableCardHeader.InnerViewElementsSetUpListener() {
                        @Override
                        public void onInnerViewElementsSetUp(EnableDisableCardHeader header) {
                            setLocationItemsEnabled(false);
                            enableLocationCheckBox = header.getCheckBox();
                            enableLocationCheckBox.setChecked(false);
                            enableLocationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {
                                        locationCard.doExpand();
                                        setLocationItemsEnabled(true);
                                        storedLocations = LocationEnvironmentVariable.getLocationEnvironmentVariables(getActivity());
                                        mSelectedLocationItem = -1;
                                    } else {
                                        locationCard.doCollapse();
                                        setLocationItemsEnabled(false);
                                    }
                                }
                            });
                            header.setTitle(getString(R.string.text_view_location));
                        }
                    });
            locationCard.addCardHeader(locationCardHeader);
            locationCard.addCardExpand(new CardLocationExpand(getActivity()));

            locationCardView.setCard(locationCard);
        }

        /**
         * Sets up the passphrase UI elements including action listeners.
         */
        public void setUpPassphraseElements(){

            passphraseEditText = new EditText(getActivity());
            passphraseEditText.setLayoutParams(marginTopLayoutParams);
            passphraseConfirmationEditText = new EditText(getActivity());
            passphraseConfirmationEditText.setLayoutParams(marginTopLayoutParams);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            passphraseEnterPatternTextView = new TextView(getActivity());
            passphraseEnterPatternTextView.setText(getString(R.string.text_view_enter_pattern));
            passphraseEnterPatternTextView.setMinHeight(listPreferredItemHeight);
            passphraseEnterPatternTextView.setGravity(Gravity.CENTER_VERTICAL);
            passphraseEnterPatternTextView.setPadding((int) getResources().getDimension(R.dimen.activity_vertical_margin), 0, 0, 0);
            passphraseEnterPatternTextView.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Medium);
            passphraseEnterPatternTextView.setLayoutParams(params);
            passphraseEnterPatternTextView.setOnTouchListener(new TextViewTouchListener());
            passphraseEnterPatternTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent patternIntent = new Intent(getActivity(), StorePattern.class);
                    startActivityForResult(patternIntent, REQUEST_CREATE_PATTERN);
                }
            });

            final Card passphraseCard = new Card(getActivity());
            ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().enableForExpandAction();
            passphraseCard.setViewToClickToExpand(viewToClickToExpand);

            PassphraseCardHeader passphraseCardHeader = new PassphraseCardHeader(getActivity(),
                    new PassphraseCardHeader.InnerViewElementsSetUpListener() {
                        @Override
                        public void onInnerViewElementsSetUp(PassphraseCardHeader header) {
                            header.setTitle(getString(R.string.text_view_passphrase));
                            passphraseTypeSpinner = header.getSpinner();
                            //Adapter for spinner
                            passphraseAdapter = new ArrayAdapter<String>(getActivity(),
                                    android.R.layout.simple_spinner_dropdown_item, Passphrase.passphraseTypes);

                            passphraseTypeSpinner.setAdapter(passphraseAdapter);
                            passphraseTypeSpinner.setSelection(Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD);
                            selectedPassphrasetype = Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD;
                            passphraseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    passphraseEditText.setHint("Set " + Passphrase.passphraseTypes[position]);
                                    passphraseConfirmationEditText.setHint("Confirm " + Passphrase.passphraseTypes[position]);
                                    selectedPassphrasetype = position;
                                    if (position == Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD) {
                                        setPassphraseItemsEnabled(true);
                                        setPassphraseItemsVisible(true);
                                        setPatternTextViewVisible(false);
                                        passphraseEditText.setText("");
                                        passphraseConfirmationEditText.setText("");
                                        passphraseEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                        passphraseConfirmationEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                        passphraseEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                        passphraseConfirmationEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                        pattern = null;
                                        passphraseCard.doExpand();
                                    } else if (position == Passphrase.INDEX_PASSPHRASE_TYPE_PIN) {
                                        setPassphraseItemsEnabled(true);
                                        setPassphraseItemsVisible(true);
                                        setPatternTextViewVisible(false);
                                        passphraseEditText.setText("");
                                        passphraseConfirmationEditText.setText("");
                                        passphraseEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
                                        passphraseConfirmationEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
                                        passphraseEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                        passphraseConfirmationEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                        pattern = null;
                                        passphraseCard.doExpand();
                                    } else if (position == Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN) {
                                        setPassphraseItemsEnabled(false);
                                        setPassphraseItemsVisible(false);
                                        setPatternTextViewVisible(true);
                                        passphraseCard.doExpand();
                                    } else if (position == Passphrase.INDEX_PASSPHRASE_TYPE_NONE) {
                                        setPassphraseItemsEnabled(false);
                                        setPassphraseItemsVisible(false);
                                        setPatternTextViewVisible(false);
                                        pattern = null;
                                        passphraseCard.doCollapse();
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                    passphraseTypeSpinner.setSelection(Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD);
                                    selectedPassphrasetype = Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD;
                                    setPassphraseItemsEnabled(true);
                                    setPassphraseItemsVisible(true);
                                    passphraseEditText.setText("");
                                    passphraseConfirmationEditText.setText("");
                                    passphraseEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                    passphraseConfirmationEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                    passphraseEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    passphraseConfirmationEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    passphraseCard.doExpand();
                                }
                            });

                        }
                    });
            passphraseCard.addCardHeader(passphraseCardHeader);
            passphraseCard.addCardExpand(new CardPassphraseExpand(getActivity()));

            passphraseCardView.setCard(passphraseCard);
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

        public void setPassphraseItemsEnabled(boolean flag){
            passphraseEditText.setEnabled(flag);
            passphraseConfirmationEditText.setEnabled(flag);
        }

        public void setPassphraseItemsVisible(boolean flag){
            if(flag){
                passphraseEditText.setVisibility(View.VISIBLE);
                passphraseConfirmationEditText.setVisibility(View.VISIBLE);
            }
            else {
                passphraseEditText.setVisibility(View.INVISIBLE);
                passphraseConfirmationEditText.setVisibility(View.INVISIBLE);
            }
        }

        public void setPatternTextViewVisible(boolean flag){
            if(flag){
                passphraseEnterPatternTextView.setVisibility(View.VISIBLE);
            }
            else {
                passphraseEnterPatternTextView.setVisibility(View.GONE);
            }
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
            newEnvironment.updateInDatabase(getActivity(),this.environmentName);

            /* Updating passphrase */
            if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_NONE || (!passphraseEditText.getText().toString().equals("") &&
                    passphraseConfirmationEditText.getText().toString().equals(passphraseEditText.getText().toString())) ||
                    pattern != null){
                //Password changed
                Log.d(LOG_TAG, "Password changed. Updating in db.");
                if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD){
                    Password password = new Password(passphraseEditText.getText().toString());
                    User.getDefaultUser(getActivity()).setPassphraseForEnvironment(getActivity(),password,newEnvironment);
                }
                else if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_PIN){
                    Pin pin = new Pin(passphraseEditText.getText().toString());
                    User.getDefaultUser(getActivity()).setPassphraseForEnvironment(getActivity(),pin,newEnvironment);
                } else if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_NONE){
                    NoSecurity noSecurity = new NoSecurity();
                    User.getDefaultUser(getActivity()).setPassphraseForEnvironment(getActivity(), noSecurity, newEnvironment);
                } else if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN){
                    Pattern pattern1 = new Pattern(pattern);
                    User.getDefaultUser(getActivity()).setPassphraseForEnvironment(getActivity(), pattern1, newEnvironment);
                }
            }
            /* done with updating passphrase */
            getActivity().startService(BaseService.getServiceIntent(getActivity(), null,
                    BaseService.ACTION_ADD_GEOFENCES));
            getActivity().startService(BaseService.getServiceIntent(getActivity(), null,
                    BaseService.ACTION_DETECT_WIFI));
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

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if(requestCode == REQUEST_CREATE_PATTERN){
                if (resultCode == RESULT_OK) {
                    pattern = data.getIntegerArrayListExtra(StorePattern.EXTRA_PATTERN);
                    passphraseEnterPatternTextView.setText(getString(R.string.text_view_enter_pattern_after_entry));
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

        private class CardBluetoothExpand extends CardExpand {
            public CardBluetoothExpand(Context context) {
                super(context);
            }

            @Override
            public View getInnerView(Context context, ViewGroup parent) {
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(selectBluetoothDevicesTextView);
                layout.addView(bluetoothAllCheckbox);
                parent.addView(layout);
                return layout;
            }
        }

        private class CardWifiExpand extends CardExpand {
            public CardWifiExpand(Context context) {
                super(context);
            }

            @Override
            public View getInnerView(Context context, ViewGroup parent) {
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(selectWiFiConnectionTextView);
                parent.addView(layout);
                return layout;
            }
        }

        private class CardLocationExpand extends CardExpand {
            public CardLocationExpand(Context context) {
                super(context);
            }

            @Override
            public View getInnerView(Context context, ViewGroup parent) {
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(selectLocationTextView);
                layout.addView(selectStoredLocationTextView);
                layout.addView(nameLocationEditText);
                layout.addView(latLocationEditText);
                layout.addView(lonLocationEditText);
                layout.addView(radLocationEditText);
                parent.addView(layout);
                return layout;
            }
        }

        private class CardPassphraseExpand extends CardExpand {
            public CardPassphraseExpand(Context context) {
                super(context);
            }

            @Override
            public View getInnerView(Context context, ViewGroup parent) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                RelativeLayout relativeLayout = new RelativeLayout(getActivity());
                LinearLayout layout = new LinearLayout(context);
                layout.setLayoutParams(params);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(passphraseEditText);
                layout.addView(passphraseConfirmationEditText);
                relativeLayout.addView(layout);
                relativeLayout.addView(passphraseEnterPatternTextView);
                parent.addView(relativeLayout);
                return layout;
            }
        }

        private class CardEnvironmentExpand extends CardExpand {
            public CardEnvironmentExpand(Context context) {
                super(context);
            }

            @Override
            public View getInnerView(Context context, ViewGroup parent) {
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(environmentNameEditText);
                layout.addView(environmentHintEditText);
                parent.addView(layout);
                return layout;
            }
        }

        private int convertDipToPx(int pixel){
            float scale = getResources().getDisplayMetrics().density;
            return (int) ((pixel * scale) + 0.5f);
        }

        public class TextViewTouchListener implements View.OnTouchListener{
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundColor(textViewTouchedColor);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.setBackgroundColor(textViewNormalColor);
                }
                return false;
            }
        }
    }
}
