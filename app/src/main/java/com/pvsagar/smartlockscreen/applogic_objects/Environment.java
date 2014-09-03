package com.pvsagar.smartlockscreen.applogic_objects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.BluetoothDevicesEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.EnvironmentEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.GeoFenceEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.WiFiNetworksEntry;
import com.pvsagar.smartlockscreen.environmentdb.mappers.DatabaseToObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by aravind on 10/8/14.
 */
public class Environment {
    private static final String LOG_TAG = Environment.class.getSimpleName();

    private LocationEnvironmentVariable locationEnvironmentVariable;
    private Vector<BluetoothEnvironmentVariable> bluetoothEnvironmentVariables;
    //true for all, false for any
    private boolean bluetoothAllOrAny;
    private WiFiEnvironmentVariable wiFiEnvironmentVariable;
    private NoiseLevelEnvironmentVariable noiseLevelEnvironmentVariable;
    private String name, hint;
    private boolean isEnabled;

    public boolean hasLocation, hasBluetoothDevices, hasWiFiNetwork, hasNoiseLevel;
    public Environment(){
        hasLocation = hasNoiseLevel = hasWiFiNetwork = hasBluetoothDevices = false;
        bluetoothAllOrAny = false;
        isEnabled = true;
    }

    public Environment(String name, EnvironmentVariable... variables){
        this();
        setName(name);
        addEnvironmentVariables(variables);
    }

    public Environment(String name, List<EnvironmentVariable> variables){
        this();
        EnvironmentVariable[] variableArray = new EnvironmentVariable[variables.size()];
        variables.toArray(variableArray);
        setName(name);
        addEnvironmentVariables(variableArray);
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

    public boolean isEnabled(){
        return isEnabled;
    }

    public void setEnabled(boolean enabled){
        isEnabled = enabled;
    }

    public String getHint(){
        return hint;
    }

    public void setHint(String hint){
        this.hint = hint;
    }

    public void insertIntoDatabase(Context context){
        Environment e = this;
        ContentValues environmentValues = new ContentValues();
        environmentValues.put(EnvironmentEntry.COLUMN_NAME, e.getName());
        environmentValues.put(EnvironmentEntry.COLUMN_IS_WIFI_ENABLED, 0);
        environmentValues.put(EnvironmentEntry.COLUMN_IS_LOCATION_ENABLED, 0);
        environmentValues.put(EnvironmentEntry.COLUMN_BLUETOOTH_ALL_OR_ANY,
                e.isBluetoothAllOrAny()?1:0);
        environmentValues.put(EnvironmentEntry.COLUMN_IS_BLUETOOTH_ENABLED, 0);
        environmentValues.put(EnvironmentEntry.COLUMN_IS_ENABLED, e.isEnabled()?1:0);
        environmentValues.put(EnvironmentEntry.COLUMN_ENVIRONMENT_HINT, e.getHint());

        if(e.hasNoiseLevel && e.getNoiseLevelEnvironmentVariable() != null){
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED,
                    e.getNoiseLevelEnvironmentVariable().hasUpperLimit);
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED,
                    e.getNoiseLevelEnvironmentVariable().hasLowerLimit);
            environmentValues.put(EnvironmentEntry.COLUMN_MAX_NOISE_LEVEL,
                    e.getNoiseLevelEnvironmentVariable().getUpperLimit());
            environmentValues.put(EnvironmentEntry.COLUMN_MIN_NOISE_LEVEL,
                    e.getNoiseLevelEnvironmentVariable().getLowerLimit());
        } else {
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED, false);
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED, false);
        }

        Uri environmentUri = context.getContentResolver().insert(
                EnvironmentEntry.CONTENT_URI, environmentValues);
        long environmentId = EnvironmentEntry.getEnvironmentIdFromUri(environmentUri);

        if(e.hasBluetoothDevices && e.getBluetoothEnvironmentVariables() != null
                && !e.getBluetoothEnvironmentVariables().isEmpty()){
            Vector<BluetoothEnvironmentVariable> bluetoothEnvironmentVariables =
                    e.getBluetoothEnvironmentVariables();
            Uri insertUri = EnvironmentEntry.buildEnvironmentUriWithIdAndBluetooth(environmentId);
            for(BluetoothEnvironmentVariable variable: bluetoothEnvironmentVariables) {
                ContentValues bluetoothValues = new ContentValues();
                bluetoothValues.put(BluetoothDevicesEntry.COLUMN_DEVICE_NAME,
                        variable.getDeviceName());
                bluetoothValues.put(BluetoothDevicesEntry.COLUMN_DEVICE_ADDRESS,
                        variable.getDeviceAddress());
                context.getContentResolver().insert(insertUri, bluetoothValues);
            }
        }
        if(e.hasLocation && e.getLocationEnvironmentVariable() != null){
            LocationEnvironmentVariable variable = e.getLocationEnvironmentVariable();
            Uri insertUri = EnvironmentEntry.buildEnvironmentUriWithIdAndLocation(environmentId);
            ContentValues locationValues = new ContentValues();
            locationValues.put(GeoFenceEntry.COLUMN_COORD_LAT, variable.getLatitude());
            locationValues.put(GeoFenceEntry.COLUMN_COORD_LONG, variable.getLongitude());
            locationValues.put(GeoFenceEntry.COLUMN_RADIUS, variable.getRadius());
            locationValues.put(GeoFenceEntry.COLUMN_LOCATION_NAME, variable.getLocationName());
            context.getContentResolver().insert(insertUri, locationValues);
        }
        if(e.hasWiFiNetwork && e.getWiFiEnvironmentVariable() != null){
            WiFiEnvironmentVariable variable = e.getWiFiEnvironmentVariable();
            Uri insertUri = EnvironmentEntry.buildEnvironmentUriWithIdAndWifi(environmentId);
            ContentValues wifiValues = new ContentValues();
            wifiValues.put(WiFiNetworksEntry.COLUMN_SSID, variable.getSSID());
            wifiValues.put(WiFiNetworksEntry.COLUMN_ENCRYPTION_TYPE, variable.getEncryptionType());
            context.getContentResolver().insert(insertUri, wifiValues);
        }
    }

    /**
     * Returns list of names of all the environment in the database.
     * To get the full environment details, see getFullEnvironment()
     * @param context
     * @return A list of strings of environment names
     */
    public static List<String> getAllEnvironments(Context context){
        Cursor envCursor = context.getContentResolver().query(EnvironmentEntry.CONTENT_URI, null,
                null, null, null);
        ArrayList<String> environmentNames = new ArrayList<String>();

        for(envCursor.moveToFirst(); !envCursor.isAfterLast(); envCursor.moveToNext()){
            String envName = envCursor.getString(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_NAME));
            environmentNames.add(envName);
        }
        return environmentNames;
    }

    public static Environment getFullEnvironment(Context context, String environmentName){
        String selection = EnvironmentEntry.COLUMN_NAME + " = ? ";
        String[] selectionArgs = new String[]{environmentName};
        Cursor envCursor = context.getContentResolver().query(EnvironmentEntry.CONTENT_URI, null,
                selection, selectionArgs, null);
        Environment e;
        if(envCursor.moveToFirst()){
            long environmentId = envCursor.getLong(envCursor.getColumnIndex(EnvironmentEntry._ID));
            Cursor bluetoothCursor = context.getContentResolver().query
                    (EnvironmentEntry.buildEnvironmentUriWithIdAndBluetooth(environmentId),
                            null, null, null, null),
                    wifiCursor = context.getContentResolver().query(
                            EnvironmentEntry.buildEnvironmentUriWithIdAndWifi(environmentId),
                            null, null, null, null),
                    locationCursor = context.getContentResolver().query(
                            EnvironmentEntry.buildEnvironmentUriWithIdAndLocation(environmentId),
                            null, null, null, null);
            List<EnvironmentVariable> environmentVariables = DatabaseToObjectMapper.
                    getBluetoothEnvironmentVariablesFromCursor(bluetoothCursor);
            environmentVariables.addAll(DatabaseToObjectMapper.
                    getLocationEnvironmentVariablesFromCursor(locationCursor));
            environmentVariables.addAll(DatabaseToObjectMapper.
                    getWiFiEnvironmentVariablesFromCursor(wifiCursor));
            environmentVariables.addAll(DatabaseToObjectMapper.
                    getNoiseLevelEnvironmentVariablesFromCursor(envCursor));
            e = new Environment(environmentName, environmentVariables);

            e.setBluetoothAllOrAny(envCursor.getInt(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_BLUETOOTH_ALL_OR_ANY)) == 1);
            e.setHint(envCursor.getString(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_ENVIRONMENT_HINT)));
            e.setEnabled(envCursor.getInt(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_IS_ENABLED)) == 1);
        } else {
            return null;
        }
        return e;
    }

    //Todo: A function which will take context and environment name and return whether it is enabled or not
    public static boolean getEnvironmentEnabled(Context context, String environmentName){
        String selection = EnvironmentEntry.COLUMN_NAME + " = ? ";
        String[] selectionArgs = new String[]{environmentName};
        Cursor envCursor = context.getContentResolver().query(EnvironmentEntry.CONTENT_URI, null,
                selection, selectionArgs, null);
        Environment e;
        if(envCursor.moveToFirst()){
            long environmentId = envCursor.getLong(envCursor.getColumnIndex(EnvironmentEntry._ID));
            return (envCursor.getInt(envCursor.getColumnIndex(EnvironmentEntry.COLUMN_IS_ENABLED)) == 1?true:false);
        } else {
            return false;
        }
    }
    //Todo: A function which will take the context, environment and boolean to setenabled : directly to database
}
