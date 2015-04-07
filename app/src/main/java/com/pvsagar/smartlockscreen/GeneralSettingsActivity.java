package com.pvsagar.smartlockscreen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.pvsagar.smartlockscreen.backend_helpers.RootHelper;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.frontend_helpers.MediaStoreUtils;
import com.pvsagar.smartlockscreen.frontend_helpers.WallpaperHelper;
import com.pvsagar.smartlockscreen.services.BaseService;

import java.io.File;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class GeneralSettingsActivity extends PreferenceActivity implements RootHelper.RootAccessCheckedListener {
    private static final String LOG_TAG = GeneralSettingsActivity.class.getSimpleName();
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = true;
    public static String PREF_KEY_ENABLE_NOTIFICATION;
    public static String PREF_KEY_SHOW_LOCKSCREEN_NOTIFICATIONS;
    public static String PREF_KEY_HIDE_PERSISTENT_NOTIFICATIONS;
    public static String PREF_KEY_HIDE_LOW_PRIORITY_NOTIFICATIONS;
    public static String PREF_KEY_SET_WALLPAPER;
    public static String PREF_KEY_PATTERN_TYPE;
    public static String PREF_KEY_VISIBLE_PATTERN;

    private static int REQUEST_PICTURE = 1;
    private static int REQUEST_CROP_PICTURE = 2;

    private static Preference wallpaperPreference, hideLowPriorityNotificationPreference, hidePersistentNotificationPreference;
    private SLSPreferenceChangeListener preferenceChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PREF_KEY_ENABLE_NOTIFICATION = getResources().getString(R.string.pref_key_enable_notification);
        PREF_KEY_SHOW_LOCKSCREEN_NOTIFICATIONS = getResources().getString(R.string.pref_key_show_lockscreen_notifications);
        PREF_KEY_HIDE_PERSISTENT_NOTIFICATIONS = getResources().getString(R.string.pref_key_hide_persistent_notifications);
        PREF_KEY_HIDE_LOW_PRIORITY_NOTIFICATIONS = getResources().getString(R.string.pref_key_hide_low_priority_notifications);
        PREF_KEY_SET_WALLPAPER = getResources().getString(R.string.pref_key_lockscreen_wallpaper);
        PREF_KEY_PATTERN_TYPE = getResources().getString(R.string.pref_key_pattern_type);
        PREF_KEY_VISIBLE_PATTERN = getResources().getString(R.string.pref_key_is_visible_pattern);
        setupActionBar();
        RootHelper.hasRootAccessAsync(this);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        // get the root container of the preferences list
        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.preferences_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bar.setTitle(R.string.title_activity_general_settings);
        bar.setTitleTextColor(Color.WHITE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        preferenceChangeListener = new SLSPreferenceChangeListener(GeneralSettingsActivity.this);

        //General Preferences
        addPreferencesFromResource(R.xml.pref_general);
        setAndCallListener(findPreference(PREF_KEY_ENABLE_NOTIFICATION),preferenceChangeListener);

        //Lockscreen Preferences
        PreferenceCategory lockscreenHeader = new PreferenceCategory(this);
        lockscreenHeader.setTitle(R.string.pref_header_lockscreen);
        getPreferenceScreen().addPreference(lockscreenHeader);
        addPreferencesFromResource(R.xml.pref_lockscreen);
        hideLowPriorityNotificationPreference = findPreference(PREF_KEY_HIDE_LOW_PRIORITY_NOTIFICATIONS);
        hidePersistentNotificationPreference = findPreference(PREF_KEY_HIDE_PERSISTENT_NOTIFICATIONS);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getPreferenceScreen().removePreference(findPreference(PREF_KEY_SHOW_LOCKSCREEN_NOTIFICATIONS));
            getPreferenceScreen().removePreference(hidePersistentNotificationPreference);
            getPreferenceScreen().removePreference(hideLowPriorityNotificationPreference);
        } else {
            setAndCallListener(findPreference(PREF_KEY_SHOW_LOCKSCREEN_NOTIFICATIONS),preferenceChangeListener);
            setAndCallListener(findPreference(PREF_KEY_HIDE_PERSISTENT_NOTIFICATIONS),preferenceChangeListener);
            setAndCallListener(findPreference(PREF_KEY_HIDE_LOW_PRIORITY_NOTIFICATIONS),preferenceChangeListener);
        }
        wallpaperPreference = findPreference(PREF_KEY_SET_WALLPAPER);
        wallpaperPreference.setOnPreferenceChangeListener(preferenceChangeListener);
        String[] wallpaperValues = getResources().getStringArray(R.array.pref_values_lockscreen_wallpaper);
        if(SharedPreferencesHelper.getWallpaperPreference(this).equals(wallpaperValues[1])){
            wallpaperPreference.setSummary(R.string.pref_description_lockscreen_wallpaper_custom);
        } else {
            wallpaperPreference.setSummary(R.string.pref_description_lockscreen_wallpaper_system);
        }

    }

    @Override
    public void onRootAccessChecked(boolean hasRootAccess) {
        //Pattern preferences
        if(hasRootAccess) {
            PreferenceCategory patternHeader = new PreferenceCategory(this);
            patternHeader.setTitle(R.string.pref_header_pattern);
            getPreferenceScreen().addPreference(patternHeader);
            addPreferencesFromResource(R.xml.pref_pattern);
            setAndCallListener(findPreference(PREF_KEY_PATTERN_TYPE), preferenceChangeListener);
        }
    }

    private static class SLSPreferenceChangeListener implements Preference.OnPreferenceChangeListener {
        private Context mContext;
        public SLSPreferenceChangeListener(Context context){
            mContext = context;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if(preference.getKey().equals(PREF_KEY_ENABLE_NOTIFICATION)) {
                if ((Boolean) newValue) {
                    String notificationText;
                    if (BaseService.getCurrentEnvironments() == null || BaseService.getCurrentEnvironments().isEmpty()) {
                        notificationText = "Unknown Environment";
                    } else {
                        notificationText = "Current Environment: " + BaseService.getCurrentEnvironments().get(0).getName();
                    }
                    mContext.startService(BaseService.getServiceIntent(mContext,
                            notificationText, null));
                } else {
                    mContext.startService(BaseService.getServiceIntent(mContext, null,
                            BaseService.ACTION_REMOVE_PERSISTENT_NOTIFICATION));
                }
            } else if (preference.getKey().equals(PREF_KEY_SHOW_LOCKSCREEN_NOTIFICATIONS)){
                Intent lockscreenTypeIntent = BaseService.getServiceIntent(mContext, null, BaseService.ACTION_SET_LOCKSCREEN_TYPE);
                if((boolean) newValue) {
                    lockscreenTypeIntent.putExtra(BaseService.EXTRA_LOCKSCREEN_TYPE, BaseService.LOCKSCREEN_TYPE_NOTIFICATIONS);
                    hideLowPriorityNotificationPreference.setEnabled(true);
                    hidePersistentNotificationPreference.setEnabled(true);
                } else {
                    lockscreenTypeIntent.putExtra(BaseService.EXTRA_LOCKSCREEN_TYPE, BaseService.LOCKSCREEN_TYPE_MINIMAL);
                    hideLowPriorityNotificationPreference.setEnabled(false);
                    hidePersistentNotificationPreference.setEnabled(false);
                }
                mContext.startService(lockscreenTypeIntent);
            } else if(preference.getKey().equals(PREF_KEY_HIDE_PERSISTENT_NOTIFICATIONS)){

            } else if(preference.getKey().equals(PREF_KEY_HIDE_LOW_PRIORITY_NOTIFICATIONS)){

            } else if(preference.getKey().equals(PREF_KEY_SET_WALLPAPER)){
                String[] wallpaperTypes = mContext.getResources().getStringArray(R.array.pref_values_lockscreen_wallpaper);
                if(newValue.equals(wallpaperTypes[1])){
                    ((Activity)mContext).startActivityForResult(MediaStoreUtils.getPickImageIntent(mContext), REQUEST_PICTURE);
                    return false;
                } else {
                    preference.setSummary(R.string.pref_description_lockscreen_wallpaper_system);
                    WallpaperHelper.onWallpaperChanged(mContext, wallpaperTypes[0]);
                }
            } else if(preference.getKey().equals(PREF_KEY_PATTERN_TYPE)){
                if(newValue.equals("inbuilt")){
                    preference.setSummary(mContext.getString(R.string.pref_summary_inbuilt_pattern));
                } else if(newValue.equals("system")){
                    preference.setSummary(mContext.getString(R.string.pref_summary_system_pattern));
                }
                mContext.startService(BaseService.getServiceIntent(mContext, null, BaseService.ACTION_DETECT_ENVIRONMENT));
            }
            return true;
        }
    }

    private static void setAndCallListener(Preference preference, Preference.OnPreferenceChangeListener listener){
        preference.setOnPreferenceChangeListener(listener);

        if(preference instanceof CheckBoxPreference) {
            listener.onPreferenceChange(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext()).getBoolean(preference.getKey(), false));
        } else {
            listener.onPreferenceChange(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        File croppedImageFile = new File(getFilesDir(), "wallpaper.jpg");

        if ((requestCode == REQUEST_PICTURE) && (resultCode == RESULT_OK)) {
            Uri croppedImage = Uri.fromFile(croppedImageFile);

            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(getDisplayWidth(), getDisplayHeight(), getDisplayWidth(), getDisplayHeight(), croppedImage);
            cropImage.setSourceImage(data.getData());
            cropImage.setOutputQuality(100);

            startActivityForResult(cropImage.getIntent(this), REQUEST_CROP_PICTURE);
        } else if ((requestCode == REQUEST_CROP_PICTURE) && (resultCode == RESULT_OK)) {
            Toast.makeText(this, getString(R.string.toast_wallpaper_set), Toast.LENGTH_SHORT).show();
            wallpaperPreference.setSummary(R.string.pref_description_lockscreen_wallpaper_custom);
            String[] values = getResources().getStringArray(R.array.pref_values_lockscreen_wallpaper);
            SharedPreferencesHelper.setWallpaperPreference(this, values[1]);
            WallpaperHelper.onWallpaperChanged(this, values[1]);
        }
    }

    private int getDisplayWidth(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    private int getDisplayHeight(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            setAndCallListener(findPreference(PREF_KEY_ENABLE_NOTIFICATION),
                    new SLSPreferenceChangeListener(getActivity()));
        }
    }

    public static class LockScreenSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_lockscreen);
            SLSPreferenceChangeListener preferenceChangeListener = new SLSPreferenceChangeListener(getActivity());
            setAndCallListener(findPreference(PREF_KEY_HIDE_PERSISTENT_NOTIFICATIONS), preferenceChangeListener);
            setAndCallListener(findPreference(PREF_KEY_HIDE_LOW_PRIORITY_NOTIFICATIONS), preferenceChangeListener);
            wallpaperPreference = findPreference(PREF_KEY_SET_WALLPAPER);
            wallpaperPreference.setOnPreferenceChangeListener(preferenceChangeListener);
            String[] wallpaperValues = getResources().getStringArray(R.array.pref_values_lockscreen_wallpaper);
            if(SharedPreferencesHelper.getWallpaperPreference(getActivity()).equals(wallpaperValues[1])){
                wallpaperPreference.setSummary(R.string.pref_description_lockscreen_wallpaper_custom);
            } else {
                wallpaperPreference.setSummary(R.string.pref_description_lockscreen_wallpaper_system);
            }
        }
    }

    public static class PatternSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }
    }
}
