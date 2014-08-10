package com.pvsagar.smartlockscreen.environmentdb;

import android.provider.BaseColumns;

/**
 * Created by aravind on 9/8/14.
 */
public class EnvironmentDatabaseContract {

    public static final class GeoFenceEntry implements BaseColumns {
        public static final String TABLE_NAME = "geofences";

        //Latitude and longitude of the center
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

        //Radius of the geofence
        public static final String COLUMN_RADIUS = "radius";

        //Given name of the geofence
        public static final String COLUMN_LOCAION_NAME = "location_name";
    }

    public static final class BluetoothDevicesEntry implements BaseColumns {
        public static final String TABLE_NAME = "bluetooth_devices";

        public static final String COLUMN_DEVICE_ADDRESS = "device_address";

        public static final String COLUMN_DEVICE_NAME = "device_name";
    }

    public static final class WiFiNetworksEntry implements BaseColumns {
        public static final String TABLE_NAME = "wifi_networks";

        public static final String COLUMN_SSID = "ssid";

        public static final String COLUMN_ENCRYPTION_TYPE = "encryption_type";
    }

    public static final class EnvironmentEntry implements BaseColumns {
        public static final String TABLE_NAME = "environments";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_IS_LOCATION_ENABLED = "is_location_enabled";

        public static final String COLUMN_GEOFENCE_ID = "geofence_id";

        public static final String COLUMN_IS_BLUETOOTH_ENABLED = "is_bluetooth_enabled";

        //Boolean to see whether allthe bluetooth devices specified should be present or
        // any of the bluetooth devices in the list will do
        public static final String COLUMN_BLUETOOTH_ALL_OR_ANY = "bluetooth_all_or_any";

        public static final String COLUMN_IS_WIFI_ENABLED = "is_wifi_enabled";

        public static final String COLUMN_WIFI_ID = "wifi_id";

        public static final String COLUMN_IS_MIN_NOISE_ENABLED = "is_min_noise_enabled";

        public static final String COLUMN_IS_MAX_NOISE_ENABLED = "is_max_noise_enabled";

        public static final String COLUMN_MIN_NOISE_LEVEL = "min_noise_level";

        public static final String COLUMN_MAX_NOISE_LEVEL = "max_noise_level";
    }

    public static final class EnvironmentBluetoothEntry implements BaseColumns {
        public static final String TABLE_NAME = "environment_bluetooth_devices";

        public static final String COLUMN_ENVIRONMENT_ID = "environment_id";

        public static final String COLUMN_BLUETOOTH_ID = "bluetooth_device_id";
    }
}
