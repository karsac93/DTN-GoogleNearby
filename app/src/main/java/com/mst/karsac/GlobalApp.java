package com.mst.karsac;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.mst.karsac.DbHelper.DbHelper;
import com.mst.karsac.connections.BackgroundService;
import com.mst.karsac.servicedns.WiFiDirectBroadcastReceiver;

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
        wifiManager.setWifiEnabled(false);
        wifiManager.setWifiEnabled(true);
        WifiInfo info = wifiManager.getConnectionInfo();
        source_mac = info.getMacAddress();
        Log.d("GLOBAL", source_mac);
        startService(new Intent(this, BackgroundService.class));
    }
}
