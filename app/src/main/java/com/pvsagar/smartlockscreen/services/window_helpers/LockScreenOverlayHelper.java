package com.pvsagar.smartlockscreen.services.window_helpers;

import android.animation.Animator;
import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.adapters.NotificationListAdapter;
import com.pvsagar.smartlockscreen.applogic_objects.LockScreenNotification;
import com.pvsagar.smartlockscreen.backend_helpers.Picture;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.baseclasses.Overlay;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.frontend_helpers.CustomFlingListener;
import com.pvsagar.smartlockscreen.frontend_helpers.ExternalIntents;
import com.pvsagar.smartlockscreen.frontend_helpers.NotificationAreaHelper;
import com.pvsagar.smartlockscreen.receivers.AdminActions;
import com.pvsagar.smartlockscreen.services.BaseService;
import com.pvsagar.smartlockscreen.services.NotificationService;

/**
 * Created by aravind on 19/9/14.
 * Helper class for managing LockScreenOverlay
 */
public class LockScreenOverlayHelper extends Overlay{
    private static final String LOG_TAG = LockScreenOverlayHelper.class.getSimpleName();
    private NotificationListAdapter notificationListAdapter;
    private LinearLayout notificationCardsLayout;
    private LinearLayout layout;

    public static int clickedCard = -1;
    public static final int MAX_NOTIFICATION_SHOWN = 4;
    public static String KEY_NOTIFICATION_TITLE = "android.title";
    public static String KEY_NOTIFICATION_TEXT = "android.text";
    public static String KEY_NOTIFICATION_TEXTLINES = "android.textLines";
    public static final float CARD_NORMAL_ELEVATION = 8.5f;
    public static final float CARD_TOUCHED_ELEVATION = 0f;
    public static final int CARD_VIEW_NORMAL_ALPHA = 200;
    public static final int CARD_VIEW_SELECTED_ALPHA = 255;
    private static final int DEFAULT_START_ANIMATION_VELOCITY = 0;

    public LockScreenOverlayHelper(Context context, WindowManager windowManager){
        super(context, windowManager);
    }

    @Override
    protected View getLayout(){
        final RelativeLayout rLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_lock_screen, null);
        layout = (LinearLayout) rLayout.findViewById(R.id.lockscreen_linear_layout);
        notificationCardsLayout = (LinearLayout) layout.findViewById(R.id.linear_layout_notification_cards);

        layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        /*layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v.hasOnClickListeners()){
                    v.callOnClick();
                }
                return true;
            }
        });*/
//      WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        //TODO add wallpaper support!
        ImageView wallpaperView = (ImageView) rLayout.findViewById(R.id.wallpaper_image_view);
        Drawable wallpaper = context.getResources().getDrawable(R.drawable.background);
        wallpaper.setColorFilter(Color.argb(100, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
        wallpaperView.setImageDrawable(wallpaper);

        Button unlockButton = (Button) layout.findViewById(R.id.unlock_button);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockScreenDismiss(DEFAULT_START_ANIMATION_VELOCITY);
            }
        });
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            notificationChanged();
        }
        //ListView notificationsListView = (ListView) rLayout.findViewById(R.id.list_view_notifications);
        //notificationListAdapter = new NotificationListAdapter(context);
        //notificationsListView.setAdapter(notificationListAdapter);

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
                    layout.setTranslationY(deltaY);
                } else {
                    float deltaX = event.getRawX() - downRawX;
                    layout.setTranslationX(deltaX);
                }
            }

            @Override
            public void onSwipeFail() {
                layout.animate().translationY(0).start();
                layout.animate().translationX(0).start();
            }

            @Override
            public void onDirectionUnknown() {

            }
        });


        return rLayout;
    }

    private void lockScreenDismiss(float endVelocity){
        lockScreenDismiss(CustomFlingListener.DIRECTION_UP, endVelocity);
    }

    private void lockScreenDismiss(int direction, float endVelocity){
        if(endVelocity > 0) {
            if (direction == CustomFlingListener.DIRECTION_UP) {
                layout.animate().translationY(-layout.getHeight()).setInterpolator(new DecelerateInterpolator(endVelocity / 2))
                        .setListener(new AnimateEndListener()).start();
            } else if (direction == CustomFlingListener.DIRECTION_DOWN) {
                layout.animate().translationY(layout.getHeight()).setInterpolator(new DecelerateInterpolator(endVelocity / 2))
                        .setListener(new AnimateEndListener()).start();
            } else if (direction == CustomFlingListener.DIRECTION_LEFT) {
                layout.animate().translationX(-layout.getWidth()).setInterpolator(new DecelerateInterpolator(endVelocity / 2))
                        .setListener(new AnimateEndListener()).start();
            } else {
                layout.animate().translationX(+layout.getWidth()).setInterpolator(new DecelerateInterpolator(endVelocity / 2))
                        .setListener(new AnimateEndListener()).start();
            }
        } else {
            Log.d(LOG_TAG,"Accelerate ip");
            layout.animate().translationY(-layout.getHeight()).setListener(new AnimateEndListener())
                    .setInterpolator(new AccelerateInterpolator()).start();
        }

    }

    private class AnimateEndListener implements Animator.AnimatorListener{
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
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            onAnimationEnd(animation);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    public void notificationChanged(){
        //NotificationListAdapter.currentNotifications.add(lsn);
        /*if(notificationListAdapter != null) {
            notificationListAdapter.notifyDataSetChanged();
        }*/
        Log.d(LOG_TAG,"Entered notification changed");
        if(notificationCardsLayout != null){
            Log.d(LOG_TAG, "Linear Layout not null");
            if(((LinearLayout)notificationCardsLayout).getChildCount() > 0){
                ((LinearLayout)notificationCardsLayout).removeAllViews();
            }
            //Update the notifications
            for(int i = 0; i < NotificationService.currentNotifications.size()
                    && i < MAX_NOTIFICATION_SHOWN; i++){
                final int position = i;
                final CardView cardView = (CardView) inflater.inflate(R.layout.list_item_notification, notificationCardsLayout, false);
                TextView titleTextView = (TextView) cardView.findViewById(R.id.notification_card_title);
                TextView subTextTextView = (TextView) cardView.findViewById(R.id.notification_card_subtext);
                ImageView notificationImageView = (ImageView) cardView.findViewById(R.id.image_view_notification);
                /* Populating the notification card */
                final LockScreenNotification lsn =  NotificationService.currentNotifications.get(i);
                final boolean isClearable = lsn.isClearable();
                final Notification mNotification = lsn.getNotification();
                final Bundle extras = mNotification.extras;
                titleTextView.setText(extras.getString(KEY_NOTIFICATION_TITLE));
                subTextTextView.setText(extras.getString(KEY_NOTIFICATION_TEXT));
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
                    @Override
                    public void onClick(View v) {
                        Log.d(LOG_TAG, "Card clicked: "+position);
                        if(clickedCard == position){
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
                            clickedCard = position;
                            cardView.setMaxCardElevation(CARD_TOUCHED_ELEVATION);
                            cardView.setCardElevation(CARD_TOUCHED_ELEVATION);
                            cardView.getBackground().setAlpha(CARD_VIEW_SELECTED_ALPHA);
                            CountDownTimer cdt = new CountDownTimer((long)2000,(long)2000) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }
                                @Override
                                public void onFinish() {
                                    clickedCard = -1;
                                    cardView.setMaxCardElevation(CARD_NORMAL_ELEVATION);
                                    cardView.setCardElevation(CARD_NORMAL_ELEVATION);
                                    cardView.getBackground().setAlpha(CARD_VIEW_NORMAL_ALPHA);
                                    //notificationChanged();
                                }
                            }.start();
                        }
                    }
                });
                /*cardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Log.d(LOG_TAG,"Long clicked card:" + position);
                        return true;
                    }
                });*/

                cardView.setOnTouchListener(new CustomFlingListener(context) {
                    @Override
                    public void onRightToLeft(float endVelocity) {
                        Log.d(LOG_TAG,"Swipe right to left");
                        if(isClearable){
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
                        Log.d(LOG_TAG,"Swipe left to right");
                        if(isClearable){
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
                    }

                    @Override
                    public void onBottomToTop(float endVelocity) {
                        lockScreenDismiss(CustomFlingListener.DIRECTION_UP, endVelocity);
                    }

                    @Override
                    public void onMove(MotionEvent event, int direction, float downRawX, float downRawY) {
                        if(direction == CustomFlingListener.DIRECTION_LEFT || direction == CustomFlingListener.DIRECTION_RIGHT){
                            // Horizontal motion
                            Log.d(LOG_TAG,"Down x: "+downRawX+"  eventx: "+event.getRawX());
                            cardView.setTranslationX(event.getRawX() - downRawX);
                        } else if(direction == CustomFlingListener.DIRECTION_UP || direction == CustomFlingListener.DIRECTION_DOWN){
                            float deltaY = event.getRawY() - downRawY;
                            layout.setTranslationY(deltaY);
                        }
                    }

                    @Override
                    public void onDirectionUnknown() {
                        cardView.callOnClick();
                    }

                    @Override
                    public void onSwipeFail() {
                        cardView.animate().translationX(0).start();
                        layout.animate().translationY(0).start();
                    }
                });
                notificationCardsLayout.addView(cardView);
            }
        }
    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams(){
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DIM_BEHIND
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.OPAQUE);

        if(AdminActions.getCurrentPassphraseType().equals(Passphrase.TYPE_NONE)) {
            params.dimAmount = 1;
        } else {
            params.dimAmount = 0;
        }
        params.x = 0;
        params.y = 0;
        return params;
    }

    @Override
    public void remove() {
//        NotificationAreaHelper.expand(context);
        super.remove();
    }

    private int convertPxToDip(int pixel){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((pixel / scale) + 0.5f);
    }
}
