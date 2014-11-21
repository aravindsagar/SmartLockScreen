package com.pvsagar.smartlockscreen;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.User;

/**
 * Activity which provides the ui for user to pick a picture for user/environment
 */
public class ChoosePicture extends Activity {
    private static final String LOG_TAG = ChoosePicture.class.getSimpleName();
    private static final String PACKAGE_NAME = ChoosePicture.class.getPackage().getName();
    public static final String EXTRA_OBJECT_TYPE = PACKAGE_NAME + ".OBJECT_TYPE";
    public static final String EXTRA_OBJECT_ID = PACKAGE_NAME + ".OBJECT_ID";
    public static final String EXTRA_IMAGE_VIEW_START_LOCATION = PACKAGE_NAME + ".START_LOCATION";

    private int initX, initY;

    private ImageView imageView, backgroundImageView;
    private LinearLayout cardLinearLayout;

    public enum ObjectType {USER, ENVIRONMENT}

    ObjectType pictureObjectType;
    private long objectId;

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

        pictureObjectType = (ObjectType) getIntent().getSerializableExtra(EXTRA_OBJECT_TYPE);

        setUpImageView();
    }

    private void setUpImageView(){

        int[] location = getIntent().getIntArrayExtra(EXTRA_IMAGE_VIEW_START_LOCATION);
//        clickedImageView.getLocationInWindow(location);
        if(location != null && location.length >= 2) {
            initX = location[0];
            initY = location[1];
        } else {
            initX = initY = 0;
        }
        Log.d(LOG_TAG, "x = " + initX + ", y = " + initY);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        params.setMargins(initX, initY, 0, 0);
        imageView.setLayoutParams(params);

        objectId = getIntent().getLongExtra(EXTRA_OBJECT_ID, -1);
        Drawable imageDrawable;
        switch (pictureObjectType){
            case ENVIRONMENT:
                imageDrawable = Environment.getBareboneEnvironment(this, objectId).getEnvironmentPictureDrawable(this);
                break;
            case USER:
                imageDrawable = User.getUserWithId(this, objectId).getUserPictureDrawable(this);
                break;
            default:
                imageDrawable = null;
        }
        imageView.setImageDrawable(imageDrawable);
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