package com.pvsagar.smartlockscreen.applogic_objects;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract;

import java.util.Vector;

/**
 * Created by aravind on 10/8/14.
 */
public class Environment {
    private LocationEnvironmentVariable locationEnvironmentVariable;
    private Vector<BluetoothEnvironmentVariable> bluetoothEnvironmentVariables;
    //true for all, false for any
    private boolean bluetoothAllOrAny;
    private WiFiEnvironmentVariable wiFiEnvironmentVariable;
    private NoiseLevelEnvironmentVariable noiseLevelEnvironmentVariable;
    private String name;

    public boolean hasLocation, hasBluetoothDevices, hasWiFiNetwork, hasNoiseLevel;
    public Environment(){
        hasLocation = hasNoiseLevel = hasWiFiNetwork = hasBluetoothDevices = false;
        bluetoothAllOrAny = false;
    }

    public Environment(String name, EnvironmentVariable... variables){
        this();
        setName(name);
        addEnvironmentVariables(variables);
    }

    public void setName(String name){
        if(name != null) {
            this.name = name;
        } else {
            throw new IllegalArgumentException("Name cannot be null.");
        }
    }

    public void setBluetoothAllOrAny(boolean b){
        bluetoothAllOrAny = b;
    }

    public void addEnvironmentVariables(EnvironmentVariable... variables){
        for(EnvironmentVariable variable: variables){
            if(variable.getVariableType().equals(EnvironmentVariable.TYPE_LOCATION)){
                addLocationVariable((LocationEnvironmentVariable) variable);
            }
            if(variable.getVariableType().equals(EnvironmentVariable.TYPE_BLUETOOTH_DEVICES)){
                addBluetoothDevicesEnvironmentVariable((BluetoothEnvironmentVariable) variable);
            }
            if(variable.getVariableType().equals(EnvironmentVariable.TYPE_WIFI_NETWORKS)){
                addWiFiEnvironmentVariable((WiFiEnvironmentVariable) variable);
            }
            if(variable.getVariableType().equals(EnvironmentVariable.TYPE_NOISE_LEVEL)){
                addNoiseLevelEnvironmentVariable((NoiseLevelEnvironmentVariable) variable);
            }
        }
    }

    public void addLocationVariable(LocationEnvironmentVariable variable){
        locationEnvironmentVariable = variable;
        hasLocation = variable != null;
    }

    public void addBluetoothDevicesEnvironmentVariable(BluetoothEnvironmentVariable variable){
        if(variable != null) {
            if (bluetoothEnvironmentVariables == null) {
                bluetoothEnvironmentVariables = new Vector<BluetoothEnvironmentVariable>();
            }
            bluetoothEnvironmentVariables.add(variable);
            hasBluetoothDevices = true;
        } else {
            bluetoothEnvironmentVariables = null;
            hasBluetoothDevices = false;
        }
    }

    public void addWiFiEnvironmentVariable(WiFiEnvironmentVariable variable){
        wiFiEnvironmentVariable = variable;
        hasWiFiNetwork = variable != null;
    }

    public void addNoiseLevelEnvironmentVariable(NoiseLevelEnvironmentVariable variable){
        noiseLevelEnvironmentVariable = variable;
        hasNoiseLevel = variable != null;
    }

    public LocationEnvironmentVariable getLocationEnvironmentVariable(){
        return locationEnvironmentVariable;
    }

    public Vector<BluetoothEnvironmentVariable> getBluetoothEnvironmentVariables(){
        return bluetoothEnvironmentVariables;
    }

    public WiFiEnvironmentVariable getWiFiEnvironmentVariable(){
        return wiFiEnvironmentVariable;
    }

    public NoiseLevelEnvironmentVariable getNoiseLevelEnvironmentVariable(){
        return noiseLevelEnvironmentVariable;
    }

    public String getName(){
        return name;
    }

    public boolean isBluetoothAllOrAny(){
        return bluetoothAllOrAny;
    }

    public void insertIntoDatabase(Context context){
        Environment e = this;
        ContentValues environmentValues = new ContentValues();
        environmentValues.put(EnvironmentDatabaseContract.EnvironmentEntry.COLUMN_NAME,
                e.getName());
        environmentValues.put(
                EnvironmentDatabaseContract.EnvironmentEntry.COLUMN_IS_WIFI_ENABLED, 0);
        environmentValues.put(
                EnvironmentDatabaseContract.EnvironmentEntry.COLUMN_IS_LOCATION_ENABLED, 0);
        environmentValues.put(
                EnvironmentDatabaseContract.EnvironmentEntry.COLUMN_BLUETOOTH_ALL_OR_ANY,
                e.isBluetoothAllOrAny()?1:0);
        environmentValues.put(
                EnvironmentDatabaseContract.EnvironmentEntry.COLUMN_IS_BLUETOOTH_ENABLED, 0);

        if(e.hasNoiseLevel && e.getNoiseLevelEnvironmentVariable() != null){
            environmentValues.put(
                    EnvironmentDatabaseContract.EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED,
                    e.getNoiseLevelEnvironmentVariable().hasUpperLimit);
            environmentValues.put(
                    EnvironmentDatabaseContract.EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED,
                    e.getNoiseLevelEnvironmentVariable().hasLowerLimit);
            environmentValues.put(
                    EnvironmentDatabaseContract.EnvironmentEntry.COLUMN_MAX_NOISE_LEVEL,
                    e.getNoiseLevelEnvironmentVariable().getUpperLimit());
            environmentValues.put(
                    EnvironmentDatabaseContract.EnvironmentEntry.COLUMN_MIN_NOISE_LEVEL,
                    e.getNoiseLevelEnvironmentVariable().getLowerLimit());
        } else {
            environmentValues.put(
                    EnvironmentDatabaseContract.EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED,
                    false);
            environmentValues.put(
                    EnvironmentDatabaseContract.EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED,
                    false);
        }

        Uri environmentUri = context.getContentResolver().insert(
                EnvironmentDatabaseContract.EnvironmentEntry.CONTENT_URI, environmentValues);
        long environmentId = EnvironmentDatabaseContract.EnvironmentEntry.
                getEnvironmentIdFromUri(environmentUri);

        if(e.hasBluetoothDevices && e.getBluetoothEnvironmentVariables() != null
                && !e.getBluetoothEnvironmentVariables().isEmpty()){
            Vector<BluetoothEnvironmentVariable> bluetoothEnvironmentVariables =
                    e.getBluetoothEnvironmentVariables();
            Uri insertUri = EnvironmentDatabaseContract.EnvironmentEntry.buildEnvironmentUriWithIdAndBluetooth(environmentId);
            for(BluetoothEnvironmentVariable variable: bluetoothEnvironmentVariables) {
                ContentValues bluetoothValues = new ContentValues();
                bluetoothValues.put(EnvironmentDatabaseContract.BluetoothDevicesEntry.COLUMN_DEVICE_NAME,
                        variable.getDeviceName());
                bluetoothValues.put(EnvironmentDatabaseContract.BluetoothDevicesEntry.COLUMN_DEVICE_ADDRESS,
                        variable.getDeviceAddress());
                context.getContentResolver().insert(insertUri, bluetoothValues);
            }
        }
        if(e.hasLocation && e.getLocationEnvironmentVariable() != null){
            LocationEnvironmentVariable variable = e.getLocationEnvironmentVariable();
            Uri insertUri = EnvironmentDatabaseContract.EnvironmentEntry.buildEnvironmentUriWithIdAndLocation(environmentId);
            ContentValues locationValues = new ContentValues();
            locationValues.put(EnvironmentDatabaseContract.GeoFenceEntry.COLUMN_COORD_LAT, variable.getLatitude());
            locationValues.put(EnvironmentDatabaseContract.GeoFenceEntry.COLUMN_COORD_LONG, variable.getLongitude());
            locationValues.put(EnvironmentDatabaseContract.GeoFenceEntry.COLUMN_RADIUS, variable.getRadius());
            locationValues.put(EnvironmentDatabaseContract.GeoFenceEntry.COLUMN_LOCATION_NAME, variable.getLocationName());
            context.getContentResolver().insert(insertUri, locationValues);
        }
        if(e.hasWiFiNetwork && e.getWiFiEnvironmentVariable() != null){
            WiFiEnvironmentVariable variable = e.getWiFiEnvironmentVariable();
            Uri insertUri = EnvironmentDatabaseContract.EnvironmentEntry.buildEnvironmentUriWithIdAndWifi(environmentId);
            ContentValues wifiValues = new ContentValues();
            wifiValues.put(EnvironmentDatabaseContract.WiFiNetworksEntry.COLUMN_SSID, variable.getSSID());
            wifiValues.put(EnvironmentDatabaseContract.WiFiNetworksEntry.COLUMN_ENCRYPTION_TYPE, variable.getEncryptionType());
            context.getContentResolver().insert(insertUri, wifiValues);
        }
    }
}
