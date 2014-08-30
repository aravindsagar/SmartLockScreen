package com.pvsagar.smartlockscreen;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ViewAnimator;

/**
 * Created by aravind on 6/8/14.
 */
public class LockScreenFragment extends Fragment {

    ViewAnimator passphraseAnimator;

    public LockScreenFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lock_screen, container, false);
        passphraseAnimator = (ViewAnimator)rootView.findViewById(R.id.passphraseInput);
        //Adding the different passphrase views
        addViewToPassphraseAnimator(R.layout.password_lock, R.id.passwordLayout,
                inflater, container, 0);

        passphraseAnimator.setDisplayedChild(0);

        Button button = (Button) rootView.findViewById(R.id.UnlockButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("LockScreenActivity","Unlock Button Clicked");
                Intent intent = new Intent(getActivity(),ManageEnvironment.class);
                startActivity(intent);
                //Intent intent = new Intent(getActivity(),SelectLocation.class);
                //startActivity(intent);
            }
        });

        return rootView;
    }

    private void addViewToPassphraseAnimator(int layoutId, int viewId, LayoutInflater inflater,
                                             ViewGroup container, int index){
        View passphraseLayout = inflater.inflate(layoutId, container, false);
        View passphraseView = passphraseLayout.findViewById(viewId);
        passphraseAnimator.addView(passphraseView, index);
    }
}