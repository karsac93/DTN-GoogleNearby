package com.mst.karsac.connections;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.mst.karsac.interest.Interest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ServerIntentService extends IntentService {

    public static final String TAG = ServerIntentService.class.getSimpleName();
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String ROLE = "role";
    public static final String INTEREST_MSG = "interest_msg";


    public ServerIntentService() {
        super("ServerIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.getAction().equals(ACTION_SEND_FILE)) {
            try {
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(BackgroundService.PORT));
                Socket client = serverSocket.accept();
                Log.d(TAG, "Client's InetAddress:" + client.getInetAddress());
                InetAddress clientAddress = client.getInetAddress();
                ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream is = new ObjectInputStream(client.getInputStream());
                Log.d(TAG, "Read the object");
                MessageExchange msg = (MessageExchange) is.readObject();

                ArrayList<Interest> interests = msg.getInterestArrayList();
                Log.d(TAG, "" + interests.size());
                for (Interest interest : interests) {
                    Log.d(TAG, interest.getInterest());
                }

                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    String role = bundle.getString(ROLE);
                    if (role == BackgroundService.OWNER) {
                        MessageExchange msg_serializable = (MessageExchange) bundle.getSerializable(INTEREST_MSG);
                        try {
                            Log.d(TAG, "Inside client");
                            Socket socket = new Socket();
                            try {
                                socket.connect(new InetSocketAddress(clientAddress, BackgroundService.PORT), BackgroundService.PORT);
                                OutputStream stream = socket.getOutputStream();
                                ObjectOutputStream oos = new ObjectOutputStream(stream);
                                MessageExchange temp = msg_serializable;
                                Log.d(TAG, "Client: " + temp.getInterestArrayList().get(0).getInterest());
                                oos.writeObject(msg_serializable);
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

                    }
                }
                os.close();
                is.close();
                client.close();
                serverSocket.close();

            } catch (Exception e) {

            }

        }
    }
}
