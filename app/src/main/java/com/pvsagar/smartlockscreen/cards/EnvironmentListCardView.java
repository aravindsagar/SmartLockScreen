package com.pvsagar.smartlockscreen.cards;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

/**
 * Created by aravind on 19/10/14.
 */
public class EnvironmentListCardView extends CardView {

    public EnvironmentListCardView(Context context) {
        super(context);
    }

    public EnvironmentListCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EnvironmentListCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public float getElevation(){
        return super.getCardElevation();
    }

    public void setElevation(float newElevation){
        super.setCardElevation(newElevation);
    }

    public float getMaxElevation(){
        return super.getMaxCardElevation();
    }

    public void setMaxElevation(float newElevation){
        super.setMaxCardElevation(newElevation);
    }
}
