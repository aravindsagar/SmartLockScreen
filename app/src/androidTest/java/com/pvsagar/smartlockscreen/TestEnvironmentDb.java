package com.pvsagar.smartlockscreen;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.BluetoothDevicesEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.GeoFenceEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by aravind on 17/8/14.
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
        if(geofenceCursor.moveToFirst()){
            validateCursor(geofenceValues, geofenceCursor);
        }
        else{
            fail("No geofence data returned.");
        }

        //Testing bluetooth devices table
        ContentValues bluetoothDeviceValues = getBluetoothDeviceValues();
        long bluetoothDeviceId = db.insert(BluetoothDevicesEntry.TABLE_NAME, null,
                bluetoothDeviceValues);
        assertTrue(bluetoothDeviceId != -1);

        Cursor bluetoothDeviceCursor = db.query(BluetoothDevicesEntry.TABLE_NAME,
                null, null, null, null, null, null);
        if(bluetoothDeviceCursor.moveToFirst()){
            validateCursor(bluetoothDeviceValues, bluetoothDeviceCursor);
        }
        else{
            fail("No bluetooth device data returned.");
        }
    }

    static private void validateCursor(ContentValues expectedValues, Cursor valueCursor) {
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
        final String deviceName = "Nexus 4";
        final String deviceAddress = "C4:66:9B:0F:00:DB";
        values.put(BluetoothDevicesEntry.COLUMN_DEVICE_NAME, deviceName);
        values.put(BluetoothDevicesEntry.COLUMN_DEVICE_ADDRESS, deviceAddress);
        return values;
    }
}
