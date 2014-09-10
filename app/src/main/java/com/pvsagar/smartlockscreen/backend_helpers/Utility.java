package com.pvsagar.smartlockscreen.backend_helpers;

import android.util.Log;

/**
 * Created by aravind on 10/9/14.
 * Utility class containing miscellaneous utility functions
 */
public class Utility {

    public static void checkForNullAndThrowException(Object o){
        if(o == null){
            throw new NullPointerException("Object cannot be null.");
        }
    }

    public static boolean checkForNullAndWarn(Object o, final String LOG_TAG){
        if(o == null){
            Log.w(LOG_TAG, "Object passed is null.");
        }
        return o == null;
    }
}
