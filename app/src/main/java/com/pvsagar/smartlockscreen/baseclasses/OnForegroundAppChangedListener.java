package com.pvsagar.smartlockscreen.baseclasses;

/**
 * Created by aravind on 5/11/14.
 */
public interface OnForegroundAppChangedListener {
    public void onForegroundAppChanged(String packageName, String activityName);
}
