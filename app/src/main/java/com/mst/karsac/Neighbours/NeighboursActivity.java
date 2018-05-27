package com.mst.karsac.Neighbours;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mst.karsac.GlobalApp;
import com.mst.karsac.R;

import java.util.ArrayList;
import java.util.List;

public class NeighboursActivity extends AppCompatActivity implements WifiP2pManager.PeerListListener, DeviceListenerNeigh {

    public static final String TAG = NeighboursActivity.class.getSimpleName();
    TextView device_name, device_status;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    ListView listView;
    WiFiPeerListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neighbours);
        if(Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        GlobalApp.receiver.setNeighbourContext(this);
        device_name = findViewById(R.id.my_device_name);
        device_status = findViewById(R.id.my_device_status);
        listView = findViewById(R.id.listview);
        adapter = new WiFiPeerListAdapter(this, R.layout.row_devices, peers);
        listView.setAdapter(adapter);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        peers.clear();
        peers.addAll(wifiP2pDeviceList.getDeviceList());
        Log.d(TAG, peers.size() + " ");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void deviceDetails(WifiP2pDevice device) {
        Log.d(TAG, "Name:" + device.deviceName);
        if (device_name == null || device_status == null) {
            device_name = findViewById(R.id.my_device_name);
            device_status = findViewById(R.id.my_device_status);
        }
        device_name.setText("My device:" + device.deviceName);
        device_status.setText("Status: " + getDeviceStatus(device.status));
    }

    private static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items;

        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                                   List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(getDeviceStatus(device.status));
                }
            }

            return v;

        }
    }

}
