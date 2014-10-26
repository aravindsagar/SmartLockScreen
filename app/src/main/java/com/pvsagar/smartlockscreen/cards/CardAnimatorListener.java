package com.pvsagar.smartlockscreen.cards;

import android.animation.Animator;
import android.support.v7.widget.CardView;

import java.util.Vector;

/**
 * Created by aravind on 19/10/14.
 * Receives events related to animations applied to card, and keeps track of the card elevations.
 * An instance of this class should be set as Animator Listener while animating the card.
 */
public class CardAnimatorListener implements Animator.AnimatorListener{
    /**
     * Position of the card in its list
     */
    private int mPosition;

    /**
     * The vector where elevations are stored
     */
    private Vector<Float> mElevations;

    /**
     * CardView corresponding to the card to be monitored
     */
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
