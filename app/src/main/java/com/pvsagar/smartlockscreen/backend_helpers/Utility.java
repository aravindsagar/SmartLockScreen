package com.pvsagar.smartlockscreen.backend_helpers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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

    public static  Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }
}
