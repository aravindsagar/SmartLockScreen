package com.pvsagar.smartlockscreen.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pvsagar.smartlockscreen.applogic_objects.BluetoothEnvironmentVariable;
import com.pvsagar.smartlockscreen.services.BaseService;

/**
 * Created by aravind on 5/9/14.
 */
public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String mAction = intent.getAction();
        if(mAction.equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            BaseService.addBluetoothDeviceToConnectedDevices(
                    new BluetoothEnvironmentVariable(device.getName(), device.getAddress()));
        } else if(mAction.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            BaseService.removeBluetoothDeviceFromConnectedDevices(
                    new BluetoothEnvironmentVariable(device.getName(), device.getAddress()));
        }
    }
}
