package com.pvsagar.smartlockscreen.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.pvsagar.smartlockscreen.applogic_objects.BluetoothEnvironmentVariable;
import com.pvsagar.smartlockscreen.services.BaseService;

/**
 * Created by aravind on 5/9/14.
 * Receives an intent whenever a bluetooth device is connected/disconnected, and takes the
 * required actions
 */
public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String mAction = intent.getAction();
        BluetoothDevice device;
        if(mAction.equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            BaseService.addBluetoothDeviceToConnectedDevices(
                    new BluetoothEnvironmentVariable(device.getName(), device.getAddress()));
        } else if(mAction.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            BaseService.removeBluetoothDeviceFromConnectedDevices(
                    new BluetoothEnvironmentVariable(device.getName(), device.getAddress()));
        } else return;
        Toast.makeText(context, device.getName() + " connected.", Toast.LENGTH_SHORT).show();
    }
}
