package com.pvsagar.smartlockscreen;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.pvsagar.smartlockscreen.fragments.ManageEnvironmentFragment;

public class ManageEnvironment extends ActionBarActivity {

    private static final String LOG_TAG = ManageEnvironment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_environment);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ManageEnvironmentFragment())
                    .commit();
        }

    }
}
