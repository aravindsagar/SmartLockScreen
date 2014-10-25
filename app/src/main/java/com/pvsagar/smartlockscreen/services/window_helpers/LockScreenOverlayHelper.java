package com.pvsagar.smartlockscreen.services.window_helpers;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.adapters.NotificationListAdapter;
import com.pvsagar.smartlockscreen.applogic_objects.LockScreenNotification;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.baseclasses.Overlay;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.cards.CardTouchListener;
import com.pvsagar.smartlockscreen.cards.NotificationCardHeader;
import com.pvsagar.smartlockscreen.frontend_helpers.CustomFlingListener;
import com.pvsagar.smartlockscreen.frontend_helpers.ExternalIntents;
import com.pvsagar.smartlockscreen.frontend_helpers.OnFlingGestureListener;
import com.pvsagar.smartlockscreen.receivers.AdminActions;
import com.pvsagar.smartlockscreen.services.BaseService;
import com.pvsagar.smartlockscreen.services.NotificationService;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.prototypes.SwipeDismissListItemViewTouchListener;

/**
 * Created by aravind on 19/9/14.
 * Helper class for managing LockScreenOverlay
 */
public class LockScreenOverlayHelper extends Overlay{
    private static final String LOG_TAG = LockScreenOverlayHelper.class.getSimpleName();
    private NotificationListAdapter notificationListAdapter;
    private LinearLayout notificationCardsLayout;

    public static int clickedCard = -1;
    public static final int MAX_NOTIFICATION_SHOWN = 4;
    public static String KEY_NOTIFICATION_TITLE = "android.title";
    public static String KEY_NOTIFICATION_TEXT = "android.text";
    public static String KEY_NOTIFICATION_TEXTLINES = "android.textLines";
    public static final float CARD_NORMAL_ELEVATION = 8.5f;
    public static final float CARD_TOUCHED_ELEVATION = 0f;
    public static final int CARD_VIEW_NORMAL_ALPHA = 200;
    public static final int CARD_VIEW_SELECTED_ALPHA = 255;

    public LockScreenOverlayHelper(Context context, WindowManager windowManager){
        super(context, windowManager);
    }

    @Override
    protected View getLayout(){
        final RelativeLayout rLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_lock_screen, null);
        final LinearLayout layout = (LinearLayout) rLayout.findViewById(R.id.lockscreen_linear_layout);
        notificationCardsLayout = (LinearLayout) layout.findViewById(R.id.linear_layout_notification_cards);

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
                lockScreenDismiss();
            }
        });
        notificationChanged();
        //ListView notificationsListView = (ListView) rLayout.findViewById(R.id.list_view_notifications);
        //notificationListAdapter = new NotificationListAdapter(context);
        //notificationsListView.setAdapter(notificationListAdapter);

        notificationCardsLayout.setOnTouchListener(new CustomFlingListener(context) {
            @Override
            public void onRightToLeft() {
                ExternalIntents.launchCamera(context);
                lockScreenDismiss();
            }

            @Override
            public void onLeftToRight() {
                ExternalIntents.launchDialer(context);
                lockScreenDismiss();
            }

            @Override
            public void onTopToBottom() {
                lockScreenDismiss();
            }

            @Override
            public void onBottomToTop() {
                lockScreenDismiss();
            }
        });


        return rLayout;
    }

    private void lockScreenDismiss(){
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

    public void notificationChanged(){
        //NotificationListAdapter.currentNotifications.add(lsn);
        /*if(notificationListAdapter != null) {
            notificationListAdapter.notifyDataSetChanged();
        }*/
        Log.d(LOG_TAG,"Entered notification changed");
        if(notificationCardsLayout != null){
            Log.d(LOG_TAG,"Linear Layout not null");
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
                    notificationImageView.setImageBitmap(Utility.getCroppedBitmap(img, 0));
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
                                lockScreenDismiss();
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

                cardView.setOnTouchListener(new OnFlingGestureListener(context) {
                    @Override
                    public void onRightToLeft() {
                        Log.d(LOG_TAG,"Right to left");
                        if(isClearable){
                            cardView.animate().translationX(cardView.getWidth()).alpha(0);
                            lsn.dismiss(context);
                        }
                    }

                    @Override
                    public void onLeftToRight() {
                        Log.d(LOG_TAG,"Left to right");
                        if(isClearable){
                            cardView.animate().translationX(cardView.getWidth()).alpha(0);
                            lsn.dismiss(context);
                        }
                    }

                    @Override
                    public void onBottomToTop() {
                        Log.d(LOG_TAG,"bottom to top");
                        lockScreenDismiss();
                    }

                    @Override
                    public void onTopToBottom() {
                        Log.d(LOG_TAG,"Top to bottom");
                        lockScreenDismiss();
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
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DIM_BEHIND,
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
}
