package com.pvsagar.smartlockscreen;

import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardView;

//TODO see whether the location name entered exists for other lat/long/radius entries, and take appropriate actions

public class AddEnvironment extends ActionBarActivity {

    private static final String LOG_TAG = AddEnvironment.class.getSimpleName();
    private static final int REQUEST_CREATE_PATTERN = 32;
    public static final int REQUEST_LOCATION_SELECT = 31;
    public static final String INTENT_EXTRA_SELECTED_LOCATION = "selectedLocation";

    /* These variables hold the data of the present environment being added */
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

    private static List<Integer> pattern;

    private PlaceholderFragment placeholderFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_environment);
        if (savedInstanceState == null) {
            placeholderFragment = new PlaceholderFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, placeholderFragment)
                    .commit();
        }

    }


    @Override
    public void onBackPressed() {
        // Confirm the user about exiting
        placeholderFragment.onCancelButtonClick();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == BluetoothEnvironmentVariable.REQUEST_BLUETOOTH_ENABLE){

            // Checking whether bluetooth is enabled or not
            if(resultCode == RESULT_OK){
                //Bluetooth enable request success: the following piece of code will find the list
                //of paired devices and populate the list
                //Get the list of paired devices, populate the list, set the adapter
                Toast.makeText(getBaseContext(),"Bluetooth switched on",Toast.LENGTH_SHORT).show();
                placeholderFragment.setBluetoothCheckBoxChecked(true);
                ArrayList<BluetoothDevice> bluetoothDevices = BluetoothEnvironmentVariable.getPairedBluetoothDevices(this);
                ArrayList<String> deviceNamesArrayList = new ArrayList<String>();
                if(bluetoothDevices != null) {
                    //Populate the String list and Set the adapter for the list view
                    for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
                        deviceNamesArrayList.add(bluetoothDevice.getName());
                    }
                    AddEnvironment.bluetoothDevices = bluetoothDevices;
                    placeholderFragment.setBluetoothItemsEnabled(true);
                }
            }
            else{
                //Bluetooth enable request failed: the following peice of code will disable the
                // bluetooth items in the UI
                //Un check the checkbox, disable the list
                Toast.makeText(getBaseContext(),"Unable to switch on Bluetooth",Toast.LENGTH_SHORT).show();
                placeholderFragment.setBluetoothCheckBoxChecked(false);
                AddEnvironment.bluetoothDevices = new ArrayList<BluetoothDevice>();
                placeholderFragment.setBluetoothItemsEnabled(false);

            }
        }
        else if(requestCode == REQUEST_LOCATION_SELECT){
            if(resultCode == RESULT_OK){
                // The select location request is success: will update the location UI elements.
                Bundle bundle = data.getExtras();
                Location location = (Location)bundle.get(AddEnvironment.INTENT_EXTRA_SELECTED_LOCATION);
                placeholderFragment.latLocationEditText.setText(""+location.getLatitude());
                placeholderFragment.lonLocationEditText.setText(""+location.getLongitude());
            }
        }
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

        /* Misc */
        int listPreferredItemHeight;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_add_environment, container, false);

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

            /* Initialization */
            setupEnvironmentBasic();
            setUpBluetoothElements();
            setUpWiFiElements();
            setUpLocationElements();
            setUpPassphraseElements();
            setUpActionBar();

            return rootView;
        }

        /**
         * Sets up the contextual action bar containing Done and cance button.
         * This will populate a custom made action bar from @layout/actionbar_custom_view_done_cancel.xml
         */
        private void setUpActionBar(){
            /* Adding Contextual Action Bar with Done and Cancel Button */
            final LayoutInflater layoutInflater = (LayoutInflater) ((ActionBarActivity)getActivity()).getSupportActionBar().getThemedContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        private void setupEnvironmentBasic(){
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = convertDipToPx(14);

            environmentHintEditText = new EditText(getActivity());
            environmentHintEditText.setHint(getString(R.string.edit_text_environment_hint));
            environmentHintEditText.setSingleLine(true);
            environmentHintEditText.setLayoutParams(layoutParams);

            environmentNameEditText = new EditText(getActivity());
            environmentNameEditText.setHint(getString(R.string.edit_text_environment_name));
            environmentNameEditText.setSingleLine(true);
            environmentNameEditText.setLayoutParams(layoutParams);

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

        private float getListPreferredItemHeight(){
            android.util.TypedValue value = new android.util.TypedValue();
            boolean b = getActivity().getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
            String s = TypedValue.coerceToString(value.type, value.data);
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            return value.getDimension(metrics);
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
                                            AddEnvironment.bluetoothDevices = bluetoothDevices;
                                            setBluetoothItemsEnabled(true);


                                        } else {
                                            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                            if (mBluetoothAdapter == null) {
                                                enableBluetoothCheckBox.setChecked(false);
                                                //Disable the list
                                                AddEnvironment.bluetoothDevices = new ArrayList<BluetoothDevice>();
                                                setBluetoothItemsEnabled(false);
                                            }
                                        }

                                    } else {
                                        bluetoothCard.doCollapse();
                                        //Disable the list
                                        AddEnvironment.bluetoothDevices = new ArrayList<BluetoothDevice>();
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
            LinearLayout.LayoutParams marginTopLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            marginTopLayoutParams.topMargin = convertDipToPx(14);

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
            selectLocationTextView.setLayoutParams(marginTopLayoutParams);
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
            LinearLayout.LayoutParams marginTopLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            marginTopLayoutParams.topMargin = convertDipToPx(14);

            passphraseEditText = new EditText(getActivity());
            passphraseEditText.setLayoutParams(marginTopLayoutParams);
            passphraseConfirmationEditText = new EditText(getActivity());
            passphraseConfirmationEditText.setLayoutParams(marginTopLayoutParams);

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

                                        Intent patternIntent = new Intent(getActivity(), StorePattern.class);
                                        startActivityForResult(patternIntent, REQUEST_CREATE_PATTERN);
                                        passphraseCard.doCollapse();
                                    } else if (position == Passphrase.INDEX_PASSPHRASE_TYPE_NONE) {
                                        setPassphraseItemsEnabled(false);
                                        setPassphraseItemsVisible(false);
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

        /**
         * Enables or disables the bluetooth UI elements
         * @param flag true: enable | false: disable
         */
        public void setBluetoothItemsEnabled(boolean flag){
            selectBluetoothDevicesTextView.setEnabled(flag);
            bluetoothAllCheckbox.setEnabled(flag);
        }

        /**
         * Enables or disables the WiFi UI elements
         * @param flag true: enable | false: disable
         */
        public void setWiFiItemsEnabled(boolean flag){
            selectWiFiConnectionTextView.setEnabled(flag);
        }

        /**
         * Enables or disables the location UI elements
         * @param flag true: enable | false: disable
         */
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

        /**
         * When the done button is clicked in contextual action bar, the following function is called.
         * The function parses the data for adding an environment, and updates it to the database.
         */
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

                environmentVariables.add(new LocationEnvironmentVariable(latLocation,lonLocation,(int)radLocation,locationName));
            }

            if(!bluetoothFlag && !wifiFlag && !locationFlag){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.alert_no_variables_title).setMessage(R.string.alert_no_variables_message);
                builder.setPositiveButton(R.string.ok,null);
                builder.create().show();
                return;
            }

            /* Passphrase */

            if((selectedPassphrasetype != Passphrase.INDEX_PASSPHRASE_TYPE_NONE &&
                    selectedPassphrasetype != Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN &&
                    passphraseEditText.getText().toString().equals("")) ||
                    selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN &&
                    pattern == null){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.alert_no_passphrase_title).setMessage(R.string.alert_no_passphrase_message);
                builder.setPositiveButton(R.string.ok,null);
                builder.create().show();
                return;
            } else{
                if (selectedPassphrasetype != Passphrase.INDEX_PASSPHRASE_TYPE_NONE &&
                        selectedPassphrasetype != Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN &&
                        !passphraseEditText.getText().toString().equals(passphraseConfirmationEditText.getText().toString())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.alert_no_passphrase_match_title).setMessage(R.string.alert_no_passphrase_match_message);
                    builder.setPositiveButton(R.string.ok,null);
                    builder.create().show();
                    return;
                }
            }
            /* Data Parsed */

            /* Creating Environment */
            Environment environment = new Environment(environmentName,environmentVariables);
            if(bluetoothFlag){
                environment.setBluetoothAllOrAny(bluetoothAllFlag);
            }
            environment.setHint(environmentHint);
            environment.insertIntoDatabase(getActivity());
            /* Setting passphrase */
            if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD){
                Password password = new Password(passphraseEditText.getText().toString());
                User.getDefaultUser(getActivity()).setPassphraseForEnvironment(getActivity(),password,environment);
            } else if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_PIN){
                Pin pin = new Pin(passphraseEditText.getText().toString());
                User.getDefaultUser(getActivity()).setPassphraseForEnvironment(getActivity(),pin,environment);
            } else if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_NONE){
                NoSecurity noSecurity = new NoSecurity();
                User.getDefaultUser(getActivity()).setPassphraseForEnvironment(getActivity(),noSecurity,environment);
            } else if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN) {
                Pattern pattern1 = new Pattern(pattern);
                User.getDefaultUser(getActivity()).setPassphraseForEnvironment(getActivity(),pattern1,environment);
            }
            /* done with setting passphrase */
            getActivity().startService(BaseService.getServiceIntent(getActivity(), null,
                    BaseService.ACTION_ADD_GEOFENCES));
            getActivity().startService(BaseService.getServiceIntent(getActivity(), null,
                    BaseService.ACTION_DETECT_WIFI));
            getActivity().startService(BaseService.getServiceIntent(getActivity(), null,
                    BaseService.ACTION_DETECT_ENVIRONMENT));
            getActivity().finish();

        }

        /**
         * When the cancel button is clicked in contextual action bar, the following function is called.
         * The function confirms the cancelling of adding environment and quits the activity, if confirmed.
         */
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

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if(requestCode == REQUEST_CREATE_PATTERN){
                if (resultCode == RESULT_OK) {
                    pattern = data.getIntegerArrayListExtra(StorePattern.EXTRA_PATTERN);
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
                layout.addView(getNewSeparatorView());
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
                layout.addView(getNewSeparatorView());
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
                layout.addView(getNewSeparatorView());
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
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(getNewSeparatorView());
                layout.addView(passphraseEditText);
                layout.addView(passphraseConfirmationEditText);
                parent.addView(layout);
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
                layout.addView(getNewSeparatorView());
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

        private View getNewSeparatorView(){
            View view = new View(getActivity());
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    convertDipToPx(2)));
            view.setBackgroundColor(Color.parseColor("#2B497B"));
            return view;
        }

        public void setBluetoothCheckBoxChecked(boolean checked){
            enableBluetoothCheckBox.setChecked(checked);
        }
    }
}
