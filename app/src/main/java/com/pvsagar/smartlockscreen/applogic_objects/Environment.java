package com.pvsagar.smartlockscreen.applogic_objects;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;

import java.util.Vector;

/**
 * Created by aravind on 10/8/14.
 */
public class Environment {
    private LocationEnvironmentVariable locationEnvironmentVariable;
    private Vector<BluetoothEnvironmentVariable> bluetoothEnvironmentVariables;
    //true for all, false for any
    private boolean bluetoothAllOrAny;
    private WiFiEnvironmentVariable wiFiEnvironmentVariable;
    private NoiseLevelEnvironmentVariable noiseLevelEnvironmentVariable;
    private String name;

    public boolean hasLocation, hasBluetoothDevices, hasWiFiNetwork, hasNoiseLevel;
    public Environment(){
        hasLocation = hasNoiseLevel = hasWiFiNetwork = hasBluetoothDevices = false;
        bluetoothAllOrAny = false;
    }

    public Environment(String name, EnvironmentVariable... variables){
        this();
        setName(name);
        addEnvironmentVariables(variables);
    }

    public void setName(String name){
        if(name != null) {
            this.name = name;
        } else {
            throw new IllegalArgumentException("Name cannot be null.");
        }
    }

    public void setBluetoothAllOrAny(boolean b){
        bluetoothAllOrAny = b;
    }

    public void addEnvironmentVariables(EnvironmentVariable... variables){
        for(EnvironmentVariable variable: variables){
            if(variable.getVariableType().equals(EnvironmentVariable.TYPE_LOCATION)){
                addLocationVariable((LocationEnvironmentVariable) variable);
            }
            if(variable.getVariableType().equals(EnvironmentVariable.TYPE_BLUETOOTH_DEVICES)){
                addBluetoothDevicesEnvironmentVariable((BluetoothEnvironmentVariable) variable);
            }
            if(variable.getVariableType().equals(EnvironmentVariable.TYPE_WIFI_NETWORKS)){
                addWiFiEnvironmentVariable((WiFiEnvironmentVariable) variable);
            }
            if(variable.getVariableType().equals(EnvironmentVariable.TYPE_NOISE_LEVEL)){
                addNoiseLevelEnvironmentVariable((NoiseLevelEnvironmentVariable) variable);
            }
        }
    }

    public void addLocationVariable(LocationEnvironmentVariable variable){
        locationEnvironmentVariable = variable;
        hasLocation = variable != null;
    }

    public void addBluetoothDevicesEnvironmentVariable(BluetoothEnvironmentVariable variable){
        if(variable != null) {
            if (bluetoothEnvironmentVariables == null) {
                bluetoothEnvironmentVariables = new Vector<BluetoothEnvironmentVariable>();
            }
            bluetoothEnvironmentVariables.add(variable);
            hasBluetoothDevices = true;
        } else {
            bluetoothEnvironmentVariables = null;
            hasBluetoothDevices = false;
        }
    }

    public void addWiFiEnvironmentVariable(WiFiEnvironmentVariable variable){
        wiFiEnvironmentVariable = variable;
        hasWiFiNetwork = variable != null;
    }

    public void addNoiseLevelEnvironmentVariable(NoiseLevelEnvironmentVariable variable){
        noiseLevelEnvironmentVariable = variable;
        hasNoiseLevel = variable != null;
    }

    public LocationEnvironmentVariable getLocationEnvironmentVariable(){
        return locationEnvironmentVariable;
    }

    public Vector<BluetoothEnvironmentVariable> getBluetoothEnvironmentVariables(){
        return bluetoothEnvironmentVariables;
    }

    public WiFiEnvironmentVariable getWiFiEnvironmentVariable(){
        return wiFiEnvironmentVariable;
    }

    public NoiseLevelEnvironmentVariable getNoiseLevelEnvironmentVariable(){
        return noiseLevelEnvironmentVariable;
    }

    public String getName(){
        return name;
    }

    public boolean isBluetoothAllOrAny(){
        return bluetoothAllOrAny;
    }
}
