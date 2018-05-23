package com.mst.karsac.Roger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mst.karsac.DbHelper.DbHelper;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.R;
import com.mst.karsac.Utils.SharedPreferencesHandler;
import com.mst.karsac.connections.ImageMessage;
import com.mst.karsac.messages.Messages;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RogerActivity extends AppCompatActivity {
    public static final int PORT = 8080;
    public static final int SOCKET_TIMEOUT = 5000;

    EditText editText_IP;
    Button sendtoIp_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roger);

        editText_IP = findViewById(R.id.ip);
        sendtoIp_btn = findViewById(R.id.sendtoip_btn);

        String last_ip_address = SharedPreferencesHandler.getStringPreferences(getApplicationContext(), SharedPreferencesHandler.LAST_IPADDRESS);
        if(last_ip_address.length() > 0){
            editText_IP.setText(last_ip_address);
        }
        sendtoIp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ipAddress = editText_IP.getText().toString();
                boolean checkIp = validIP(ipAddress);
                if (checkIp == false)
                    Toast.makeText(getApplicationContext(), "Enter a valid IP address!", Toast.LENGTH_SHORT).show();
                else {
                    ArrayList<ImageMessage> imageMessages = getAllImages();
                    RogerIp rogerObject = new RogerIp(GlobalApp.source_mac, imageMessages);
                    IPAsyncTask ipAsyncTask = new IPAsyncTask(ipAddress, rogerObject);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        ipAsyncTask.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR, new Void[]{null});
                    } else
                        ipAsyncTask.execute();
                }

            }
        });
    }

    private ArrayList<ImageMessage> getAllImages() {
        ArrayList<ImageMessage> imageMessages = new ArrayList<>();
        DbHelper dbHelper = GlobalApp.dbHelper;
        List<Messages> my_msgs = dbHelper.getAllMessages(0);
        List<Messages> received_msgs = dbHelper.getAllMessages(1);
        my_msgs.addAll(received_msgs);
        for(Messages msg : my_msgs){
            String img_string = encodeImageBase64(msg.imgPath);
            ImageMessage imageMessage = new ImageMessage(msg, img_string);
            imageMessages.add(imageMessage);
        }
        return imageMessages;
    }

    private String encodeImageBase64(String imgPath) {
        String img_string = null;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // In case you want to compress your image, here it's at 40%
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
           img_string = new String(Base64.encodeBase64(byteArray));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return img_string;
    }

    public static boolean validIP(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }

            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            for (String s : parts) {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }
            if (ip.endsWith(".")) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    class IPAsyncTask extends AsyncTask<Void, Void, String> {
        String serverAddress;
        RogerIp rogerIp;

        public IPAsyncTask(String ipAddress, RogerIp rogerIp) {
            this.serverAddress = ipAddress;
            this.rogerIp = rogerIp;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Socket socket = null;
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(serverAddress, PORT), SOCKET_TIMEOUT);
                SharedPreferencesHandler.setStringPreferences(getApplicationContext(), SharedPreferencesHandler.LAST_IPADDRESS, serverAddress);
                Log.d("RogerActivity", "Client socket - " + socket.isConnected());
                OutputStream outputStream = socket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(rogerIp);
                objectOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                return "Failure";
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.contains("Success")) {
                Toast.makeText(getApplicationContext(), "Successfully transferred!", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getApplicationContext(), "Transfer failed!", Toast.LENGTH_SHORT).show();
        }
    }
}
