package com.pvsagar.smartlockscreen.applogic_objects.environment_variables;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.EnvironmentEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 8/8/14.
 * Not yet in use. Unsure whether to use.
 */
public class NoiseLevelEnvironmentVariable extends EnvironmentVariable {
    private static final String LOG_TAG = NoiseLevelEnvironmentVariable.class.getSimpleName();

    public boolean hasLowerLimit = true, hasUpperLimit = true;

    private static final int NUMBER_OF_FLOAT_VALUES = 2;
    private static final int INDEX_LOWER_LIMIT = 0;
    private static final int INDEX_UPPER_LIMIT = 1;

    public static final double MAX_NOISE_LEVEL = (double) 100.0;
    public static final double MIN_NOISE_LEVEL = (double) 0.0;

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

    @Override
    public ContentValues getContentValues() {
        return null;
    }

    public double getUpperLimit(){
        try {
            return getFloatValue(INDEX_UPPER_LIMIT);
        } catch (Exception e){
            Log.e(LOG_TAG, "Internal application error, please file a bug report to developer."
                    + e.getMessage());
            return 0.0; //Should think of a better way, though it should never come to this.
        }
    }

    public double getLowerLimit(){
        try {
            return getFloatValue(INDEX_LOWER_LIMIT);
        } catch (Exception e){
            Log.e(LOG_TAG, "Internal application error, please file a bug report to developer."
                    + e.getMessage());
            return (double) 0.0; //Should think of a better way, though it should never come to this.
        }
    }

    public void setUpperLimit(double upperLimit){
        setFloatValue(upperLimit, INDEX_UPPER_LIMIT);
    }

    public void setLowerLimit(double lowerLimit){
        setFloatValue(lowerLimit, INDEX_LOWER_LIMIT);
    }

    public static List<EnvironmentVariable> getNoiseLevelEnvironmentVariablesFromCursor
            (Cursor environmentCursor){
        int initialCursorPosition = environmentCursor.getPosition();
        ArrayList<EnvironmentVariable> environmentVariables =
                new ArrayList<EnvironmentVariable>();
        try {
            if (environmentCursor.moveToFirst()) {
                for (; !environmentCursor.isAfterLast(); environmentCursor.moveToNext()) {
                    boolean minNoiseEnabled = environmentCursor.getInt(environmentCursor.
                            getColumnIndex(EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED)) == 1,
                            maxNoiseEnabled = environmentCursor.getInt(environmentCursor.
                                    getColumnIndex(EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED))
                                    == 1;
                    if(minNoiseEnabled || maxNoiseEnabled){
                        environmentVariables.add(new NoiseLevelEnvironmentVariable
                                (minNoiseEnabled, maxNoiseEnabled));
                    }
                }
            }
        } catch (Exception e){
            Log.w(LOG_TAG, e + ": " + e.getMessage());
        }
        environmentCursor.moveToFirst();
        environmentCursor.move(initialCursorPosition);
        return environmentVariables;
    }

}
