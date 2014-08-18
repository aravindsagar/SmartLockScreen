package com.pvsagar.smartlockscreen.environmentdb;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.BluetoothDevicesEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.EnvironmentEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.GeoFenceEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.UserPasswordsEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.UsersEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.WiFiNetworksEntry;

/**
 * Created by aravind on 17/8/14.
 * base_uri is defined in EnvironmentDatabaseContract.BASE_CONTENT_URI
 * Content provider for Environment database. Uris for accessing content is listed below:
 *   - CONTENT_URL is provided in the respective EnvironmentDatabaseContract.*Entry classes for
 *   geofences,environments, bluetooth devices, wifi networks and users
 *   - Users CONTENT_URL/environment_id/encrypted_password can be used for inserting passwords and
 *   checking whether password is right
 *   - More coming
 */
public class EnvironmentProvider extends ContentProvider {
    private static final String LOG_TAG = EnvironmentProvider.class.getSimpleName();

    //Declaring some integer constants for identification throughout the class
    //100 to 199 reserved for possible further uris extending GEOFENCES. Similarly for others.
    private static final int GEOFENCE = 100;
    private static final int GEOFENCE_WITH_ID = 101;
    private static final int ENVIRONMENT = 200;
    private static final int ENVIRONMENT_WITH_ID = 201;
    private static final int BLUETOOTH_DEVICE = 300;
    private static final int BLUETOOTH_DEVICE_WITH_ID = 301;
    private static final int WIFI_NETWORK = 400;
    private static final int WIFI_NETWORK_WITH_ID = 401;
    private static final int USER = 500;
    private static final int USER_WITH_ID = 501;
    private static final int USER_WITH_ID_AND_ENVIRONMENT_PASSWORD = 502;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private EnvironmentDbHelper mEnvironmentDbHelper;
    private static final SQLiteQueryBuilder sPasswordMatcherForUserWithEnvironment;

    static {
        sPasswordMatcherForUserWithEnvironment = new SQLiteQueryBuilder();
        sPasswordMatcherForUserWithEnvironment.setTables(
                UsersEntry.TABLE_NAME + " INNER JOIN " + UserPasswordsEntry.TABLE_NAME + " ON " +
                        UsersEntry.TABLE_NAME + "." + UsersEntry._ID + " = " +
                        UserPasswordsEntry.TABLE_NAME + "." + UserPasswordsEntry.COLUMN_USER_ID
        );
    }

    @Override
    public boolean onCreate() {
        mEnvironmentDbHelper = new EnvironmentDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case GEOFENCE:
                return GeoFenceEntry.CONTENT_TYPE;
            case GEOFENCE_WITH_ID:
                return GeoFenceEntry.CONTENT_ITEM_TYPE;
            case BLUETOOTH_DEVICE:
                return BluetoothDevicesEntry.CONTENT_TYPE;
            case BLUETOOTH_DEVICE_WITH_ID:
                return BluetoothDevicesEntry.CONTENT_ITEM_TYPE;
            case WIFI_NETWORK:
                return WiFiNetworksEntry.CONTENT_TYPE;
            case WIFI_NETWORK_WITH_ID:
                return WiFiNetworksEntry.CONTENT_ITEM_TYPE;
            case ENVIRONMENT:
                return EnvironmentEntry.CONTENT_TYPE;
            case ENVIRONMENT_WITH_ID:
                return EnvironmentEntry.CONTENT_ITEM_TYPE;
            case USER:
                return UsersEntry.CONTENT_TYPE;
            case USER_WITH_ID:
            case USER_WITH_ID_AND_ENVIRONMENT_PASSWORD:
                return UsersEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private static UriMatcher buildUriMatcher(){
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = EnvironmentDatabaseContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_GEOFENCES, GEOFENCE);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_GEOFENCES + "/#",
                GEOFENCE_WITH_ID);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_ENVIRONMENTS, ENVIRONMENT);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_ENVIRONMENTS + "/#",
                ENVIRONMENT_WITH_ID);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_BLUETOOTH_DEVICES,
                BLUETOOTH_DEVICE);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_BLUETOOTH_DEVICES + "/#",
                BLUETOOTH_DEVICE_WITH_ID);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_WIFI_NETWORKS, WIFI_NETWORK);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_WIFI_NETWORKS + "/#",
                WIFI_NETWORK_WITH_ID);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_USERS, USER);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_USERS + "/#", USER_WITH_ID);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_USERS + "/#/#/*",
                USER_WITH_ID_AND_ENVIRONMENT_PASSWORD);
        return matcher;
    }
}
