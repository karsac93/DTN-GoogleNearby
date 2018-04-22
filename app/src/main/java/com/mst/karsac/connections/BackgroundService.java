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
import android.os.Bundle;
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

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
    public static final int SOCKET_TIMEOUT = 5000;

    private static WifiP2pManager manager;
    private final IntentFilter intentFilter = new IntentFilter();
    private static WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    public static final String OWNER = "owner";
    public static final String CLIENT = "client";


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
    }

    private void startRegistration() {
        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                final boolean[] flag = {false};
                Map record = new HashMap();
                record.put(TXTRECORD_PROP_AVAILABLE, deviceMac);
                WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo
                        .newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
                manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        manager.setDnsSdResponseListeners(channel, new WifiP2pManager.DnsSdServiceResponseListener() {
                            @Override
                            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice wifiP2pDevice) {
                                Log.d(TAG, "Service Found");
                                flag[0] = true;

                            }
                        }, new WifiP2pManager.DnsSdTxtRecordListener() {
                            @Override
                            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, WifiP2pDevice wifiP2pDevice) {
                                if (flag[0] == true) {
                                    flag[0] = false;
                                    Log.d(TAG, "Service Found");
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

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(int i) {
                                                Log.d(TAG, "discoverPeers Failure");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(int i) {
                                        Log.d(TAG, "addServiceRequest Failure");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(int i) {
                                Log.d(TAG, "clearServiceRequests Failure");
                            }
                        });
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d(TAG, "addLocalService Failure");
                    }
                });
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "clearLocalServices Failure");
            }
        });
    }

    Handler handler = new Handler();

    Runnable handlerTask = new Runnable() {
        @Override
        public void run() {
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

                        }
                    });
                }

                @Override
                public void onFailure(int i) {
                    Log.d(TAG, "discoverPeers Failure");
                }
            });
        }
    };


    private void connectP2p(final WifiP2pDevice wifiP2pDevice) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiP2pDevice.deviceAddress;
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
            Intent intent = new Intent(this, ServerIntentService.class);
            intent.setAction(ServerIntentService.ACTION_SEND_FILE);
            Bundle bundle = new Bundle();
            bundle.putString(ServerIntentService.ROLE, OWNER);
            bundle.putSerializable(ServerIntentService.INTEREST_MSG, messageExchange);
            intent.putExtras(bundle);
            startService(intent);
        } else {
            Log.d(TAG, "CLIENT");

            ClientAsyncTask clientAsyncTask = new ClientAsyncTask(this, wifiP2pInfo.groupOwnerAddress);
            clientAsyncTask.execute(messageExchange);

            Intent intent = new Intent(this, ServerIntentService.class);
            intent.setAction(ServerIntentService.ACTION_SEND_FILE);
            intent.putExtra(ServerIntentService.ROLE, CLIENT);
            startService(intent);
        }

    }

    public static class ClientAsyncTask extends AsyncTask<MessageExchange, String, MessageExchange> {
        Context context;
        String role;
        InetAddress groupOwnerAddress;

        public ClientAsyncTask(Context context, InetAddress groupOwnerAddress) {
            this.context = context;
            this.groupOwnerAddress = groupOwnerAddress;
        }

        @Override
        protected void onPostExecute(MessageExchange msg) {
            Log.d(TAG, "onPostExecute");
        }

        @Override
        protected MessageExchange doInBackground(MessageExchange... messageExchanges) {
            MessageExchange msg = null;
            try {
                Log.d(TAG, "Inside client");
                Socket socket = new Socket();
                try {
                    socket.connect(new InetSocketAddress(groupOwnerAddress, PORT), SOCKET_TIMEOUT);
                    OutputStream stream = socket.getOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(stream);
                    MessageExchange temp = messageExchanges[0];
                    Log.d(TAG, "Client: " + temp.getInterestArrayList().get(0).getInterest());
                    oos.writeObject(messageExchanges[0]);
                    oos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (socket != null) {
                        if (socket.isConnected())
                            socket.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return msg;
        }
    }
}
