package com.pvsagar.smartlockscreen.frontend_helpers;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by aravind on 23/11/14.
 */
public class NotificationCardInterpolator implements Interpolator {

    private float mPercentComplete, mThreshold;
    private AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();

    public NotificationCardInterpolator(int positionFromBottom, int maxCards, float percentageComplete){
        mThreshold = (float) positionFromBottom/(float) maxCards;
        mPercentComplete = percentageComplete;
    }

    @Override
    public float getInterpolation(float input) {
        float adjustedInput = mPercentComplete + input/(1-mPercentComplete);
        if(adjustedInput < mThreshold){
            return accelerateDecelerateInterpolator.getInterpolation(adjustedInput)/2;
        } else {
            return accelerateDecelerateInterpolator.getInterpolation(adjustedInput);
        }
    }
}
