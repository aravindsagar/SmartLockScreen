package com.pvsagar.smartlockscreen.frontend_helpers;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;

import java.io.File;

/**
 * Created by aravind on 23/11/14.
 */
public class WallpaperHelper {
    private static Bitmap wallpaperBitmap;
    private static boolean isWallpaperLoaded = true;

    public static Drawable getWallpaperDrawable(Context context){
        Drawable wallpaper;
        if(wallpaperBitmap != null){
            wallpaper = new BitmapDrawable(context.getResources(), wallpaperBitmap);
        } else {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            wallpaper = wallpaperManager.peekDrawable();
            if (wallpaper == null) {
                wallpaper = wallpaperManager.getDrawable();
            }
            if (wallpaper == null) {
                wallpaper = context.getResources().getDrawable(R.drawable.background);
            }
        }
        wallpaper.setColorFilter(Color.argb(100, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
        return wallpaper;
    }

    public static void onWallpaperChanged(Context context){
        onWallpaperChanged(context, SharedPreferencesHelper.getWallpaperPreference(context));
    }

    public static void onWallpaperChanged(Context context, String preferenceValue){
        String[] values = context.getResources().getStringArray(R.array.pref_values_lockscreen_wallpaper);
        if(preferenceValue.equals(values[1])){
            File croppedImageFile = new File(context.getFilesDir(), "wallpaper.jpg");
            wallpaperBitmap = BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath());
        } else {
            wallpaperBitmap = null;
        }
    }
}
