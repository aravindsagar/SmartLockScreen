package com.pvsagar.smartlockscreen.environmentdb;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

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
    private static final int ENVIRONMENT = 200;
    private static final int BLUETOOTH_DEVICE = 300;
    private static final int WIFI_NETWORK = 400;
    private static final int USER = 500;
    private static final int USER_WITH_ID = 501;
    private static final int USER_WITH_ID_AND_ENVIRONMENT_PASSWORD = 502;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private EnvironmentDbHelper mEnvironmentDbHelper;
    private static final SQLiteQueryBuilder sPasswordMatcherForUserWithEnvironment;

    static {
        sPasswordMatcherForUserWithEnvironment = new SQLiteQueryBuilder();
        sPasswordMatcherForUserWithEnvironment.setTables(
                EnvironmentDatabaseContract.UsersEntry.TABLE_NAME + " INNER JOIN " +
                        EnvironmentDatabaseContract.UserPasswordsEntry.TABLE_NAME + " ON " +
                        EnvironmentDatabaseContract.UsersEntry.TABLE_NAME + "." +
                        EnvironmentDatabaseContract.UsersEntry._ID + " = " +
                        EnvironmentDatabaseContract.UserPasswordsEntry.TABLE_NAME + "." +
                        EnvironmentDatabaseContract.UserPasswordsEntry.COLUMN_USER_ID
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
        return null;
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
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_ENVIRONMENTS, ENVIRONMENT);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_BLUETOOTH_DEVICES,
                BLUETOOTH_DEVICE);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_WIFI_NETWORKS, WIFI_NETWORK);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_USERS, USER);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_USERS + "/#", USER_WITH_ID);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_USERS + "/#/#/*",
                USER_WITH_ID_AND_ENVIRONMENT_PASSWORD);
        return matcher;
    }
}
