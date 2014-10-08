package com.pvsagar.smartlockscreen;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.WindowManager;

import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.fragments.SetMasterPasswordFragment;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class SetMasterPassword extends ActionBarActivity implements SetMasterPasswordFragment.MasterPasswordSetListener {
    private static final String LOG_TAG = SetMasterPassword.class.getSimpleName();

    private SetMasterPasswordFragment mSetMasterPasswordFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_master_password);

        mSetMasterPasswordFragment = new SetMasterPasswordFragment();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mSetMasterPasswordFragment)
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if(!Utility.checkForNullAndWarn(actionBar, LOG_TAG)) {
            actionBar.setBackgroundDrawable(new ColorDrawable(
                    getResources().getColor(R.color.action_bar_setup)));
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.action_bar_setup));
        }
    }

    @Override
    public void onBackPressed() {
        mSetMasterPasswordFragment.doCancelButtonPress();
    }

    @Override
    public void onMasterPasswordSet() {
        finish();
    }

    @Override
    public void onCancelSetMasterPassword() {
        finish();
    }
}
