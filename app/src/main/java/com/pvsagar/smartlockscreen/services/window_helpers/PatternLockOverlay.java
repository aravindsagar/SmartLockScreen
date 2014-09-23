package com.pvsagar.smartlockscreen.services.window_helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Pattern;
import com.pvsagar.smartlockscreen.baseclasses.Overlay;
import com.pvsagar.smartlockscreen.receivers.AdminActions;
import com.pvsagar.smartlockscreen.services.BaseService;
import com.sagar.lockpattern_gridview.PatternGridView;
import com.sagar.lockpattern_gridview.PatternInterface;

import java.util.List;
import java.util.Timer;

/**
 * Created by aravind on 23/9/14.
 */
public class PatternLockOverlay extends Overlay {

    private static final int COLOR_INVALID_PATTERN = Color.rgb(255, 80, 50);
    private static final int COLOR_VALID_PATTERN = Color.rgb(80, 200, 70);

    public PatternLockOverlay(Context context, WindowManager windowManager) {
        super(context, windowManager);
    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT);
        params.dimAmount = 0.5f;
        params.x = 0;
        params.y = 0;
        return params;
    }

    @Override
    protected View getLayout() {
        final String currentPassword = AdminActions.getCurrentPassphraseString();

        RelativeLayout enterPatternLayout = (RelativeLayout) inflater.inflate(R.layout.enter_pattern, null);
        final TextView statusView = (TextView) enterPatternLayout.findViewById(R.id.enter_pattern_status_textview);
        final PatternGridView patternGridView = (PatternGridView) enterPatternLayout.findViewById(R.id.enter_pattern_grid);

        int systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            systemUiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        enterPatternLayout.setSystemUiVisibility(systemUiVisibility);

        patternGridView.setPatternListener(new PatternInterface.PatternListener() {
            Timer mClearTimer;
            @Override
            public void onPatternStarted() {

            }

            @Override
            public void onPatternEntered(List<Integer> pattern) {
                Pattern enteredPattern = new Pattern(pattern);
                if(enteredPattern.compareString(currentPassword)){
                    context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_UNLOCK));
                    patternGridView.setRingColor(COLOR_VALID_PATTERN);
                } else {
                    patternGridView.setRingColor(COLOR_INVALID_PATTERN);
                    statusView.setText("Wrong pattern");
                }
            }

            @Override
            public void onPatternCleared() {
                if(mClearTimer != null){
                    mClearTimer.cancel();
                    mClearTimer = null;
                }
                statusView.setText("");
            }
        });
        return (View) enterPatternLayout;
    }
}