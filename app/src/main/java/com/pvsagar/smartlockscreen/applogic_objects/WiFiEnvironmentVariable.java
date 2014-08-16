package com.pvsagar.smartlockscreen.applogic_objects;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;

/**
 * Created by aravind on 10/8/14.
 */
public class WiFiEnvironmentVariable extends EnvironmentVariable {
    public WiFiEnvironmentVariable() {
        super(EnvironmentVariable.TYPE_WIFI_NETWORKS);
    }

    @Override
    public boolean isStringValuesSupported() {
        return false;
    }

    @Override
    public boolean isFloatValuesSupported() {
        return false;
    }
}
