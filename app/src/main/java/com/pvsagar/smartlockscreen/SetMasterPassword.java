package com.pvsagar.smartlockscreen;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.pvsagar.smartlockscreen.fragments.SetMasterPasswordFragment;

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
