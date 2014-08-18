package com.pvsagar.smartlockscreen.applogic_objects;

import android.util.Log;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;

/**
 * Created by aravind on 10/8/14.
 */
public class BluetoothEnvironmentVariable extends EnvironmentVariable {
    private static final String LOG_TAG = BluetoothEnvironmentVariable.class.getSimpleName();

    private static final int NUMBER_OF_STRING_VALUES = 2;
    private static final int INDEX_DEVICE_NAME = 0;
    private static final int INDEX_DEVICE_ADDRESS = 1;

    public BluetoothEnvironmentVariable() {
        super(EnvironmentVariable.TYPE_BLUETOOTH_DEVICES, 0 , NUMBER_OF_STRING_VALUES);
    }

    public BluetoothEnvironmentVariable(String deviceName, String deviceAddress){
        super(EnvironmentVariable.TYPE_BLUETOOTH_DEVICES, null,
                new String[]{deviceName, deviceAddress});
    }

    @Override
    public boolean isStringValuesSupported() {
        return false;
    }

    @Override
    public boolean isFloatValuesSupported() {
        return false;
    }

    public String getDeviceName(){
        try {
            return getStringValue(INDEX_DEVICE_NAME);
        } catch (Exception e){
            Log.e(LOG_TAG, "Internal application error, please file a bug report to developer."
                    + e.getMessage());
            return null;
        }
    }

    public String getDeviceAddress(){
        try {
            return getStringValue(INDEX_DEVICE_ADDRESS);
        } catch (Exception e){
            Log.e(LOG_TAG, "Internal application error, please file a bug report to developer."
                    + e.getMessage());
            return null;
        }
    }

    public void setDeviceName(String deviceName){
        setStringValue(deviceName, INDEX_DEVICE_NAME);
    }

    public void setDeviceAddress(String deviceAddress){
        setStringValue(deviceAddress, INDEX_DEVICE_ADDRESS);
    }
}
