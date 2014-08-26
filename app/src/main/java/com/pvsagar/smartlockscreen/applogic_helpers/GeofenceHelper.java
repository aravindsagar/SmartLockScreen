package com.pvsagar.smartlockscreen.applogic_helpers;

import android.content.Context;
import android.database.Cursor;

import com.google.android.gms.location.Geofence;
import com.pvsagar.smartlockscreen.applogic_objects.LocationEnvironmentVariable;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.GeoFenceEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 25/8/14.
 */
public class GeofenceHelper {
    private static final int NOTIFICATION_RESPONSIVENESS = 30000;
    public static List<LocationEnvironmentVariable> getGeofences(Context context){
        Cursor cursor = context.getContentResolver().query(GeoFenceEntry.CONTENT_URI, null, null, null,
                null);
        ArrayList<LocationEnvironmentVariable> locationEnvironmentVariables =
                new ArrayList<LocationEnvironmentVariable>();
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            LocationEnvironmentVariable variable = new LocationEnvironmentVariable(
                    cursor.getFloat(cursor.getColumnIndex(GeoFenceEntry.COLUMN_COORD_LAT)),
                    cursor.getFloat(cursor.getColumnIndex(GeoFenceEntry.COLUMN_COORD_LONG)),
                    (int) cursor.getFloat(cursor.getColumnIndex(GeoFenceEntry.COLUMN_RADIUS)),
                    cursor.getString(cursor.getColumnIndex(GeoFenceEntry.COLUMN_LOCATION_NAME))
            );
            locationEnvironmentVariables.add(variable);
        }
        return locationEnvironmentVariables;
    }

    public static List<Geofence> getAndroidGeofences(Context context) {
        Cursor cursor = context.getContentResolver().query(GeoFenceEntry.CONTENT_URI, null, null, null,
                null);
        ArrayList<Geofence> geofences = new ArrayList<Geofence>();
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            geofences.add((new Geofence.Builder()).setCircularRegion(
                    cursor.getFloat(cursor.getColumnIndex(GeoFenceEntry.COLUMN_COORD_LAT)),
                    cursor.getFloat(cursor.getColumnIndex(GeoFenceEntry.COLUMN_COORD_LONG)),
                    cursor.getFloat(cursor.getColumnIndex(GeoFenceEntry.COLUMN_RADIUS))).
                    setTransitionTypes(
                            Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT).
                    setExpirationDuration(Geofence.NEVER_EXPIRE).
                    setNotificationResponsiveness(NOTIFICATION_RESPONSIVENESS).
                    setRequestId(cursor.getString(cursor.getColumnIndex(GeoFenceEntry._ID))).
                    build());
        }
        return geofences;
    }
}
