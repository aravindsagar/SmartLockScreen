package com.pvsagar.smartlockscreen.baseclasses;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.adapters.UserListAdapter;
import com.pvsagar.smartlockscreen.applogic.EnvironmentDetector;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.frontend_helpers.CharacterDrawable;
import com.pvsagar.smartlockscreen.receivers.ScreenReceiver;
import com.pvsagar.smartlockscreen.services.BaseService;

import java.util.List;

/**
 * Created by aravind on 13/1/15.
 */
public abstract class LockScreenOverlay extends  Overlay{
    private static final String LOG_TAG = LockScreenOverlay.class.getSimpleName();
    private View environmentOptionsView, userOptionsView;
    private ImageView environmentImageView;
    private GridView userGridView;

    protected static final int DEFAULT_START_ANIMATION_VELOCITY = 0;

    private long mDeviceOwnerId;

    public LockScreenOverlay(Context context, WindowManager windowManager) {
        super(context, windowManager);
        mDeviceOwnerId = SharedPreferencesHelper.getDeviceOwnerUserId(context);
    }

    /**
     * Method to attach environment options views to the UI elements and do one-time initialization stuff
     * @param root
     * @param addToRoot
     */
    protected void buildEnvironmentOptions(ViewGroup root, boolean addToRoot){
        environmentOptionsView = getInflater().inflate(R.layout.lockscreen_environment_options, root, addToRoot);
        View masterPassphraseButton = environmentOptionsView.findViewById(R.id.button_master_passphrase_unlock);
        masterPassphraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Passphrase.getMasterPassword(getContext()).setAsCurrentPassword(getContext());
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            EnvironmentDetector.manageEnvironmentDetectionCriticalSection.acquire();
                            Thread.sleep(10 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            EnvironmentDetector.manageEnvironmentDetectionCriticalSection.release();
                            getContext().startService(BaseService.getServiceIntent(getContext(), null, BaseService.ACTION_DETECT_ENVIRONMENT));
                        }
                    }
                }.start();
                ScreenReceiver.turnScreenOff(getContext());
                ScreenReceiver.turnScreenOn(getContext());
            }
        });
        environmentImageView = (ImageView) environmentOptionsView.findViewById(R.id.image_view_environment_picture);
    }

    protected void buildEnvironmentOptions(ViewGroup root){
        buildEnvironmentOptions(root, true);
    }

    /**
     * Method to attach environment options views to the UI elements and do one-time initialization stuff
     * @param root
     * @param addToRoot
     */
    protected void buildUserOptions(ViewGroup root, boolean addToRoot){
        userOptionsView = getInflater().inflate(R.layout.lockscreen_user_options, root, addToRoot);
        userGridView = (GridView) userOptionsView.findViewById(R.id.grid_view_all_users);
    }

    protected void buildUserOptions(ViewGroup root){
        buildUserOptions(root, true);
    }

    /**
     * Method to populate the fields of environment options
     */
    public void setUpEnvironmentOptions(){
        if(environmentOptionsView == null){
            return;
        }
        String currentEnvironmentName;
        if(BaseService.getCurrentEnvironments() != null && !BaseService.getCurrentEnvironments().isEmpty()) {
            environmentImageView.setImageDrawable(BaseService.getCurrentEnvironments().get(0).getEnvironmentPictureDrawable(getContext()));
            currentEnvironmentName = "Current Environment: " + BaseService.getCurrentEnvironments().get(0).getName();
        } else {
            environmentImageView.setImageDrawable(new CharacterDrawable('?', Color.rgb(150, 150, 150)));
            currentEnvironmentName = getContext().getString(R.string.unknown_environment);
        }
        TextView currentEnvironmentNameTextView = (TextView) environmentOptionsView.findViewById(R.id.text_view_current_environment);
        currentEnvironmentNameTextView.setText(currentEnvironmentName);
    }

    /**
     * Method to populate the fields of user options
     */
    public void setUpUserOptions(){
        if(userGridView == null){
            return;
        }
        final List<User> allUsers = User.getAllUsers(getContext());
        int mDeviceOwnerIndex = 0;
        for (int i = 0; i < allUsers.size(); i++) {
            User user = allUsers.get(i);
            if (user.getId() == mDeviceOwnerId) {
                mDeviceOwnerIndex = i;
                break;
            }
        }

        userGridView.setAdapter(new UserListAdapter(getContext(), R.layout.grid_item_user,
                R.layout.grid_item_settings, allUsers, mDeviceOwnerIndex, new UserListAdapter.OnUsersModifiedListener() {
            @Override
            public void onUsersModified() {}

            @Override
            public void onSettingsClicked() {
                lockScreenDismiss(DEFAULT_START_ANIMATION_VELOCITY);
            }
        }));

        userGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User.setCurrentUser(allUsers.get(position));
                getContext().startService(BaseService.getServiceIntent(getContext(), null, BaseService.ACTION_DETECT_ENVIRONMENT_SWITCH_USER));
            }
        });
    }

    public View getEnvironmentOptionsView() {
        return environmentOptionsView;
    }

    public View getUserOptionsView() {
        return userOptionsView;
    }

    protected Drawable getEnvironmentDrawable(){
        return environmentImageView.getDrawable();
    }

    protected abstract void lockScreenDismiss(float endVelocity);
}
