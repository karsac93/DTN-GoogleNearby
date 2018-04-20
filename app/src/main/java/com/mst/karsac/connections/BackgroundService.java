package com.mst.karsac.connections;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.mst.karsac.Algorithm.ChitchatAlgo;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.Utils.SharedPreferencesHandler;
import com.mst.karsac.interest.Interest;
import com.mst.karsac.servicedns.WiFiDirectBroadcastReceiver;
import com.mst.karsac.servicedns.WiFiP2pService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackgroundService extends Service implements WifiP2pManager.ConnectionInfoListener {
    public static final String TAG = "BackgroundService";
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    public String deviceMac = "";
    public static final int PORT = 8085;
    public static final String OWNER = "owner";
    public static final String CLIENT = "client";
    public static final int SOCKET_TIMEOUT = 5000;

    private static WifiP2pManager manager;
    private final IntentFilter intentFilter = new IntentFilter();
    private static WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    public static final int SERVICE_BROADCASTING_INTERVAL = 20000;

    HashMap<WiFiP2pService, Integer> serviceList = new HashMap<>();
    private WifiP2pDnsSdServiceRequest serviceRequest;
    Handler mServiceBroadcastingHandler = new Handler();
    Handler mServiceDiscoveringHandler = new Handler();


    public BackgroundService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Inside service, onCreate!");
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        this.registerReceiver(receiver, intentFilter);
        WifiManager wifiManager = (WifiManager) getApplicationContext().
                getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        deviceMac = info.getMacAddress();

        startRegistration();
        discoverService();


    }

    private void startRegistration() {
        Map record = new HashMap();
        record.put(TXTRECORD_PROP_AVAILABLE, deviceMac);

        final WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);

       manager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {
           @Override
           public void onSuccess() {
               Log.d(TAG, "Registration Successful");

           }

           @Override
           public void onFailure(int i) {
               Log.d(TAG, "Registration not successful");

           }
       });
    }


    private void discoverService() {
        manager.setDnsSdResponseListeners(channel, new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
                    WiFiP2pService service = new WiFiP2pService(srcDevice, instanceName, registrationType);
                    serviceList.put(service, 0);
                    Log.d(TAG, "New service detected");
                }
            }
        }, new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, WifiP2pDevice wifiP2pDevice) {
                Log.d(TAG, wifiP2pDevice.deviceName + " Mac:" + record.get(TXTRECORD_PROP_AVAILABLE));
                String receivedMac = record.get(TXTRECORD_PROP_AVAILABLE).replace(":", "");
                deviceMac = deviceMac.replace(":", "");
                boolean flag = false;
                for (int i = 0; i < receivedMac.length(); i++) {
                    int own = (int) deviceMac.charAt(i);
                    int received = (int) receivedMac.charAt(i);
                    if (own != received) {
                        if (own > received) {
                            flag = true;
                        }
                        break;
                    }
                }
                if (flag) {
                    connectP2p(wifiP2pDevice);
                }

            }
        });

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel,serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "discoverServices");
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d(TAG, "discoverServices Failure");
                    }
                });
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "addServiceRequest Failure");
            }
        });
    }



    private void connectP2p(final WifiP2pDevice wifiP2pDevice) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiP2pDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        removeGroupsBefore();
        if (wifiP2pDevice.status != WifiP2pDevice.CONNECTED) {
            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Connected successfully");
                    Toast.makeText(getApplicationContext(), "Connected to" + wifiP2pDevice.deviceName, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int i) {
                    Log.d(TAG, "Connection Failed");
                }
            });
        }
    }

    private void removeGroupsBefore() {
        if (manager != null && channel != null) {
            manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
                }

                @Override
                public void onSuccess() {
                }
            });
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Inside on destry!");
        if (manager != null && channel != null) {
            manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
                }

                @Override
                public void onSuccess() {
                }
            });
        }
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        Log.d(TAG, "Inside onConnectionAvailable method");
        int init_time = SharedPreferencesHandler.getTimestamp(getApplicationContext(), GlobalApp.TIMESTAMP);
        SharedPreferencesHandler.setTimestamp(getApplicationContext(), GlobalApp.TIMESTAMP, init_time + 1);
        List<Interest> decayedInterest = new ChitchatAlgo().decayingFunction(init_time + 1);
        MessageExchange messageExchange = new MessageExchange((ArrayList<Interest>) decayedInterest);
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            Log.d(TAG, "Server");
            ServerAsyncTask serverAsyncTask = new ServerAsyncTask(this, OWNER, wifiP2pInfo.groupOwnerAddress);
            serverAsyncTask.execute(messageExchange);

        } else {
            Log.d(TAG, "CLIENT");
            ServerAsyncTask serverAsyncTask = new ServerAsyncTask(this, CLIENT, wifiP2pInfo.groupOwnerAddress);
            serverAsyncTask.execute(messageExchange);
        }

    }

    public static class ServerAsyncTask extends AsyncTask<MessageExchange, String, MessageExchange> {
        Context context;
        String role;
        InetAddress groupOwnerAddress;

        public ServerAsyncTask(Context context, String role, InetAddress groupOwnerAddress) {
            this.context = context;
            this.role = role;
            this.groupOwnerAddress = groupOwnerAddress;
        }

        @Override
        protected void onPostExecute(MessageExchange msg) {
            ArrayList<Interest> interests = msg.getInterestArrayList();
            for (Interest interest : interests) {
                Log.d(TAG, interest.getInterest());
            }
        }

        @Override
        protected MessageExchange doInBackground(MessageExchange... messageExchanges) {
            MessageExchange msg = null;
            try {
                if (role == CLIENT) {
                    Log.d(TAG, "Inside client");
                    Socket socket = new Socket();
                    try {
                        socket.connect(new InetSocketAddress(groupOwnerAddress, PORT), SOCKET_TIMEOUT);
                        OutputStream stream = socket.getOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(stream);
                        oos.writeObject(messageExchanges);
                        oos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (socket != null) {
                            if (socket.isConnected())
                                socket.close();
                        }
                    }
                }
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(PORT));

                Socket client = serverSocket.accept();
                Log.d(TAG, "Client's InetAddress:" + client.getInetAddress());
                ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream is = new ObjectInputStream(client.getInputStream());
                msg = (MessageExchange) is.readObject();
                client.close();
                serverSocket.close();


            } catch (Exception e) {
                e.printStackTrace();
            }
            return msg;
        }
    }
}
