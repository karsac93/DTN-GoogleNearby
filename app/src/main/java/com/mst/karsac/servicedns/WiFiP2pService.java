package com.mst.karsac.servicedns;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by ks2ht on 3/23/2018.
 */

public class WiFiP2pService {
    WifiP2pDevice device;
    String instanceName = null;
    String serviceRegistrationType = null;

    public WiFiP2pService(WifiP2pDevice device, String instanceName, String serviceRegistrationType) {
        this.device = device;
        this.instanceName = instanceName;
        this.serviceRegistrationType = serviceRegistrationType;
    }
}
