package com.pvsagar.smartlockscreen.applogic_helpers;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.pvsagar.smartlockscreen.applogic_objects.BluetoothEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.LocationEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.WiFiEnvironmentVariable;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.BluetoothDevicesEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.EnvironmentEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.GeoFenceEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.WiFiNetworksEntry;

import java.util.Vector;

/**
 * Created by aravind on 10/8/14.
 * Helper class for insertion, fetching etc of environments.
 */
public class EnvironmentHelper {

    public static void insertEnvironment(Environment e, Context context){
        ContentValues environmentValues = new ContentValues();
        environmentValues.put(EnvironmentEntry.COLUMN_NAME, e.getName());
        environmentValues.put(EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED,
                e.hasNoiseLevel && e.getNoiseLevelEnvironmentVariable().hasUpperLimit);
        environmentValues.put(EnvironmentEntry.COLUMN_IS_WIFI_ENABLED, 0);
        environmentValues.put(EnvironmentEntry.COLUMN_IS_LOCATION_ENABLED, 0);
        environmentValues.put(EnvironmentEntry.COLUMN_MAX_NOISE_LEVEL,
                e.getNoiseLevelEnvironmentVariable().getUpperLimit());
        environmentValues.put(EnvironmentEntry.COLUMN_MIN_NOISE_LEVEL,
                e.getNoiseLevelEnvironmentVariable().getLowerLimit());
        environmentValues.put(EnvironmentEntry.COLUMN_BLUETOOTH_ALL_OR_ANY,
                e.isBluetoothAllOrAny()?1:0);
        environmentValues.put(EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED,
                e.hasNoiseLevel && e.getNoiseLevelEnvironmentVariable().hasLowerLimit);
        environmentValues.put(EnvironmentEntry.COLUMN_IS_BLUETOOTH_ENABLED, 0);

        Uri environmentUri = context.getContentResolver().insert(EnvironmentEntry.CONTENT_URI,
                environmentValues);
        long environmentId = EnvironmentEntry.getEnvironmentIdFromUri(environmentUri);

        if(e.hasBluetoothDevices){
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
        if(e.hasLocation){
            LocationEnvironmentVariable variable = e.getLocationEnvironmentVariable();
            Uri insertUri = EnvironmentEntry.buildEnvironmentUriWithIdAndLocation(environmentId);
            ContentValues locationValues = new ContentValues();
            locationValues.put(GeoFenceEntry.COLUMN_COORD_LAT, variable.getLatitude());
            locationValues.put(GeoFenceEntry.COLUMN_COORD_LONG, variable.getLongitude());
            locationValues.put(GeoFenceEntry.COLUMN_RADIUS, variable.getRadius());
            locationValues.put(GeoFenceEntry.COLUMN_LOCATION_NAME, variable.getLocationName());
            context.getContentResolver().insert(insertUri, locationValues);
        }
        if(e.hasWiFiNetwork){
            WiFiEnvironmentVariable variable = e.getWiFiEnvironmentVariable();
            Uri insertUri = EnvironmentEntry.buildEnvironmentUriWithIdAndWifi(environmentId);
            ContentValues wifiValues = new ContentValues();
            wifiValues.put(WiFiNetworksEntry.COLUMN_SSID, variable.getSSID());
            wifiValues.put(WiFiNetworksEntry.COLUMN_ENCRYPTION_TYPE, variable.getEncryptionType());
            context.getContentResolver().insert(insertUri, wifiValues);
        }
    }

}
