package com.pvsagar.smartlockscreen.frontend_helpers;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Date;

/**
 * Created by PV on 10/25/2014.
 */
public abstract class CustomFlingListener implements View.OnTouchListener {
    private static String LOG_TAG = CustomFlingListener.class.getSimpleName();
    public static int DIRECTION_UP = 0;
    public static int DIRECTION_DOWN = 1;
    public static int DIRECTION_RIGHT = 2;
    public static int DIRECTION_LEFT = 3;

    float upRawX, upRawY;
    float last1MoveX = -1, last1MoveY = -1, last2MoveX = -1, last2MoveY = -1;
    long last1MoveTime = -1, last2MoveTime = -1;
    float downRawX, downRawY;
    long downTime,upTime;
    float velocityX,velocityY,distX,distY;
    static final int MIN_SWIPE_DIST = 60;
    static final int MIN_THRESHOLD_VELOCITY = 250;
    boolean directionKnown;
    int direction;
    Context mContext;

    public CustomFlingListener(Context context){
        mContext = context;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                last1MoveX = event.getRawX();
                last1MoveY = event.getRawY();
                downRawX = event.getRawX();
                downRawY = event.getRawY();
                downTime = new Date().getTime();
                directionKnown = false;
                break;
            case MotionEvent.ACTION_MOVE:
                last2MoveX = last1MoveX;
                last2MoveY = last1MoveY;
                last1MoveX = event.getRawX();
                last1MoveY = event.getRawY();
                last2MoveTime = last1MoveTime;
                last1MoveTime = new Date().getTime();
                if(!directionKnown){
                    if(downRawY - event.getRawY() > MIN_SWIPE_DIST){
                        directionKnown = true;
                        direction = DIRECTION_UP;
                        Log.d(LOG_TAG,"Direction up");
                    } else if(downRawY - event.getRawY() < -MIN_SWIPE_DIST){
                        directionKnown = true;
                        direction = DIRECTION_DOWN;
                        Log.d(LOG_TAG,"Direction down");
                    } else if(downRawX - event.getRawX() > MIN_SWIPE_DIST){
                        directionKnown = true;
                        direction = DIRECTION_LEFT;
                        Log.d(LOG_TAG,"Direction left");
                    } else if(downRawX - event.getRawX() < -MIN_SWIPE_DIST){
                        directionKnown = true;
                        direction = DIRECTION_RIGHT;
                        Log.d(LOG_TAG,"Direction right");
                    }
                } else{
                    onMove(event, direction, downRawX, downRawY);
                }
                break;
            case MotionEvent.ACTION_UP:
                upRawX = event.getRawX();
                upRawY = event.getRawY();
                Log.d(LOG_TAG, "upX: " + upRawX + ", upY: " + upRawY);
                upTime = new Date().getTime();
                distX = convertPxToDip((int)(upRawX-downRawX));
                distY = convertPxToDip((int)(upRawY-downRawY));
                velocityX = distX / ((upTime - downTime)/1000.0f);
                velocityY = distY / ((upTime - downTime)/1000.0f);
                float endVelocityX;
                if(last2MoveX > 0) {
                    endVelocityX = (event.getRawX() - last2MoveX) / (upTime - last2MoveTime);
                } else {
                    endVelocityX = velocityX;
                }
                float endVelocityY;
                if(last2MoveY > 0) {
                    endVelocityY = (event.getRawY() - last2MoveY) / (upTime - last2MoveTime);
                } else {
                    endVelocityY = velocityY;
                }

                if(directionKnown){
                    if(direction == DIRECTION_UP && velocityY < -MIN_THRESHOLD_VELOCITY){
                        Log.d(LOG_TAG,"UP");
                        onBottomToTop(-endVelocityY);
                    } else if(direction == DIRECTION_DOWN && velocityY > MIN_THRESHOLD_VELOCITY){
                        Log.d(LOG_TAG,"Down");
                        onTopToBottom(endVelocityY);
                    } else if(direction == DIRECTION_LEFT && velocityX < -MIN_THRESHOLD_VELOCITY){
                        Log.d(LOG_TAG,"Left");
                        onRightToLeft(-endVelocityX);
                    } else if(direction == DIRECTION_RIGHT && velocityX > MIN_THRESHOLD_VELOCITY){
                        Log.d(LOG_TAG,"Right");
                        onLeftToRight(endVelocityX);
                    } else {
                        Log.d(LOG_TAG,"Swipe fail velX: "+velocityX+"  velY: "+velocityY);
                        onSwipeFail();
                    }
                } else {
                    onDirectionUnknown();
                }
                /*if(Math.abs(distX) > Math.abs(distY)){
                    if(velocityX > MIN_THRESHOLD_VELOCITY && distX > MIN_SWIPE_DIST){
                        onLeftToRight();
                    } else if (velocityX < -MIN_THRESHOLD_VELOCITY && distX < -MIN_SWIPE_DIST){
                        onRightToLeft();
                    }
                } else if(Math.abs(distX) < Math.abs(distY)){
                    if(velocityY > MIN_THRESHOLD_VELOCITY && distY > MIN_SWIPE_DIST){
                        onTopToBottom();
                    } else if (velocityY < -MIN_THRESHOLD_VELOCITY && distY < -MIN_SWIPE_DIST){
                        onBottomToTop();
                    }
                }*/
                break;
        }
        return true;
    }

    public abstract void onRightToLeft(float endVelocity);

    public abstract void onLeftToRight(float endVelocity);

    public abstract void onTopToBottom(float endVelocity);

    public abstract void onBottomToTop(float endVelocity);

    public abstract void onMove(MotionEvent event, int direction, float downRawX, float downRawY);

    public abstract void onSwipeFail();

    public abstract void onDirectionUnknown();

    private int convertPxToDip(int pixel){
        float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) ((pixel / scale) + 0.5f);
    }
}
