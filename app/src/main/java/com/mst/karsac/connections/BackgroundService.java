package com.mst.karsac.connections;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.mst.karsac.Algorithm.ChitchatAlgo;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.Settings.Setting;
import com.mst.karsac.Utils.SharedPreferencesHandler;
import com.mst.karsac.interest.Interest;
import com.mst.karsac.ratings.RatingPOJ;
import com.mst.karsac.servicedns.WiFiDirectBroadcastReceiver;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service implements WifiP2pManager.ConnectionInfoListener, TsInterestsInterface, WiFiDirectBroadcastReceiver.NotifyPeerChange {
    public static final String TAG = "BackgroundService";
    public static String SERVICE_INSTANCE = "_wifidemotest";
    public static final String INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    public static final int PORT = 8888;
    public static final int SOCKET_TIMEOUT = 60000;

    private static WifiP2pManager manager;
    private final IntentFilter intentFilter = new IntentFilter();
    private static WifiP2pManager.Channel channel;
    private WiFiDirectBroadcastReceiver receiver = null;
    public static final String OWNER = "owner";
    public static final String CLIENT = "client";
    static MessageSerializer my_messageSerializer_temp = null;
    public static final int SERVICE_BROADCASTING_INTERVAL = 60000;
    public static final int SERVICE_DISCOVERING_INTERVAL = 60000;
    WifiP2pServiceRequest wifiP2pServiceRequest;
    WifiP2pDevice my_device;
    public static int num_failures = 0;
    ArrayList<String> wifiaddresses = new ArrayList<>();
    public static boolean wifip2p_enabled = false;
    public static boolean check_connected = false;


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
        GlobalApp.receiver = receiver;
        this.registerReceiver(receiver, intentFilter);
        SERVICE_INSTANCE = SERVICE_INSTANCE + "|" + GlobalApp.source_mac;
        wifiP2pServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();

    }


    public void threestepstodiscovery() {
        if ((my_device != null && my_device.status == WifiP2pDevice.AVAILABLE)) {
            Log.d(TAG, "Listeners setup");
            startListeners();
            Log.d(TAG, "Prepare discovery");
            prepareServiceDiscovery();
            Log.d(TAG, "start service discovery");
            startServiceDiscovery();
        }
    }

    private void startServiceDiscovery() {
        manager.removeServiceRequest(channel, wifiP2pServiceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                manager.addServiceRequest(channel, wifiP2pServiceRequest, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                mServiceDiscoveringHandler.removeCallbacks(mServiceDiscoveringRunnable);
                                mServiceDiscoveringHandler.postDelayed(
                                        mServiceDiscoveringRunnable,
                                        SERVICE_DISCOVERING_INTERVAL);
                                Log.d(TAG, "Discover service success!");
                            }

                            @Override
                            public void onFailure(int i) {
                                Log.d(TAG, "Discover Failure:" + i);
                                manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "Stopped Peer discovery");
                                        mServiceDiscoveringHandler.removeCallbacks(mServiceDiscoveringRunnable);
                                        mServiceDiscoveringHandler.postDelayed(
                                                mServiceDiscoveringRunnable,
                                                SERVICE_DISCOVERING_INTERVAL);
                                    }

                                    @Override
                                    public void onFailure(int i) {
                                        Log.d(TAG, "Failed to Stop Peer discovery");
                                        mServiceDiscoveringHandler.removeCallbacks(mServiceDiscoveringRunnable);
                                        mServiceDiscoveringHandler.postDelayed(
                                                mServiceDiscoveringRunnable,
                                                SERVICE_DISCOVERING_INTERVAL);
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d(TAG, "addServiceRequest Failure:" + i);
                        mServiceDiscoveringHandler.removeCallbacks(mServiceDiscoveringRunnable);
                        mServiceDiscoveringHandler.postDelayed(
                                mServiceDiscoveringRunnable,
                                SERVICE_DISCOVERING_INTERVAL);
                    }
                });

            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "removeServiceRequest Failure:" + i);
                mServiceDiscoveringHandler.postDelayed(
                        mServiceDiscoveringRunnable,
                        SERVICE_DISCOVERING_INTERVAL);
            }
        });
    }

    Handler mServiceDiscoveringHandler = new Handler();
    private Runnable mServiceDiscoveringRunnable = new Runnable() {
        @Override
        public void run() {
            if (my_device.status == WifiP2pDevice.AVAILABLE) {
                Log.d(TAG, "Running service discovery in the background");
                startServiceDiscovery();
                mServiceBroadcastingHandler
                        .postDelayed(mServiceDiscoveringRunnable, SERVICE_DISCOVERING_INTERVAL);
            }
        }
    };

    private void prepareServiceDiscovery() {
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
    }


    private void startListeners() {
        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo
                        .newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, null);
                manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        mServiceBroadcastingHandler
                                .postDelayed(mServiceBroadcastingRunnable,
                                        SERVICE_BROADCASTING_INTERVAL);
                        Log.d(TAG, "Add Local service success");
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d(TAG, "addLocalService Failure:" + i);

                    }
                });
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "clearLocalServices Failure:" + i);
            }
        });
    }

    Handler mServiceBroadcastingHandler = new Handler();
    private Runnable mServiceBroadcastingRunnable = new Runnable() {
        @Override
        public void run() {
            if (my_device.status == WifiP2pDevice.AVAILABLE) {
                Log.d(TAG, "Discovering peers in the background");
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Discover Peers successful!");
                    }

                    @Override
                    public void onFailure(int error) {
                        Log.d(TAG, "Discover Peers Failure!");
                    }
                });

                mServiceBroadcastingHandler
                        .postDelayed(mServiceBroadcastingRunnable, SERVICE_BROADCASTING_INTERVAL);
            }

        }
    };


    private void connectP2p(final WifiP2pDevice wifiP2pDevice) {
        Log.d(TAG, "Inside connectP2p");
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiP2pDevice.deviceAddress;
        final String address = wifiP2pDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        boolean flag = false;
        for (String wifip2pAddress : wifiaddresses) {
            if (wifip2pAddress.contains(wifiP2pDevice.deviceAddress)) {
                flag = true;
                break;
            }
        }
        if (flag == false) {
            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Connected successfully");
                    Toast.makeText(getApplicationContext(), "Connected to" + wifiP2pDevice.deviceName, Toast.LENGTH_SHORT).show();
                    wifiaddresses.add(address);
                    check_connected = false;
                    handlerConnection.postDelayed(runnableConnectionCheck, 20000);

                }

                @Override
                public void onFailure(int i) {
                    Log.d(TAG, "Connection Failed:" + i);
                    Toast.makeText(getApplicationContext(), "This device either declined the " +
                            "connection or some problem, wait for sometime!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    Handler handlerConnection = new Handler();
    Runnable runnableConnectionCheck = new Runnable() {
        @Override
        public void run() {
            if (check_connected == false)
                Log.d(TAG, "Removing the last device address");
                wifiaddresses.remove(wifiaddresses.size() - 1);
        }
    };

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
        check_connected = true;
        mServiceDiscoveringHandler.removeCallbacks(mServiceBroadcastingRunnable);
        mServiceDiscoveringHandler.removeCallbacks(mServiceDiscoveringRunnable);

        MessageSerializer my_messageSerializer;
        Log.d(TAG, "Inside onConnectionAvailable method");
        int init_time = SharedPreferencesHandler.getIntPreferences(getApplicationContext(), GlobalApp.TIMESTAMP);
        SharedPreferencesHandler.setIntPreference(getApplicationContext(), GlobalApp.TIMESTAMP, init_time + 1);
        List<Interest> my_interests = new ChitchatAlgo().decayingFunction(SharedPreferencesHandler.getIntPreferences(this, GlobalApp.TIMESTAMP));
        Log.d(TAG, "Size of decayed interest:---" + my_interests.size());
        my_messageSerializer = new MessageSerializer(my_interests, MessageSerializer.INTEREST_MODE);
        Log.d(TAG, "Size of decayed interest:@@@" + my_messageSerializer.my_interests.size());
        List<RatingPOJ> ratingPOJS = GlobalApp.dbHelper.getRatings();
        my_messageSerializer.ratingPOJList = ratingPOJS;
        String mode_type = SharedPreferencesHandler.getStringPreferences(getApplicationContext(), Setting.MODE_SELECTION);
        Mode mode;
        if (mode_type.contains(Setting.PUSH) || mode_type.trim().length() == 0) {
            mode = new Mode(Setting.PUSH);
        } else {
            mode = new Mode(Setting.PULL);
            mode.lat_lon = SharedPreferencesHandler.getStringPreferences(getApplicationContext(), Setting.LAT_LON_KEY);
            mode.radius = SharedPreferencesHandler.getRadiusPreferences(getApplicationContext(), Setting.RADIUS);
            String[] pull_tags = SharedPreferencesHandler.getStringPreferences(getApplicationContext(), Setting.TAG_KEYS).split(",");
            Log.d(TAG, "pull tags:" + SharedPreferencesHandler.getStringPreferences(getApplicationContext(), Setting.TAG_KEYS) + " - " + pull_tags[0]);
            List<Interest> new_interest = new ArrayList<>();
            if (pull_tags != null && pull_tags.length > 0) {
                for (String pull_interest : pull_tags) {
                    pull_interest = pull_interest.trim();
                    for (Interest nor_interest : my_interests) {
                        if (pull_interest.equals(nor_interest.getInterest())) {
                            new_interest.add(nor_interest);
                            Log.d(TAG, "Found interest of pull");
                        }
                    }

                }
            }
            my_messageSerializer.my_interests = new_interest;
        }
        my_messageSerializer.mode_type = mode;
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            Log.d(TAG, "Server");
            Log.d(TAG, "Size of decayed interest:%%%" + my_messageSerializer.my_interests.size());
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
        Log.d(TAG, "Size of decayed interest:" + my_messageSerializer_temp.my_interests.size());
        return my_messageSerializer_temp;
    }

    @Override
    public void setMessageSerializer(MessageSerializer msg) {
        Log.d(TAG, "Size of decayed interest:" + msg.my_interests.size());
        my_messageSerializer_temp = msg;
    }

    @Override
    public void notifyComplete() {
        disconnect();
        deletePersistentGroups();
        manager.clearLocalServices(channel, null);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        my_device = null;
        wifiManager.setWifiEnabled(true);

    }

    @Override
    public void notifyCompleteClient() {
        disconnect();
        deletePersistentGroups();
        manager.clearLocalServices(channel, null);
        manager.clearLocalServices(channel, null);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        my_device = null;
        wifiManager.setWifiEnabled(true);
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void peersHaveChanged(WifiP2pDevice device) {
        if (my_device == null) {
            my_device = device;
            if (wifip2p_enabled == true) {
                wifip2p_enabled = false;
                threestepstodiscovery();
            }
        }
        my_device = device;
    }

    @Override
    public void checkP2pEnabled(boolean flag) {
        if (wifip2p_enabled == false) {
            if (my_device != null) {
                threestepstodiscovery();
            }
        }
        wifip2p_enabled = true;

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
            }
            return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Inside onstartcommand");
        return START_NOT_STICKY;
    }
}
