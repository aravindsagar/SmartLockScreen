package com.pvsagar.smartlockscreen.environmentdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.BluetoothDevicesEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.EnvironmentBluetoothEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.EnvironmentEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.GeoFenceEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.WiFiNetworksEntry;

/**
 * Created by aravind on 10/8/14.
 * Extending SQLiteOpenHelper to manage creation and updating of environment database.
 */
public class EnvironmentDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "environment.db";

    public EnvironmentDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * This is where the creation of tables and the initial population of the tables should happen.
     * @param db A database object in which table creation queries should be executed
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        /**
         * Few things to keep in mind:
         * 1. sqlite3 does not have a separate boolean type. Integer is used
         * 2. Validating whether the environment is unique is not included here.
         *    It will be built separately
         * 3. Bluetooth all or any: value of 0 for any, 1 for all
         */
        final String SQL_CREATE_GEOFENCES = "CREATE TABLE " + GeoFenceEntry.TABLE_NAME + " ( " +
                GeoFenceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GeoFenceEntry.COLUMN_LOCATION_NAME + " TEXT NOT NULL, " +
                GeoFenceEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                GeoFenceEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +
                GeoFenceEntry.COLUMN_RADIUS + " INTEGER NOT NULL, " +
                "UNIQUE (" + GeoFenceEntry.COLUMN_COORD_LAT + ", " +
                GeoFenceEntry.COLUMN_COORD_LONG + ", " + GeoFenceEntry.COLUMN_RADIUS +
                ") ON CONFLICT IGNORE, UNIQUE (" + GeoFenceEntry.COLUMN_LOCATION_NAME + ")" +
                " ON CONFLICT IGNORE);";

        final String SQL_CREATE_BLUETOOTH_DEVICES = "CREATE TABLE " +
                BluetoothDevicesEntry.TABLE_NAME + " ( " +
                BluetoothDevicesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BluetoothDevicesEntry.COLUMN_DEVICE_ADDRESS + " TEXT NOT NULL, " +
                BluetoothDevicesEntry.COLUMN_DEVICE_NAME + " TEXT NOT NULL);";

        final String SQL_CREATE_WIFI_NETWORKS = "CREATE TABLE " +
                WiFiNetworksEntry.TABLE_NAME + " ( " +
                WiFiNetworksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WiFiNetworksEntry.COLUMN_SSID + " TEXT NOT NULL, " +
                WiFiNetworksEntry.COLUMN_ENCRYPTION_TYPE + " TEXT NOT NULL, " +
                "UNIQUE (" + WiFiNetworksEntry.COLUMN_SSID + ", " +
                WiFiNetworksEntry.COLUMN_ENCRYPTION_TYPE + ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_ENVIRONMENTS = "CREATE TABLE " +
                EnvironmentEntry.TABLE_NAME + " ( " +
                EnvironmentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EnvironmentEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                EnvironmentEntry.COLUMN_IS_LOCATION_ENABLED + " INTEGER NOT NULL, " +
                EnvironmentEntry.COLUMN_GEOFENCE_ID + " INTEGER, " +
                EnvironmentEntry.COLUMN_IS_BLUETOOTH_ENABLED + " INTEGER NOT NULL, " +
                EnvironmentEntry.COLUMN_BLUETOOTH_ALL_OR_ANY + " INTEGER, " +
                EnvironmentEntry.COLUMN_IS_WIFI_ENABLED + " INTEGER NOT NULL, " +
                EnvironmentEntry.COLUMN_WIFI_ID + " INTEGER, " +
                EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED + " INTEGER NOT NULL, " +
                EnvironmentEntry.COLUMN_MAX_NOISE_LEVEL + " REAL, " +
                EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED + " INTEGER NOT NULL, " +
                EnvironmentEntry.COLUMN_MIN_NOISE_LEVEL + " REAL, " +
                " FOREIGN KEY (" + EnvironmentEntry.COLUMN_GEOFENCE_ID + ") REFERENCES " +
                GeoFenceEntry.TABLE_NAME + "(" + GeoFenceEntry._ID + "), " +
                " FOREIGN KEY (" + EnvironmentEntry.COLUMN_WIFI_ID + ") REFERENCES " +
                WiFiNetworksEntry.TABLE_NAME + "(" + WiFiNetworksEntry._ID + "), " +
                "UNIQUE (" + EnvironmentEntry.COLUMN_NAME + ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_ENVIRONMENT_BLUETOOTH_DEVICES = "CREATE TABLE " +
                EnvironmentBluetoothEntry.TABLE_NAME + " ( " +
                EnvironmentBluetoothEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EnvironmentBluetoothEntry.COLUMN_BLUETOOTH_ID + " INTEGER NOT NULL, " +
                EnvironmentBluetoothEntry.COLUMN_ENVIRONMENT_ID + " INTEGET NOT NULL, " +
                " FOREIGN KEY (" + EnvironmentBluetoothEntry.COLUMN_BLUETOOTH_ID + ") REFERENCES " +
                BluetoothDevicesEntry.TABLE_NAME + "(" + BluetoothDevicesEntry._ID + "), " +
                " FOREIGN KEY (" + EnvironmentBluetoothEntry.COLUMN_ENVIRONMENT_ID +
                ") REFERENCES " + EnvironmentEntry.TABLE_NAME + "(" + EnvironmentEntry._ID + "), " +
                "UNIQUE (" + EnvironmentBluetoothEntry.COLUMN_ENVIRONMENT_ID + ", " +
                EnvironmentBluetoothEntry.COLUMN_BLUETOOTH_ID + ") ON CONFLICT IGNORE);";

        //TODO: remaining tables

        db.execSQL(SQL_CREATE_GEOFENCES);
        db.execSQL(SQL_CREATE_BLUETOOTH_DEVICES);
        db.execSQL(SQL_CREATE_WIFI_NETWORKS);
        db.execSQL(SQL_CREATE_ENVIRONMENTS);
        db.execSQL(SQL_CREATE_ENVIRONMENT_BLUETOOTH_DEVICES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
