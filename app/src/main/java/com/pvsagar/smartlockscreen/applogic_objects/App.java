package com.pvsagar.smartlockscreen.applogic_objects;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by aravind on 11/11/14.
 */
public class App{
    private String packageName, appName;
    private Drawable appIcon;

    public App(String packageName,String appName, Drawable appIcon){
        this.packageName = packageName;
        this.appName = appName;
        this.appIcon = appIcon;
    }

    public App(String packageName, Context context){
        this.packageName = packageName;
        setAppNameAndIconFromPackageName(packageName, context);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public static App getAppFromCursor(Cursor cursor, Context context){
        String packageName;
        try{
            packageName = cursor.getString(cursor.getColumnIndex(EnvironmentDatabaseContract.AppWhitelistEntry.COLUMN_PACKAGE_NAME));
        } catch (Exception e){
            throw new IllegalArgumentException("Cursor should be populated with AppWhitelist table data.");
        }

        return new App(packageName, context);
    }

    private void setAppNameAndIconFromPackageName(String packageName, Context context){
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
            appIcon = context.getPackageManager().getApplicationIcon(info);
            appName = context.getPackageManager().getApplicationLabel(info).toString();
        } catch (PackageManager.NameNotFoundException e){
            //Do nothing
        }
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof App)){
            return false;
        }
        App app = (App) o;
        return app.packageName.equals(packageName);
    }

    @Override
    public String toString() {
        return appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public static List<App> getAllApps(PackageManager packageManager){
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0);

        List<App> allApps = new ArrayList<App>();
        for(ResolveInfo info: pkgAppsList){
            ApplicationInfo appInfo = info.activityInfo.applicationInfo;
            allApps.add(new App(appInfo.packageName, appInfo.loadLabel(packageManager).toString(),
                    appInfo.loadIcon(packageManager)));
        }
        Collections.sort(allApps, new AppComparator());
        return allApps;
    }

    public static class AppComparator implements Comparator<App>{
        @Override
        public int compare(App lhs, App rhs) {
            return lhs.getAppName().compareToIgnoreCase(rhs.getAppName());
        }
    }
}
