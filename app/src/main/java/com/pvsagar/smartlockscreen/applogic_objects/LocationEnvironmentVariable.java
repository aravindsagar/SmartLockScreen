package com.pvsagar.smartlockscreen.applogic_objects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.GeoFenceEntry;
import com.pvsagar.smartlockscreen.environmentdb.mappers.DatabaseToObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 8/8/14.
 * Class for storing Geofences.
 */
public class LocationEnvironmentVariable extends EnvironmentVariable {
    private static String LOG_TAG = LocationEnvironmentVariable.class.getSimpleName();

    private static final int NUMBER_OF_FLOAT_VALUES = 3;
    private static final int NUMBER_OF_STRING_VALUES = 1;
    //float value indices
    private static final int INDEX_LATITUDE = 0;
    private static final int INDEX_LONGITUDE = 1;
    private static final int INDEX_RADIUS = 2;

    //String value indices
    private static final int INDEX_LOCATION_NAME = 0;

    public LocationEnvironmentVariable(){
        super(TYPE_LOCATION, NUMBER_OF_FLOAT_VALUES, NUMBER_OF_STRING_VALUES);
    }

    public LocationEnvironmentVariable
            (float latitude, float longitude, int radius, String locationName){
        super(TYPE_LOCATION, new float[]{latitude,longitude,radius}, new String[]{locationName});
    }

    @Override
    public boolean isStringValuesSupported() {
        return true;
    }

    @Override
    public boolean isFloatValuesSupported() {
        return true;
    }

    public float getLatitude(){
        try {
            return getFloatValue(INDEX_LATITUDE);
        } catch (Exception e){
            Log.e(LOG_TAG, "Internal application error, please file a bug report to developer."
                    + e.getMessage());
            return (float) 0.0; //Should think of a better way, though it should never come to this.
        }
    }

    public float getLongitude(){
        try {
            return getFloatValue(INDEX_LONGITUDE);
        } catch (Exception e){
            Log.e(LOG_TAG, "Internal application error, please file a bug report to developer."
                    + e.getMessage());
            return (float) 0.0; //Should think of a better way, though it should never come to this.
        }
    }

    public int getRadius(){
        try {
            return (int) getFloatValue(INDEX_RADIUS);
        } catch (Exception e){
            Log.e(LOG_TAG, "Internal application error, please file a bug report to developer."
                    + e.getMessage());
            return 0; //Should think of a better way, though it should never come to this.
        }
    }

    public String getLocationName(){
        try {
            return getStringValue(INDEX_LOCATION_NAME);
        } catch (Exception e){
            Log.e(LOG_TAG, "Internal application error, please file a bug report to developer."
                    + e.getMessage());
            return null;
        }
    }

    public void setLatitude(float latitude){
        setFloatValue(latitude, INDEX_LATITUDE);
    }

    public void setLongitude(float longitude){
        setFloatValue(longitude, INDEX_LONGITUDE);
    }

    public void setRadius(int radius){
        setFloatValue(radius, INDEX_RADIUS);
    }

    public void setLocationName(String locationName){
        setStringValue(locationName, INDEX_RADIUS);
    }

    private static final int NOTIFICATION_RESPONSIVENESS = 30000;

    public static List<EnvironmentVariable> getLocationEnvironmentVariables
            (Context context){
        Cursor cursor = context.getContentResolver().query(
                        GeoFenceEntry.CONTENT_URI, null, null, null, null);
        List<EnvironmentVariable> locationEnvironmentVariables =
                DatabaseToObjectMapper.getLocationEnvironmentVariablesFromCursor(cursor);
        return locationEnvironmentVariables;
    }

    public static List<Geofence> getAndroidGeofences(Context context) {
        Cursor cursor = context.getContentResolver().query(GeoFenceEntry.CONTENT_URI,
                null, null, null, null);
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

    @Override
    public ContentValues getContentValues() {
        ContentValues locationValues = new ContentValues();
        locationValues.put(GeoFenceEntry.COLUMN_COORD_LAT, getLatitude());
        locationValues.put(GeoFenceEntry.COLUMN_COORD_LONG, getLongitude());
        locationValues.put(GeoFenceEntry.COLUMN_RADIUS, getRadius());
        locationValues.put(GeoFenceEntry.COLUMN_LOCATION_NAME, getLocationName());
        return locationValues;
    }
}
