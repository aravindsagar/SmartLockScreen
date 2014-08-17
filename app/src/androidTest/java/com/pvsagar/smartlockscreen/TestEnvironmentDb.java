package com.pvsagar.smartlockscreen;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.BluetoothDevicesEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.EnvironmentBluetoothEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.EnvironmentEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.GeoFenceEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.PasswordEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.UserPasswordsEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.UsersEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.WiFiNetworksEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by aravind on 17/8/14.
 * Contains the tests for testing creation, insertion and querying of all the tables in
 * environment.db
 */
public class TestEnvironmentDb extends AndroidTestCase {
    private static final String LOG_TAG = TestEnvironmentDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(EnvironmentDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new EnvironmentDbHelper(mContext).getWritableDatabase();
        assertTrue(db.isOpen());
    }

    public void testInsertReadDb() {
        EnvironmentDbHelper dbHelper = new EnvironmentDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Testing geofences table
        ContentValues geofenceValues = getGeofenceContentValues();
        long geofenceId = db.insert(GeoFenceEntry.TABLE_NAME, null, geofenceValues);
        assertTrue(geofenceId != -1);

        Cursor geofenceCursor = db.query(GeoFenceEntry.TABLE_NAME,
                null, null, null, null, null, null);
        validateCursor(geofenceValues, geofenceCursor);

        //Testing bluetooth devices table
        ContentValues bluetoothDeviceValues = getBluetoothDeviceValues();
        long bluetoothDeviceId = db.insert(BluetoothDevicesEntry.TABLE_NAME, null,
                bluetoothDeviceValues);
        assertTrue(bluetoothDeviceId != -1);

        Cursor bluetoothDeviceCursor = db.query(BluetoothDevicesEntry.TABLE_NAME,
                null, null, null, null, null, null);
        validateCursor(bluetoothDeviceValues, bluetoothDeviceCursor);

        //Testing wifi networks table
        ContentValues wifiNetworkValues = getWifiNetworkContentValues();
        long wifiNetworkId = db.insert(WiFiNetworksEntry.TABLE_NAME, null,
                wifiNetworkValues);
        assertTrue(wifiNetworkId != -1);

        Cursor wifiNetworkCursor = db.query(WiFiNetworksEntry.TABLE_NAME,
                null, null, null, null, null, null);
        validateCursor(wifiNetworkValues, wifiNetworkCursor);

        //Testing environments table
        ContentValues environmentValues = getEnvironmentContentValues(geofenceId, wifiNetworkId);
        long environmentId = db.insert(EnvironmentEntry.TABLE_NAME, null,
                environmentValues);
        assertTrue(environmentId != -1);

        Cursor environmentCursor = db.query(EnvironmentEntry.TABLE_NAME,
                null, null, null, null, null, null);
        validateCursor(environmentValues, environmentCursor);

        //Testing environment bluetooth devices table
        ContentValues environmentBluetoothDeviceValues =
                getEnvironmentBluetoothDeviceContentValues(environmentId, bluetoothDeviceId);
        long environmentBluetoothDeviceId = db.insert(EnvironmentBluetoothEntry.TABLE_NAME, null,
                environmentBluetoothDeviceValues);
        assertTrue(environmentBluetoothDeviceId != -1);

        Cursor environmentBluetoothDeviceCursor = db.query(EnvironmentBluetoothEntry.TABLE_NAME,
                null, null, null, null, null, null);
        validateCursor(environmentBluetoothDeviceValues, environmentBluetoothDeviceCursor);

        //Testing users table
        ContentValues userValues = getUserContentValues();
        long userId = db.insert(UsersEntry.TABLE_NAME, null,
                userValues);
        assertTrue(userId != -1);

        Cursor userCursor = db.query(UsersEntry.TABLE_NAME,
                null, null, null, null, null, null);
        validateCursor(userValues, userCursor);

        //Testing passwords table
        ContentValues passwordValues = getPasswordContentValues();
        long passwordId = db.insert(PasswordEntry.TABLE_NAME, null,
                passwordValues);
        assertTrue(passwordId != -1);

        Cursor passwordCursor = db.query(PasswordEntry.TABLE_NAME,
                null, null, null, null, null, null);
        validateCursor(passwordValues, passwordCursor);

        //Testing user passwords table
        ContentValues userPasswordValues = getUserPasswordContentValues
                (userId, environmentId, passwordId);
        long userPasswordId = db.insert(UserPasswordsEntry.TABLE_NAME, null,
                userPasswordValues);
        assertTrue(userPasswordId != -1);

        Cursor userPasswordCursor = db.query(UserPasswordsEntry.TABLE_NAME,
                null, null, null, null, null, null);
        validateCursor(userPasswordValues, userPasswordCursor);
    }

    static private void validateCursor(ContentValues expectedValues, Cursor valueCursor) {
        if(!valueCursor.moveToFirst()){
            Log.d(LOG_TAG, "cursor position:" + String.valueOf(valueCursor.getPosition()));
            fail("No data returned in cursor");
        }
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for(Map.Entry<String, Object> entry : valueSet){
            String columnName = entry.getKey();
            int index = valueCursor.getColumnIndex(columnName);
            assertFalse(index == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(index));
        }
    }

    private ContentValues getGeofenceContentValues() {
        ContentValues values = new ContentValues();
        final double testLatitude = 22.5;
        final double testLongitude = 68.9;
        final int testRadius = 15;
        final String testLocationName = "home";

        values.put(GeoFenceEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(GeoFenceEntry.COLUMN_COORD_LONG, testLongitude);
        values.put(GeoFenceEntry.COLUMN_RADIUS, testRadius);
        values.put(GeoFenceEntry.COLUMN_LOCATION_NAME, testLocationName);
        return values;
    }

    private ContentValues getBluetoothDeviceValues(){
        ContentValues values = new ContentValues();
        final String testDeviceName = "Nexus 4";
        final String testDeviceAddress = "C4:66:9B:0F:00:DB";
        values.put(BluetoothDevicesEntry.COLUMN_DEVICE_NAME, testDeviceName);
        values.put(BluetoothDevicesEntry.COLUMN_DEVICE_ADDRESS, testDeviceAddress);
        return values;
    }

    private ContentValues getWifiNetworkContentValues(){
        ContentValues values = new ContentValues();
        final String testSSID = "homeWifi";
        final String testEncryptionType = "WEP";
        values.put(WiFiNetworksEntry.COLUMN_SSID, testSSID);
        values.put(WiFiNetworksEntry.COLUMN_ENCRYPTION_TYPE, testEncryptionType);
        return values;
    }

    private ContentValues getEnvironmentContentValues(long geofenceId, long wifiNetwokId){
        ContentValues values = new ContentValues();
        final int testEnbled = 1;
        final String testEnvironmentName = "home";
        final double testMaxNoiseLevel = 30.2;
        final double testMinNoiseLevel = 11.1;
        values.put(EnvironmentEntry.COLUMN_NAME, testEnvironmentName);
        values.put(EnvironmentEntry.COLUMN_IS_LOCATION_ENABLED, testEnbled);
        values.put(EnvironmentEntry.COLUMN_GEOFENCE_ID, geofenceId);
        values.put(EnvironmentEntry.COLUMN_IS_BLUETOOTH_ENABLED, testEnbled);
        values.put(EnvironmentEntry.COLUMN_BLUETOOTH_ALL_OR_ANY, testEnbled);
        values.put(EnvironmentEntry.COLUMN_IS_WIFI_ENABLED, testEnbled);
        values.put(EnvironmentEntry.COLUMN_WIFI_ID, wifiNetwokId);
        values.put(EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED, testEnbled);
        values.put(EnvironmentEntry.COLUMN_MAX_NOISE_LEVEL, testMaxNoiseLevel);
        values.put(EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED, testEnbled);
        values.put(EnvironmentEntry.COLUMN_MIN_NOISE_LEVEL,testMinNoiseLevel);
        return values;
    }

    private ContentValues getEnvironmentBluetoothDeviceContentValues
            (long environmentId, long bluetoothDeviceId){
        ContentValues values = new ContentValues();
        values.put(EnvironmentBluetoothEntry.COLUMN_ENVIRONMENT_ID, environmentId);
        values.put(EnvironmentBluetoothEntry.COLUMN_BLUETOOTH_ID, bluetoothDeviceId);
        return values;
    }

    private ContentValues getUserContentValues(){
        ContentValues values = new ContentValues();
        final String testUsername = "sagar";
        values.put(UsersEntry.COLUMN_USER_NAME, testUsername);
        return values;
    }

    private ContentValues getPasswordContentValues(){
        ContentValues values = new ContentValues();
        final String passwordType = "alphabetical";
        final String passwordString = "will be encrypted";
        values.put(PasswordEntry.COLUMN_PASSWORD_TYPE, passwordType);
        values.put(PasswordEntry.COLUMN_PASSWORD_STRING, passwordString);
        return values;
    }

    private ContentValues getUserPasswordContentValues
            (long userId, long environmentId, long passwordId){
        ContentValues values = new ContentValues();
        values.put(UserPasswordsEntry.COLUMN_ENVIRONMENT_ID, environmentId);
        values.put(UserPasswordsEntry.COLUMN_USER_ID, userId);
        values.put(UserPasswordsEntry.COLUMN_PASSWORD_ID, passwordId);
        return values;
    }
}
