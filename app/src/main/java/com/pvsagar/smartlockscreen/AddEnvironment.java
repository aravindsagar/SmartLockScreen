package com.pvsagar.smartlockscreen;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pvsagar.smartlockscreen.applogic_objects.BluetoothEnvironmentVariable;

import java.util.ArrayList;


public class AddEnvironment extends ActionBarActivity {

    private static final String LOG_TAG = AddEnvironment.class.getSimpleName();
    private static ArrayAdapter<String> bluetoothListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_environment);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_environment, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BluetoothEnvironmentVariable.REQUEST_BLUETOOTH_ENABLE){
            CheckBox enableBluetoothCheckBox = (CheckBox)findViewById(R.id.checkbox_enable_bluetooth);
            ListView bluetoothDevicesListView = (ListView)findViewById(R.id.list_view_bluetooth_devices);
            TextView selectBluetoothDevicesTextView = (TextView)findViewById(R.id.text_view_bluetooth_devices_select);
            ArrayAdapter<String> adapter;

            // Checking whether bluetooth is enabled or not
            if(resultCode == RESULT_OK){
                //Bluetooth Enabled
                //Todo: Get the list of paired devices, populate the list, set the adapter
                Toast.makeText(getBaseContext(),"Bluetooth switched on",Toast.LENGTH_SHORT).show();
                enableBluetoothCheckBox.setChecked(true);
                ArrayList<BluetoothDevice> bluetoothDevices = new BluetoothEnvironmentVariable().getPairedBluetoothDevices(this);
                ArrayList<String> deviceNamesArrayList = new ArrayList<String>();
                if(bluetoothDevices != null) {
                    //Todo: Populate the String list and Set the adapter for the list view
                    for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
                        deviceNamesArrayList.add(bluetoothDevice.getName());
                    }
                    selectBluetoothDevicesTextView.setVisibility(View.VISIBLE);
                    displayBluetoothDevices(deviceNamesArrayList);
                }

            }
            else{
                //Bluetooth not enabled
                //Todo: uncheck the checkbox, disable the list
                Toast.makeText(getBaseContext(),"Unable to switch on Bluetooth",Toast.LENGTH_SHORT).show();
                enableBluetoothCheckBox.setChecked(false);
                selectBluetoothDevicesTextView.setVisibility(View.INVISIBLE);
                displayBluetoothDevices(new ArrayList<String>());

            }
        }
    }

    public void displayBluetoothDevices(ArrayList<String> deviceNamesArrayList){
        ListView bluetoothDevicesListView = (ListView)findViewById(R.id.list_view_bluetooth_devices);
        bluetoothListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice, deviceNamesArrayList);
        bluetoothDevicesListView.setAdapter(bluetoothListAdapter);
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

        private ListView bluetoothDevicesListView;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_add_environment, container, false);
            final CheckBox enableBluetoothCheckBox = (CheckBox)rootView.findViewById(R.id.checkbox_enable_bluetooth);
            final TextView selectBluetoothDevicesTextView = (TextView)rootView.findViewById(R.id.text_view_bluetooth_devices_select);
            bluetoothDevicesListView = (ListView)rootView.findViewById(R.id.list_view_bluetooth_devices);
            bluetoothDevicesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            /* Initialization */
            bluetoothListAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_multiple_choice, new ArrayList<String>());
            bluetoothDevicesListView.setAdapter(bluetoothListAdapter);

            selectBluetoothDevicesTextView.setVisibility(View.INVISIBLE);

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
                            selectBluetoothDevicesTextView.setVisibility(View.VISIBLE);
                            ((AddEnvironment)getActivity()).displayBluetoothDevices(deviceNamesArrayList);

                        } else {
                            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (mBluetoothAdapter == null) {
                                enableBluetoothCheckBox.setChecked(false);
                                //Disable the list
                                selectBluetoothDevicesTextView.setVisibility(View.INVISIBLE);
                                ((AddEnvironment)getActivity()).displayBluetoothDevices(new ArrayList<String>());
                            }
                        }

                    } else {
                        //Disable the list
                        selectBluetoothDevicesTextView.setVisibility(View.INVISIBLE);
                        ((AddEnvironment)getActivity()).displayBluetoothDevices(new ArrayList<String>());
                    }
                }
            });
            return rootView;
        }
        /*public void displayBluetoothDevices(ArrayList<String> deviceNamesArrayList){
            bluetoothListAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_multiple_choice, deviceNamesArrayList);
            bluetoothDevicesListView.setAdapter(bluetoothListAdapter);
        }*/



    }
}
