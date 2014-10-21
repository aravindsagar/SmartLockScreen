package com.pvsagar.smartlockscreen;

import android.animation.Animator;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.pvsagar.smartlockscreen.adapters.EnvironmentListAdapter;


public class ChoosePicture extends Activity {
    private static final String LOG_TAG = ChoosePicture.class.getSimpleName();
    private static final String PACKAGE_NAME = ChoosePicture.class.getPackage().getName();
    public static final String EXTRA_IS_ENVIRONMENT = PACKAGE_NAME + ".IS_ENVIRONMENT";
    public static final String EXTRA_IS_USER = PACKAGE_NAME + ".IS_USER";
    public static final String EXTRA_NAME = PACKAGE_NAME + ".NAME";

    private int initX, initY;

    private ImageView imageView, backgroundImageView;
    private LinearLayout cardLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_picture);
        imageView = (ImageView) findViewById(R.id.image_view_edit_picture);
        cardLinearLayout = (LinearLayout) findViewById(R.id.card_linear_layout);
        backgroundImageView = (ImageView) findViewById(R.id.backgroud_image_view);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            int flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(flags, flags);
        }

        setUpImageView();
    }

    private void setUpImageView(){
        ImageView clickedImageView = EnvironmentListAdapter.getClickedImageView();
        int[] location = new int[2];
        clickedImageView.getLocationInWindow(location);
        initX = location[0];
        initY = location[1];
        Log.d(LOG_TAG, "x = " + initX + ", y = " + initY);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        params.setMargins(initX, initY, 0, 0);
        imageView.setLayoutParams(params);

        imageView.setImageDrawable(clickedImageView.getDrawable());
    }

    @Override
    public void onBackPressed() {
        imageView.animate().translationY(0).setInterpolator(new AccelerateDecelerateInterpolator()).
                setDuration(200).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finish();
                        overridePendingTransition(0, android.R.anim.fade_out);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        finish();
                        overridePendingTransition(0, android.R.anim.fade_out);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
//        backgroundImageView.animate().alpha(0.0f).start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        imageView.animate().translationY(cardLinearLayout.getY() - imageView.getY()).
                setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200).start();
        backgroundImageView.animate().alpha(0.9f).start();
        cardLinearLayout.animate().alpha(1).start();
    }
}