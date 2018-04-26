package com.mst.karsac.connections;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class BackgroundService extends Service implements WifiP2pManager.ConnectionInfoListener, TsInterestsInterface {
    public static final String TAG = "BackgroundService";
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static String SERVICE_INSTANCE = "_wifidemotest";
    public static final String INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    public static final int PORT = 8888;
    public static final int SOCKET_TIMEOUT = 60000;

    private static WifiP2pManager manager;
    private final IntentFilter intentFilter = new IntentFilter();
    private static WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    public static final String OWNER = "owner";
    public static final String CLIENT = "client";
    MessageSerializer my_messageSerializer = null;
    public static final String LAST_DEVICE = "lastdevice";


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
        SERVICE_INSTANCE = SERVICE_INSTANCE + "|" + GlobalApp.source_mac;
        startRegistration();
    }

    private void startRegistration() {
        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo
                        .newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, null);
                manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        manager.setDnsSdResponseListeners(channel, new WifiP2pManager.DnsSdServiceResponseListener() {
                            @Override
                            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice wifiP2pDevice) {
                                Log.d(TAG, "Service Found:" + instanceName);
                                if (instanceName.contains(INSTANCE)) {
                                    Log.d(TAG, instanceName.substring(instanceName.indexOf("|") + 1, instanceName.length()));
                                    String receivedMac = instanceName.substring(instanceName.indexOf("|") + 1, instanceName.length()).replace(":", "");
                                    String deviceMac = GlobalApp.source_mac.replace(":", "");
                                    boolean flag = false;
                                    for (int i = 0; i < receivedMac.length(); i++) {
                                        int own = (int) deviceMac.charAt(i);
                                        int received = (int) receivedMac.charAt(i);
                                        if (own != received) {
                                            if (own > received) {
                                                Log.d(TAG, "Initiating the connection");
                                                flag = true;
                                            }
                                            break;
                                        }
                                    }
                                    if (flag) {
                                        connectP2p(wifiP2pDevice);
                                    }
                                }
                            }
                        }, new WifiP2pManager.DnsSdTxtRecordListener() {
                            @Override
                            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, WifiP2pDevice wifiP2pDevice) {
                            }

                        });
                        manager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                manager.addServiceRequest(channel, WifiP2pDnsSdServiceRequest.newInstance(), new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                                            @Override
                                            public void onSuccess() {
                                                manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
                                                    @Override
                                                    public void onSuccess() {
                                                        handler.postDelayed(handlerTask, 60000);
                                                    }

                                                    @Override
                                                    public void onFailure(int i) {
                                                        Log.d(TAG, "Discover services failure:" + i);
                                                        handler.postDelayed(handlerTask, 60000);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(int i) {
                                                Log.d(TAG, "discoverPeers Failure:" + i);
                                                handler.postDelayed(handlerTask, 60000);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(int i) {
                                        Log.d(TAG, "addServiceRequest Failure:" + i);
                                        handler.postDelayed(handlerTask, 60000);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(int i) {
                                Log.d(TAG, "clearServiceRequests Failure:" + i);
                                handler.postDelayed(handlerTask, 60000);
                            }
                        });
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d(TAG, "addLocalService Failure:" + i);
                        handler.postDelayed(handlerTask, 60000);
                    }
                });
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "clearLocalServices Failure:" + i);
            }
        });
    }

    Handler handler = new Handler();

    Runnable handlerTask = new Runnable() {
        @Override
        public void run() {
            startRegistration();
            handler.postDelayed(handlerTask, 60000);
        }
    };


    private void connectP2p(final WifiP2pDevice wifiP2pDevice) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiP2pDevice.deviceAddress;
        String last_device = SharedPreferencesHandler.getStringPreferences(this, LAST_DEVICE);
        if (!last_device.equals(wifiP2pDevice.deviceAddress)){
            SharedPreferencesHandler.setStringPreferences(getApplicationContext(), BackgroundService.LAST_DEVICE, wifiP2pDevice.deviceAddress);
            Log.d(TAG, "New device:" + last_device + " Device:" + wifiP2pDevice.deviceAddress);
            config.wps.setup = WpsInfo.PBC;
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
        } else {
            Log.d(TAG, "This was already connected last time:" + last_device + " Device:" + wifiP2pDevice.deviceAddress);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Inside on destroy!");
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
        List<Interest> my_interests = new ChitchatAlgo().decayingFunction(SharedPreferencesHandler.getTimestamp(this, GlobalApp.TIMESTAMP));
        my_messageSerializer = new MessageSerializer(my_interests, MessageSerializer.INTEREST_MODE);
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            Log.d(TAG, "Server");
            FileReceiveAsyncTask fileReceiveAsyncTask = new FileReceiveAsyncTask(this, my_messageSerializer, BackgroundService.OWNER);
            fileReceiveAsyncTask.execute();

        } else {
            Log.d(TAG, "CLIENT");
            FileTransferAsyncTask fileTransferAsyncTask = new FileTransferAsyncTask(this, wifiP2pInfo.groupOwnerAddress, my_messageSerializer);
            fileTransferAsyncTask.execute();
            FileReceiveAsyncTask fileReceiveAsyncTask = new FileReceiveAsyncTask(this, my_messageSerializer, BackgroundService.CLIENT);
            fileReceiveAsyncTask.execute();
        }

    }

    @Override
    public MessageSerializer getTsInterests() {
        return my_messageSerializer;
    }

    @Override
    public void notifyComplete() {
        disconnect();
        deletePersistentGroups();
    }

    public static void disconnect() {
        if (manager != null && channel != null) {
            manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && manager != null && channel != null
                            && group.isGroupOwner()) {
                        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }
    }

    public static void deletePersistentGroups() {
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(manager, channel, netid, null);
                    }
                }
            }

            manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Successfully stopped Peer Discovery");
                }

                @Override
                public void onFailure(int i) {
                    Log.d(TAG, "Not able to stop Peer discovery!");
                    deletePersistentGroups();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class FileTransferAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        InetAddress host;
        MessageSerializer messageSerializer;

        public FileTransferAsyncTask(Context context, InetAddress host, MessageSerializer messageSerializer) {
            this.context = context;
            this.host = host;
            this.messageSerializer = messageSerializer;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Socket socket = new Socket();
            try {
                socket.bind(null);
                socket.connect(new InetSocketAddress(host, PORT), SOCKET_TIMEOUT);
                OutputStream stream = socket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(stream);
                if (messageSerializer != null && messageSerializer.mode.contains(MessageSerializer.INTEREST_MODE)) {
                    Log.d(TAG, "Size" + messageSerializer.my_interests.size());
                }
                oos.writeObject(messageSerializer);
                stream.close();
                oos.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
                if (messageSerializer.mode.contains(MessageSerializer.RECEIVED_MODE)) {
                    deletePersistentGroups();
                }
            }
            return null;
        }
    }
}
