package com.pvsagar.smartlockscreen.services.window_helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.baseclasses.Overlay;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;

/**
 * Created by aravind on 5/11/14.
 */
public class AppLockScreenOverlay extends Overlay {

    private RelativeLayout rLayout;
    private ImageView wallpaperView;
    private TextView messageView;
    private EditText masterPasswordEditText;
    private Button continueButton;

    public AppLockScreenOverlay(Context context, WindowManager windowManager) {
        super(context, windowManager);
    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DIM_BEHIND
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.OPAQUE);

        params.dimAmount = 1;
        params.x = 0;
        params.y = 0;
        return params;
    }

    @Override
    protected View getLayout() {
        if(rLayout == null) {
            rLayout = (RelativeLayout) inflater.inflate(R.layout.app_lockscreen_overlay, null);
            wallpaperView = (ImageView) rLayout.findViewById(R.id.wallpaper_image_view);
            messageView = (TextView) rLayout.findViewById(R.id.text_view_app_lockscreen);
            masterPasswordEditText = (EditText) rLayout.findViewById(R.id.edit_text_enter_master_password);

            Button cancelButton = (Button) rLayout.findViewById(R.id.button_cancel);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent
                            .setAction(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_HOME)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    remove();
                }
            });

            continueButton = (Button) rLayout.findViewById(R.id.button_confirm);
        }

        messageView.setText(context.getString(R.string.app_locked_message));

        Drawable wallpaper = context.getResources().getDrawable(R.drawable.background);
        wallpaper.setColorFilter(Color.argb(100, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
        wallpaperView.setImageDrawable(wallpaper);


        final Passphrase masterPassphrase = Passphrase.getMasterPassword(context);
        masterPasswordEditText.setText("");
        masterPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
        if(masterPassphrase.getPassphraseType().equals(Passphrase.TYPE_PIN)){
            masterPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
        } else if(masterPassphrase.getPassphraseType().equals(Passphrase.TYPE_PASSWORD)){
            masterPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(masterPassphrase.compareString(masterPasswordEditText.getText().toString())) {
                    remove();
                } else {
                    masterPasswordEditText.setText("");
                    messageView.setText("Incorrect password");
                }
            }
        });
        return rLayout;
    }


}
