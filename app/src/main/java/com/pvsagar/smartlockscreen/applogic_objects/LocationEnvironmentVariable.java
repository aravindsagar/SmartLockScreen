package com.pvsagar.smartlockscreen.applogic_objects;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;

/**
 * Created by aravind on 8/8/14.
 */
public class LocationEnvironmentVariable extends EnvironmentVariable {

    public LocationEnvironmentVariable(){
        super(TYPE_LOCATION);
    }

    @Override
    public boolean isStringValuesSupported() {
        return true;
    }

    @Override
    public boolean isFloatValuesSupported() {
        return true;
    }
}
