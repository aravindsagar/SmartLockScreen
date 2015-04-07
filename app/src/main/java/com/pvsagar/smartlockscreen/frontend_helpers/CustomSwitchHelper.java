package com.pvsagar.smartlockscreen.frontend_helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.pvsagar.smartlockscreen.R;

/**
 * Created by aravind on 12/11/14.
 */
public class CustomSwitchHelper {
    public static Drawable getSwitchOnDrawable(Context context){
        return new ColorDrawable(context.getResources().getColor(R.color.switch_color));
    }

    public static Drawable getSwitchOffDrawable(){
        return new ColorDrawable(Color.LTGRAY);
    }

    public static abstract class CustomSwitchCheckedChangeListener implements CompoundButton.OnCheckedChangeListener{
        Drawable switchOn, switchOff;

        public CustomSwitchCheckedChangeListener(Context context){
            switchOn = getSwitchOnDrawable(context);
            switchOff = getSwitchOffDrawable();
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Switch mSwitch = (Switch) buttonView;

            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                if (isChecked) {
                    mSwitch.setThumbDrawable(switchOn);
                } else {
                    mSwitch.setThumbDrawable(switchOff);
                }
            }

            onCustomCheckedChanged(buttonView, isChecked);
        }

        public abstract void onCustomCheckedChanged(CompoundButton buttonView, boolean isChecked);
    }
}
