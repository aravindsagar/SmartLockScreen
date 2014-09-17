package com.pvsagar.smartlockscreen;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.haibison.android.lockpattern.LockPatternActivity;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.receivers.AdminActions;

/**
 * Created by aravind on 6/8/14.
 */
public class LockScreenFragment extends Fragment {
    private static final int REQUEST_ENTER_PATTERN = 33;

    public LockScreenFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_lock_screen, container, false);
        Button unlockButton = (Button) rootView.findViewById(R.id.unlock_button);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AdminActions.getCurrentPassphraseType() != null &&
                        AdminActions.getCurrentPassphraseType().equals(Passphrase.TYPE_PATTERN)){
                    Intent patternIntent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                            getActivity(), LockPatternActivity.class);
                    patternIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    patternIntent.putExtra(LockPatternActivity.EXTRA_THEME, R.style.TransparentThemeNoActionBar);
                    if(AdminActions.getCurrentPassphraseString() != null) {
                        patternIntent.putExtra(LockPatternActivity.EXTRA_PATTERN,
                                AdminActions.getCurrentPassphraseString().toCharArray());
                        startActivityForResult(patternIntent, REQUEST_ENTER_PATTERN);
//                        getActivity().overridePendingTransition(0, 0);
                    } else {
                        getActivity().finish();
                    }
                } else {
                    getActivity().finish();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENTER_PATTERN: {
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        AdminActions.changePassword("", Passphrase.TYPE_NONE);
                        Intent intent = new Intent(getActivity(), DismissKeyguardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        getActivity().startActivity(intent);
                        getActivity().overridePendingTransition(0, 0);
                        getActivity().finish();
                        getActivity().overridePendingTransition(0, 0);
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user cancelled the task
                        break;
                    case LockPatternActivity.RESULT_FAILED:
                        // The user failed to enter the pattern
                        break;
                    case LockPatternActivity.RESULT_FORGOT_PATTERN:
                        // The user forgot the pattern and invoked your recovery Activity.
                        break;
                }
                int retryCount = data.getIntExtra(
                        LockPatternActivity.EXTRA_RETRY_COUNT, 0);

                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}