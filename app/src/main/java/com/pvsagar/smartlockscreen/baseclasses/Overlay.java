package com.pvsagar.smartlockscreen.baseclasses;

import android.content.Context;
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
    private View layout;
    protected WindowManager.LayoutParams params;

    public Overlay(Context context, WindowManager windowManager){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.windowManager = windowManager;
    }

    protected abstract WindowManager.LayoutParams getLayoutParams();

    protected abstract View getLayout();

    /**
     * Adds the view specified by getLayout() to the windowManager passed during initialization
     */
    public void execute(){
        if(layout != null) {
            remove();
        }
        layout = getLayout();
        layout.setLayerType(View.LAYER_TYPE_HARDWARE,null);
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
            layout = null;
            params = null;
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
}
