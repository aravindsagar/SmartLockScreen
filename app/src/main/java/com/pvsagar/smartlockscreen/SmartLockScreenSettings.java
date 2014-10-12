package com.pvsagar.smartlockscreen;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.pvsagar.smartlockscreen.adapters.NavigationDrawerListAdapter;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.fragments.ManageEnvironmentFragment;
import com.pvsagar.smartlockscreen.fragments.OverlappingEnvironmentsFragment;
import com.pvsagar.smartlockscreen.fragments.SetMasterPasswordFragment;
import com.pvsagar.smartlockscreen.frontend_helpers.OneTimeInitializer;
import com.pvsagar.smartlockscreen.receivers.AdminActions;
import com.pvsagar.smartlockscreen.services.BaseService;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmartLockScreenSettings extends ActionBarActivity
        implements SetMasterPasswordFragment.MasterPasswordSetListener,
        ManageEnvironmentFragment.ActionModeListener{
    private static final String LOG_TAG = SmartLockScreenSettings.class.getSimpleName();

    private static final int INDEX_MANAGE_ENVIRONMENTS = 0;
    private static final int INDEX_ENVIRONMENT_OVERLAP = 1;
    private static final int INDEX_MASTER_PASSWORD = 2;
    private static final int INDEX_SETTINGS = 0;
    private static final int INDEX_HELP = 1;
    private static final int INDEX_ABOUT = 2;

    private static final int MASTER_PASSWORD_REQUEST = 41;

    private static int mPaddingTop = 0, mPaddingBottom = 0;

    SystemBarTintManager tintManager;

    DrawerLayout drawerLayout;
    ListView navDrawerListView;
    NavigationDrawerListAdapter listAdapter;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    int actionBarColor;

    List<String> mainItemList;

    String mTitle;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_lock_screen_settings);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ManageEnvironmentFragment())
                    .commit();
        }
        OneTimeInitializer.initialize(this, MASTER_PASSWORD_REQUEST);

        startService(BaseService.getServiceIntent(this, null, null));

        setUpActionBar();
        setUpNavDrawer();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_main_settings);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                switch (listAdapter.getItemViewType(position)){
                    case NavigationDrawerListAdapter.ITEM_TYPE_PROFILE:
                        listAdapter.setSelectedProfileIndex(listAdapter.getItemArrayIndex(position));
                        break;
                    case NavigationDrawerListAdapter.ITEM_TYPE_NEW_PROFILE:
                        //TODO after user profiles are enabled
                        break;
                    case NavigationDrawerListAdapter.ITEM_TYPE_MAIN:
                        FragmentManager fragmentManager = getFragmentManager();
                        boolean isValid = true;
                        int itemArrayIndex = listAdapter.getItemArrayIndex(position);
                        switch (itemArrayIndex){
                            case INDEX_MANAGE_ENVIRONMENTS:
                                fragmentManager.beginTransaction()
                                        .replace(R.id.container, new ManageEnvironmentFragment())
                                        .commit();
                                break;
                            case INDEX_ENVIRONMENT_OVERLAP:
                                fragmentManager.beginTransaction()
                                        .replace(R.id.container, new OverlappingEnvironmentsFragment())
                                        .commit();
                                break;
                            case INDEX_MASTER_PASSWORD:
                                fragmentManager.beginTransaction()
                                        .replace(R.id.container, new SetMasterPasswordFragment())
                                        .commit();
                                break;
                            default:
                                isValid = false;
                        }
                        if(isValid){
                            mTitle = mainItemList.get(itemArrayIndex);
                            listAdapter.setSelectedMainItemIndex(itemArrayIndex);
                            listAdapter.notifyDataSetChanged();
                        }
                        break;
                    case NavigationDrawerListAdapter.ITEM_TYPE_SECONDARY:
                        fragmentManager = getFragmentManager();
                        switch (listAdapter.getItemArrayIndex(position)){
                            //TODO launch appropriate activities
                            default:
                                Toast.makeText(SmartLockScreenSettings.this, "Not yet implemented", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        return;
                }
                if(mTitle != null) {
                    setTitle(mTitle);
                }
                position = -1;
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mTitle = getTitle().toString();
                setTitle(getString(R.string.title_activity_smart_lock_screen_settings));
            }
        };
        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        drawerLayout.openDrawer(Gravity.START);
    }

    private void setUpActionBar(){
        ActionBar actionBar = getSupportActionBar();
        actionBarColor = getResources().getColor(R.color.action_bar_settings);
        if(!Utility.checkForNullAndWarn(actionBar, LOG_TAG)){
            actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(actionBarColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mPaddingTop = tintManager.getConfig().getPixelInsetTop(true);
            mPaddingBottom = tintManager.getConfig().getNavigationBarHeight();
        }
    }

    private void setUpNavDrawer(){
        List<User> userList = new ArrayList<User>(Arrays.asList(new User[]{User.getDefaultUser(this)}));
        mainItemList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.nav_drawer_main_items)));
        List<Integer> mainItemRIds = new ArrayList<Integer>();
        mainItemRIds.add(R.drawable.ic_environment);
        mainItemRIds.add(R.drawable.ic_env_overlap);
        mainItemRIds.add(R.drawable.ic_master_password);
        List<String> secondaryItemList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.nav_drawer_secondary_items)));
        List<Integer> secondaryItemIds = new ArrayList<Integer>();
        secondaryItemIds.add(R.drawable.ic_settings);
        secondaryItemIds.add(R.drawable.ic_help);
        secondaryItemIds.add(R.drawable.ic_about);
        listAdapter = new NavigationDrawerListAdapter(this, userList, mainItemList, mainItemRIds, secondaryItemList, secondaryItemIds);

        navDrawerListView = (ListView) findViewById(R.id.drawer_list_view);
        switch (getResources().getConfiguration().orientation){
            case Configuration.ORIENTATION_UNDEFINED:
            case Configuration.ORIENTATION_PORTRAIT:
                View footerView = new View(this);
                footerView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mPaddingBottom));
                footerView.setBackgroundColor(Color.TRANSPARENT);
                navDrawerListView.addFooterView(footerView, null, false);
                break;
        }
        navDrawerListView.setPadding(navDrawerListView.getPaddingLeft(), navDrawerListView.getPaddingTop() + mPaddingTop,
                navDrawerListView.getPaddingRight(), navDrawerListView.getPaddingBottom());
        navDrawerListView.setAdapter(listAdapter);

        navDrawerListView.setSelection(userList.size() + 2);
        navDrawerListView.setOnItemClickListener(new DrawerItemClickListener());
        listAdapter.setSelectedProfileIndex(0);
        listAdapter.setSelectedMainItemIndex(0);
        mTitle = mainItemList.get(0);
    }

    @Override
    public void onMasterPasswordSet() {
        Toast.makeText(this, "Master passphrase changed", Toast.LENGTH_SHORT).show();
        drawerLayout.openDrawer(Gravity.START);
    }

    @Override
    public void onCancelSetMasterPassword() {
        drawerLayout.openDrawer(Gravity.START);
    }

    @Override
    public void onActionModeDestroyed() {
        tintManager.setTintColor(actionBarColor);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onActionModeCreated() {
        tintManager.setTintColor(getResources().getColor(R.color.action_mode));
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SmartLockScreenSettings.this.position = position;
            drawerLayout.closeDrawers();
        }
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(Gravity.START)){
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MASTER_PASSWORD_REQUEST){
            if(!(resultCode == RESULT_OK && AdminActions.isAdminEnabled())){
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
