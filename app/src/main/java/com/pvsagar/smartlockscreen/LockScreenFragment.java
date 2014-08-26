package com.pvsagar.smartlockscreen;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewAnimator;

import com.pvsagar.smartlockscreen.services.GeoFenceIntentService;

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
                inflater, container, 0, R.id.unlockButton);

        passphraseAnimator.setDisplayedChild(0);


        return rootView;
    }

    private void addViewToPassphraseAnimator(int layoutId, int viewId, LayoutInflater inflater,
                                             ViewGroup container, int index, int buttonId){
        View passphraseLayout = inflater.inflate(layoutId, container, false);
        View passphraseView = passphraseLayout.findViewById(viewId);
        View unlockButton = passphraseLayout.findViewById(buttonId);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), GeoFenceIntentService.class);
                getActivity().startService(intent);
            }
        });
        passphraseAnimator.addView(passphraseView, index);
    }
}