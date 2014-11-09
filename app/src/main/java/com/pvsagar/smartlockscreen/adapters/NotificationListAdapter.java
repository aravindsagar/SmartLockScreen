package com.pvsagar.smartlockscreen.adapters;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.applogic_objects.LockScreenNotification;
import com.pvsagar.smartlockscreen.backend_helpers.Picture;
import com.pvsagar.smartlockscreen.cards.NotificationCardHeader;
import com.pvsagar.smartlockscreen.services.NotificationService;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardView;

/**
 * Created by PV on 10/7/2014.
 */
public class NotificationListAdapter extends ArrayAdapter<LockScreenNotification> {

    private String LOG_TAG = NotificationListAdapter.class.getSimpleName();
    public static String KEY_NOTIFICATION_TITLE = "android.title";
    public static String KEY_NOTIFICATION_TEXT = "android.text";
    public static String KEY_NOTIFICATION_TEXTLINES = "android.textLines";

    public static int clickedCard = -1;

    public NotificationListAdapter(Context context) {
        super(context, R.layout.list_view_notifications, NotificationService.currentNotifications);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_view_notifications, parent, false);

        /* Populating the notification card */
        final LockScreenNotification lsn =  NotificationService.currentNotifications.get(position);
        final Notification mNotification = lsn.getNotification();
        final Bundle extras = mNotification.extras;

        final CardView cardView = (CardView) rootView.findViewById(R.id.card_view_notification);
        Card card = new Card(getContext());
        CardHeader cardHeader = new NotificationCardHeader(getContext(),new NotificationCardHeader.InnerViewElementsSetUpListener() {
            @Override
            public void onInnerViewElementsSetUp(NotificationCardHeader header) {
                //notification.
                header.setTitle(extras.getString(KEY_NOTIFICATION_TITLE));
                header.setText(extras.getString(KEY_NOTIFICATION_TEXT));

                // Setting image
                try{
                    int icon = mNotification.icon;
                    //Resources res = getContext().getPackageManager().getResourcesForApplication(lsn.getPackageName());
                    Bitmap img = (Bitmap)mNotification.extras.get(Notification.EXTRA_LARGE_ICON);
                    if(img == null) {
                        Drawable app_icon = getContext().getPackageManager().getApplicationIcon(lsn.getPackageName());
                        img = ((BitmapDrawable) app_icon).getBitmap();
                    }
                    header.setImageBitmap(Picture.getCroppedBitmap(img, 0));
                    //Drawable img = res.getDrawable(icon);

                } catch (Exception e){
                    Log.e(LOG_TAG,e.toString());
                }
            }
        });
        card.addCardHeader(cardHeader);
        /*if(extras.containsKey(KEY_NOTIFICATION_TEXTLINES)){
            CardExpand cardExpand = new CardExpand(getContext());
            cardExpand.setTitle(extras.getString(KEY_NOTIFICATION_TEXTLINES));
            card.addCardExpand(cardExpand);
            ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().enableForExpandAction();
            card.setViewToClickToExpand(viewToClickToExpand);
            card.setOnClickListener(new Card.OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    if (card.isExpanded()) {

                    } else {
                        card.doExpand();
                    }
                }
            });
        } else {
            card.setOnClickListener(new Card.OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    try{
                        mNotification.contentIntent.send();
                    } catch (Exception e){
                        Log.e(LOG_TAG,e.toString());
                    }
                }
            });
        }*/
        if(position == clickedCard){
            card.setBackgroundResource(new ColorDrawable(0xFFFFFFFF));
        } else {
            card.setBackgroundResource(new ColorDrawable(0xCCFFFFFF));
        }
        card.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Log.d(LOG_TAG, "Card clicked");
                if(clickedCard == position){
                    try {
                        mNotification.contentIntent.send();
                    } catch (Exception e){
                        Log.e(LOG_TAG,e.toString());
                    }
                } else {
                    clickedCard = position;
                    CountDownTimer cdt = new CountDownTimer((long)2000,(long)2000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }
                        @Override
                        public void onFinish() {
                            clickedCard = -1;
                            notifyDataSetChanged();
                        }
                    }.start();
                    notifyDataSetChanged();
                }
            }
        });

        card.setSwipeable(!lsn.isOngoing());
        if(!lsn.isOngoing()){
            card.setOnSwipeListener(new Card.OnSwipeListener() {
                @Override
                public void onSwipe(Card card) {
                    lsn.dismiss(getContext());
                }
            });
        }
        cardView.setCard(card);
        return rootView;
    }

    public static void deleteItem(int position){
        NotificationService.currentNotifications.remove(position);
    }

    public static void addItem(LockScreenNotification lsn){
        NotificationService.currentNotifications.add(lsn);
    }

}
