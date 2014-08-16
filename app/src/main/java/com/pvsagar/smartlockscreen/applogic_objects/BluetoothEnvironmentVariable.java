package com.pvsagar.smartlockscreen.applogic_objects;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;

/**
 * Created by aravind on 10/8/14.
 */
public class BluetoothEnvironmentVariable extends EnvironmentVariable {

    public BluetoothEnvironmentVariable() {
        super(EnvironmentVariable.TYPE_BLUETOOTH_DEVICES);
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
