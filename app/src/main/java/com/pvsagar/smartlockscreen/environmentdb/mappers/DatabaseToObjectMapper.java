package com.pvsagar.smartlockscreen.environmentdb.mappers;

import android.database.Cursor;
import android.util.Log;

import com.pvsagar.smartlockscreen.applogic_objects.BluetoothEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.LocationEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.NoiseLevelEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.WiFiEnvironmentVariable;
import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.BluetoothDevicesEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.EnvironmentEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.WiFiNetworksEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 2/9/14.
 *
 * This class maps the database cursors with values, to objects defined by the classes in
 * applogic_objects.
 */
public class DatabaseToObjectMapper {
    private static final String LOG_TAG = DatabaseToObjectMapper.class.getSimpleName();

    /**
     * Converts cursor containing data from bluetooth_devices table, to objects of
     * BluetoothEnvironmentVariable
     * @param bluetoothCursor
     * @return List of BluetoothEnvironmentVariables
     */
    public static List<EnvironmentVariable> getBluetoothEnvironmentVariablesFromCursor
            (Cursor bluetoothCursor){
        ArrayList<EnvironmentVariable> environmentVariables =
                new ArrayList<EnvironmentVariable>();
        try {
            if (bluetoothCursor.moveToFirst()) {
                for (; !bluetoothCursor.isAfterLast(); bluetoothCursor.moveToNext()) {
                    environmentVariables.add(new BluetoothEnvironmentVariable(
                            bluetoothCursor.getString(bluetoothCursor.getColumnIndex(
                                    BluetoothDevicesEntry.COLUMN_DEVICE_NAME)),
                            bluetoothCursor.getString(bluetoothCursor.getColumnIndex(
                                    BluetoothDevicesEntry.COLUMN_DEVICE_ADDRESS))));
                }
            }
        } catch (Exception e){
            Log.w(LOG_TAG, e + ": " + e.getMessage());
        }
        return environmentVariables;
    }

    public static List<EnvironmentVariable> getWiFiEnvironmentVariablesFromCursor
            (Cursor wifiCursor){
        ArrayList<EnvironmentVariable> environmentVariables =
                new ArrayList<EnvironmentVariable>();
        try {
            if (wifiCursor.moveToFirst()) {
                for (; !wifiCursor.isAfterLast(); wifiCursor.moveToNext()) {
                    environmentVariables.add(new WiFiEnvironmentVariable(
                            wifiCursor.getString(wifiCursor.getColumnIndex(
                                    WiFiNetworksEntry.COLUMN_SSID)),
                            wifiCursor.getString(wifiCursor.getColumnIndex(
                                    WiFiNetworksEntry.COLUMN_ENCRYPTION_TYPE))));
                }
            }
        } catch (Exception e){
            Log.w(LOG_TAG, e + ": " + e.getMessage());
        }
        return environmentVariables;
    }

    public static List<EnvironmentVariable> getNoiseLevelEnvironmentVariablesFromCursor
            (Cursor environmentCursor){
        ArrayList<EnvironmentVariable> environmentVariables =
                new ArrayList<EnvironmentVariable>();
        try {
            if (environmentCursor.moveToFirst()) {
                for (; !environmentCursor.isAfterLast(); environmentCursor.moveToNext()) {
                    boolean minNoiseEnabled = environmentCursor.getInt(environmentCursor.
                            getColumnIndex(EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED)) == 1,
                            maxNoiseEnabled = environmentCursor.getInt(environmentCursor.
                            getColumnIndex(EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED)) == 1;
                    if(minNoiseEnabled || maxNoiseEnabled){
                        environmentVariables.add(new NoiseLevelEnvironmentVariable
                                (minNoiseEnabled, maxNoiseEnabled));
                    }
                }
            }
        } catch (Exception e){
            Log.w(LOG_TAG, e + ": " + e.getMessage());
        }
        return environmentVariables;
    }

    public static List<EnvironmentVariable> getLocationEnvironmentVariablesFromCursor
            (Cursor cursor){
        ArrayList<EnvironmentVariable> locationEnvironmentVariables =
                new ArrayList<EnvironmentVariable>();
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            LocationEnvironmentVariable variable = new LocationEnvironmentVariable(
                    cursor.getFloat(cursor.getColumnIndex(
                            EnvironmentDatabaseContract.GeoFenceEntry.COLUMN_COORD_LAT)),
                    cursor.getFloat(cursor.getColumnIndex(
                            EnvironmentDatabaseContract.GeoFenceEntry.COLUMN_COORD_LONG)),
                    (int) cursor.getFloat(cursor.getColumnIndex(
                            EnvironmentDatabaseContract.GeoFenceEntry.COLUMN_RADIUS)),
                    cursor.getString(cursor.getColumnIndex(
                            EnvironmentDatabaseContract.GeoFenceEntry.COLUMN_LOCATION_NAME))
            );
            locationEnvironmentVariables.add(variable);
        }
        return locationEnvironmentVariables;
    }

}
