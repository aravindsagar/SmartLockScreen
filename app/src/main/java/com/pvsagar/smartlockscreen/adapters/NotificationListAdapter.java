package com.pvsagar.smartlockscreen.adapters;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.applogic_objects.LockScreenNotification;
import com.pvsagar.smartlockscreen.cards.NotificationCardHeader;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardView;

/**
 * Created by PV on 10/7/2014.
 */
public class NotificationListAdapter extends ArrayAdapter<LockScreenNotification> {

    private String LOG_TAG = NotificationListAdapter.class.getSimpleName();
    public static String KEY_NOTIFICATION_TITLE = "android.title";

    public static ArrayList<LockScreenNotification> currentNotifications =
            new ArrayList<LockScreenNotification>();

    public NotificationListAdapter(Context context) {
        super(context, R.layout.list_view_notifications,currentNotifications);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_view_notifications, parent, false);

        /* Populating the notification card */
        final LockScreenNotification lsn =  currentNotifications.get(position);
        final Notification mNotification = lsn.getNotification();
        final Bundle extras = mNotification.extras;

        CardView cardView = (CardView) rootView.findViewById(R.id.card_view_notification);
        Card card = new Card(getContext());
        CardHeader cardHeader = new NotificationCardHeader(getContext(),new NotificationCardHeader.InnerViewElementsSetUpListener() {
            @Override
            public void onInnerViewElementsSetUp(NotificationCardHeader header) {
                //notification.
                header.setTitle(extras.getString(KEY_NOTIFICATION_TITLE));
                // Setting image
                try{
                    int icon = mNotification.icon;
                    //Resources res = getContext().getPackageManager().getResourcesForApplication(lsn.getPackageName());
                    Bitmap img = (Bitmap)mNotification.extras.get(Notification.EXTRA_LARGE_ICON);
                    if(img == null){
                        Drawable app_icon = getContext().getPackageManager().getApplicationIcon(lsn.getPackageName());
                        header.setImageDrawable(app_icon);
                    } else{
                        header.setImageBitmap(img);
                    }
                    //Drawable img = res.getDrawable(icon);

                } catch (Exception e){
                    Log.e(LOG_TAG,e.toString());
                }
            }
        });
        card.addCardHeader(cardHeader);
        cardView.setCard(card);
        return rootView;
    }

}
