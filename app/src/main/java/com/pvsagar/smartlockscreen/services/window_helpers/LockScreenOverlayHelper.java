package com.pvsagar.smartlockscreen.services.window_helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pvsagar.smartlockscreen.LockScreenActivity;
import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.receivers.AdminActions;
import com.pvsagar.smartlockscreen.services.BaseService;

/**
 * Created by aravind on 19/9/14.
 */
public class LockScreenOverlayHelper {
    private static final String LOG_TAG = LockScreenOverlayHelper.class.getSimpleName();
    private static final int DISMISS_KEYGAURD_REQUEST_CODE = 1001;
    private static final int REQUEST_ENTER_PATTERN = 33;

    private LayoutInflater inflater;
    private WindowManager windowManager;
    private Context context;
    private View layout;
    private WindowManager.LayoutParams params;

    public LockScreenOverlayHelper(Context context, WindowManager windowManager){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.windowManager = windowManager;
    }

    private LinearLayout getLayout(){
        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_lock_screen, null);
        int systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            systemUiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        layout.setSystemUiVisibility(systemUiVisibility);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v.hasOnClickListeners()){
                    v.callOnClick();
                }
                return true;
            }
        });
        Button unlockButton = (Button) layout.findViewById(R.id.unlock_button);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassphraseType = AdminActions.getCurrentPassphraseType(),
                        currentPassphraseString = AdminActions.getCurrentPassphraseString();
                if (!Utility.checkForNullAndWarn(currentPassphraseType, LOG_TAG)){
                    if (currentPassphraseType.equals(Passphrase.TYPE_PATTERN) &&
                            AdminActions.getCurrentPassphraseString() != null) {
                        Intent patternIntent = new Intent(context, LockScreenActivity.class);
                        patternIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.getApplicationContext().startActivity(patternIntent);

                    } else if(currentPassphraseType.equals(Passphrase.TYPE_NONE)) {
                        //windowManager.removeView(layout);
                        context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_DISMISS_KEYGUARD));
                    } else {
                        windowManager.removeView(layout);
                    }
                }

            }
        });
        return layout;
    }

    private WindowManager.LayoutParams getLayoutParams(){
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.OPAQUE);
        params.x = 0;
        params.y = 0;
        return params;
    }

    public void execute(){
        if(layout == null){
            layout = getLayout();
        }
        if(params == null){
            params = getLayoutParams();
        }
        try {
            windowManager.addView(layout, params);
        } catch (IllegalStateException e){
            //Dont do anything
        }
    }

    public void remove(){
        if(layout == null){
            layout = getLayout();
        }
        if(params == null){
            params = getLayoutParams();
        }
        try {
            windowManager.removeView(layout);
        } catch (Exception e){
            //Do nothing
        }
    }
}
