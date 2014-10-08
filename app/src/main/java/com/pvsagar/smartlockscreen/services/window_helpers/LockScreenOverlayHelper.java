package com.pvsagar.smartlockscreen.services.window_helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.baseclasses.Overlay;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.receivers.AdminActions;
import com.pvsagar.smartlockscreen.services.BaseService;

/**
 * Created by aravind on 19/9/14.
 */
public class LockScreenOverlayHelper extends Overlay{
    private static final String LOG_TAG = LockScreenOverlayHelper.class.getSimpleName();

    public LockScreenOverlayHelper(Context context, WindowManager windowManager){
        super(context, windowManager);
    }

    @Override
    protected View getLayout(){
        final RelativeLayout rLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_lock_screen, null);
        final LinearLayout layout = (LinearLayout) rLayout.findViewById(R.id.lockscreen_linear_layout);

        layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v.hasOnClickListeners()){
                    v.callOnClick();
                }
                return true;
            }
        });
//        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        //TODO add wallpaper support!
        ImageView wallpaperView = (ImageView) rLayout.findViewById(R.id.wallpaper_image_view);
        Drawable wallpaper = context.getResources().getDrawable(R.drawable.background);
        wallpaper.setColorFilter(Color.argb(100, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
        wallpaperView.setImageDrawable(wallpaper);

        Button unlockButton = (Button) layout.findViewById(R.id.unlock_button);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassphraseType = AdminActions.getCurrentPassphraseType();
                if (!Utility.checkForNullAndWarn(currentPassphraseType, LOG_TAG)){
                    if (currentPassphraseType.equals(Passphrase.TYPE_PATTERN) &&
                            AdminActions.getCurrentPassphraseString() != null) {
                        context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_START_PATTERN_OVERLAY));
                    } else {
                        remove();
                    }
                } else {
                    remove();
                }
            }
        });
        return rLayout;
    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams(){
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                PixelFormat.OPAQUE);
        params.x = 0;
        params.y = 0;
        return params;
    }

    @Override
    public void remove() {
//        NotificationAreaHelper.expand(context);
        super.remove();
    }
}
