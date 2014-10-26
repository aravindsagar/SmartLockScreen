package com.pvsagar.smartlockscreen.frontend_helpers;

import android.content.Context;
import android.database.DatabaseUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.games.GamesMetadata;

import java.util.Date;

/**
 * Created by PV on 10/25/2014.
 */
public abstract class CustomFlingListener implements View.OnTouchListener {
    public static String LOG_TAG = CustomFlingListener.class.getSimpleName();
    public static int DIRECTION_UP = 0;
    public static int DIRECTION_DOWN = 1;
    public static int DIRECTION_RIGHT = 2;
    public static int DIRECTION_LEFT = 3;

    float downX, downY, upX, upY;
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
                downX = event.getX();
                downY = event.getY();
                downTime = new Date().getTime();
                directionKnown = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if(!directionKnown){
                    if(downY - event.getY() > MIN_SWIPE_DIST){
                        directionKnown = true;
                        direction = DIRECTION_UP;
                        Log.d(LOG_TAG,"Direction up");
                    } else if(downY - event.getY() < -MIN_SWIPE_DIST){
                        directionKnown = true;
                        direction = DIRECTION_DOWN;
                        Log.d(LOG_TAG,"Direction down");
                    } else if(downX - event.getX() > MIN_SWIPE_DIST){
                        directionKnown = true;
                        direction = DIRECTION_LEFT;
                        Log.d(LOG_TAG,"Direction left");
                    } else if(downX - event.getX() < -MIN_SWIPE_DIST){
                        directionKnown = true;
                        direction = DIRECTION_RIGHT;
                        Log.d(LOG_TAG,"Direction right");
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                upX = event.getX();
                upY = event.getY();
                upTime = new Date().getTime();
                distX = convertPxToDip((int)(upX-downX));
                distY = convertPxToDip((int)(upY-downY));
                velocityX = distX / ((upTime - downTime)/1000.0f);
                velocityY = distY / ((upTime - downTime)/1000.0f);
                Log.d(LOG_TAG,"downTime: "+downTime+"\t upTime: "+upTime);
                Log.d(LOG_TAG,"velocityX: "+velocityX+"\t velocityY: "+velocityY);
                if(directionKnown){
                    if(direction == DIRECTION_UP && velocityY < -MIN_THRESHOLD_VELOCITY){
                        onBottomToTop();
                    } else if(direction == DIRECTION_DOWN && velocityY > MIN_THRESHOLD_VELOCITY){
                        onTopToBottom();
                    } else if(direction == DIRECTION_LEFT && velocityX < -MIN_THRESHOLD_VELOCITY){
                        onRightToLeft();
                    } else if(direction == DIRECTION_RIGHT && velocityX > MIN_THRESHOLD_VELOCITY){
                        onLeftToRight();
                    }
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

    public abstract void onRightToLeft();

    public abstract void onLeftToRight();

    public abstract void onTopToBottom();

    public abstract void onBottomToTop();

    private int convertPxToDip(int pixel){
        float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) ((pixel / scale) + 0.5f);
    }
}
