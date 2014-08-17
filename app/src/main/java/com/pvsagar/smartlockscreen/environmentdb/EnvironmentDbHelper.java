package com.pvsagar.smartlockscreen.environmentdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.GeoFenceEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.BluetoothDevicesEntry;

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

        //TODO: remaining tables

        db.execSQL(SQL_CREATE_GEOFENCES);
        db.execSQL(SQL_CREATE_BLUETOOTH_DEVICES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
