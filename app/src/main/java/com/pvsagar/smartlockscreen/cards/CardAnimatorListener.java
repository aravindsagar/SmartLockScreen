package com.pvsagar.smartlockscreen.cards;

import android.animation.Animator;
import android.support.v7.widget.CardView;

import java.util.Vector;

/**
 * Created by aravind on 19/10/14.
 */
public class CardAnimatorListener implements Animator.AnimatorListener{
    private int mPosition;
    private Vector<Float> mElevations;
    private CardView mCardView;

    public CardAnimatorListener(int position, Vector<Float> elevations, CardView cardView){
        mPosition = position;
        mElevations = elevations;
        mCardView = cardView;
    }

    @Override
    public void onAnimationStart(Animator animation) {
        mElevations.set(mPosition, mCardView.getCardElevation());
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        mElevations.set(mPosition, mCardView.getCardElevation());
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        mElevations.set(mPosition, mCardView.getCardElevation());
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        mElevations.set(mPosition, mCardView.getCardElevation());
    }
}
