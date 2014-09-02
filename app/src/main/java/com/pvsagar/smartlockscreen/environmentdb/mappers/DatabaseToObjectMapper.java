package com.pvsagar.smartlockscreen.environmentdb.mappers;

import android.database.Cursor;

import com.pvsagar.smartlockscreen.applogic_objects.BluetoothEnvironmentVariable;
import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 2/9/14.
 *
 * This class maps the database cursors with values, to objects defined by the classes in
 * applogic_objects.
 */
public class DatabaseToObjectMapper {

    /**
     * Converts cursor containing data from bluetooth_devices table, to objects of
     * BluetoothEnvironmentVariable
     * @param bluetoothCursor
     * @return List of BluetoothEnvironmentVariables
     */
    public static List<EnvironmentVariable> getBluetoothEnvironmentVariablesFromCursor
            (Cursor bluetoothCursor){
        ArrayList<EnvironmentVariable> environmentVariables =
                new ArrayList<EnvironmentVariable>();
        if(bluetoothCursor.moveToFirst()){
            for(;!bluetoothCursor.isAfterLast(); bluetoothCursor.moveToNext()){
                environmentVariables.add(new BluetoothEnvironmentVariable(
                        bluetoothCursor.getString(bluetoothCursor.getColumnIndex(
                                EnvironmentDatabaseContract.BluetoothDevicesEntry.COLUMN_DEVICE_NAME)),
                        bluetoothCursor.getString(bluetoothCursor.getColumnIndex(
                                EnvironmentDatabaseContract.BluetoothDevicesEntry.COLUMN_DEVICE_ADDRESS))));
            }
        }
        return environmentVariables;
    }

    //TODO Functions for mapping to other objects
}
