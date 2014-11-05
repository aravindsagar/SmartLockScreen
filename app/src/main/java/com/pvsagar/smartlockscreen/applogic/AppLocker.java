package com.pvsagar.smartlockscreen.applogic;

import android.content.Context;
import android.util.Log;

import com.pvsagar.smartlockscreen.baseclasses.OnForegroundAppChangedListener;

/**
 * Created by aravind on 5/11/14.
 */
public class AppLocker implements OnForegroundAppChangedListener {
    private static final String TEST_PACKAGE_NAME = "com.rovio.angrybirdstransformers";
    private static final String LOG_TAG = OnForegroundAppChangedListener.class.getSimpleName();

    private Context mContext;

    public AppLocker(Context context){
        mContext = context;
    }

    @Override
    public void onForegroundAppChanged(String packageName, String activityName) {
        Log.d(LOG_TAG, "Package Name: " + packageName + ", activity name: " + activityName);
    }
}
