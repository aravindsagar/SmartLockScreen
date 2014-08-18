package com.pvsagar.smartlockscreen.applogic_objects;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;

/**
 * Created by aravind on 8/8/14.
 */
public class NoiseLevelEnvironmentVariable extends EnvironmentVariable {

    public NoiseLevelEnvironmentVariable(){
        super(TYPE_NOISE_LEVEL);
    }

    @Override
    public boolean isStringValuesSupported() {
        return false;
    }

    @Override
    public boolean isFloatValuesSupported() {
        return true;
    }
}
