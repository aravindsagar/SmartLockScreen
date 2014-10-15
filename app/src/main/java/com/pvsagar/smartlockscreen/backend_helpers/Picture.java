package com.pvsagar.smartlockscreen.backend_helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.pvsagar.smartlockscreen.frontend_helpers.CharacterDrawable;

import java.io.ByteArrayOutputStream;

/**
 * Created by aravind on 13/10/14.
 * Class to store pictures for environment and users
 */
public class Picture {
    private static final String PACKAGE_NAME = Picture.class.getPackage().getName();

    /**
     * Picture type color: A circle with a specific color with a letter written at the center will
     * be shown. The color is is stored in pictureDescription. The character to be drawn should be
     * passed in to get the bitmap,else it will use a '?' instead
     */
    public static final String PICTURE_TYPE_COLOR = PACKAGE_NAME + ".PICTURE_TYPE.COLOR";

    /**
     * A built in picture is used. Drawable name should be stored in pictureDescription.
     */
    public static final String PICTURE_TYPE_BUILT_IN = PACKAGE_NAME + ".PICTURE_TYPE.BUILT_IN";

    /**
     * Custom picture, selected by the user from gallery. Image from database will be stored to image
     */
    public static final String PICTURE_TYPE_CUSTOM = PACKAGE_NAME + ".PICTURE_TYPE.CUSTOM";

    private String pictureType;
    private String pictureDescription;
    private byte[] image;

    public Picture(){

    }

    public Picture(String pictureType, String pictureDescription, byte[] image){
        this.pictureDescription = pictureDescription;
        this.pictureType = pictureType;
        this.image = image;
    }

    public String getPictureDescription() {
        return pictureDescription;
    }

    public void setPictureDescription(String pictureDescription) {
        this.pictureDescription = pictureDescription;
    }

    public String getPictureType() {
        return pictureType;
    }

    public void setPictureType(String pictureType) {
        this.pictureType = pictureType;
    }

    public Drawable getDrawable(Character c, Context context){
        if(pictureType.equals(PICTURE_TYPE_COLOR)){
            return new CharacterDrawable(c, Integer.parseInt(pictureDescription));
        } else if(pictureType.equals(PICTURE_TYPE_BUILT_IN)){
            Resources resources = context.getResources();
            final int resourceId = resources.getIdentifier(pictureDescription, "drawable",
                    context.getPackageName());
            return resources.getDrawable(resourceId);
        } else if(pictureType.equals(PICTURE_TYPE_CUSTOM)){
            return byteToDrawable(image, context);
        }
        return null;
    }

    public void setDrawable(BitmapDrawable drawable){
        image = drawableToByteArray(drawable);
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public class PictureTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageView view = (ImageView) v;
                    //overlay is black with transparency of 0x77 (119)
                    view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                    view.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    ImageView view = (ImageView) v;
                    //clear the overlay
                    view.getDrawable().clearColorFilter();
                    view.invalidate();
                    break;
                }
            }

            return false;
        }
    }

    public static byte[] drawableToByteArray(Drawable d) {

        if (d != null) {
            Bitmap imageBitmap = ((BitmapDrawable) d).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

            return baos.toByteArray();
        } else
            return null;

    }


    public static Drawable byteToDrawable(byte[] data, Context context) {

        if (data == null)
            return null;
        else
            return new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(data, 0, data.length));
    }
}