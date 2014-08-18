package com.pvsagar.smartlockscreen.baseclasses;

/**
 * Created by aravind on 5/8/14.
 *
 * Abstract class for Environment Variables. Each variable will extend it and use it accordingly
 */
public abstract class EnvironmentVariable {

    /**
     * Constant values for use
     */
    public static final String TYPE_LOCATION = "com.pvsagar.applogic_objects.TYPE_LOCATION";
    public static final String TYPE_BLUETOOTH_DEVICES =
            "com.pvsagar.applogic_objects.TYPE_BLUETOOTH_DEVICES";
    public static final String TYPE_WIFI_NETWORKS =
            "com.pvsagar.applogic_objects.TYPE_WIFI_NETWORKS";
    public static final String TYPE_NOISE_LEVEL = "com.pvsagar.applogic_objects.TYPE_NOISE_LEVEL";

    //Stores the numeric values associated with the variable
    private float[] floatValues;

    //Stores the String values associated with the variable
    private String[] stringValues;

    //Stores the variable type. This will be one of the TYPE_* strings defined above
    private String variableType;

    //To keep track of whether the variable has been initialized with a valid type
    private boolean isInitialized = false;

    /**
     * Constructor for EnvironmentVariable
     * @param variableType Type of environment variable. Should be one of the TYPE_* strings
     *                     defined above
     * @param numberOfFloatValues
     * @param numberOfStringValues
     */
    public EnvironmentVariable(String variableType,
                               int numberOfFloatValues, int numberOfStringValues){
        boolean isValid = checkTypeValidity(variableType) && !isInitialized;
        if(isValid){
            this.variableType = variableType;
            setFloatValues(new float[numberOfFloatValues]);
            setStringValues(stringValues = new String[numberOfStringValues]);
            isInitialized = true;
        }
    }

    public EnvironmentVariable(String variableType, float[] floatValues, String[] stringValues){
        boolean isValid = checkTypeValidity(variableType) && !isInitialized;
        if(isValid){
            this.variableType = variableType;
            setFloatValues(floatValues);
            setStringValues(stringValues);
            isInitialized = true;
        }
    }

    /**
     * Set the floatValues array.
     * @param floatValues The values to be set
     * @return true if values have been set, false if it cannot be set.
     */
    protected boolean setFloatValues(float[] floatValues){
        if(isFloatValuesSupported() && isInitialized) {
            this.floatValues = floatValues;
            return true;
        }
        return false;
    }

    /**
     * Set a single float value at the specified index, in the floatValues array
     * @param floatValue
     * @param index
     * @return true if value has been set, false if it cannot be set.
     */
    protected boolean setFloatValue(float floatValue, int index){
        if(isFloatValuesSupported() && isInitialized) {
            this.floatValues[index] = floatValue;
            return true;
        }
        return false;
    }

    /**
     * Set the stringValues array
     * @param stringValues The values to be set
     * @return true if values have been set, false if it cannot be set.
     */
    protected boolean setStringValues(String[] stringValues){
        if(isStringValuesSupported() && isInitialized) {
            this.stringValues = stringValues;
            return true;
        }
        return false;
    }

    /**
     * Set a single string value at the specified index, in the stringValues array
     * @param stringValue
     * @param index
     * @return true if value has been set, false if it cannot be set.
     */
    protected boolean setStringValue(String stringValue, int index){
        if(isStringValuesSupported() && isInitialized) {
            this.stringValues[index] = stringValue;
            return true;
        }
        return false;
    }

    public String getVariableType(){
        if(isInitialized){
            return variableType;
        }
        else return null;
    }

    private boolean checkTypeValidity(String variableType){
        if(variableType.equals(TYPE_BLUETOOTH_DEVICES) || variableType.equals(TYPE_WIFI_NETWORKS)
                || variableType.equals(TYPE_NOISE_LEVEL) || variableType.equals(TYPE_LOCATION)){
            return true;
        }
        return false;
    }

    public abstract boolean isStringValuesSupported();

    public abstract boolean isFloatValuesSupported();

    protected float[] getFloatValues(){
        if(isFloatValuesSupported() && isInitialized){
            return floatValues;
        }
        else{
            return null;
        }
    }

    public float getFloatValue(int index) throws Exception{
        if(isFloatValuesSupported() && isInitialized){
            return floatValues[index];
        }
        else{
            throw new Exception("No float values associated with " + variableType) ;
        }
    }

    public String[] getStringValues(){
        if(isStringValuesSupported() && isInitialized){
            return stringValues;
        }
        else{
            return null;
        }
    }

    public String getStringValue(int index) throws Exception{
        if(isStringValuesSupported() && isInitialized){
            return stringValues[index];
        }
        else{
            throw new Exception("No String values associated with " + variableType) ;
        }
    }
}
