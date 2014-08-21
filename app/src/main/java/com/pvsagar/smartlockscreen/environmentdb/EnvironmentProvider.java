package com.pvsagar.smartlockscreen.environmentdb;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

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
 * Created by aravind on 17/8/14.
 * base_uri is defined in EnvironmentDatabaseContract.BASE_CONTENT_URI
 * Content provider for Environment database. Uris for accessing content is listed below:
 * (Replace items beginning and ending with '-', with actual values.
 *   - CONTENT_URL is provided in the respective EnvironmentDatabaseContract.*Entry classes for
 *   geofences,environments, bluetooth devices, wifi networks and users
 *   (The Uris below can be built using helper methods in EnvironmentDatabaseContract inner classes)
 *   - Users CONTENT_URL/-user_id-/-environment_id-/passwords can be used for inserting and
 *   retrieving encrypted password
 *   - Users CONTENT_URL/-user_id-/app_whitelist will insert/bulk insert/get whitelisted apps
 *   - Environments CONTENT_URL/-environment_id-/bluetooth_devices will insert/bulk insert/get the
 *   bluetooth devices associated with the environment
 *   - Similarly, Environments CONTENT_URL/-environment_id-/wifi_networks and
 *   CONTENT_URL/-environment_id-/geofences can be used.
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
    private static final int ENVIRONMENT_WITH_ID_AND_BLUETOOTH_DEVICES = 202;
    private static final int ENVIRONMENT_WITH_ID_AND_WIFI_NETWORKS = 203;
    private static final int ENVIRONMENT_WITH_ID_AND_GEOFENCES = 204;
    private static final int BLUETOOTH_DEVICE = 300;
    private static final int BLUETOOTH_DEVICE_WITH_ID = 301;
    private static final int WIFI_NETWORK = 400;
    private static final int WIFI_NETWORK_WITH_ID = 401;
    private static final int USER = 500;
    private static final int USER_WITH_ID = 501;
    private static final int USER_WITH_ID_AND_APP_WHITELIST = 502;
    private static final int USER_WITH_ID_ENVIRONMENT_AND_PASSWORD = 503;

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
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {
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
            case ENVIRONMENT_WITH_ID_AND_BLUETOOTH_DEVICES:
            case ENVIRONMENT_WITH_ID_AND_GEOFENCES:
            case ENVIRONMENT_WITH_ID_AND_WIFI_NETWORKS:
                return EnvironmentEntry.CONTENT_ITEM_TYPE;
            case USER:
                return UsersEntry.CONTENT_TYPE;
            case USER_WITH_ID:
            case USER_WITH_ID_AND_APP_WHITELIST:
            case USER_WITH_ID_ENVIRONMENT_AND_PASSWORD:
                return UsersEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mEnvironmentDbHelper.getWritableDatabase();
        long _id;
        Uri returnUri;
        switch (sUriMatcher.match(uri)){
            case GEOFENCE:
                _id = db.insert(GeoFenceEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    returnUri = GeoFenceEntry.buildGeofenceUriWithId(_id);
                }
                else{
                    Log.e(LOG_TAG, "Failed to insert row into " + uri);
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            case ENVIRONMENT:
                _id = db.insert(EnvironmentEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    returnUri = EnvironmentEntry.buildEnvironmentUriWithId(_id);
                }
                else{
                    Log.e(LOG_TAG, "Failed to insert row into " + uri);
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            case WIFI_NETWORK:
                _id = db.insert(WiFiNetworksEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    returnUri = WiFiNetworksEntry.buildWiFiUriWithId(_id);
                }
                else{
                    Log.e(LOG_TAG, "Failed to insert row into " + uri);
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            case ENVIRONMENT_WITH_ID_AND_WIFI_NETWORKS:
                _id = insertEnvironmentWifiNetwork(uri, db, values);
                if(_id > 0){
                    returnUri = EnvironmentEntry.buildEnvironmentUriWithId(
                            Long.parseLong(uri.getPathSegments().get(1)));
                }
                else{
                    Log.e(LOG_TAG, "Failed to insert row into " + uri);
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            case BLUETOOTH_DEVICE:
                _id = db.insert(BluetoothDevicesEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    returnUri = BluetoothDevicesEntry.buildBluetoothUriWithId(_id);
                }
                else{
                    Log.e(LOG_TAG, "Failed to insert row into " + uri);
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            case ENVIRONMENT_WITH_ID_AND_BLUETOOTH_DEVICES:
                _id = insertEnvironmentBluetoothDevice(uri, db, values);
                if(_id > 0){
                    returnUri = EnvironmentEntry.buildEnvironmentUriWithId
                            (Long.parseLong(uri.getPathSegments().get(1)));
                }
                else{
                    Log.e(LOG_TAG, "Failed to insert row into " + uri);
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            case USER:
                _id = db.insert(UsersEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    returnUri = UsersEntry.buildUserUriWithId(_id);
                } else{
                    Log.e(LOG_TAG, "Failed to insert row into " + uri);
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            case USER_WITH_ID_AND_APP_WHITELIST:
                String userIdString = uri.getPathSegments().get(1);
                if(values.get(AppWhitelistEntry.COLUMN_USER_ID) == null){
                    values.put(AppWhitelistEntry.COLUMN_USER_ID, userIdString);
                }
                _id = db.insert(AppWhitelistEntry.TABLE_NAME, null, values);
                if(_id > 0) {
                    returnUri = UsersEntry.buildUserUriWithId(Long.valueOf(userIdString));
                } else{
                    Log.e(LOG_TAG, "Failed to insert row into " + uri);
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            case USER_WITH_ID_ENVIRONMENT_AND_PASSWORD:
                _id = insertPassword(uri, db, values);
                if(_id > 0){
                    returnUri = UsersEntry.buildUserUriWithId(Long.parseLong
                            (uri.getPathSegments().get(1)));
                } else{
                    Log.e(LOG_TAG, "Failed to insert row into " + uri);
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int returnValue;
        SQLiteDatabase db = mEnvironmentDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)){
            case ENVIRONMENT:
                returnValue = deleteEnvironment(db, selection, selectionArgs);
                break;
            case USER:
                returnValue = deleteUser(db, selection, selectionArgs);
                break;
            case GEOFENCE:
                returnValue = db.delete(GeoFenceEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            case WIFI_NETWORK:
                returnValue = db.delete(WiFiNetworksEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            case BLUETOOTH_DEVICE:
                returnValue = db.delete(BluetoothDevicesEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
        }
        if(selection == null || returnValue != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return returnValue;
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
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_ENVIRONMENTS + "/#/" +
                BluetoothDevicesEntry.TABLE_NAME, ENVIRONMENT_WITH_ID_AND_BLUETOOTH_DEVICES);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_ENVIRONMENTS + "/#/" +
                WiFiNetworksEntry.TABLE_NAME, ENVIRONMENT_WITH_ID_AND_WIFI_NETWORKS);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_ENVIRONMENTS + "/#/" +
                GeoFenceEntry.TABLE_NAME, ENVIRONMENT_WITH_ID_AND_GEOFENCES);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_BLUETOOTH_DEVICES,
                BLUETOOTH_DEVICE);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_BLUETOOTH_DEVICES + "/#",
                BLUETOOTH_DEVICE_WITH_ID);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_WIFI_NETWORKS, WIFI_NETWORK);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_WIFI_NETWORKS + "/#",
                WIFI_NETWORK_WITH_ID);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_USERS, USER);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_USERS + "/#", USER_WITH_ID);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_USERS + "/#/" +
                AppWhitelistEntry.TABLE_NAME, USER_WITH_ID_AND_APP_WHITELIST);
        matcher.addURI(authority, EnvironmentDatabaseContract.PATH_USERS + "/#/#/" +
                PasswordEntry.TABLE_NAME, USER_WITH_ID_ENVIRONMENT_AND_PASSWORD);
        return matcher;
    }

    private int deleteEnvironment(SQLiteDatabase db, String selection, String[] selectionArgs){
        //Finding the environment ids of the environments to be deleted, so that they can be
        // deleted in other tables which stores the environment id.
        Cursor envCursor = db.query(
                EnvironmentEntry.TABLE_NAME,
                new String[]{EnvironmentEntry._ID},
                selection,
                selectionArgs,
                null, null, null
        );

        for(envCursor.moveToFirst(); !envCursor.isAfterLast(); envCursor.moveToNext()){
            long envId = envCursor.getLong(envCursor.getColumnIndex(EnvironmentEntry._ID));
            String envSelection = EnvironmentBluetoothEntry.COLUMN_ENVIRONMENT_ID + " = ? ";
            String[] envSelectionArgs = new String[]{String.valueOf(envId)};
            db.delete(EnvironmentBluetoothEntry.TABLE_NAME, envSelection, envSelectionArgs);

            deletePasswordWithEnvironmentId(envId, db);
        }

        return db.delete(EnvironmentEntry.TABLE_NAME,
                selection, selectionArgs);
    }

    private int deleteUser(SQLiteDatabase db, String selection, String[] selectionArgs){
        Cursor userCursor = db.query(
                UsersEntry.TABLE_NAME,
                new String[]{UsersEntry._ID},
                selection,
                selectionArgs,
                null, null, null
        );

        for(userCursor.moveToFirst(); !userCursor.isAfterLast(); userCursor.moveToNext()){
            long userId = userCursor.getLong(userCursor.getColumnIndex(UsersEntry._ID));
            deletePasswordWithUserId(userId, db);

            String userSelection = AppWhitelistEntry.COLUMN_USER_ID + " = ? ";
            String[] userSelectionArgs = new String[]{String.valueOf(userId)};
            db.delete(AppWhitelistEntry.TABLE_NAME, userSelection, userSelectionArgs);
        }
        return db.delete(UsersEntry.TABLE_NAME, selection, selectionArgs);
    }

    private void deletePasswordWithUserId(long userId, SQLiteDatabase db){
        String passwordIdsSelection = UserPasswordsEntry.COLUMN_USER_ID + " = ? ";
        String[] passwordIdsSelectionArgs = new String[]{String.valueOf(userId)};
        Cursor passwordIds = db.query(UserPasswordsEntry.TABLE_NAME,
                new String[]{UserPasswordsEntry.COLUMN_PASSWORD_ID},
                passwordIdsSelection,
                passwordIdsSelectionArgs,
                null, null, null, null);
        for(passwordIds.moveToFirst(); !passwordIds.isAfterLast(); passwordIds.moveToNext()){
            String passwordSelection = PasswordEntry._ID + " = ? ";
            String[] passwordSelectionArgs = new String[]{String.valueOf(passwordIds.getDouble(
                    passwordIds.getColumnIndex(UserPasswordsEntry.COLUMN_PASSWORD_ID)))};
            db.delete(PasswordEntry.TABLE_NAME, passwordSelection, passwordSelectionArgs);
        }
        db.delete(UserPasswordsEntry.TABLE_NAME, passwordIdsSelection, passwordIdsSelectionArgs);
    }

    private void deletePasswordWithEnvironmentId(long environmentId, SQLiteDatabase db){
        String passwordIdsSelection = UserPasswordsEntry.COLUMN_ENVIRONMENT_ID + " = ? ";
        String[] passwordIdsSelectionArgs = new String[]{String.valueOf(environmentId)};
        Cursor passwordIds = db.query(UserPasswordsEntry.TABLE_NAME,
                new String[]{UserPasswordsEntry.COLUMN_PASSWORD_ID},
                passwordIdsSelection,
                passwordIdsSelectionArgs,
                null, null, null, null);
        for(passwordIds.moveToFirst(); !passwordIds.isAfterLast(); passwordIds.moveToNext()){
            String passwordSelection = PasswordEntry._ID + " = ? ";
            String[] passwordSelectionArgs = new String[]{String.valueOf(passwordIds.getDouble(
                    passwordIds.getColumnIndex(UserPasswordsEntry.COLUMN_PASSWORD_ID)))};
            db.delete(PasswordEntry.TABLE_NAME, passwordSelection, passwordSelectionArgs);
        }
        db.delete(UserPasswordsEntry.TABLE_NAME, passwordIdsSelection, passwordIdsSelectionArgs);
    }

    private long insertPassword(Uri uri, SQLiteDatabase db, ContentValues values){
        //Inserting into Passwords table
        long passwordId = db.insert(PasswordEntry.TABLE_NAME, null, values);

        //Inserting into user_passwords table
        long userId = Long.parseLong(uri.getPathSegments().get(1));
        long environmentId = Long.parseLong(uri.getPathSegments().get(2));
        ContentValues userPasswordValues = new ContentValues();
        userPasswordValues.put(UserPasswordsEntry.COLUMN_ENVIRONMENT_ID, environmentId);
        userPasswordValues.put(UserPasswordsEntry.COLUMN_USER_ID, userId);
        userPasswordValues.put(UserPasswordsEntry.COLUMN_PASSWORD_ID, passwordId);
        return db.insert(UserPasswordsEntry.TABLE_NAME, null, userPasswordValues);
    }

    private long insertEnvironmentBluetoothDevice(Uri uri, SQLiteDatabase db, ContentValues values){
        long environmentId = Long.parseLong(uri.getPathSegments().get(1));
        String bluetoothSelection = BluetoothDevicesEntry.COLUMN_DEVICE_ADDRESS + " = ? AND " +
                BluetoothDevicesEntry.COLUMN_DEVICE_NAME + " = ? ";
        String[] bluetoothSelectionArgs = new String[]{
                values.getAsString(BluetoothDevicesEntry.COLUMN_DEVICE_ADDRESS),
                values.getAsString(BluetoothDevicesEntry.COLUMN_DEVICE_NAME)
        };
        Cursor bluetoothCursor = db.query(BluetoothDevicesEntry.TABLE_NAME,
                new String[]{BluetoothDevicesEntry._ID}, bluetoothSelection,
                bluetoothSelectionArgs, null, null, null);

        long bluetoothDeviceId;
        if(bluetoothCursor.getCount() == 0){
            bluetoothDeviceId = db.insert(BluetoothDevicesEntry.TABLE_NAME, null, values);
        } else {
            bluetoothCursor.moveToFirst();
            bluetoothDeviceId = bluetoothCursor.getLong(
                    bluetoothCursor.getColumnIndex(BluetoothDevicesEntry._ID));
        }
        if(bluetoothDeviceId == -1){
            return -1;
        }

        ContentValues environmentContentValues = new ContentValues();
        environmentContentValues.put(EnvironmentEntry.COLUMN_IS_BLUETOOTH_ENABLED, 1);
        db.update(EnvironmentEntry.TABLE_NAME, environmentContentValues,
                EnvironmentEntry._ID + " = ? ", new String[]{String.valueOf(environmentId)});

        ContentValues environmentBluetoothContentValues = new ContentValues();
        environmentBluetoothContentValues.put(EnvironmentBluetoothEntry.COLUMN_BLUETOOTH_ID,
                bluetoothDeviceId);
        environmentBluetoothContentValues.put(EnvironmentBluetoothEntry.COLUMN_ENVIRONMENT_ID,
                environmentId);
        return db.insert(EnvironmentBluetoothEntry.TABLE_NAME, null,
                environmentBluetoothContentValues);
    }

    private long insertEnvironmentWifiNetwork(Uri uri, SQLiteDatabase db, ContentValues values){
        long environmentId = Long.parseLong(uri.getPathSegments().get(1));
        String wifiSelection = WiFiNetworksEntry.COLUMN_SSID+ " = ? AND " +
                WiFiNetworksEntry.COLUMN_ENCRYPTION_TYPE + " = ? ";
        String[] wifiSelectionArgs = new String[]{
                values.getAsString(WiFiNetworksEntry.COLUMN_SSID),
                values.getAsString(WiFiNetworksEntry.COLUMN_ENCRYPTION_TYPE)
        };
        Cursor wifiCursor = db.query(WiFiNetworksEntry.TABLE_NAME,
                new String[]{WiFiNetworksEntry._ID}, wifiSelection, wifiSelectionArgs,
                null, null, null);

        long wifiId;
        if(wifiCursor.getCount() == 0){
            wifiId = db.insert(WiFiNetworksEntry.TABLE_NAME, null, values);
        } else {
            wifiCursor.moveToFirst();
            wifiId = wifiCursor.getLong(wifiCursor.getColumnIndex(WiFiNetworksEntry._ID));
        }
        if(wifiId == -1){
            return -1;
        }

        ContentValues environmentContentValues = new ContentValues();
        environmentContentValues.put(EnvironmentEntry.COLUMN_IS_WIFI_ENABLED, 1);
        environmentContentValues.put(EnvironmentEntry.COLUMN_WIFI_ID, wifiId);
        return db.update(EnvironmentEntry.TABLE_NAME, environmentContentValues,
                EnvironmentEntry._ID + " = ? ", new String[]{String.valueOf(environmentId)});
    }
}
