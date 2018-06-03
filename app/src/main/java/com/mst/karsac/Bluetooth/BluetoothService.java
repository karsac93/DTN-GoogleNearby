package com.mst.karsac.Bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothService extends Service {
    public static final String TAG = "BluetoothService";
    private BluetoothAdapter mBtAdapter;
    private List<String> mNewDevicesList = new ArrayList<>();

    public BluetoothService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    Handler discoverBondedAndNewhandler = new Handler();
    Runnable findAndNewDevicesRunnable = new Runnable() {
        @Override
        public void run() {
            mNewDevicesList.clear();
            Set<BluetoothDevice> bondedDevices = mBtAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                mNewDevicesList.add(device.getBondState() + "\n" + device.getAddress());
            }
        }
    };
}
