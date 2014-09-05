package com.pvsagar.smartlockscreen.applogic_objects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.EnvironmentEntry;
import com.pvsagar.smartlockscreen.environmentdb.mappers.DatabaseToObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by aravind on 10/8/14.
 */
public class Environment {
    private static final String LOG_TAG = Environment.class.getSimpleName();

    private long id;
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
                ContentValues bluetoothValues = variable.getContentValues();
                context.getContentResolver().insert(insertUri, bluetoothValues);
            }
        }
        if(e.hasLocation && e.getLocationEnvironmentVariable() != null){
            LocationEnvironmentVariable variable = e.getLocationEnvironmentVariable();
            Uri insertUri = EnvironmentEntry.buildEnvironmentUriWithIdAndLocation(environmentId);
            ContentValues locationValues = variable.getContentValues();
            context.getContentResolver().insert(insertUri, locationValues);
        }
        if(e.hasWiFiNetwork && e.getWiFiEnvironmentVariable() != null){
            WiFiEnvironmentVariable variable = e.getWiFiEnvironmentVariable();
            Uri insertUri = EnvironmentEntry.buildEnvironmentUriWithIdAndWifi(environmentId);
            ContentValues wifiValues = variable.getContentValues();
            context.getContentResolver().insert(insertUri, wifiValues);
        }
        e.id = environmentId;
    }

    /**
     * Update the environment entry in database. Old name of the environment should be passed if
     * name has changed. If oldName is null, current name of the environment is taken and used for
     * finding the records to update
     * @param context activity/service context
     * @param oldName The name of the environment to be modified
     * @return success code
     */
    public boolean updateInDatabase(Context context, String oldName){
        if(oldName == null || oldName.isEmpty()){
            oldName = getName();
        }
        Environment oldEnvironment = getFullEnvironment(context, oldName);
        if(oldEnvironment == null) return false;
        ContentValues environmentValues = new ContentValues();
        environmentValues.put(EnvironmentEntry.COLUMN_NAME, getName());
        environmentValues.put(EnvironmentEntry.COLUMN_BLUETOOTH_ALL_OR_ANY,
                isBluetoothAllOrAny()?1:0);
        environmentValues.put(EnvironmentEntry.COLUMN_IS_ENABLED, isEnabled()?1:0);
        environmentValues.put(EnvironmentEntry.COLUMN_ENVIRONMENT_HINT, getHint());
        if(hasNoiseLevel && getNoiseLevelEnvironmentVariable() != null){
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED,
                    getNoiseLevelEnvironmentVariable().hasUpperLimit);
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED,
                    getNoiseLevelEnvironmentVariable().hasLowerLimit);
            environmentValues.put(EnvironmentEntry.COLUMN_MAX_NOISE_LEVEL,
                    getNoiseLevelEnvironmentVariable().getUpperLimit());
            environmentValues.put(EnvironmentEntry.COLUMN_MIN_NOISE_LEVEL,
                    getNoiseLevelEnvironmentVariable().getLowerLimit());
        } else {
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED, false);
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED, false);
        }
        context.getContentResolver().update(EnvironmentEntry.
                buildEnvironmentUriWithId(oldEnvironment.id), environmentValues, null, null);
        if(oldEnvironment.hasLocation){
            if(hasLocation && !
                    oldEnvironment.locationEnvironmentVariable.equals(locationEnvironmentVariable)){
                context.getContentResolver().update(
                        EnvironmentEntry.buildEnvironmentUriWithIdAndLocation(oldEnvironment.id),
                                locationEnvironmentVariable.getContentValues(), null, null);
            }
            else if(!hasLocation){
                context.getContentResolver().delete(EnvironmentEntry.
                        buildEnvironmentUriWithIdAndLocation(oldEnvironment.id), null, null);
            }
        } else if(hasLocation) {
            context.getContentResolver().insert(EnvironmentEntry.
                    buildEnvironmentUriWithIdAndLocation(oldEnvironment.id),
                    locationEnvironmentVariable.getContentValues());
        }

        if(oldEnvironment.hasWiFiNetwork){
            if(hasWiFiNetwork && !
                    oldEnvironment.wiFiEnvironmentVariable.equals(wiFiEnvironmentVariable)){
                context.getContentResolver().update(
                        EnvironmentEntry.buildEnvironmentUriWithIdAndWifi(oldEnvironment.id),
                        wiFiEnvironmentVariable.getContentValues(), null, null);
            }
            else if(!hasWiFiNetwork){
                context.getContentResolver().delete(EnvironmentEntry.
                        buildEnvironmentUriWithIdAndWifi(oldEnvironment.id), null, null);
            }
        } else if(hasWiFiNetwork) {
            context.getContentResolver().insert(EnvironmentEntry.
                            buildEnvironmentUriWithIdAndWifi(oldEnvironment.id),
                    wiFiEnvironmentVariable.getContentValues());
        }

        context.getContentResolver().delete(EnvironmentEntry.
                buildEnvironmentUriWithIdAndBluetooth(oldEnvironment.id), null, null);
        if(hasBluetoothDevices && getBluetoothEnvironmentVariables() != null
                && !getBluetoothEnvironmentVariables().isEmpty()){
            Vector<BluetoothEnvironmentVariable> bluetoothEnvironmentVariables =
                    getBluetoothEnvironmentVariables();
            Uri insertUri = EnvironmentEntry.
                    buildEnvironmentUriWithIdAndBluetooth(oldEnvironment.id);
            for(BluetoothEnvironmentVariable variable: bluetoothEnvironmentVariables) {
                ContentValues bluetoothValues = variable.getContentValues();
                context.getContentResolver().insert(insertUri, bluetoothValues);
            }
        }
        return true;
    }

    /**
     * Sets the enabled flag of any environment in the database
     * @param context activity/service context
     * @param environmentName Name of the environment whose enabled flag should b changed
     * @param enabled The new enabled value
     */
    public static void setEnabledInDatabase(Context context, String environmentName,
                                               boolean enabled){
        ContentValues environmentValues = new ContentValues();
        environmentValues.put(EnvironmentEntry.COLUMN_IS_ENABLED, enabled);
        Environment e = getBareboneEnvironment(context, environmentName);
        long id;
        if(e != null) {
            id = e.id;
            context.getContentResolver().update(EnvironmentEntry.buildEnvironmentUriWithId(id),
                    environmentValues, null, null);
        }
    }

    /**
     * Returns list of names of all the environment in the database.
     * To get the full environment details, see getFullEnvironment()
     * @param context activity/service context
     * @return A list of strings of environment names
     */
    public static List<String> getAllEnvironmentNames(Context context){
        Cursor envCursor = context.getContentResolver().query(EnvironmentEntry.CONTENT_URI, null,
                null, null, null);
        ArrayList<String> environmentNames = new ArrayList<String>();

        for(envCursor.moveToFirst(); !envCursor.isAfterLast(); envCursor.moveToNext()){
            String envName = envCursor.getString(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_NAME));
            environmentNames.add(envName);
        }
        envCursor.close();
        return environmentNames;
    }

    /**
     * Returns a list of Environment instances, without the environment variables populated,
     * for all environments in the database. Useful for getting id, name, isEnabled and Hint
     * of all environments
     * @param context Activity/ service context
     * @return List of barebone Environment instances (without the Environment Variables populated)
     */
    public static List<Environment> getAllEnvironmentBarebones(Context context){
        Cursor envCursor = context.getContentResolver().query(EnvironmentEntry.CONTENT_URI, null,
                null, null, null);
        ArrayList<Environment> environments = new ArrayList<Environment>();

        for(envCursor.moveToFirst(); !envCursor.isAfterLast(); envCursor.moveToNext()){
            String envName = envCursor.getString(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_NAME));
            String envHint = envCursor.getString(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_ENVIRONMENT_HINT));
            long id = envCursor.getLong(envCursor.getColumnIndex(
                    EnvironmentEntry._ID));
            boolean enabled = envCursor.getInt(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_IS_ENABLED)) == 1;
            Environment e = new Environment();
            e.setName(envName);
            e.setHint(envHint);
            e.setEnabled(enabled);
            e.id = id;
            environments.add(e);
        }
        envCursor.close();
        return environments;
    }

    /**
     * Get the complete environment details of an environment from the database
     * @param context activity/service context
     * @param environmentName Name of the environment whose details are required
     * @return An instance of Environment with the required details
     */
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
            e.id = environmentId;
            envCursor.close();
            bluetoothCursor.close();
            wifiCursor.close();
            locationCursor.close();
        } else {
            return null;
        }
        return e;
    }

    /**
     * Returns an environemt instance of the environment specified by its name. Only name, hint, id
     * and enabled flag are populated. The environment variables are not populated
     * @param context Activity/ service context
     * @param environmentName Name of the environment whose details are required
     * @return Instance of environment with specified fields populated.
     */
    public static Environment getBareboneEnvironment(Context context, String environmentName){
        String selection = EnvironmentEntry.COLUMN_NAME + " = ? ";
        String[] selectionArgs = new String[]{environmentName};
        Cursor envCursor = context.getContentResolver().query(EnvironmentEntry.CONTENT_URI, null,
                selection, selectionArgs, null);
        Environment e = new Environment();
        if(envCursor.moveToFirst()){
            String envName = envCursor.getString(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_NAME));
            String envHint = envCursor.getString(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_ENVIRONMENT_HINT));
            long id = envCursor.getLong(envCursor.getColumnIndex(
                    EnvironmentEntry._ID));
            boolean enabled = envCursor.getInt(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_IS_ENABLED)) == 1;
            e.setName(envName);
            e.setHint(envHint);
            e.setEnabled(enabled);
            e.id = id;
        }
        return e;
    }

    /**
     * Function to delete an environment from the database
     * @param context Activity/ service context
     * @param environmentName Name of the environment to be deleted
     */
    public static void deleteEnvironmentFromDatabase(Context context, String environmentName){
        Environment e = getBareboneEnvironment(context, environmentName);
        if(e != null){
            context.getContentResolver().delete(EnvironmentEntry.
                    buildEnvironmentUriWithIdAndBluetooth(e.id), null, null);
            context.getContentResolver().delete(EnvironmentEntry.
                    buildEnvironmentUriWithIdAndWifi(e.id), null, null);
            context.getContentResolver().delete(EnvironmentEntry.
                    buildEnvironmentUriWithIdAndLocation(e.id), null, null);
            context.getContentResolver().delete(EnvironmentEntry.buildEnvironmentUriWithId(
                    e.id), null, null);
        }
    }
}
