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


    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c <= '/' || c >= ':') {
                return false;
            }
        }
        return true;
    }

    public static boolean isEqual(double a, double b, double doubleErrorTolerance){
        if(Math.abs(a-b) <= doubleErrorTolerance){
            return true;
        }
        return false;
    }

    public static boolean isEqual(double a, double b){
        final double defaultDoubleErrorTolerance = 0.0000449;
        return isEqual(a, b, defaultDoubleErrorTolerance);
    }
}
