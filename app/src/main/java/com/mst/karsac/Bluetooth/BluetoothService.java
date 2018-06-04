package com.mst.karsac.Bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.mst.karsac.Algorithm.ChitchatAlgo;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.connections.MessageSerializer;
import com.mst.karsac.ratings.DeviceRating;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Service {
    static int i=0;
    private static final String NAME_INSECURE = "BluetoothChatInsecure";
    public static final String TAG = "BluetoothService";
    private BluetoothAdapter mBtAdapter;
    private static List<String> connectedDevices = new ArrayList();
    private Set<BluetoothDevice> mNewDevicesList = new HashSet<BluetoothDevice>() {
    };
    String macAddress;
    public static final UUID MY_UUID_INSECURE = UUID.fromString("00000000-0000-1000-8000-00805F9B34FB");
    private ConnectThread mConnectThread;
    private AcceptThread mAcceptThread;
    boolean mState = false;
    static boolean first_check = false;
    static boolean check_completed = false;

    public BluetoothService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBtAdapter = manager.getAdapter();
        } else {
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        macAddress = mBtAdapter.getAddress();
        Log.d(TAG, "My mac address:" + macAddress);
        mAcceptThread = new AcceptThread(this);
        mAcceptThread.start();
        discoverHandler.post(discoverRunnable);
        discoverBondedAndNewhandler.post(findAndNewDevicesRunnable);
    }

    @Override
    public void onDestroy() {
        connectedDevices.clear();
        mNewDevicesList.clear();
        synchronized (BluetoothService.this) {
            mConnectThread = null;
            mAcceptThread = null;
        }
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        discoverHandler.removeCallbacks(discoverRunnable);
        discoverBondedAndNewhandler.removeCallbacks(findAndNewDevicesRunnable);
        this.unregisterReceiver(mReceiver);
    }

    Handler discoverHandler = new Handler();
    Runnable discoverRunnable = new Runnable() {
        @Override
        public void run() {
            Method method;
            try {
                method = mBtAdapter.getClass().getMethod("setScanMode", int.class, int.class);
                method.invoke(mBtAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 300);
                Log.e("invoke", "method invoke successfully");
                discoverHandler.postDelayed(discoverRunnable, 300000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    Handler discoverBondedAndNewhandler = new Handler();
    Runnable findAndNewDevicesRunnable = new Runnable() {
        @Override
        public void run() {
            if (mState == false && check_completed == false) {
                Set<BluetoothDevice> bondedDevices = mBtAdapter.getBondedDevices();
                for (BluetoothDevice device : bondedDevices) {
                    unpairDevice(device);
                    Log.d(TAG, "Already paired:" + device.getName() + " Address" + device.getAddress());
                }
                if (mBtAdapter.isDiscovering()) {
                    mBtAdapter.cancelDiscovery();
                }
                Log.d(TAG, "Starting Discovery");
                mNewDevicesList.clear();
                mBtAdapter.startDiscovery();
                check_completed = true;
                discoverBondedAndNewhandler.postDelayed(findAndNewDevicesRunnable, 40000);
            }else{
                if(i > 1){
                    check_completed = false;
                    i =0;
                }
                else {
                    i = i +1;
                }
            }

        }
    };

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    addDevices(mBtAdapter.getRemoteDevice(device.getAddress()));
                    Log.d(TAG, "New devices:" + device.getName() + " Address" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                check_completed = false;
                Log.d(TAG, "Finished Discovery!" + mNewDevicesList.size());
                if (mState == false) {
                    if (mNewDevicesList.size() > 0) {
                       for(BluetoothDevice device : mNewDevicesList) {
                           startConnection(device);
                           break;
                       }
                    }
                }
                else{
                    Log.d(TAG, "mstate is true in receiver");
                }
            }
        }
    };

    public void startConnection(BluetoothDevice device) {
        mConnectThread = new ConnectThread(device, this);
        mConnectThread.start();
    }

    public void addDevices(BluetoothDevice device) {
        if (!connectedDevices.contains(device.getAddress())) {
            Log.d(TAG, "Not connected previously, so added");
            mNewDevicesList.add(device);
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final Context context;
        String connState = "";

        public ConnectThread(BluetoothDevice device, Context context) {
            mmDevice = device;
            this.context = context;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            InputStream in = null;
            ObjectInputStream ois = null;
            try {
                boolean flag = false;

                mNewDevicesList.remove(mmDevice);
                mmSocket.connect();
                if (mmSocket.isConnected()) {
                    mState = true;
                    Log.d(TAG, "Device connected:" + mmDevice.getName());
                    discoverBondedAndNewhandler.removeCallbacks(findAndNewDevicesRunnable);
                    MessageSerializer sendMsg = new BluetoothHelper().sendInterestData(getApplicationContext(), macAddress);
                    sendMessage(sendMsg, mmSocket);
                    while (true) {
                        in = mmSocket.getInputStream();
                        ois = new ObjectInputStream(in);
                        MessageSerializer receivedObj = (MessageSerializer) ois.readObject();
                        switch (receivedObj.mode) {
                            case MessageSerializer.INTEREST_MODE:
                                Log.d(TAG, "client inside interest mode");
                                showToast("Connected and transfer starts!", getBaseContext());
                                connState = "received";
                                connectedDevices.add(receivedObj.bluetoothMac);
                                Log.d(TAG, "Received objects bluetooth:" + receivedObj.bluetoothMac);
                                sendMsg.bluetoothMac = receivedObj.bluetoothMac;
                                new ChitchatAlgo().growthAlgorithm(receivedObj.my_interests, sendMsg.my_interests);
                                MessageSerializer imageTransfer = new BluetoothHelper().sendImageMessages(receivedObj, sendMsg.my_interests, getApplicationContext());
                                sendMessage(imageTransfer, mmSocket);
                                break;
                            case MessageSerializer.MESSAGE_MODE:
                                connState = "completed";
                                Log.d(TAG, "client inside message mode");
                                showToast("File Transfer complete!", getBaseContext());
                                new BluetoothHelper().handleImages(receivedObj, getApplicationContext());
                                MessageSerializer receivedMessage = new MessageSerializer(MessageSerializer.RECEIVED_MODE);
                                sendMessage(receivedMessage, mmSocket);
                                synchronized (BluetoothService.this) {
                                    mConnectThread = null;
                                }
                                flag = true;
                                break;
                        }
                        if (flag)
                            break;
                    }
                    mState = false;
                    mNewDevicesList.clear();
                    discoverBondedAndNewhandler.post(findAndNewDevicesRunnable);
                }
            } catch (Exception e) {
                if(in != null){
                    try {
                        in.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                if(ois != null){
                    try {
                        ois.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                if (connState.length() > 0 && !connState.contains("completed")) {
                    showToast("Transfer Failed!", context);
                }
                e.printStackTrace();
                Log.d(TAG, "Device not connected:" + mmDevice.getName());
                if (connState.length() > 0) {
                    mState = false;
                }
                if (mState == false && mNewDevicesList.size() > 0) {
                    for(BluetoothDevice device : mNewDevicesList)
                    if (!connectedDevices.contains(device.getAddress())) {
                        startConnection(device);
                    }
                } else {
                    discoverBondedAndNewhandler.post(findAndNewDevicesRunnable);
                }

                synchronized (BluetoothService.this) {
                    mConnectThread = null;
                }
            }

        }
    }

    public void showToast(final String toastMsg, final Context context) {
        Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private final Context context;
        private String connState = "";

        public AcceptThread(Context context) {
            BluetoothServerSocket tmp = null;
            this.context = context;
            // Create a new listening server socket
            try {
                tmp = mBtAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        NAME_INSECURE, MY_UUID_INSECURE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            ObjectInputStream ois = null;
            InputStream in = null;
            try {

                MessageSerializer sendMsg = null;
                MessageSerializer receivedInterestMsg = null;
                Log.d(TAG, "Waiting to connect Accept thread");
                socket = mmServerSocket.accept();
                discoverBondedAndNewhandler.removeCallbacks(findAndNewDevicesRunnable);
                if (mBtAdapter.isDiscovering())
                    mBtAdapter.cancelDiscovery();
                Log.d(TAG, "Accepted connection");
                mState = true;
                synchronized (BluetoothService.this) {
                    mConnectThread = null;
                }
                mNewDevicesList.clear();
                boolean flag = false;
                while (true) {
                    in = socket.getInputStream();
                    ois = new ObjectInputStream(in);
                    MessageSerializer receivedObj = (MessageSerializer) ois.readObject();
                    switch (receivedObj.mode) {
                        case MessageSerializer.INTEREST_MODE:
                            connState = "received";
                            Log.d(TAG, "Inside server interest Mode");
                            showToast("Connected and transfer starts!", getBaseContext());
                            receivedInterestMsg = receivedObj;
                            connectedDevices.add(receivedObj.bluetoothMac);
                            sendMsg = new BluetoothHelper().sendInterestData(getApplicationContext(), macAddress);
                            new ChitchatAlgo().growthAlgorithm(receivedObj.my_interests, sendMsg.my_interests);
                            sendMessage(sendMsg, socket);
                            Log.d(TAG, "sending the interest msg");
                            break;
                        case MessageSerializer.MESSAGE_MODE:
                            Log.d(TAG, "Server inside message mode");
                            MessageSerializer imageTransfer = new BluetoothHelper().sendImageMessages(receivedInterestMsg, sendMsg.my_interests, getApplicationContext());
                            new BluetoothHelper().handleImages(receivedObj, getApplicationContext());
                            sendMessage(imageTransfer, socket);
                            break;

                        case MessageSerializer.RECEIVED_MODE:
                            Log.d(TAG, "Received all messages");
                            connState = "completed";
                            showToast("File Transfer complete!", getBaseContext());
                            in.close();
                            ois.close();
                            socket.close();
                            flag = true;
                            break;
                    }
                    if (flag)
                        break;
                }
                mState = false;
                check_completed = false;
                mNewDevicesList.clear();
                discoverBondedAndNewhandler.post(findAndNewDevicesRunnable);
                mAcceptThread = new AcceptThread(context);
                mAcceptThread.start();
            } catch (Exception e) {
                if (connState.length() > 0 && !connState.contains("completed")) {
                    showToast("Transfer Failed!", getBaseContext());
                }
                e.printStackTrace();
                if (connState.length() > 0)
                    mState = false;
                mNewDevicesList.clear();
                if(in != null) {
                    try {
                        in.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                if(ois != null){
                    try {
                        ois.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                discoverBondedAndNewhandler.post(findAndNewDevicesRunnable);
                mAcceptThread = new AcceptThread(context);
                mAcceptThread.start();

            }
        }
    }

    private void sendMessage(MessageSerializer sendMsg, BluetoothSocket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(sendMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (first_check == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBtAdapter = manager.getAdapter();
            } else {
                mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            }
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(mReceiver, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(mReceiver, filter);
            macAddress = mBtAdapter.getAddress();
            Log.d(TAG, "My mac address:" + macAddress);
            mAcceptThread = new AcceptThread(this);
            mAcceptThread.start();
            discoverHandler.post(discoverRunnable);
            discoverBondedAndNewhandler.post(findAndNewDevicesRunnable);
        }
        first_check = true;
        return START_NOT_STICKY;
    }

    private void handleDeviceRatings(List<DeviceRating> deviceRatingList) {
        for (DeviceRating deviceRating : deviceRatingList) {
            if (!deviceRating.getDevice_uuid().contains(GlobalApp.source_mac)) {
                GlobalApp.dbHelper.insertOrUpdateDeviceRatings(deviceRating);
            }
        }
    }
}
