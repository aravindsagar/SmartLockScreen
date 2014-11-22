package com.pvsagar.smartlockscreen;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pvsagar.smartlockscreen.adapters.NavigationDrawerListAdapter;
import com.pvsagar.smartlockscreen.adapters.UserListAdapter;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.fragments.AllowedAppsFragment;
import com.pvsagar.smartlockscreen.fragments.ManageEnvironmentFragment;
import com.pvsagar.smartlockscreen.fragments.OverlappingEnvironmentsFragment;
import com.pvsagar.smartlockscreen.fragments.SetMasterPasswordFragment;
import com.pvsagar.smartlockscreen.fragments.SetPasswordFragment;
import com.pvsagar.smartlockscreen.frontend_helpers.OneTimeInitializer;
import com.pvsagar.smartlockscreen.receivers.AdminActions;
import com.pvsagar.smartlockscreen.services.BaseService;
import com.pvsagar.smartlockscreen.services.NotificationService;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;

/**
 * The main settings activity with a navigation drawer.
 */
public class SmartLockScreenSettings extends ActionBarActivity
        implements SetMasterPasswordFragment.MasterPasswordSetListener,
        ManageEnvironmentFragment.ActionModeListener,
        UserListAdapter.OnUsersModifiedListener,
        SetPasswordFragment.SetPassphraseInterface{
    private static final String LOG_TAG = SmartLockScreenSettings.class.getSimpleName();

    private static final int INDEX_MANAGE_ENVIRONMENTS = 0;
    private static final int INDEX_ENVIRONMENT_OVERLAP = 1;
    private static final int INDEX_MASTER_PASSWORD = 2;
    private static final int INDEX_ALLOWED_APPS = 0;
    private static final int INDEX_PASSWORD = 1;
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

    List<String> mainItemList, secondaryItemList, restrictedMainItemList;
    List<Integer> mainItemRIds, secondaryItemIds, restrictedMainItemRIds;

    String mTitle;
    int position, prevPosition = -1;

    ListView usersList;
    ImageView currentUserPicture;
    TextView currentUserName, currentUserDescription;
    ImageView userListDropDown;
    UserListAdapter adapter;
    int mSelectedUserIndex;
    private long mDeviceOwnerId;
    private int mDeviceOwnerIndex = 0;
    private List<User> allUsers;
    private boolean isInUserList = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_lock_screen_settings);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ManageEnvironmentFragment())
                    .commit();
        }
        OneTimeInitializer.initialize(this, MASTER_PASSWORD_REQUEST);

        startService(BaseService.getServiceIntent(this, null, null));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && !NotificationService.isInstanceCreated()){
            Intent intent = new Intent(this,NotificationService.class);
            startService(intent);
        }

        mDeviceOwnerId = SharedPreferencesHelper.getDeviceOwnerUserId(this);

        setUpActionBar();
        setUpNavDrawer();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_main_settings);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if(position == prevPosition){
                    if(mTitle != null) {
                        setTitle(mTitle);
                    }
                    return;
                }
                switch (listAdapter.getItemViewType(position)){
                    case NavigationDrawerListAdapter.ITEM_TYPE_MAIN:
                        handleMainItemClick();
                        break;
                    case NavigationDrawerListAdapter.ITEM_TYPE_SECONDARY:
                        handleSecondaryItemClick();
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

    private void handleMainItemClick(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft;
        boolean isValid = true;
        int itemArrayIndex = listAdapter.getItemArrayIndex(position);
        if(mSelectedUserIndex == mDeviceOwnerIndex) {
            switch (itemArrayIndex) {
                case INDEX_MANAGE_ENVIRONMENTS:
                    ft = fragmentManager.beginTransaction();
                    ft.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
                    ft.replace(R.id.container, new ManageEnvironmentFragment())
                            .commit();
                    break;
                case INDEX_ENVIRONMENT_OVERLAP:
                    ft = fragmentManager.beginTransaction();
                    ft.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
                    ft.replace(R.id.container, new OverlappingEnvironmentsFragment())
                            .commit();
                    break;
                case INDEX_MASTER_PASSWORD:
                    ft = fragmentManager.beginTransaction();
                    ft.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
                    ft.replace(R.id.container, new SetMasterPasswordFragment())
                            .commit();
                    break;
                default:
                    isValid = false;
            }
            if (isValid) {
                mTitle = mainItemList.get(itemArrayIndex);
                listAdapter.setSelectedMainItemIndex(itemArrayIndex);
                listAdapter.notifyDataSetChanged();
            }
        } else {
            switch (itemArrayIndex){
                /*case INDEX_MANAGE_ENVIRONMENTS:
                    ft = fragmentManager.beginTransaction();
                    ft.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
                    ft.replace(R.id.container, new ManageEnvironmentFragment())
                            .commit();
                    break;*/
                case INDEX_ALLOWED_APPS:
                    ft = fragmentManager.beginTransaction();
                    ft.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
                    ft.replace(R.id.container, AllowedAppsFragment.getNewInstance(allUsers.get(mSelectedUserIndex).getId()))
                            .commit();
                    break;
                case INDEX_PASSWORD:
                    ft = fragmentManager.beginTransaction();
                    ft.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
                    ft.replace(R.id.container, new SetPasswordFragment())
                            .commit();
                    break;
                default:
                    isValid = false;
            }
            if (isValid) {
                mTitle = restrictedMainItemList.get(itemArrayIndex);
                listAdapter.setSelectedMainItemIndex(itemArrayIndex);
                listAdapter.notifyDataSetChanged();
            }
        }
        if(position != -1) {
            prevPosition = position;
        }
    }

    private void handleSecondaryItemClick(){
        switch (listAdapter.getItemArrayIndex(position)){
            case INDEX_SETTINGS:
                startActivity(new Intent(SmartLockScreenSettings.this, GeneralSettingsActivity.class));
                break;
            default:
                Toast.makeText(SmartLockScreenSettings.this, "Not yet implemented", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUsersModified() {
        refreshUserList();
    }

    private void refreshUserList(){
        allUsers = User.getAllUsers(this);
        for (int i = 0; i < allUsers.size(); i++) {
            User user = allUsers.get(i);
            if (user.getId() == mDeviceOwnerId) {
                mDeviceOwnerIndex = i;
                break;
            }
        }
        adapter = new UserListAdapter(this, R.layout.nav_drawer_list_item_profile,
                R.layout.nav_drawer_new_profile, allUsers, mDeviceOwnerIndex, this);
        usersList.setAdapter(adapter);
        setCurrentUserView();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onPostCreate(savedInstanceState, persistentState);
        }
        actionBarDrawerToggle.syncState();
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
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
                mPaddingTop += 16;
            }
            mPaddingBottom = tintManager.getConfig().getNavigationBarHeight();
        }
    }

    private void setUpNavDrawer(){

        //Main drawer items for Device Owner
        mainItemList = new ArrayList<String>();
        mainItemList.add(INDEX_MANAGE_ENVIRONMENTS, getString(R.string.title_activity_manage_environment));
        mainItemList.add(INDEX_ENVIRONMENT_OVERLAP, getString(R.string.title_activity_environment_overlap));
        mainItemList.add(INDEX_MASTER_PASSWORD, getString(R.string.title_activity_set_master_password));

        mainItemRIds = new ArrayList<Integer>();
        mainItemRIds.add(INDEX_MANAGE_ENVIRONMENTS, R.drawable.ic_environment);
        mainItemRIds.add(INDEX_ENVIRONMENT_OVERLAP, R.drawable.ic_env_overlap);
        mainItemRIds.add(INDEX_MASTER_PASSWORD, R.drawable.ic_master_password);

        //Main drawer items for Restricted profile
        restrictedMainItemList = new ArrayList<String>();
//        restrictedMainItemList.add(INDEX_MANAGE_ENVIRONMENTS, getString(R.string.title_activity_manage_environment));
        restrictedMainItemList.add(INDEX_ALLOWED_APPS, getString(R.string.title_activity_allowed_apps));
        restrictedMainItemList.add(INDEX_PASSWORD, getString(R.string.title_activity_set_passphrase));

        restrictedMainItemRIds = new ArrayList<Integer>();
//        restrictedMainItemRIds.add(INDEX_MANAGE_ENVIRONMENTS, R.drawable.ic_environment);
        restrictedMainItemRIds.add(INDEX_ALLOWED_APPS, R.drawable.ic_apps);
        restrictedMainItemRIds.add(INDEX_PASSWORD, R.drawable.ic_master_password);

        //Secondary drawer items
        secondaryItemList = new ArrayList<String>();
        secondaryItemList.add(INDEX_SETTINGS, getString(R.string.title_activity_general_settings));
        secondaryItemList.add(INDEX_HELP, getString(R.string.title_activity_help));
        secondaryItemList.add(INDEX_ABOUT, getString(R.string.title_activity_about));

        secondaryItemIds = new ArrayList<Integer>();
        secondaryItemIds.add(INDEX_SETTINGS, R.drawable.ic_settings);
        secondaryItemIds.add(INDEX_HELP, R.drawable.ic_help);
        secondaryItemIds.add(INDEX_ABOUT, R.drawable.ic_about);

        listAdapter = new NavigationDrawerListAdapter(this, mainItemList, mainItemRIds, secondaryItemList, secondaryItemIds);

        RelativeLayout navDrawerLayout = (RelativeLayout) findViewById(R.id.linear_layout_nav_drawer);
        final LinearLayout currentUserLinearLayout = (LinearLayout) navDrawerLayout.findViewById(R.id.linear_layout_current_user);
        currentUserLinearLayout.bringToFront();
        navDrawerLayout.requestLayout();
        navDrawerLayout.invalidate();
        currentUserPicture = (ImageView) currentUserLinearLayout.findViewById(R.id.user_image_view);
        currentUserName = (TextView) currentUserLinearLayout.findViewById(R.id.user_name_text_view);
        currentUserDescription = (TextView) currentUserLinearLayout.findViewById(R.id.user_type_text_view);
        userListDropDown = (ImageView) currentUserLinearLayout.findViewById(R.id.user_selected_tick_image_view);
        currentUserLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInUserList){
                    hideUserList();
                } else {
                    showUserList();
                }
                isInUserList = !isInUserList;
            }
        });

        navDrawerListView = (ListView) navDrawerLayout.findViewById(R.id.drawer_list_view);

        usersList = (ListView) navDrawerLayout.findViewById(R.id.list_view_user_profiles);
        refreshUserList();
        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedUserIndex = position;
                setCurrentUserView();
                if (position == mDeviceOwnerIndex) {
                    onDeviceOwnerSelected();
                } else {
                    onRestrictedProfileSelected();
                }
                currentUserLinearLayout.callOnClick();
            }
        });

        switch (getResources().getConfiguration().orientation){
            case Configuration.ORIENTATION_UNDEFINED:
            case Configuration.ORIENTATION_PORTRAIT:
                View footerView = new View(this);
                footerView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mPaddingBottom));
                footerView.setBackgroundColor(Color.TRANSPARENT);
                navDrawerListView.addFooterView(footerView, null, false);
                break;
        }
        navDrawerLayout.setPadding(navDrawerLayout.getPaddingLeft(), navDrawerLayout.getPaddingTop() + mPaddingTop,
                navDrawerLayout.getPaddingRight(), navDrawerLayout.getPaddingBottom());
        navDrawerListView.setAdapter(listAdapter);

        navDrawerListView.setSelection(0);
        navDrawerListView.setOnItemClickListener(new DrawerItemClickListener());
        listAdapter.setSelectedMainItemIndex(0);
        mTitle = mainItemList.get(0);
        position = prevPosition = navDrawerListView.getSelectedItemPosition();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!isInUserList) {
            usersList.setVisibility(View.GONE);
            usersList.setTranslationY(-usersList.getHeight());
        }
    }

    private void showUserList(){
        usersList.clearAnimation();
        usersList.setVisibility(View.VISIBLE);

        usersList.animate().alpha(1f).translationY(0f).setInterpolator(new DecelerateInterpolator())
                .setListener(null).start();
        navDrawerListView.animate().translationY(usersList.getHeight()).alpha(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                navDrawerListView.setVisibility(View.GONE);
                usersList.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                navDrawerListView.setVisibility(View.GONE);
                usersList.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).setInterpolator(new DecelerateInterpolator()).start();

        userListDropDown.animate().rotation(180).setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }

    private void hideUserList(){
        usersList.clearAnimation();
        usersList.setBackgroundColor(Color.WHITE);
        usersList.animate().translationY(-usersList.getHeight()).setInterpolator(new AccelerateDecelerateInterpolator()).
                setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        usersList.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        usersList.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        navDrawerListView.setVisibility(View.VISIBLE);
        navDrawerListView.animate().translationY(0).alpha(1f).setListener(null).
                setInterpolator(new AccelerateDecelerateInterpolator()).start();
        userListDropDown.animate().rotation(0).setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }

    private void setCurrentUserView(){
        User currentUser = allUsers.get(mSelectedUserIndex);
        currentUserPicture.setImageDrawable(currentUser.getUserPictureDrawable(this));
        currentUserName.setText(currentUser.getUserName());
        if(currentUser.getId() == mDeviceOwnerId){
            currentUserDescription.setText(this.getString(R.string.user_type_device_owner));
        } else {
            currentUserDescription.setText(this.getString(R.string.user_type_restricted_profile));
        }
    }

    private void onDeviceOwnerSelected(){
        listAdapter.setMainItems(mainItemList, mainItemRIds);
        navDrawerListView.setSelection(0);
        listAdapter.setSelectedMainItemIndex(0);
        mTitle = mainItemList.get(0);
        position = 0;
        prevPosition = -1;
    }

    private void onRestrictedProfileSelected(){
        listAdapter.setMainItems(restrictedMainItemList, restrictedMainItemRIds);
        navDrawerListView.setSelection(0);
        listAdapter.setSelectedMainItemIndex(0);
        mTitle = mainItemList.get(0);
        position = 0;
        prevPosition = -1;
    }

    @Override
    public void onMasterPasswordSet() {
        Toast.makeText(this, "Passphrase changed", Toast.LENGTH_SHORT).show();
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
//        tintManager.setTintColor(getResources().getColor(R.color.action_mode));
        tintManager.setTintColor(Color.BLACK); //TODO ActionMode color not working after updating to API 21. Will revert after finding a fix
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
    public void onSettingsClicked() {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MASTER_PASSWORD_REQUEST){
            if(!(resultCode == RESULT_OK && AdminActions.isAdminEnabled())){
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPassphraseEntered(Passphrase passphrase) {
        allUsers.get(mSelectedUserIndex).setPassphraseForUnknownEnvironment(this, passphrase);
        onMasterPasswordSet();
    }


    @Override
    public Passphrase getCurrentPassphrase() {
        return null;
    }
}
