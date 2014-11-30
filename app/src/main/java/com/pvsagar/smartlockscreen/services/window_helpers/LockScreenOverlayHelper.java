package com.pvsagar.smartlockscreen.services.window_helpers;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.adapters.UserListAdapter;
import com.pvsagar.smartlockscreen.applogic.EnvironmentDetector;
import com.pvsagar.smartlockscreen.applogic_objects.LockScreenNotification;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.backend_helpers.Picture;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.baseclasses.Overlay;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.frontend_helpers.CharacterDrawable;
import com.pvsagar.smartlockscreen.frontend_helpers.CustomFlingListener;
import com.pvsagar.smartlockscreen.frontend_helpers.ExternalIntents;
import com.pvsagar.smartlockscreen.frontend_helpers.FontCache;
import com.pvsagar.smartlockscreen.frontend_helpers.NotificationAreaHelper;
import com.pvsagar.smartlockscreen.frontend_helpers.WallpaperHelper;
import com.pvsagar.smartlockscreen.receivers.AdminActions;
import com.pvsagar.smartlockscreen.receivers.ScreenReceiver;
import com.pvsagar.smartlockscreen.services.BaseService;
import com.pvsagar.smartlockscreen.services.NotificationService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by aravind on 19/9/14.
 * Helper class for managing LockScreenOverlay
 */
public class LockScreenOverlayHelper extends Overlay{
    private static final String LOG_TAG = LockScreenOverlayHelper.class.getSimpleName();
    private LinearLayout notificationCardsLayout;
    private RelativeLayout rLayout;
    private LinearLayout lLayout;
    private ImageView userImageView, environmentImageView;
    private CardView userGridCardView, environmentOptionsCardView;
    private ImageView backgroundDimmer;
    private LinearLayout timeDateLayout;
    private ImageView phoneIcon, cameraIcon, phoneIconBackground, cameraIconBackground;
    private ArrayList<CardView> notificationCards;
    private CardView moreCard;
    private ImageView wallpaperView;
    private GridView userGridView;
    private TextView dateView;

    public static int clickedCard = -1;
    public static final int MAX_NOTIFICATION_SHOWN = 4;
    public static final String KEY_NOTIFICATION_TITLE = "android.title";
    public static final String KEY_NOTIFICATION_TEXT = "android.text";
    public static final String KEY_NOTIFICATION_TEXTLINES = "android.textLines";
    public static final float CARD_NORMAL_ELEVATION = 8.5f;
    public static final float CARD_TOUCHED_ELEVATION = 0f;
    public static final int CARD_VIEW_NORMAL_ALPHA = 200;
    public static final int CARD_VIEW_SELECTED_ALPHA = 255;
    private static final int DEFAULT_START_ANIMATION_VELOCITY_DOWN = 1;
    private static final int DEFAULT_START_ANIMATION_VELOCITY = 0;
    private static final String EXTRA_NOTIFICATION_SUBSTRING_PREFIX = "+ ";
    private static final String EXTRA_NOTIFICATION_SUBSTRING_SUFFIX = " more";
    private static final float CARDS_MIN_ALPHA = 0.0f;
    private static final float CARDS_MAX_ALPHA = 0.95f;

    private long mDeviceOwnerId;
    private int verticalPadding, horizontalPadding;
    private float shortcutBackgroundMaxScale = 1;
    private float lastDeltaY;

    private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
    private Picture.PictureTouchListener pictureTouchListener = new Picture.PictureTouchListener();

    public LockScreenOverlayHelper(Context context, WindowManager windowManager){
        super(context, windowManager);
        mDeviceOwnerId = SharedPreferencesHelper.getDeviceOwnerUserId(context);
        verticalPadding = convertDipToPx((int) context.getResources().getDimension(R.dimen.activity_vertical_margin));
        horizontalPadding = convertDipToPx((int) context.getResources().getDimension(R.dimen.activity_horizontal_margin));
    }

    @Override
    protected View getLayout(){
        if(rLayout == null) {
            rLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_lock_screen, null);
            lLayout = (LinearLayout) rLayout.findViewById(R.id.lockscreen_linear_layout);
            timeDateLayout = (LinearLayout) lLayout.findViewById(R.id.linear_layout_time_date);
            DigitalClock digitalClock = (DigitalClock) timeDateLayout.findViewById(R.id.digital_clock_lock_screen);
            dateView = (TextView) timeDateLayout.findViewById(R.id.text_view_date);

            notificationCardsLayout = (LinearLayout) lLayout.findViewById(R.id.linear_layout_notification_cards);
            wallpaperView = (ImageView) rLayout.findViewById(R.id.wallpaper_image_view);
            environmentImageView = (ImageView) rLayout.findViewById(R.id.environment_image_view);

            digitalClock.setPadding(digitalClock.getPaddingLeft() + verticalPadding,
                    digitalClock.getPaddingTop() + horizontalPadding,
                    digitalClock.getPaddingRight() + verticalPadding, 0);
            notificationCardsLayout.bringToFront();
            notificationCardsLayout.setAlpha(CARDS_MAX_ALPHA);

            environmentOptionsCardView = (CardView) rLayout.findViewById(R.id.card_view_environment_options);
            Button masterPassphraseButton = (Button) environmentOptionsCardView.findViewById(R.id.button_master_passphrase_unlock);
            masterPassphraseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Passphrase.getMasterPassword(context).setAsCurrentPassword(context);
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            try {
                                EnvironmentDetector.manageEnvironmentDetectionCriticalSection.acquire();
                                Thread.sleep(10 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally {
                                EnvironmentDetector.manageEnvironmentDetectionCriticalSection.release();
                                context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_DETECT_ENVIRONMENT));
                            }
                        }
                    }.start();
                    ScreenReceiver.turnScreenOff(context);
                    ScreenReceiver.turnScreenOn(context);
                }
            });

            userGridCardView = (CardView) rLayout.findViewById(R.id.card_view_user_grid);
            userGridView = (GridView) userGridCardView.findViewById(R.id.grid_view_all_users);

            rLayout.setOnTouchListener(new CustomFlingListener(context) {
                @Override
                public void onRightToLeft(float endVelocity) {
                    ExternalIntents.launchCamera(context);
                    lockScreenDismiss(CustomFlingListener.DIRECTION_LEFT, endVelocity);
                }

                @Override
                public void onLeftToRight(float endVelocity) {
                    ExternalIntents.launchDialer(context);
                    lockScreenDismiss(CustomFlingListener.DIRECTION_RIGHT, endVelocity);
                }

                @Override
                public void onTopToBottom(float endVelocity) {
                    lockScreenDismiss(CustomFlingListener.DIRECTION_DOWN, endVelocity);
                    NotificationAreaHelper.expand(context);
                }

                @Override
                public void onBottomToTop(float endVelocity) {
                    lockScreenDismiss(CustomFlingListener.DIRECTION_UP, endVelocity);
                }

                @Override
                public void onMove(MotionEvent event, int direction, float downRawX, float downRawY) {
                    if(direction == CustomFlingListener.DIRECTION_UP || direction == CustomFlingListener.DIRECTION_DOWN){
                        float deltaY = event.getRawY() - downRawY;
                        setLayoutPropertiesOnVerticalMove(deltaY);
                    } else {
                        float deltaX = event.getRawX() - downRawX;
                        setLayoutPropertiesOnHorizontalMove(deltaX);
                    }
                }

                @Override
                public void onSwipeFail() {
                    resetLayoutPropertiesWithAnimation();
                }
            });

            setSystemUiVisibility();
            setUpShortcuts();
        }

        notificationCardsLayout.removeAllViews();

        Drawable wallpaper = WallpaperHelper.getWallpaperDrawable(context);
        wallpaperView.setImageDrawable(wallpaper);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Intent intent = new Intent(context,NotificationService.class);
            intent.setAction(NotificationService.ACTION_GET_CURRENT_NOTIFICATION_CLEAR_PREVIOUS);
            context.startService(intent);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy");
        String dateString = sdf.format(new Date());
        dateView.setText(dateString);

        setUpAllUsersOverlay();

        setUpEnvironmentOptions();

        //TODO reset properties without animation
        resetLayoutPropertiesWithAnimation();

        return rLayout;
    }

    public void setUpEnvironmentOptions() {
        if(rLayout == null) return;

        String currentEnvironmentName;
        if(BaseService.getCurrentEnvironments() != null && !BaseService.getCurrentEnvironments().isEmpty()) {
            environmentImageView.setImageDrawable(BaseService.getCurrentEnvironments().get(0).getEnvironmentPictureDrawable(context));
            currentEnvironmentName = "Current Environment: " + BaseService.getCurrentEnvironments().get(0).getName();
        } else {
            environmentImageView.setImageDrawable(new CharacterDrawable('?', Color.rgb(150, 150, 150)));
            currentEnvironmentName = context.getString(R.string.unknown_environment);
        }

        TextView currentEnvironmentNameTextView = (TextView) environmentOptionsCardView.findViewById(R.id.text_view_current_environment);
        currentEnvironmentNameTextView.setText(currentEnvironmentName);

        environmentImageView.setOnTouchListener(pictureTouchListener);
        environmentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(environmentOptionsCardView.getVisibility() == View.GONE) {
                    showEnvironmentOptions();
                } else {
                    hideEnvironmentOptions();
                }
            }
        });
        environmentOptionsCardView.setTranslationY(-userGridCardView.getHeight());
        environmentOptionsCardView.setAlpha(0f);
        environmentOptionsCardView.setVisibility(View.GONE);
    }

    private void setUpShortcuts() {
        phoneIcon = (ImageView) rLayout.findViewById(R.id.image_view_phone_icon);
        cameraIcon = (ImageView) rLayout.findViewById(R.id.image_view_camera_icon);
        phoneIconBackground = (ImageView) rLayout.findViewById(R.id.image_view_background_circle_phone);
        cameraIconBackground = (ImageView) rLayout.findViewById(R.id.image_view_background_circle_camera);

        int shortcutBackgroundOriginalSize = convertDipToPx((int) context.getResources().getDimension(R.dimen.user_picture_dimen));
        shortcutBackgroundMaxScale = (float) Math.sqrt(sqr(getDisplayHeight()) + sqr(getDisplayWidth())) * 2.0f/
                shortcutBackgroundOriginalSize;
    }

    private int sqr(int x){
        return x*x;
    }

    private void setSystemUiVisibility() {
        rLayout.setSystemUiVisibility(getFullScreenSystemUiVisibility());
        rLayout.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                setSystemUiVisibility();
            }
        });
    }

    private void setLayoutPropertiesOnVerticalMove(float deltaY){
        lastDeltaY = deltaY;
        float scaleFactor = deltaY/(float)(lLayout.getHeight());
        /*notificationCardsLayout.setTranslationY(deltaY);*/
        int numNotifications = notificationCardsLayout.getChildCount();
        if(numNotifications > 0) {
            float cardHeight = notificationCardsLayout.getChildAt(numNotifications - 1).getBottom();
            for (int i = 0; i < numNotifications; i++) {
                CardView notificationCard = (CardView) notificationCardsLayout.getChildAt(i);
                float cardFactor = (numNotifications - i - 1.0f) / numNotifications;
                float targetDeltaY = Math.abs(deltaY) - cardFactor * cardHeight;

                cardFactor *= 1.5;
                if (cardFactor > Math.abs(scaleFactor)) {
                    targetDeltaY = (Math.abs(deltaY / 2) + (targetDeltaY - Math.abs(deltaY / 2)) * (Math.abs(scaleFactor) / cardFactor));
                }
                if (deltaY < 0) {
                    targetDeltaY -= Math.abs(deltaY);
                    targetDeltaY *= -1;
                }
                notificationCard.setTranslationY(targetDeltaY);

            }
            if (deltaY < 0) {
                notificationCardsLayout.setTranslationY(deltaY);
            }
        }
        notificationCardsLayout.setAlpha((1 - Math.abs(scaleFactor)) * (CARDS_MAX_ALPHA - CARDS_MIN_ALPHA));

        timeDateLayout.setAlpha(1.0f - Math.abs(scaleFactor));
        timeDateLayout.setScaleX(1.0f + scaleFactor);
        timeDateLayout.setScaleY(1.0f + scaleFactor);

        userImageView.setScaleX(1.0f - Math.abs(scaleFactor));
        userImageView.setScaleY(1.0f - Math.abs(scaleFactor));

        environmentImageView.setScaleX(1.0f - Math.abs(scaleFactor));
        environmentImageView.setScaleY(1.0f - Math.abs(scaleFactor));

        phoneIconBackground.setScaleX(1.0f - Math.abs(scaleFactor));
        phoneIconBackground.setScaleY(1.0f - Math.abs(scaleFactor));

        phoneIcon.setScaleX(1.0f - Math.abs(scaleFactor));
        phoneIcon.setScaleY(1.0f - Math.abs(scaleFactor));

        cameraIconBackground.setScaleX(1.0f - Math.abs(scaleFactor));
        cameraIconBackground.setScaleY(1.0f - Math.abs(scaleFactor));

        cameraIcon.setScaleX(1.0f - Math.abs(scaleFactor));
        cameraIcon.setScaleY(1.0f - Math.abs(scaleFactor));
    }

    private void setLayoutPropertiesOnHorizontalMove(float deltaX){
        float scaleFactor = 1.0f - Math.abs(deltaX)/(float) lLayout.getWidth();
        lLayout.setScaleX(scaleFactor);
        lLayout.setScaleY(scaleFactor);
        userImageView.setAlpha(scaleFactor);
        environmentImageView.setAlpha(scaleFactor);
        if(deltaX > 0){
            phoneIconBackground.setScaleX(deltaX/phoneIconBackground.getWidth()/2 + 1);
            phoneIconBackground.setScaleY(deltaX/phoneIconBackground.getWidth()/2 + 1);
            cameraIcon.setAlpha(scaleFactor);
            cameraIconBackground.setAlpha(scaleFactor);
        } else {
            cameraIconBackground.setScaleX(-deltaX/cameraIconBackground.getWidth()/2 + 1);
            cameraIconBackground.setScaleY(-deltaX/cameraIconBackground.getWidth()/2 + 1);
            phoneIcon.setAlpha(scaleFactor);
            phoneIconBackground.setAlpha(scaleFactor);
        }
    }

    private void resetLayoutPropertiesWithAnimation(){
        int numNotifications = notificationCardsLayout.getChildCount();
        for(int i=0; i<numNotifications; i++){
            notificationCardsLayout.getChildAt(i).animate().translationY(0).start();
        }
        lLayout.animate().translationY(0).scaleX(1).scaleY(1).
                setInterpolator(accelerateDecelerateInterpolator).start();
        notificationCardsLayout.animate().translationY(0).alpha(CARDS_MAX_ALPHA).
                setInterpolator(accelerateDecelerateInterpolator).start();
        userImageView.animate().alpha(1).scaleX(1).scaleY(1).
                setInterpolator(accelerateDecelerateInterpolator).start();
        environmentImageView.animate().alpha(1).scaleX(1).scaleY(1).
                setInterpolator(accelerateDecelerateInterpolator).start();
        timeDateLayout.animate().scaleY(1).scaleX(1).alpha(1).
                setInterpolator(accelerateDecelerateInterpolator).start();
        phoneIconBackground.animate().scaleY(1).scaleX(1).alpha(1).
                setInterpolator(accelerateDecelerateInterpolator).start();
        cameraIconBackground.animate().scaleY(1).scaleX(1).alpha(1).
                setInterpolator(accelerateDecelerateInterpolator).start();
        phoneIcon.animate().scaleX(1).scaleY(1).alpha(1).start();
        cameraIcon.animate().scaleX(1).scaleY(1).alpha(1).start();
        backgroundDimmer.animate().alpha(0).start();
        backgroundDimmer.setVisibility(View.GONE);
    }

    private void setUpAllUsersOverlay(){
        final List<User> allUsers = User.getAllUsers(context);
        int mDeviceOwnerIndex = 0;
        for (int i = 0; i < allUsers.size(); i++) {
            User user = allUsers.get(i);
            if (user.getId() == mDeviceOwnerId) {
                mDeviceOwnerIndex = i;
                break;
            }
        }

        userGridView.setAdapter(new UserListAdapter(context, R.layout.grid_item_user,
                R.layout.grid_item_settings, allUsers, mDeviceOwnerIndex, new UserListAdapter.OnUsersModifiedListener() {
            @Override
            public void onUsersModified() {}

            @Override
            public void onSettingsClicked() {
                lockScreenDismiss(DEFAULT_START_ANIMATION_VELOCITY);
            }
        }));
        userGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User.setCurrentUser(allUsers.get(position));
                context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_DETECT_ENVIRONMENT_SWITCH_USER));
            }
        });

        backgroundDimmer = (ImageView) rLayout.findViewById(R.id.image_view_background_dimmer);

        userImageView = (ImageView) rLayout.findViewById(R.id.user_image_view);
        userImageView.setImageDrawable(User.getCurrentUser(context).getUserPictureDrawable(context));
        userImageView.setOnTouchListener(pictureTouchListener);
        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userGridCardView.getVisibility() == View.GONE) {
                    showUserGrid();
                } else {
                    hideUserGrid();
                }
            }
        });

        backgroundDimmer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideUserGrid();
                hideEnvironmentOptions();
                return true;
            }
        });
        userGridCardView.setTranslationY(-userGridCardView.getHeight());
        userGridCardView.setAlpha(0f);
        userGridCardView.setVisibility(View.GONE);
    }

    private void showUserGrid(){
        showBackgroundDimmer();
        userGridCardView.setVisibility(View.VISIBLE);
        userGridCardView.animate().alpha(1f).translationY(0).setListener(null).
                setInterpolator(decelerateInterpolator).start();
        rLayout.invalidate();
    }

    private void hideUserGrid(){
        hideBackgroundDimmer();
        userGridCardView.animate().translationY(-userGridCardView.getHeight()).alpha(0).
                setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        userGridCardView.setVisibility(View.GONE);
                        userGridCardView.animate().setListener(null).start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        onAnimationEnd(animation);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                }).start();
    }

    private void showEnvironmentOptions(){
        environmentOptionsCardView.setVisibility(View.VISIBLE);
        environmentOptionsCardView.animate().alpha(1f).translationY(0).setListener(null).
                setInterpolator(decelerateInterpolator).start();
        showBackgroundDimmer();
    }

    private void hideEnvironmentOptions(){
        hideBackgroundDimmer();
        environmentOptionsCardView.animate().translationY(-environmentOptionsCardView.getHeight()).alpha(0).
                setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        environmentOptionsCardView.setVisibility(View.GONE);
                        environmentOptionsCardView.animate().setListener(null).start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        onAnimationEnd(animation);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                }).start();
    }

    private void showBackgroundDimmer(){
        backgroundDimmer.setAlpha(0f);
        backgroundDimmer.setVisibility(View.VISIBLE);
        backgroundDimmer.animate().alpha(1f).setListener(null).start();
    }

    private void hideBackgroundDimmer(){

        backgroundDimmer.animate().alpha(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                backgroundDimmer.setVisibility(View.GONE);
                backgroundDimmer.animate().setListener(null).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        }).start();
    }

    private void lockScreenDismiss(float endVelocity){
        lockScreenDismiss(CustomFlingListener.DIRECTION_UP, endVelocity);
    }

    private void lockScreenDismiss(int direction, float endVelocity){
        if(endVelocity > 0) {
            if (direction == CustomFlingListener.DIRECTION_UP) {
                notificationCardsLayout.animate().translationY(-lLayout.getHeight()).setInterpolator(new DecelerateInterpolator(endVelocity / 2))
                        .alpha(CARDS_MIN_ALPHA).setListener(new AnimateEndListener(notificationCardsLayout)).start();
                timeDateLayout.animate().scaleY(0).scaleX(0).setInterpolator(new DecelerateInterpolator(endVelocity / 2)).start();
                dismissCornerIcons();
            } else if (direction == CustomFlingListener.DIRECTION_DOWN) {
                notificationCardsLayout.animate().translationY(lLayout.getHeight()).setInterpolator(new DecelerateInterpolator(endVelocity / 2))
                        .alpha(CARDS_MIN_ALPHA).setListener(new AnimateEndListener(notificationCardsLayout)).start();
                timeDateLayout.animate().scaleY(2).scaleX(2).alpha(0).setInterpolator(new DecelerateInterpolator(endVelocity / 2)).start();
                dismissCornerIcons();
            } else if (direction == CustomFlingListener.DIRECTION_LEFT) {
                lLayout.animate().scaleX(0).scaleY(0).setListener(new AnimateEndListener(lLayout)).
                        setInterpolator(new DecelerateInterpolator(endVelocity / 2)).start();
                cameraIconBackground.animate().scaleX(shortcutBackgroundMaxScale).scaleY(shortcutBackgroundMaxScale).
                        setInterpolator(new DecelerateInterpolator(0.25f)).start();
                fadeOutUserAndEnvironmentPicture();
                fadeOutPhoneIcon();
            } else {
                lLayout.animate().scaleX(0).scaleY(0).setListener(new AnimateEndListener(lLayout)).
                        setInterpolator(new DecelerateInterpolator(endVelocity / 2)).start();
                phoneIconBackground.animate().scaleX(shortcutBackgroundMaxScale).scaleY(shortcutBackgroundMaxScale).
                        setInterpolator(new DecelerateInterpolator(0.25f)).start();
                fadeOutUserAndEnvironmentPicture();
                fadeOutCameraIcon();
            }
        } else {
            notificationCardsLayout.animate().translationY(-lLayout.getHeight()).
                    setListener(new AnimateEndListener(notificationCardsLayout)).
                    alpha(CARDS_MIN_ALPHA).setInterpolator(accelerateDecelerateInterpolator).start();
            timeDateLayout.animate().scaleY(0).scaleX(0).setInterpolator(accelerateDecelerateInterpolator).start();
            userImageView.animate().alpha(0).setInterpolator(accelerateDecelerateInterpolator).start();
        }
    }

    private void fadeOutUserAndEnvironmentPicture() {
        userImageView.animate().alpha(0).start();
        environmentImageView.animate().alpha(0).start();
    }

    private void fadeOutCameraIcon(){
        cameraIconBackground.animate().alpha(0).start();
        cameraIcon.animate().alpha(0).start();
    }

    private void fadeOutPhoneIcon(){
        phoneIconBackground.animate().alpha(0).start();
        phoneIcon.animate().alpha(0).start();
    }

    private void dismissCornerIcons(){
        phoneIconBackground.animate().scaleX(0).scaleY(0).setInterpolator(accelerateInterpolator).start();
        cameraIconBackground.animate().scaleX(0).scaleY(0).setInterpolator(accelerateInterpolator).start();
        cameraIcon.animate().scaleX(0).scaleY(0).setInterpolator(accelerateInterpolator).start();
        phoneIcon.animate().scaleX(0).scaleY(0).setInterpolator(accelerateInterpolator).start();
        userImageView.animate().scaleX(0).scaleY(0).setInterpolator(accelerateInterpolator).start();
        environmentImageView.animate().scaleX(0).scaleY(0).setInterpolator(accelerateInterpolator).start();
    }

    private class AnimateEndListener implements Animator.AnimatorListener{
        View mView;
        public AnimateEndListener(View v){
            mView = v;
        }
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
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
            mView.animate().setListener(null).start();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            onAnimationEnd(animation);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    public void initNotification(){
//        Log.d(LOG_TAG,"Entered notification changed");
        if(notificationCardsLayout != null){
//            Log.d(LOG_TAG, "Linear Layout not null");
            if((notificationCardsLayout).getChildCount() > 0){
                (notificationCardsLayout).removeAllViews();
            }

            //Update the notifications
            NotificationService.currentNotificationsAccessSemaphore.acquireUninterruptibly();
            for(int i = 0; i < NotificationService.currentNotifications.size(); i++){
                if(i >= MAX_NOTIFICATION_SHOWN){
                    NotificationService.currentNotifications.get(i).setShown(false);
                    continue;
                }
                setNotificationCard(NotificationService.currentNotifications.get(i));
            }

            if(NotificationService.currentNotifications.size() > MAX_NOTIFICATION_SHOWN){
                setMoreCard();
            }
            NotificationService.currentNotificationsAccessSemaphore.release();
        }
    }

    public void notificationPosted(){
        boolean additionDone = false;
//            Log.d(LOG_TAG,"No of notification shown: "+noOfNotificationShown());
            //Show the new notification
        NotificationService.currentNotificationsAccessSemaphore.acquireUninterruptibly();
        for(int i=0; i< NotificationService.currentNotifications.size(); i++){
            if(NotificationService.currentNotifications.get(i).isShown()){
                continue;
            } else if(NotificationService.currentNotifications.get(i).getCardView() != null) {
                setNotificationCard(NotificationService.currentNotifications.get(i));
                additionDone = true;
                break;
            }
        }
        if(!additionDone){
            for(int i=0; i< NotificationService.currentNotifications.size(); i++){
                if(NotificationService.currentNotifications.get(i).isShown()){
                    continue;
                } else if(noOfNotificationShown() < MAX_NOTIFICATION_SHOWN) {
                    setNotificationCard(NotificationService.currentNotifications.get(i));
                    break;
                }
            }
        }
            // Just change the last card
        setMoreCard();
        NotificationService.currentNotificationsAccessSemaphore.release();
    }

    public void notificationRemoved(){
        if(notificationCardsLayout == null){
            return;
        }
        for(int i = NotificationService.removedNotifications.size() - 1; i >= 0; i--){
            LockScreenNotification lsn = NotificationService.removedNotifications.get(i);
            notificationCardsLayout.removeView(lsn.getCardView());
            NotificationService.removedNotifications.remove(i);
        }

        NotificationService.currentNotificationsAccessSemaphore.acquireUninterruptibly();
        while(noOfNotificationShown() <
                ((MAX_NOTIFICATION_SHOWN < NotificationService.currentNotifications.size())?
                        MAX_NOTIFICATION_SHOWN : NotificationService.currentNotifications.size())){
            //Add a notification to the lLayout
            for(int i=0; i< NotificationService.currentNotifications.size(); i++){
                if(NotificationService.currentNotifications.get(i).isShown()){
                    continue;
                } else {
                    // Show this notification
//                    Log.d(LOG_TAG, "Notification not shown");
                    setNotificationCard(NotificationService.currentNotifications.get(i));
                    break;
                }
            }
        }
        setMoreCard();
        NotificationService.currentNotificationsAccessSemaphore.release();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setNotificationCard(LockScreenNotification lockScreenNotification){
        if(notificationCardsLayout == null){
            return;
        }
        final LockScreenNotification lsn = lockScreenNotification;
        final CardView cardView;
        boolean shouldAdd;
        if(lsn.getCardView() != null){
            cardView = lsn.getCardView();
            shouldAdd = false;
        } else {
            cardView = (CardView) inflater.inflate(R.layout.list_item_notification, notificationCardsLayout, false);
            shouldAdd = true;
        }
        TextView titleTextView = (TextView) cardView.findViewById(R.id.notification_card_title);
        Typeface typeface = FontCache.get("fonts/roboto-medium.ttf", context);
        titleTextView.setTypeface(typeface);
        TextView subTextTextView = (TextView) cardView.findViewById(R.id.notification_card_subtext);
        ImageView notificationImageView = (ImageView) cardView.findViewById(R.id.image_view_notification);
                /* Populating the notification card */
        final boolean isOngoing = lsn.isOngoing();
        final Notification mNotification = lsn.getNotification();
        final Bundle extras = mNotification.extras;
        titleTextView.setText(extras.getString(KEY_NOTIFICATION_TITLE));
        CharSequence charSequence = (CharSequence) extras.getCharSequence(KEY_NOTIFICATION_TEXT);
        if(charSequence != null){
            subTextTextView.setText(charSequence.toString());
        }
        try {
            int icon = mNotification.icon;
            //Resources res = getContext().getPackageManager().getResourcesForApplication(lsn.getPackageName());
            Bitmap img = (Bitmap) mNotification.extras.get(Notification.EXTRA_LARGE_ICON);
            if (img == null) {
                Drawable app_icon = context.getPackageManager().getApplicationIcon(lsn.getPackageName());
                img = ((BitmapDrawable) app_icon).getBitmap();
            }
            notificationImageView.setImageBitmap(Picture.getCroppedBitmap(img, Color.DKGRAY));
        } catch (Exception e){
            Log.e(LOG_TAG, e.toString());
        }

        cardView.setMaxCardElevation(CARD_NORMAL_ELEVATION);
        cardView.setCardElevation(CARD_NORMAL_ELEVATION);
        cardView.getBackground().setAlpha(CARD_VIEW_NORMAL_ALPHA);

        //cardView.setOnTouchListener(new CardTouchListener());
        cardView.setOnClickListener(new View.OnClickListener() {
            boolean isClicked = false;
            @Override
            public void onClick(View v) {
                if(isClicked){
                    try {
                        mNotification.contentIntent.send();
                        lockScreenDismiss(DEFAULT_START_ANIMATION_VELOCITY);
                    } catch (Exception e){
                        Log.e(LOG_TAG,e.toString());
                    }
                    cardView.setMaxCardElevation(CARD_NORMAL_ELEVATION);
                    cardView.setCardElevation(CARD_NORMAL_ELEVATION);
                    cardView.getBackground().setAlpha(CARD_VIEW_NORMAL_ALPHA);
                } else{
                    isClicked = true;
                    cardView.setMaxCardElevation(CARD_TOUCHED_ELEVATION);
                    cardView.setCardElevation(CARD_TOUCHED_ELEVATION);
                    cardView.getBackground().setAlpha(CARD_VIEW_SELECTED_ALPHA);
                    CountDownTimer cdt = new CountDownTimer((long)2000,(long)2000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }
                        @Override
                        public void onFinish() {
                            isClicked = false;
                            cardView.setMaxCardElevation(CARD_NORMAL_ELEVATION);
                            cardView.setCardElevation(CARD_NORMAL_ELEVATION);
                            cardView.getBackground().setAlpha(CARD_VIEW_NORMAL_ALPHA);
                            //notificationChanged();
                        }
                    }.start();
                }
            }
        });

        cardView.setOnTouchListener(new CustomFlingListener(context) {
                @Override
                public void onRightToLeft(float endVelocity) {
    //                Log.d(LOG_TAG,"Swipe right to left");
                    if(!isOngoing){
                        cardView.animate().translationX(-cardView.getWidth()).setInterpolator(new DecelerateInterpolator(endVelocity/2))
                                .alpha(0f);
                        lsn.dismiss(context);
                        notificationCardsLayout.removeView(cardView);
                    } else {
                        cardView.animate().translationX(0).setInterpolator(new DecelerateInterpolator(endVelocity/2)).
                                alpha(1f);
                    }
                }

                @Override
                public void onLeftToRight(float endVelocity) {
    //                Log.d(LOG_TAG,"Swipe left to right");
                    if(!isOngoing){
                        cardView.animate().translationX(cardView.getWidth()).setInterpolator(new DecelerateInterpolator(endVelocity / 2)).
                                alpha(0f);
                        lsn.dismiss(context);
                        notificationCardsLayout.removeView(cardView);
                    } else {
                        cardView.animate().translationX(0).setInterpolator(new DecelerateInterpolator(endVelocity / 2)).
                                alpha(1f);
                    }
                }

                @Override
                public void onTopToBottom(float endVelocity) {
                    lockScreenDismiss(CustomFlingListener.DIRECTION_DOWN, endVelocity);
                    NotificationAreaHelper.expand(context);
                }

                @Override
                public void onBottomToTop(float endVelocity) {
                    lockScreenDismiss(CustomFlingListener.DIRECTION_UP, endVelocity);
                }

                @Override
                public void onMove(MotionEvent event, int direction, float downRawX, float downRawY) {
                    if(direction == CustomFlingListener.DIRECTION_LEFT || direction == CustomFlingListener.DIRECTION_RIGHT){
                        // Horizontal motion
    //                    Log.d(LOG_TAG,"Down x: "+downRawX+"  eventx: "+event.getRawX());
                        cardView.setTranslationX(event.getRawX() - downRawX);
                    } else if(direction == CustomFlingListener.DIRECTION_UP || direction == CustomFlingListener.DIRECTION_DOWN){
                        float deltaY = event.getRawY() - downRawY;
                        setLayoutPropertiesOnVerticalMove(deltaY);
                    }
                }

                @Override
                public void onSwipeFail() {
                    cardView.animate().translationX(0).start();
                    resetLayoutPropertiesWithAnimation();
                }
        });
        lsn.setCardView(cardView);
        lsn.setShown(true);
        if(shouldAdd) {
            notificationCardsLayout.addView(cardView);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setMoreCard(){
        if(notificationCardsLayout == null){
            return;
        }
        if(moreCard != null){
            try {
                notificationCardsLayout.removeView(moreCard);
                moreCard = null;
            } catch (Exception e){
                Log.e(LOG_TAG,e.toString());
            }
        }
        if(NotificationService.currentNotifications.size() <= MAX_NOTIFICATION_SHOWN){
            return;
        }
        moreCard = null;
        final CardView cardView;
        moreCard = (CardView) inflater.inflate(R.layout.card_extra_notifications, notificationCardsLayout, false);
        cardView = moreCard;
        LinearLayout moreCardLayout = (LinearLayout) cardView.findViewById(R.id.linear_layout_extra_notification);
        TextView cardExtraTextView = (TextView) moreCardLayout.findViewById(R.id.text_view_extra_notifications);
        String title = EXTRA_NOTIFICATION_SUBSTRING_PREFIX +
                (NotificationService.currentNotifications.size() - MAX_NOTIFICATION_SHOWN)
                + EXTRA_NOTIFICATION_SUBSTRING_SUFFIX;
        cardExtraTextView.setText(title);
        try {
            for (LockScreenNotification lsn : NotificationService.currentNotifications) {
                if (lsn.isShown()) continue;
                Notification mNotification = lsn.getNotification();

                Bitmap img = (Bitmap) mNotification.extras.get(Notification.EXTRA_LARGE_ICON);
                if (img == null) {
                    Drawable app_icon = context.getPackageManager().getApplicationIcon(lsn.getPackageName());
                    img = ((BitmapDrawable) app_icon).getBitmap();
                }

                ImageView notificationImageView = new ImageView(context);
                int picSize = context.getResources().getDimensionPixelSize(R.dimen.nav_bar_icon_dimen), picMargin = context.getResources().getDimensionPixelSize(R.dimen.nav_bar_picture_padding);
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(picSize, picSize);
                params1.setMargins(picMargin, picMargin, picMargin, picMargin);
                params1.gravity = Gravity.CENTER_VERTICAL;
                notificationImageView.setImageBitmap(Picture.getCroppedBitmap(img, Color.DKGRAY));
                moreCardLayout.addView(notificationImageView, params1);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockScreenDismiss(CustomFlingListener.DIRECTION_DOWN, DEFAULT_START_ANIMATION_VELOCITY_DOWN);
                NotificationAreaHelper.expand(context);
            }
        });
        cardView.setOnTouchListener(new CustomFlingListener(context) {
            @Override
            public void onRightToLeft(float endVelocity) {
                ExternalIntents.launchCamera(context);
                lockScreenDismiss(CustomFlingListener.DIRECTION_LEFT, endVelocity);
            }

            @Override
            public void onLeftToRight(float endVelocity) {
                ExternalIntents.launchDialer(context);
                lockScreenDismiss(CustomFlingListener.DIRECTION_RIGHT, endVelocity);
            }

            @Override
            public void onTopToBottom(float endVelocity) {
                lockScreenDismiss(CustomFlingListener.DIRECTION_DOWN, endVelocity);
                NotificationAreaHelper.expand(context);
            }

            @Override
            public void onBottomToTop(float endVelocity) {
                lockScreenDismiss(CustomFlingListener.DIRECTION_UP, endVelocity);
            }

            @Override
            public void onMove(MotionEvent event, int direction, float downRawX, float downRawY) {
                if (direction == CustomFlingListener.DIRECTION_UP || direction == CustomFlingListener.DIRECTION_DOWN) {
                    float deltaY = event.getRawY() - downRawY;
                    setLayoutPropertiesOnVerticalMove(deltaY);
                } else {
                    float deltaX = event.getRawX() - downRawX;
                    setLayoutPropertiesOnHorizontalMove(deltaX);
                }
            }

            @Override
            public void onSwipeFail() {
                resetLayoutPropertiesWithAnimation();
            }
        });
        cardView.getBackground().setAlpha(150);
        cardView.setMaxCardElevation(CARD_NORMAL_ELEVATION);
        cardView.setCardElevation(CARD_NORMAL_ELEVATION);
        notificationCardsLayout.addView(cardView);

    }

    public static int noOfNotificationShown(){
        int count = 0;
        for(int i=0; i<NotificationService.currentNotifications.size(); i++){
            if(NotificationService.currentNotifications.get(i).isShown()){
                count++;
            }
        }
        return count;
    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams(){
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                PixelFormat.OPAQUE);

        /*if(AdminActions.getCurrentPassphraseType() != null && AdminActions.getCurrentPassphraseType().equals(Passphrase.TYPE_NONE)) {
            params.dimAmount = 1;
        } else {
            params.dimAmount = 0;
        }*/
        params.x = 0;
        params.y = 0;
        params.systemUiVisibility = getFullScreenSystemUiVisibility();
        return params;
    }

    @Override
    public void remove() {
//        NotificationAreaHelper.expand(context);
        super.remove();
    }

    private int convertDipToPx(int pixel){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((pixel / scale) + 0.5f);
    }
}
