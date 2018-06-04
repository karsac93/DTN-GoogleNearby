package com.mst.karsac.Bluetooth;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.mst.karsac.Algorithm.ChitchatAlgo;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.NearbySupports.NearbyService;
import com.mst.karsac.Settings.Setting;
import com.mst.karsac.Utils.SharedPreferencesHandler;
import com.mst.karsac.connections.ImageMessage;
import com.mst.karsac.connections.MessageSerializer;
import com.mst.karsac.connections.Mode;
import com.mst.karsac.interest.Interest;
import com.mst.karsac.messages.Messages;
import com.mst.karsac.ratings.DeviceRating;
import com.mst.karsac.ratings.MessageRatings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BluetoothHelper {

    public static final String TAG = "BluetoothHelper";

    public MessageSerializer sendInterestData(Context context, String macaddress) {
        MessageSerializer my_messageSerializer;
        Log.d(TAG, "inside SendData method");
        int init_time = SharedPreferencesHandler.getIntPreferences(context, GlobalApp.TIMESTAMP);
        SharedPreferencesHandler.setIntPreference(context, GlobalApp.TIMESTAMP, init_time + 1);
        List<Interest> my_interests = new ChitchatAlgo().decayingFunction(SharedPreferencesHandler.getIntPreferences(context, GlobalApp.TIMESTAMP));
        HashMap<String, String> uuidList = GlobalApp.dbHelper.getMsgUUID();
        Log.d(TAG, "Size of decayed interest:---" + my_interests.size());
        my_messageSerializer = new MessageSerializer(my_interests, MessageSerializer.INTEREST_MODE);
        my_messageSerializer.msgUUIDList = uuidList;
        my_messageSerializer.incentive = SharedPreferencesHandler.getIncentive(context, Setting.INCENTIVE);
        my_messageSerializer.deviceRatingList = GlobalApp.dbHelper.getAllDeviceRatings();
        Log.d(TAG, "Size of decayed interest:@@@" + my_messageSerializer.my_interests.size());
        String mode_type = SharedPreferencesHandler.getStringPreferences(context, Setting.MODE_SELECTION);
        Mode mode;
        if (mode_type.contains(Setting.PUSH) || mode_type.trim().length() == 0) {
            mode = new Mode(Setting.PUSH);
        } else {
            mode = new Mode(Setting.PULL);
            mode.lat_lon = SharedPreferencesHandler.getStringPreferences(context, Setting.LAT_LON_KEY);
            mode.radius = SharedPreferencesHandler.getRadiusPreferences(context, Setting.RADIUS);
            String[] pull_tags = SharedPreferencesHandler.getStringPreferences(context, Setting.TAG_KEYS).split(",");
            Log.d(TAG, "pull tags:" + SharedPreferencesHandler.getStringPreferences(context, Setting.TAG_KEYS) + " - " + pull_tags[0]);
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
        my_messageSerializer.my_macaddress = GlobalApp.source_mac;
        my_messageSerializer.bluetoothMac = macaddress;
        return my_messageSerializer;
    }

    public MessageSerializer sendImageMessages(MessageSerializer interestMsg, List<Interest> my_interests, Context context) {
        new ChitchatAlgo().growthAlgorithm(interestMsg.my_interests, my_interests);
        handleDeviceRatings(interestMsg.deviceRatingList);
        MessageSerializer imageTransfer = new ChitchatAlgo().RoutingProtocol
                (interestMsg.my_interests, my_interests, interestMsg.my_macaddress,
                        interestMsg.mode_type, interestMsg.msgUUIDList, interestMsg.incentive, context);
        return imageTransfer;
    }

    private void handleDeviceRatings(List<DeviceRating> deviceRatingList) {
        for (DeviceRating deviceRating : deviceRatingList) {
            if (!deviceRating.getDevice_uuid().contains(GlobalApp.source_mac)) {
                GlobalApp.dbHelper.insertOrUpdateDeviceRatings(deviceRating);
            }
        }
    }

    public void handleImages(MessageSerializer incomingMsg, Context context) {
        List<ImageMessage> received_msgs = incomingMsg.my_mesages;
        Log.d(TAG, "Amount of incentive spent:" + incomingMsg.incentive);
        SharedPreferencesHandler.setIntPreference(context, Setting.INCENTIVE, ChitchatAlgo.present_incentive);
        int tobeAdded = SharedPreferencesHandler.getIncentive(context, Setting.INCENTIVE) - incomingMsg.incentive;
        SharedPreferencesHandler.setIntPreference(context, Setting.INCENTIVE, tobeAdded);
        UpdateDbandSetImage(received_msgs);
        HashMap<String, String> msgUUIDTags = incomingMsg.msgUUIDList;
        updateDBandSetTags(msgUUIDTags);
    }

    public void updateDBandSetTags(HashMap<String, String> msgUUIDTags) {
        for (String msgUUID : msgUUIDTags.keySet()) {
            Log.d(TAG, "Updating modified tags");
            GlobalApp.dbHelper.updateMsgTags(msgUUID, msgUUIDTags.get(msgUUID));
        }
    }

    public void UpdateDbandSetImage(List<ImageMessage> received_msgs) {
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
            List<MessageRatings> messageRatingsList = imageMessage.messageRatings;
            handleRatings(messageRatingsList);
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

    public void handleRatings(List<MessageRatings> messageRatingsList) {
        for (MessageRatings receivedRatings : messageRatingsList) {
            GlobalApp.dbHelper.insertMessageRating(receivedRatings);
        }
    }

}
