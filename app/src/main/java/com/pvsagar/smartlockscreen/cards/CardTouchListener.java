package com.pvsagar.smartlockscreen.cards;

import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by aravind on 19/10/14.
 * TouchListener for cards which alter its elevation appropriately.
 */
public class CardTouchListener implements View.OnTouchListener{
    public static final float CARD_NORMAL_ELEVATION = 0f;
    public static final float CARD_TOUCHED_ELEVATION = 2f;
    public static final float CARD_SELECTED_ELEVATION = 3.5f;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        CardView cardView = (CardView) v;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                cardView.setMaxCardElevation(CARD_TOUCHED_ELEVATION);
                cardView.setCardElevation(CARD_TOUCHED_ELEVATION);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                cardView.setMaxCardElevation(CARD_NORMAL_ELEVATION);
                cardView.setCardElevation(CARD_NORMAL_ELEVATION);
                break;
        }
        return false;
    }
}
