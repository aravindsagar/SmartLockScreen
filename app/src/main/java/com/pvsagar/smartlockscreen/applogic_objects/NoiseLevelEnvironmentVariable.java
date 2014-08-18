package com.pvsagar.smartlockscreen.applogic_objects;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;

/**
 * Created by aravind on 8/8/14.
 */
public class NoiseLevelEnvironmentVariable extends EnvironmentVariable {

    private boolean hasLowerLimit = true, hasUpperLimit = true;

    private static final int NUMBER_OF_FLOAT_VALUES = 2;
    private static final int INDEX_LOWER_LIMIT = 0;
    private static final int INDEX_UPPER_LIMIT = 1;

    public NoiseLevelEnvironmentVariable(boolean hasLowerLimit, boolean hasUpperLimit){
        super(TYPE_NOISE_LEVEL, NUMBER_OF_FLOAT_VALUES, 0);
        this.hasLowerLimit = hasLowerLimit;
        this.hasUpperLimit = hasUpperLimit;
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
