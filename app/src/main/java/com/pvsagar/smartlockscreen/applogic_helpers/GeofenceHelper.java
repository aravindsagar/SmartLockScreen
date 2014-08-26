package com.pvsagar.smartlockscreen.applogic_helpers;

import android.content.Context;
import android.database.Cursor;

import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.GeoFenceEntry;

/**
 * Created by aravind on 25/8/14.
 */
public class GeofenceHelper {
    public static Cursor getGeofencesCursor(Context context){
        return context.getContentResolver().query(GeoFenceEntry.CONTENT_URI, null, null, null,
                null);
    }
}
