package com.pvsagar.smartlockscreen.cards;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by aravind on 19/10/14.
 * TouchListener for cards which alter its elevation appropriately.
 */
public class CardTouchListener implements View.OnTouchListener{

    int touchColor;

    public CardTouchListener(int touchColor){
        this.touchColor = touchColor;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        CardView cardView = (CardView) v;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                for(int i=0; i<cardView.getChildCount(); i++){
                    cardView.getChildAt(i).setBackgroundColor(touchColor);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                for(int i=0; i<cardView.getChildCount(); i++){
                    cardView.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                }
                break;
        }
        return false;
    }
}
