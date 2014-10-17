package com.pvsagar.smartlockscreen.backend_helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.pvsagar.smartlockscreen.R;

import java.util.Date;
import java.util.Random;

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

    public static  Bitmap getCroppedBitmap(Bitmap bitmap, int borderColor) {
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
                Math.min(bitmap.getWidth(), bitmap.getHeight()) / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        Paint borderPaint = new Paint();
        final int STROKE_WIDTH = 10;
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(STROKE_WIDTH);
        canvas.drawCircle(canvas.getWidth()/2.0f, canvas.getHeight()/2.0f,
                Math.min(canvas.getWidth()/2.0f, canvas.getHeight()/2.0f) - STROKE_WIDTH/2.0f, borderPaint);
        return output;
    }

    private static final int[] colors = {
            R.color.user_pic_background1,
            R.color.user_pic_background2,
            R.color.user_pic_background3,
            R.color.user_pic_background4,
            R.color.user_pic_background5,
            R.color.user_pic_background6,
            R.color.user_pic_background7,
            R.color.user_pic_background8,
    };

    public static int getRandomColor(Context context){
        Random r = new Random();
        r.setSeed(new Date().getTime());
        int index = r.nextInt(colors.length);
        return context.getResources().getColor(colors[index]);
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private static final float SHADE_FACTOR = 0.9f;

    public static int getDarkerShade(int color) {
        return Color.rgb((int) (SHADE_FACTOR * Color.red(color)),
                (int) (SHADE_FACTOR * Color.green(color)),
                (int) (SHADE_FACTOR * Color.blue(color)));
    }

    public static int getLighterShade(int color) {
        return Color.rgb((int)(Color.red(color) / SHADE_FACTOR),
                (int)(Color.green(color) / SHADE_FACTOR),
                (int)(Color.blue(color) / SHADE_FACTOR));
    }
}
