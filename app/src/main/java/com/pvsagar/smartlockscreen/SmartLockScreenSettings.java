package com.pvsagar.smartlockscreen;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

import com.pvsagar.smartlockscreen.adapters.NavigationDrawerListAdapter;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.fragments.SetMasterPasswordFragment;
import com.pvsagar.smartlockscreen.frontend_helpers.OneTimeInitializer;
import com.pvsagar.smartlockscreen.services.BaseService;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SmartLockScreenSettings extends ActionBarActivity implements SetMasterPasswordFragment.MasterPasswordSetListener{
    private static final String LOG_TAG = SmartLockScreenSettings.class.getSimpleName();

    private static int mPaddingTop = 0, mPaddingBottom = 0;

    SystemBarTintManager tintManager;

    NavigationDrawerListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_lock_screen_settings);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ContentFragment())
                    .commit();
        }
        if(!OneTimeInitializer.initialize(this)){
            finish();
        }

        startService(BaseService.getServiceIntent(this, null, null));

        setUpActionBar();
        setUpNavDrawer();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if(!Utility.checkForNullAndWarn(actionBar, LOG_TAG)){
            actionBar.setBackgroundDrawable(new ColorDrawable(
                    getResources().getColor(R.color.action_bar_settings_main)));
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.action_bar_settings_main));
            mPaddingTop = tintManager.getConfig().getPixelInsetTop(true);
            mPaddingBottom = tintManager.getConfig().getNavigationBarHeight();
        }
    }

    private void setUpNavDrawer(){
        List<User> userList = new ArrayList<User>(Arrays.asList(new User[]{User.getDefaultUser(this)}));
        List<String> mainItemList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.nav_drawer_main_items)));
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

        ListView navDrawerListView = (ListView) findViewById(R.id.drawer_list_view);
        View footerView = new View(this);
        footerView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mPaddingBottom));
        footerView.setBackgroundColor(Color.TRANSPARENT);
        footerView.setFocusable(false);
        View headerView = new View(this);
        headerView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mPaddingTop));
        headerView.setBackgroundColor(Color.TRANSPARENT);
        navDrawerListView.addFooterView(footerView);
        navDrawerListView.addHeaderView(headerView);
        navDrawerListView.setAdapter(listAdapter);
    }

    @Override
    public void onMasterPasswordSet() {

    }

    @Override
    public void onCancelSetMasterPassword() {

    }


    public static class ContentFragment extends Fragment {

        Button manageEnvironmentButton;

        public ContentFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_manage_environment, container, false);
            return rootView;
        }
    }
}
