package com.pvsagar.smartlockscreen.applogic_objects;

import android.util.Log;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;

/**
 * Created by aravind on 8/8/14.
 * Class for storing Geofences.
 */
public class LocationEnvironmentVariable extends EnvironmentVariable {
    private static String LOG_TAG = LocationEnvironmentVariable.class.getSimpleName();

    //float value indices
    private static int INDEX_LATITUDE = 0;
    private static int INDEX_LONGITUDE = 1;
    private static int INDEX_RADIUS = 2;

    //String value indices
    private static int INDEX_LOCATION_NAME = 0;

    public LocationEnvironmentVariable(){
        super(TYPE_LOCATION);
    }

    public LocationEnvironmentVariable
            (float latitude, float longitude, int radius, String locationName){
        super(TYPE_LOCATION);
        setFloatValues(new float[]{
                latitude,
                longitude,
                radius
        });
        setStringValues(new String[]{
                locationName
        });
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
}
