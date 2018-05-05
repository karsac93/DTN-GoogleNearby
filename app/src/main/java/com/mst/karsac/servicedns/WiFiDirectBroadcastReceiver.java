package com.mst.karsac.servicedns;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.mst.karsac.GlobalApp;
import com.mst.karsac.MainActivity;
import com.mst.karsac.Neighbours.*;

/**
 * Created by ks2ht on 3/23/2018.
 */

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    public final Context context;
    private static final String TAG = "BroadcastReceiver";
    NotifyPeerChange listener;
    static WifiP2pDevice wifiP2pDevice = null;
    Context neighbourContext;
    DeviceListenerNeigh deviceListenerNeigh;


    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       Context context) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.context = context;
        this.listener = (NotifyPeerChange) context;
    }

    public void setNeighbourContext(Context context) {
        neighbourContext = context;
        DeviceListenerNeigh deviceListenerNeigh = (DeviceListenerNeigh) context;
        if (wifiP2pDevice != null)
            Log.d(TAG, "Device name:" + wifiP2pDevice.deviceName);
            deviceListenerNeigh.deviceDetails(wifiP2pDevice);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            Log.d("Onreceive", "Check");
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(TAG, "WIFI p2p Is enabled %%%%%%");
                listener.checkP2pEnabled(true);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            //listener.peersHaveChanged();
            Log.d(TAG, "Inside Peers changed listener");
            manager.requestPeers(channel, (WifiP2pManager.PeerListListener) neighbourContext);

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            if (manager == null)
                return;
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                manager.requestConnectionInfo(channel, (WifiP2pManager.ConnectionInfoListener) context);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            wifiP2pDevice = (WifiP2pDevice) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Log.d(TAG, "Device status -" + wifiP2pDevice.status + " " + wifiP2pDevice.deviceAddress);
            listener.peersHaveChanged(wifiP2pDevice);
            if (deviceListenerNeigh != null)
                deviceListenerNeigh.deviceDetails(wifiP2pDevice);
        }
    }

    public interface NotifyPeerChange {
        void peersHaveChanged(WifiP2pDevice device);
        void checkP2pEnabled(boolean flag);
    }
}

