package com.pvsagar.smartlockscreen.applogic;

import android.content.Context;

import com.pvsagar.smartlockscreen.DismissKeyguardActivity;
import com.pvsagar.smartlockscreen.applogic_objects.App;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.baseclasses.OnForegroundAppChangedListener;
import com.pvsagar.smartlockscreen.services.AppLockService;

import java.util.List;

/**
 * Created by aravind on 5/11/14.
 */
public class AppLocker implements OnForegroundAppChangedListener {
    private final String APP_PACKAGE_NAME;
    private static final String LOG_TAG = OnForegroundAppChangedListener.class.getSimpleName();

    private Context mContext;

    public AppLocker(Context context){
        mContext = context;
        APP_PACKAGE_NAME = context.getApplicationInfo().packageName;
    }

    @Override
    public void onForegroundAppChanged(final String packageName, final String activityName, int notRequired) {
        if(User.getCurrentUser(mContext).getId() == User.getDefaultUser(mContext).getId()){
            return;
        }
        if(isBlockedApp(packageName, activityName)){
            mContext.startService(AppLockService.getServiceIntent(mContext, AppLockService.ACTION_START_APP_LOCK_OVERLAY));
        } else {
            mContext.startService(AppLockService.getServiceIntent(mContext, AppLockService.ACTION_CLEAR_APP_LOCK_OVERLAY));
        }
    }

    private boolean isBlockedApp(final String packageName, final String activityName){
        //TODO add additional program logic
        if(packageName.equals(APP_PACKAGE_NAME)){
            return !activityName.equals("." + DismissKeyguardActivity.class.getSimpleName());
        }

        List<App> apps = User.getCurrentUser(mContext).getAllowedApps(mContext);
        boolean containsApp = true;
        for (App app : apps) {
            if(app.getPackageName().equals(packageName)){
                containsApp = false;
            }
        }
        return containsApp;
    }
}
