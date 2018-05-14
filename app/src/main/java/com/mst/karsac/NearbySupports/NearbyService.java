package com.mst.karsac.NearbySupports;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.mst.karsac.Algorithm.ChitchatAlgo;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.Settings.Setting;
import com.mst.karsac.Utils.SharedPreferencesHandler;
import com.mst.karsac.connections.ImageMessage;
import com.mst.karsac.connections.MessageSerializer;
import com.mst.karsac.connections.Mode;
import com.mst.karsac.interest.Interest;
import com.mst.karsac.messages.Messages;
import com.mst.karsac.ratings.RatingPOJ;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NearbyService extends Service {

    public static final String TAG = "NearbyService";
    public static final String SERVICE_ID = "_wifidemo";
    List<Interest> my_interests = new ArrayList<>();
    public static String lastdeviceEndpoint = "";
    static boolean check_connected = false;
    ArrayList<String> endpointsConnected = new ArrayList<>();

    private final ConnectionLifecycleCallback mConnectionLifeCycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(final String endpointId, ConnectionInfo connectionInfo) {
            Log.d(TAG, connectionInfo.getEndpointName());
            check_connected = true;
            String endpointName = connectionInfo.getEndpointName();
            if(connectionInfo.isIncomingConnection()){
                Log.d(TAG, "Printing id:" + connectionInfo.getAuthenticationToken() + " EndPointName:" + connectionInfo.getEndpointName());
            }
            if (endpointName.length() == 36) {
                Log.d(TAG, "old device and connected device:" + lastdeviceEndpoint + " new device:" + connectionInfo.getEndpointName());
                Log.d(TAG, "Accepting connection");

                discoveryHandler.removeCallbacks(discoverRunnable);
                advertiserHandler.removeCallbacks(advertiserRunnable);
                Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(endpointId, mPayloadCallback);
                String text = "starting file transfer!";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            } else {
                Nearby.getConnectionsClient(getApplicationContext()).startAdvertising(
                        GlobalApp.source_mac, SERVICE_ID, mConnectionLifeCycleCallback, new AdvertisingOptions(Strategy.P2P_STAR));
                advertiserHandler.postDelayed(advertiserRunnable, 20000);
                Nearby.getConnectionsClient(getApplicationContext()).startDiscovery(SERVICE_ID, mEndpointDiscoveryCallback, new DiscoveryOptions(Strategy.P2P_STAR));
                discoveryHandler.postDelayed(discoverRunnable, 20000);
                Log.d(TAG, "This is the last connected device:" + lastdeviceEndpoint + " new device:" + connectionInfo.getEndpointName());
            }
        }


        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            switch (result.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    Log.d(TAG, "Connection Ok");
                    try {
                        sendInterestData(endpointId);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    Log.d(TAG, "Connection Failed");
                    lastdeviceEndpoint = "";
                    break;
                default:
                    Log.d(TAG, "Connection broken");
                    lastdeviceEndpoint = "";
                    endpointsConnected.clear();
                    Nearby.getConnectionsClient(getApplicationContext()).startAdvertising(
                            GlobalApp.source_mac, SERVICE_ID, mConnectionLifeCycleCallback, new AdvertisingOptions(Strategy.P2P_STAR));
                    advertiserHandler.postDelayed(advertiserRunnable, 20000);
                    Nearby.getConnectionsClient(getApplicationContext()).startDiscovery(SERVICE_ID, mEndpointDiscoveryCallback, new DiscoveryOptions(Strategy.P2P_STAR));
                    discoveryHandler.postDelayed(discoverRunnable, 20000);
            }

        }

        @Override
        public void onDisconnected(String endpointId) {
            Log.d(TAG, "Disconnected");
            Nearby.getConnectionsClient(getApplicationContext()).startAdvertising(
                    GlobalApp.source_mac, SERVICE_ID, mConnectionLifeCycleCallback, new AdvertisingOptions(Strategy.P2P_STAR));
            advertiserHandler.postDelayed(advertiserRunnable, 20000);
            Nearby.getConnectionsClient(getApplicationContext()).startDiscovery(SERVICE_ID, mEndpointDiscoveryCallback, new DiscoveryOptions(Strategy.P2P_STAR));
            discoveryHandler.postDelayed(discoverRunnable, 20000);
        }
    };




    private void sendInterestData(String endpointId) throws IOException {
        MessageSerializer my_messageSerializer;
        Log.d(TAG, "inside SendData method");
        int init_time = SharedPreferencesHandler.getIntPreferences(getApplicationContext(), GlobalApp.TIMESTAMP);
        SharedPreferencesHandler.setIntPreference(getApplicationContext(), GlobalApp.TIMESTAMP, init_time + 1);
        my_interests = new ChitchatAlgo().decayingFunction(SharedPreferencesHandler.getIntPreferences(this, GlobalApp.TIMESTAMP));
        List<String> uuidList = GlobalApp.dbHelper.getMsgUUID();
        Log.d(TAG, "Size of decayed interest:---" + my_interests.size());
        my_messageSerializer = new MessageSerializer(my_interests, MessageSerializer.INTEREST_MODE);
        my_messageSerializer.msgUUIDList = uuidList;
        my_messageSerializer.incentive = SharedPreferencesHandler.getIncentive(this, Setting.INCENTIVE);
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
        try {
            byte[] interestBytes = new SerializationHelper().serialize(my_messageSerializer);
            sendPayload(endpointId, interestBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendPayload(String endpointId, byte[] bytes) {
        Payload payload = Payload.fromBytes(bytes);
        Nearby.getConnectionsClient(getApplicationContext()).sendPayload(endpointId, payload);
    }


    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    // A new payload is being sent over.
                    Log.d(TAG, "Payload Received");
                    MessageSerializer incomingMsg = null;
                    if (payload.getType() == Payload.Type.BYTES) {
                        byte[] incoming_bytes = payload.asBytes();
                        try {
                            incomingMsg = (MessageSerializer) new SerializationHelper().deserialize(incoming_bytes);
                            switch (incomingMsg.mode) {
                                case MessageSerializer.INTEREST_MODE:
                                    sendImageMessages(incomingMsg, endpointId);
                                    break;
                                case MessageSerializer.RECEIVED_MODE:
                                    Toast.makeText(getApplicationContext(), "File transfer complete!", Toast.LENGTH_SHORT).show();
                                    Nearby.getConnectionsClient(getApplicationContext()).disconnectFromEndpoint(endpointId);
                                    //Nearby.getConnectionsClient(getApplicationContext()).stopAllEndpoints();
                                    Nearby.getConnectionsClient(getApplicationContext()).startAdvertising(
                                            GlobalApp.source_mac, SERVICE_ID, mConnectionLifeCycleCallback, new AdvertisingOptions(Strategy.P2P_STAR));
                                    advertiserHandler.postDelayed(advertiserRunnable, 20000);
                                    Nearby.getConnectionsClient(getApplicationContext()).startDiscovery(SERVICE_ID, mEndpointDiscoveryCallback, new DiscoveryOptions(Strategy.P2P_STAR));
                                    discoveryHandler.postDelayed(discoverRunnable, 20000);

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    if (payload.getType() == Payload.Type.STREAM) {
                        Log.d(TAG, "Inside file");
                        InputStream inputstream = payload.asStream().asInputStream();
                        try {
                            ObjectInputStream ois = new ObjectInputStream(inputstream);
                            incomingMsg = (MessageSerializer) ois.readObject();
                            Log.d(TAG, "Message Mode:" + incomingMsg.mode);
                            handleImages(incomingMsg);
                            MessageSerializer messageSerializer = new MessageSerializer(MessageSerializer.RECEIVED_MODE);
                            byte[] bytes = new SerializationHelper().serialize(messageSerializer);
                            sendPayload(endpointId, bytes);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    // Payload progress has updated.
                }
            };

    private void handleImages(MessageSerializer incomingMsg) {
        List<ImageMessage> received_msgs = incomingMsg.my_mesages;
        Log.d(TAG, "Amount of incentive spent:" + incomingMsg.incentive);
        int tobeAdded = SharedPreferencesHandler.getIncentive(getApplicationContext(), Setting.INCENTIVE) - incomingMsg.incentive;
        SharedPreferencesHandler.setIntPreference(getApplicationContext(), Setting.INCENTIVE, tobeAdded);
        UpdateDbandSetImage(received_msgs);
    }

    private void UpdateDbandSetImage(List<ImageMessage> received_msgs) {
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "DTN-Images");
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }
        for (ImageMessage imageMessage : received_msgs) {
            Messages img_msg = imageMessage.messages;
            Log.d(TAG, "obtained file name:" + img_msg.fileName);
            File image = new File(imagesFolder, img_msg.fileName);
            decodeBase64String(imageMessage.img_path, image);
            img_msg.imgPath = image.getAbsolutePath();
            img_msg.type = 1;
            GlobalApp.dbHelper.insertImageRecord(img_msg);
        }
    }

    public void decodeBase64String(String img_string, File image) {
        try (FileOutputStream imageOutFile = new FileOutputStream(image)) {
            // Converting a Base64 String into Image byte array
            byte[] imageByteArray = Base64.decode(img_string, Base64.DEFAULT);
            Log.d(TAG, "Print the received img:" + imageByteArray);
            imageOutFile.write(imageByteArray);
        } catch (FileNotFoundException e) {
            System.out.println("Image not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the Image " + ioe);
        }
    }

    private void sendImageMessages(MessageSerializer interestMsg, String endpointId) {
        new ChitchatAlgo().growthAlgorithm(interestMsg.my_interests, my_interests);
        handleRatings(interestMsg.ratingPOJList);
        MessageSerializer imageTransfer = new ChitchatAlgo().RoutingProtocol
                (interestMsg.my_interests, my_interests, interestMsg.my_macaddress,
                        interestMsg.mode_type, interestMsg.msgUUIDList, interestMsg.incentive, getApplicationContext());
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(imageTransfer);
            oos.flush();
            oos.close();
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            Nearby.getConnectionsClient(this).sendPayload(endpointId, Payload.fromStream(is));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void handleRatings(List<RatingPOJ> ratingPOJList) {
        List<RatingPOJ> myPOJList = GlobalApp.dbHelper.getRatings();
        for (RatingPOJ receivedPoj : ratingPOJList) {
            for (RatingPOJ myPOJ : myPOJList) {
                if (receivedPoj.mac_address.contains(myPOJ.mac_address)) {
                    receivedPoj.average = (receivedPoj.average + myPOJ.average) / 2.0f;
                    break;
                }
            }
        }
        for (RatingPOJ receivedPoj : ratingPOJList) {
            if (!receivedPoj.mac_address.contains(GlobalApp.source_mac)) {
                GlobalApp.dbHelper.insertOrUpdateRating(receivedPoj);
            }
        }
    }

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    Log.d(TAG, "Endpoint found" + endpointId + " discovered endPoint info:" + discoveredEndpointInfo.getEndpointName());
                    String endpointName = discoveredEndpointInfo.getEndpointName();
                    String service_id = discoveredEndpointInfo.getServiceId();
                    Log.d(TAG, "service_id:" + discoveredEndpointInfo.getServiceId());
                    boolean flag1 = false;
                    if (service_id.contains(SERVICE_ID)) {
                        for (String previousEndpoints : endpointsConnected) {
                            if (previousEndpoints.equals(endpointName)) {
                                flag1 = true;
                                break;
                            }
                        }
                        if (flag1 == false) {
                            endpointsConnected.add(endpointName);
                            boolean flag = false;
                            for (int i = 0; i < endpointName.length(); i++) {
                                int own = (int) GlobalApp.source_mac.charAt(i);
                                int received = (int) endpointName.charAt(i);
                                if (own != received) {
                                    if (own > received) {
                                        Log.d(TAG, "Initiating the connection");
                                        flag = true;
                                    }
                                    break;
                                }
                            }
                            if (flag)
                                startConnection(endpointId, discoveredEndpointInfo);
                            else
                                Log.d(TAG, "Waiting for other device to connect");
                        }
                        else{
                            Log.d(TAG, "Previously connected");
                        }
                    }
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // A previously discovered endpoint has gone away.
                    Log.d(TAG, "Endpoint found lost");
                    lastdeviceEndpoint = "";
                    Nearby.getConnectionsClient(getApplicationContext()).startAdvertising(
                            GlobalApp.source_mac, SERVICE_ID, mConnectionLifeCycleCallback, new AdvertisingOptions(Strategy.P2P_STAR));
                    advertiserHandler.postDelayed(advertiserRunnable, 20000);
                    Nearby.getConnectionsClient(getApplicationContext()).startDiscovery(SERVICE_ID, mEndpointDiscoveryCallback, new DiscoveryOptions(Strategy.P2P_STAR));
                    discoveryHandler.postDelayed(discoverRunnable, 20000);
                }
            };

    private void startConnection(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
        discoveryHandler.removeCallbacks(discoverRunnable);
        advertiserHandler.removeCallbacks(advertiserRunnable);
        check_connected = false;
        connectionHandler.postDelayed(checkConnectionRunnable, 20000);
        Nearby.getConnectionsClient(this).stopDiscovery();
        Nearby.getConnectionsClient(this).stopAdvertising();
        Log.d(TAG, "Starting connection!");
        Nearby.getConnectionsClient(this).requestConnection(discoveredEndpointInfo.getEndpointName(),
                endpointId, mConnectionLifeCycleCallback);
    }


    Handler connectionHandler = new Handler();
    Runnable checkConnectionRunnable = new Runnable() {
        @Override
        public void run() {
            if(check_connected == false){
                advertiserHandler.postDelayed(advertiserRunnable, 20000);
                discoveryHandler.postDelayed(discoverRunnable, 20000);
            }
        }
    };


    Handler discoveryHandler = new Handler();
    Runnable discoverRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Inside Runnable searching");
            Nearby.getConnectionsClient(getApplicationContext()).startDiscovery(SERVICE_ID, mEndpointDiscoveryCallback, new DiscoveryOptions(Strategy.P2P_STAR));
            discoveryHandler.postDelayed(discoverRunnable, 20000);
        }
    };

    Handler advertiserHandler = new Handler();
    Runnable advertiserRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Inside advertiser runnable");
            Nearby.getConnectionsClient(getApplicationContext()).startAdvertising(
                    GlobalApp.source_mac, SERVICE_ID, mConnectionLifeCycleCallback, new AdvertisingOptions(Strategy.P2P_STAR));
            advertiserHandler.postDelayed(advertiserRunnable, 20000);
        }
    };

    public NearbyService() {
    }

    @Override
    public void onCreate() {
        Nearby.getConnectionsClient(this).startAdvertising(
                GlobalApp.source_mac, SERVICE_ID, mConnectionLifeCycleCallback, new AdvertisingOptions(Strategy.P2P_STAR));
        advertiserHandler.postDelayed(advertiserRunnable, 20000);
        Nearby.getConnectionsClient(this).startDiscovery(SERVICE_ID, mEndpointDiscoveryCallback, new DiscoveryOptions(Strategy.P2P_STAR));
        discoveryHandler.postDelayed(discoverRunnable, 20000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        discoveryHandler.removeCallbacks(discoverRunnable);
        advertiserHandler.removeCallbacks(advertiserRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


}
