package com.pvsagar.smartlockscreen.environmentdb;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by aravind on 9/8/14.
 * A contract class for our database.
 * Makes it convenient to make dbHelper class and content provider.
 * All the tables and the columns in each table are defined here.
 * Make use of this whenever you want to specify table name, column name etc., to avoid errors.
 */
public class EnvironmentDatabaseContract {
    public static final String CONTENT_AUTHORITY = "com.pvsagar.smartlockscreen.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_GEOFENCES = "geofences";
    public static final String PATH_ENVIRONMENTS = "environments";
    public static final String PATH_BLUETOOTH_DEVICES = "bluetoothdevices";
    public static final String PATH_WIFI_NETWORKS = "wifinetworks";
    public static final String PATH_USERS = "users";

    public static final class GeoFenceEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_GEOFENCES).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_AUTHORITY + "/" + PATH_GEOFENCES;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" +
                CONTENT_AUTHORITY + "/" + PATH_GEOFENCES;

        public static final String TABLE_NAME = "geofences";

        //Latitude and longitude of the center
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

        //Radius of the geofence
        public static final String COLUMN_RADIUS = "radius";

        //Given name of the geofence
        public static final String COLUMN_LOCATION_NAME = "location_name";

        public static Uri buildGeofenceUriWithId(long id){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }
    }

    public static final class BluetoothDevicesEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_BLUETOOTH_DEVICES).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_AUTHORITY + "/" + PATH_BLUETOOTH_DEVICES;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" +
                CONTENT_AUTHORITY + "/" + PATH_BLUETOOTH_DEVICES;

        public static final String TABLE_NAME = "bluetooth_devices";

        public static final String COLUMN_DEVICE_ADDRESS = "device_address";

        public static final String COLUMN_DEVICE_NAME = "device_name";

        public static Uri buildBluetoothUriWithId(long id){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }
    }

    public static final class WiFiNetworksEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_WIFI_NETWORKS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_AUTHORITY + "/" + PATH_WIFI_NETWORKS;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" +
                CONTENT_AUTHORITY + "/" + PATH_WIFI_NETWORKS;

        public static final String TABLE_NAME = "wifi_networks";

        public static final String COLUMN_SSID = "ssid";

        public static final String COLUMN_ENCRYPTION_TYPE = "encryption_type";

        public static Uri buildWiFiUriWithId(long id){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }
    }

    public static final class EnvironmentEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_ENVIRONMENTS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_AUTHORITY + "/" + PATH_ENVIRONMENTS;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" +
                CONTENT_AUTHORITY + "/" + PATH_ENVIRONMENTS;

        public static final String TABLE_NAME = "environments";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_IS_LOCATION_ENABLED = "is_location_enabled";

        public static final String COLUMN_GEOFENCE_ID = "geofence_id";

        public static final String COLUMN_IS_BLUETOOTH_ENABLED = "is_bluetooth_enabled";

        //Boolean to see whether all the bluetooth devices specified should be present or
        // any of the bluetooth devices in the list will do
        public static final String COLUMN_BLUETOOTH_ALL_OR_ANY = "bluetooth_all_or_any";

        public static final String COLUMN_IS_WIFI_ENABLED = "is_wifi_enabled";

        public static final String COLUMN_WIFI_ID = "wifi_id";

        public static final String COLUMN_IS_MIN_NOISE_ENABLED = "is_min_noise_enabled";

        public static final String COLUMN_IS_MAX_NOISE_ENABLED = "is_max_noise_enabled";

        public static final String COLUMN_MIN_NOISE_LEVEL = "min_noise_level";

        public static final String COLUMN_MAX_NOISE_LEVEL = "max_noise_level";

        public static Uri buildEnvironmentUriWithId(long id){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }
    }

    public static final class EnvironmentBluetoothEntry implements BaseColumns {
        public static final String TABLE_NAME = "environment_bluetooth_devices";

        public static final String COLUMN_ENVIRONMENT_ID = "environment_id";

        public static final String COLUMN_BLUETOOTH_ID = "bluetooth_device_id";
    }

    //Making a separate table for users because planning to put user picture etc later.
    public static final class UsersEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_USERS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_AUTHORITY + "/" + PATH_USERS;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" +
                CONTENT_AUTHORITY + "/" + PATH_USERS;

        public static final String TABLE_NAME = "users";

        public static final String COLUMN_USER_NAME = "user_name";

        public static Uri buildUserUriWithId(long id){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }

        public static Uri buildUserUriWithIdEnvironmentAndPassword
                (long userId, long environmentId){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(userId)).
                    appendPath(String.valueOf(environmentId)).appendPath(PasswordEntry.TABLE_NAME).
                    build();
        }

        public static Uri buildUserUriWithAppWhitelist(long userId){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(userId)).
                    appendPath(AppWhitelistEntry.TABLE_NAME).build();
        }
    }

    public static final class PasswordEntry implements BaseColumns {
        public static final String TABLE_NAME = "passwords";

        public static final String COLUMN_PASSWORD_TYPE = "password_type";
        //Should see how to store passwords. Might change this to something else
        public static final String COLUMN_PASSWORD_STRING = "password_string";
    }

    public static final class UserPasswordsEntry implements BaseColumns {
        public static final String TABLE_NAME = "user_passwords";

        public static final String COLUMN_ENVIRONMENT_ID = "environment_id";

        public static final String COLUMN_USER_ID = "user_id";

        public static final String COLUMN_PASSWORD_ID = "password_id";
    }

    public static final class AppWhitelistEntry implements BaseColumns {
        public static final String TABLE_NAME = "app_whitelist";

        public static final String COLUMN_USER_ID = "user_id";

        public static final String COLUMN_PACKAGE_NAME = "package_name";
    }
}
