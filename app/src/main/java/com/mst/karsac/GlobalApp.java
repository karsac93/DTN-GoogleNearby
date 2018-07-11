package com.mst.karsac;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.mst.karsac.DbHelper.DbHelper;
import com.mst.karsac.NearbySupports.NearbyService;
import com.mst.karsac.servicedns.WiFiDirectBroadcastReceiver;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by ks2ht on 3/25/2018.
 */

public class GlobalApp extends Application {
    public static DbHelper dbHelper;
    public static MainActivity mainActivityContext;
    public static final String TIMESTAMP = "timestamp";
    public static final int SELF_INTEREST = 0;
    public static final int TRANSIENT_INTEREST = 1;
    public static String source_mac;
    public static WiFiDirectBroadcastReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("GLOBAL", "Inside");
        dbHelper = new DbHelper(this);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        wifiManager.setWifiEnabled(false);
//        wifiManager.setWifiEnabled(true);
        WifiInfo info = wifiManager.getConnectionInfo();
        String address = getMacAddr();
        source_mac = UUID.nameUUIDFromBytes(address.replace(":", "").getBytes()).toString();
        Log.d("GLOBAL", source_mac);
        //startService(new Intent(this, BackgroundService.class));
        //startService(new Intent(this, NearbyService.class));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }
}
