package com.pvsagar.smartlockscreen.baseclasses;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by aravind on 23/9/14.
 * A base class for managing overlay windows.
 */
public abstract class Overlay {
    private static final String LOG_TAG = Overlay.class.getSimpleName();

    protected LayoutInflater inflater;
    protected WindowManager windowManager;
    protected Context context;
    protected View layout;
    protected WindowManager.LayoutParams params;

    public Overlay(Context context, WindowManager windowManager){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.windowManager = windowManager;
    }

    protected abstract WindowManager.LayoutParams getLayoutParams();

    /**
     * Gets the layout to be shown by the window manager.
     * Recycling of already inflated view must be taken care of by the implementation. Else memory leaks can occur!
     * @return layout to be shown by the window manager.
     */
    protected abstract View getLayout();

    /**
     * Adds the view specified by getLayout() to the windowManager passed during initialization
     */
    public void execute(){
        /*if(layout != null) {
            remove();
        }*/
        layout = getLayout();
        layout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        if(params == null){
            params = getLayoutParams();
        }
        try {
            windowManager.addView(layout, params);
            layout.invalidate();
        } catch (IllegalStateException e){
            //Dont do anything
        }
    }

    /**
     * Removes the view specified by getLayout() from the windowManager passed during initialization
     */
    public void remove(){
        if(layout == null){
            layout = getLayout();
        }
        try {
            windowManager.removeView(layout);
            /*layout = null;
            params = null;*/
        } catch (Exception e){
            //Do nothing
        }
    }

    protected int getDisplayWidth(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    protected int getDisplayHeight(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }


    protected int getFullScreenSystemUiVisibility(){
        int systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            systemUiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        return systemUiVisibility;
    }
}
