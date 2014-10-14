package com.pvsagar.smartlockscreen.environmentdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pvsagar.smartlockscreen.backend_helpers.Picture;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.AppWhitelistEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.BluetoothDevicesEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.EnvironmentBluetoothEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.EnvironmentEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.GeoFenceEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.PasswordEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.UserPasswordsEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.UsersEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.WiFiNetworksEntry;

/**
 * Created by aravind on 10/8/14.
 * Extending SQLiteOpenHelper to manage creation and updating of environment database.
 */
public class EnvironmentDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "environment.db";

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
                EnvironmentEntry.COLUMN_IS_ENABLED + " INTEGER NOT NULL, " +
                EnvironmentEntry.COLUMN_ENVIRONMENT_HINT + " TEXT, " +
                EnvironmentEntry.COLUMN_ENVIRONMENT_PICTURE_DESCRIPTION + " TEXT, " +
                EnvironmentEntry.COLUMN_ENVIRONMENT_PICTURE_TYPE + " TEXT, " +
                EnvironmentEntry.COLUMN_ENVIRONMENT_PICTURE + " BLOB, " +
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

        final String SQL_CREATE_USERS = "CREATE TABLE " + UsersEntry.TABLE_NAME + " ( " +
                UsersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UsersEntry.COLUMN_USER_NAME + " TEXT NOT NULL, " +
                UsersEntry.COLUMN_USER_PICTURE_DESCRIPTION + " TEXT, " +
                UsersEntry.COLUMN_USER_PICTURE_TYPE + " TEXT, " +
                UsersEntry.COLUMN_USER_PICTURE + " BLOB, " +
                "UNIQUE (" + UsersEntry.COLUMN_USER_NAME + ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_PASSWORDS = "CREATE TABLE " + PasswordEntry.TABLE_NAME + " ( " +
                PasswordEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PasswordEntry.COLUMN_PASSWORD_TYPE + " TEXT NOT NULL, " +
                PasswordEntry.COLUMN_PASSWORD_STRING + " TEXT);";

        final String SQL_CREATE_USER_PASSWORDS = "CREATE TABLE " +
                UserPasswordsEntry.TABLE_NAME + " ( " +
                UserPasswordsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserPasswordsEntry.COLUMN_ENVIRONMENT_ID + " INTEGER NOT NULL, " +
                UserPasswordsEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                UserPasswordsEntry.COLUMN_PASSWORD_ID + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + UserPasswordsEntry.COLUMN_ENVIRONMENT_ID + ") REFERENCES " +
                EnvironmentEntry.TABLE_NAME + "(" + EnvironmentEntry._ID + "), " +
                " FOREIGN KEY (" + UserPasswordsEntry.COLUMN_USER_ID + ") REFERENCES " +
                UsersEntry.TABLE_NAME + "(" + UsersEntry._ID + "), " +
                " FOREIGN KEY (" + UserPasswordsEntry.COLUMN_PASSWORD_ID + ") REFERENCES " +
                PasswordEntry.TABLE_NAME + "(" + PasswordEntry._ID + "), " +
                "UNIQUE (" + UserPasswordsEntry.COLUMN_ENVIRONMENT_ID + ", " +
                UserPasswordsEntry.COLUMN_USER_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_APP_WHITELIST = "CREATE TABLE " +
                AppWhitelistEntry.TABLE_NAME + " ( " +
                AppWhitelistEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                AppWhitelistEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                AppWhitelistEntry.COLUMN_PACKAGE_NAME + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + AppWhitelistEntry.COLUMN_USER_ID + ") REFERENCES " +
                UsersEntry.TABLE_NAME + "(" + UsersEntry._ID + "), " +
                "UNIQUE (" + AppWhitelistEntry.COLUMN_USER_ID + ", " +
                AppWhitelistEntry.COLUMN_PACKAGE_NAME + ") ON CONFLICT IGNORE);";

        db.execSQL(SQL_CREATE_GEOFENCES);
        db.execSQL(SQL_CREATE_BLUETOOTH_DEVICES);
        db.execSQL(SQL_CREATE_WIFI_NETWORKS);
        db.execSQL(SQL_CREATE_ENVIRONMENTS);
        db.execSQL(SQL_CREATE_ENVIRONMENT_BLUETOOTH_DEVICES);
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_PASSWORDS);
        db.execSQL(SQL_CREATE_USER_PASSWORDS);
        db.execSQL(SQL_CREATE_APP_WHITELIST);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //Move the next two functions to User class, wrap it up in a single function
    public static long insertDefaultUser(SQLiteDatabase db, String userName, Context context){
        if(userName == null || userName.isEmpty()){
            userName = UsersEntry.DEFAULT_USER_NAME;
        }
        //Checking whether the default user is present
        Cursor userCursor = db.query(UsersEntry.TABLE_NAME, null, UsersEntry.COLUMN_USER_NAME + " = ? ",
                new String[]{userName}, null, null, null);
        if(userCursor.getCount() > 0){
            userCursor.moveToFirst();
            long id = userCursor.getLong(userCursor.getColumnIndex(UsersEntry._ID));
            userCursor.close();
            return id;
        }
        userCursor.close();
        ContentValues userValues = new ContentValues();
        userValues.put(UsersEntry.COLUMN_USER_NAME, userName);
        userValues.put(UsersEntry.COLUMN_USER_PICTURE_TYPE, Picture.PICTURE_TYPE_COLOR);
        userValues.put(UsersEntry.COLUMN_USER_PICTURE_DESCRIPTION, Utility.getRandomColor(context));
        return db.insert(UsersEntry.TABLE_NAME, null, userValues);
    }

    public static void insertDefaultUser(Context context){
        long id = insertDefaultUser(EnvironmentDbHelper.getInstance(context).getWritableDatabase(), null, context);
        if(id >= 0){
            SharedPreferencesHelper.setDeviceOwnerUserId(context, id);
        }
    }

    private static EnvironmentDbHelper mInstance = null;

    public static EnvironmentDbHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (mInstance == null) {
            mInstance = new EnvironmentDbHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private EnvironmentDbHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }
}

