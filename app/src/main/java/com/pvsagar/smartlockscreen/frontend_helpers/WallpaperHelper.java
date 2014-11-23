package com.pvsagar.smartlockscreen.frontend_helpers;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.pvsagar.smartlockscreen.R;

/**
 * Created by aravind on 23/11/14.
 */
public class WallpaperHelper {
    //TODO add custom wallpaper support
    public static Drawable getWallpaperDrawable(Context context){
        Drawable wallpaper;
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        wallpaper = wallpaperManager.peekDrawable();
        if(wallpaper == null){
            wallpaper = wallpaperManager.getDrawable();
        }
        if(wallpaper == null) {
            wallpaper = context.getResources().getDrawable(R.drawable.background);
        }
        wallpaper.setColorFilter(Color.argb(100, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
        return wallpaper;
    }
}
