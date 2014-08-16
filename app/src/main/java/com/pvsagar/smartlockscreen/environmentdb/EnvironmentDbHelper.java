package com.pvsagar.smartlockscreen.environmentdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.GeoFenceEntry;

/**
 * Created by aravind on 10/8/14.
 */
public class EnvironmentDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "environment.db";

    public EnvironmentDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_GEOFENCES = "CREATE TABLE " + GeoFenceEntry.TABLE_NAME + " ( " +
                GeoFenceEntry._ID + " PRIMARY KEY AUTOINCREMENT, " +
                GeoFenceEntry.COLUMN_LOCATION_NAME + " TEXT NOT NULL, " +
                GeoFenceEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                GeoFenceEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +
                GeoFenceEntry.COLUMN_RADIUS + " REAL NOT NULL, " +
                "UNIQUE (" + GeoFenceEntry.COLUMN_COORD_LAT + ", " +
                GeoFenceEntry.COLUMN_COORD_LONG + ", " + GeoFenceEntry.COLUMN_RADIUS +
                ") ON CONFLICT IGNORE, UNIQUE (" + GeoFenceEntry.COLUMN_LOCATION_NAME + ")" +
                " ON CONFLICT IGNORE);";


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
