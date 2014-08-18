package com.pvsagar.smartlockscreen.applogic_objects;

import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;

import java.util.Vector;

/**
 * Created by aravind on 10/8/14.
 */
public class Environment {
    private LocationEnvironmentVariable locationEnvironmentVariable;
    private Vector<BluetoothEnvironmentVariable> bluetoothEnvironmentVariables;
    private WiFiEnvironmentVariable wiFiEnvironmentVariable;
    private NoiseLevelEnvironmentVariable noiseLevelEnvironmentVariable;

    public boolean hasLocation, hasBluetoothDevices, hasWiFiNetwork, hasNoiseLevel;
    public Environment(){
        hasLocation = hasNoiseLevel = hasWiFiNetwork = hasBluetoothDevices = false;
    }

    public Environment(EnvironmentVariable... variables){
        this();
        addEnvironmentVariables(variables);
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
        hasLocation = true;
    }

    public void addBluetoothDevicesEnvironmentVariable(BluetoothEnvironmentVariable variable){
        if(bluetoothEnvironmentVariables == null){
            bluetoothEnvironmentVariables = new Vector<BluetoothEnvironmentVariable>();
        }
        bluetoothEnvironmentVariables.add(variable);
        hasBluetoothDevices = true;
    }

    public void addWiFiEnvironmentVariable(WiFiEnvironmentVariable variable){
        wiFiEnvironmentVariable = variable;
        hasWiFiNetwork = true;
    }

    public void addNoiseLevelEnvironmentVariable(NoiseLevelEnvironmentVariable variable){
        noiseLevelEnvironmentVariable = variable;
        hasNoiseLevel = true;
    }
}
