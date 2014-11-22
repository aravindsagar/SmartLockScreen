package com.pvsagar.smartlockscreen.cards;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;

import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by PV on 10/7/2014.
 */
public class NotificationCardHeader extends CardHeader {

    InnerViewElementsSetUpListener<NotificationCardHeader> listener;
    ImageView notificationImageView;
    TextView titleTextView;
    TextView textTextView;

    public NotificationCardHeader(Context context, InnerViewElementsSetUpListener<NotificationCardHeader> listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        titleTextView = (TextView) parent.findViewById(R.id.card_header_title);
        textTextView = (TextView) parent.findViewById(R.id.card_header_text);
        notificationImageView = (ImageView) parent.findViewById(R.id.image_view_notification);
        if(listener != null){
            listener.onInnerViewElementsSetUp(this);
        }
    }

    @Override
    public String getTitle() {
        if(titleTextView != null) {
            return titleTextView.getText().toString();
        }
        return null;
    }

    @Override
    public void setTitle(String title) {
        if(titleTextView == null || title == null){
            return;
        }
        titleTextView.setText(title);
    }

    public String getText() {
        if(textTextView != null) {
            return textTextView.getText().toString();
        }
        return null;
    }

    public void setText(String text) {
        if(textTextView == null || text == null){
            return;
        }
        textTextView.setText(text);
    }

    public void setImageDrawable(Drawable drawable){
        if(notificationImageView == null || drawable == null){
            return;
        }
        notificationImageView.setImageDrawable(drawable);
    }

    public void setImageBitmap(Bitmap bitmap){
        if(notificationImageView == null || bitmap == null){
            return;
        }
        notificationImageView.setImageBitmap(bitmap);
    }
}
