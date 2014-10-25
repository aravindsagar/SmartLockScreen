package com.pvsagar.smartlockscreen.cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;

import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by aravind on 3/10/14.
 * Card header with a title and checkbox
 * For the 3rd party card library in use.
 */
public class EnvironmentCardHeader extends CardHeader {
    TextView titleView;
    InnerViewElementsSetUpListener listener;
    public EnvironmentCardHeader(Context context, InnerViewElementsSetUpListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        titleView = (TextView) parent.findViewById(R.id.card_header_title);
        if(listener != null){
            listener.onInnerViewElementsSetUp(this);
        }
    }

    @Override
    public String getTitle() {
        if(titleView != null) {
            return titleView.getText().toString();
        }
        return null;
    }

    @Override
    public void setTitle(String title) {
        if(titleView == null || title == null){
            return;
        }
        titleView.setText(title);
    }

    public interface InnerViewElementsSetUpListener {
        public void onInnerViewElementsSetUp(EnvironmentCardHeader header);
    }
}
