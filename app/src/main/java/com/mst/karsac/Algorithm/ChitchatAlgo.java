package com.mst.karsac.Algorithm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Base64;
import android.util.Log;

import com.mst.karsac.DbHelper.DbHelper;
import com.mst.karsac.GlobalApp;

import com.mst.karsac.Settings.Setting;
import com.mst.karsac.Utils.SharedPreferencesHandler;
import com.mst.karsac.connections.ImageMessage;
import com.mst.karsac.connections.MessageSerializer;
import com.mst.karsac.connections.Mode;
import com.mst.karsac.interest.Interest;
import com.mst.karsac.messages.Messages;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ChitchatAlgo {

    DbHelper dbHelper = GlobalApp.dbHelper;
    public static final String TAG = ChitchatAlgo.class.getSimpleName();
    int incentive_obtained;
    int totalTobeSent = 0;
    public static int present_incentive;
    public static ArrayList<Messages> toBeUpdated = new ArrayList<>();

    public List<Interest> decayingFunction(int timestamp) {

        List<Interest> self_interest;
        List<Interest> transient_interest;
        self_interest = dbHelper.getInterests(GlobalApp.SELF_INTEREST);
        transient_interest = dbHelper.getInterests(GlobalApp.TRANSIENT_INTEREST);
        self_interest = decayFormula(self_interest, GlobalApp.SELF_INTEREST, timestamp);
        transient_interest = decayFormula(transient_interest, GlobalApp.TRANSIENT_INTEREST, timestamp);
        self_interest.addAll(transient_interest);
        return self_interest;
    }

    private List<Interest> decayFormula(List<Interest> interests, int type, int timestamp) {
        if (type == GlobalApp.TRANSIENT_INTEREST) {
            for (Interest interest : interests) {
                float value = interest.getValue() / (timestamp - interest.getTimestamp());
                interest.setValue(value);
            }
        } else {
            for (Interest interest : interests) {
                float value = ((interest.getValue() - 0.5f) / (timestamp - interest.getTimestamp())) + 0.5f;
                interest.setValue(value);
            }
        }
        return interests;
    }

    public static int getPresent_incentive() {
        return present_incentive;
    }

    public static ArrayList<Messages> getToBeUpdated() {
        return toBeUpdated;
    }

    public void growthAlgorithm(List<Interest> obtained_interest, List<Interest> my_self_interests) {
        Log.d("Chitchat", my_self_interests.size() + " - ////////Size of my interest ");
        boolean flag;
        for (Interest obtained : obtained_interest) {
            flag = false;
            for (Interest my_interest : my_self_interests) {
                if (obtained.getInterest().equals(my_interest.getInterest())) {
                    flag = true;
                    if (obtained.getType() == 0 && my_interest.getType() == 0) {
                        float value = my_interest.getValue() + obtained.getValue();
                        if (value > 1)
                            value = 1;
                        dbHelper.updateInterest(my_interest.getInterest(), value, my_interest.getType());
                    } else if (obtained.getType() == 1 && my_interest.getType() == 0) {
                        float value = my_interest.getValue() + (obtained.getValue() / 2.0f);
                        if (value > 1)
                            value = 1;
                        dbHelper.updateInterest(my_interest.getInterest(), value, my_interest.getType());
                    } else if (obtained.getType() == 0 && my_interest.getType() == 1) {
                        float value = my_interest.getValue() + (obtained.getValue() / 3.0f);
                        if (value > 1)
                            value = 1;
                        dbHelper.updateInterest(my_interest.getInterest(), value, my_interest.getType());
                    } else if (obtained.getType() == 1 && my_interest.getType() == 1) {
                        float value = my_interest.getValue() + (obtained.getValue() / 4.0f);
                        if (value > 1)
                            value = 1;
                        dbHelper.updateInterest(my_interest.getInterest(), value, my_interest.getType());
                    }
                }
            }
            if (flag == false) {
                if (obtained.getType() == 0) {
                    float value = obtained.getValue() / 5.0f;
                    if (value > 1)
                        value = 1;
                    obtained.setValue(value);
                    dbHelper.insertInterest(obtained.getInterest(), 1, value);
                } else if (obtained.getType() == 1) {
                    float value = obtained.getValue() / 6.0f;
                    if (value > 1)
                        value = 1;
                    obtained.setValue(value);
                    dbHelper.insertInterest(obtained.getInterest(), 1, value);
                }
            }
        }

    }

    public MessageSerializer RoutingProtocol(List<Interest> obtained_interest, List<Interest> my_interest, String recevied_mac, Mode mode_type, HashMap<String, String> msgUUIDList, int incentive, Context context) {
        Log.d(TAG, "Incentive obtained:" + incentive);
        toBeUpdated.clear();
        present_incentive = SharedPreferencesHandler.getIncentive(context, Setting.INCENTIVE);
        incentive_obtained = incentive;
        totalTobeSent = incentive;
        List<ImageMessage> imageList;
        List<MessageClassification> messageClassifications = new ArrayList<>();
        List<Messages> my_self_Messages = dbHelper.getAllMessages(0);
        List<Messages> my_transient_messages = dbHelper.getAllMessages(1);
        my_self_Messages.addAll(my_transient_messages);
        for (Messages my_msg : my_self_Messages) {
            boolean check_exists = false;
            for (String receiver_UUID : msgUUIDList.keySet()) {
                if (my_msg.uuid.contains(receiver_UUID)) {
                    check_exists = true;
                    Log.d(TAG, "this message is already present with the sender:" + receiver_UUID + " Message UUID:" + my_msg.uuid);
                    Log.d(TAG, "Checking the difference in tags");
                    String my_tags = my_msg.tagsForCurrentImg;
                    String received_tags = msgUUIDList.get(receiver_UUID);
                    List<String> my_tag_list = new ArrayList<>(Arrays.asList(my_tags.split(",")));
                    List<String> received_tags_list = new ArrayList<>(Arrays.asList(received_tags.split(",")));
                    List<String> new_values = new ArrayList<>();
                    for(String each_my_tag : my_tag_list){
                        if(!received_tags_list.contains(each_my_tag)){
                            new_values.add(each_my_tag);
                        }
                    }
                    received_tags_list.addAll(new_values);
                    StringBuilder builder = new StringBuilder();
                    for(String final_tag : received_tags_list){
                        builder.append(final_tag).append(",");
                    }
                    String final_tags = "";
                    if(builder.length()>0){
                        final_tags = builder.substring(0, builder.length()-1).toString();
                    }
                    msgUUIDList.put(receiver_UUID, final_tags);
                    break;
                }
            }
            if (check_exists == false) {
                boolean flag = checkValid(my_msg, recevied_mac);
                if (flag == false) {
                    boolean inter_direct = true;
                    String[] msg_tags = my_msg.getTagsForCurrentImg().split(",");
                    float my_value = 0.0f;
                    float neighbor_value = 0.0f;
                    for (String tags : msg_tags) {
                        tags = tags.trim();
                        for (Interest interest : my_interest) {
                            Log.d("Chitchat", tags + " - " + interest);
                            if (tags.equals(interest.getInterest())) {
                                my_value = my_value + interest.getValue();
                            }
                        }
                        for (Interest obtained : obtained_interest) {
                            if (tags.equals(obtained.getInterest())) {
                                if (obtained.getType() == 0)
                                    flag = true;
                                Log.d("Chitchat", tags + " - " + obtained);
                                neighbor_value = neighbor_value + obtained.getValue();
                            }
                        }
                    }
                    Log.d("chitchat", "Comparison of tag values:" + my_value + " - " + neighbor_value);
                    if (neighbor_value != 0.0f && my_value != 0.0f) {
                        Log.d("Chitchat", mode_type.mode);
                        if(neighbor_value < 0.5f)
                            inter_direct = false;
                        if (mode_type.mode.contains(Setting.PULL) && mode_type.lat_lon != null &&
                                mode_type.lat_lon.trim().length() > 0 && mode_type.lat_lon.contains(",")) {
                            String[] latlng = mode_type.lat_lon.split(",");
                            double pull_lat = Double.parseDouble(latlng[0].trim());
                            double pull_lon = Double.parseDouble(latlng[1].trim());
                            float[] results = new float[1];
                            Location.distanceBetween(pull_lat, pull_lon, my_msg.lat, my_msg.lon, results);
                            float distance_in_miles = results[0];
                            boolean is_Within_radius = distance_in_miles < (mode_type.radius * 1500.0f);
                            Log.d("chitchat", "Pull condition:" + is_Within_radius);
                            if (is_Within_radius) {
                                Log.d("chitchat", "Pull condition statisfied, hence adding to the list");
                                MessageClassification messageClassification = new MessageClassification(my_msg, inter_direct);
                                messageClassifications.add(messageClassification);
                            }
                        } else {
                            MessageClassification messageClassification = new MessageClassification(my_msg, inter_direct);
                            messageClassifications.add(messageClassification);
                        }
                    }
                }
            }
        }

        imageList = selectMessagesToSend(messageClassifications, recevied_mac, context);
        Log.d(TAG, "Size of messages to be sent:" + imageList.size());
        MessageSerializer final_messages = new MessageSerializer(MessageSerializer.MESSAGE_MODE, imageList, (totalTobeSent - incentive_obtained));
        final_messages.msgUUIDList = msgUUIDList;
        return final_messages;

    }

    private List<ImageMessage> selectMessagesToSend(List<MessageClassification> messageClassifications, String recevied_mac, Context context) {
        int incentive_promised = 30 - getRandomNumber();
        List<ImageMessage> imageList = new ArrayList<>();
        for (MessageClassification messageClassification : messageClassifications) {
            int temp_incen = incentive_obtained;
            if (messageClassification.type == true) {
                Messages my_msg = messageClassification.messages;
                if (my_msg.incentive_promised != 0) {
                    incentive_obtained = incentive_obtained - my_msg.incentive_promised;
                    my_msg.incentive_promised = 0;
                }
                else {
                    incentive_obtained = incentive_obtained - 30;
                }
                if (incentive_obtained > 0) {
                    Log.d(TAG, "Calculating incentive for direct case:" + (temp_incen - incentive_obtained));
                    handleMyIncentive((temp_incen - incentive_obtained), context);
                    my_msg.destAddr = GlobalApp.source_mac + "|" + recevied_mac + "|";
                    my_msg.incentive_received = my_msg.incentive_received + temp_incen - incentive_obtained;
                    //GlobalApp.dbHelper.updateMsg(my_msg);
                    toBeUpdated.add(my_msg);
                    Messages msg_send = (Messages) deepCopy(my_msg);
                    msg_send.incentive_received = 0;
                    msg_send.incentive_paid = temp_incen - incentive_obtained;
                    String msg_string = getBase64String(msg_send.imgPath);
                    ImageMessage img_exchange = new ImageMessage(msg_send, msg_string);
                    imageList.add(img_exchange);
                }
                else
                    incentive_obtained = temp_incen;

            }
        }
        for (MessageClassification messageClassification : messageClassifications) {
            if (messageClassification.type == false && messageClassification.messages.incentive_promised == 0) {
                Log.d(TAG, "Calculating incentive for indirect case");
                Messages my_msg = messageClassification.messages;
                my_msg.destAddr = GlobalApp.source_mac + "|" + recevied_mac + "|";
                //GlobalApp.dbHelper.updateMsg(my_msg);
                toBeUpdated.add(my_msg);
                Messages msg_send = (Messages) deepCopy(my_msg);
                msg_send.incentive_promised = incentive_promised;
                String msg_string = getBase64String(msg_send.imgPath);
                ImageMessage img_exchange = new ImageMessage(msg_send, msg_string);
                imageList.add(img_exchange);
            }
        }
        return imageList;

    }

    public int getRandomNumber(){
        int max = 10, min = 1;
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    private void handleMyIncentive(int incentive_add, Context context) {
        Log.d(TAG, "handleMyIncentive");
        present_incentive = present_incentive + incentive_add;
        Log.d(TAG, "incentive being added:" + present_incentive);
        SharedPreferencesHandler.setIntPreference(context, Setting.INCENTIVE, present_incentive);
        Log.d(TAG, " " + SharedPreferencesHandler.getIncentive(context, Setting.INCENTIVE));
    }

    public String getBase64String(String filePath) {
        String img_string = null;
        // give your image file url in mCurrentPhotoPath
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // In case you want to compress your image, here it's at 40%
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            img_string = Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Chitchat", "Imgstring:" + img_string);
        return img_string;
    }


    public boolean checkValid(Messages msg, String received_mac) {
        Log.d("CHITCHAT", "Message name and inside checkvalid to send the image:" + msg.fileName);
        Log.d("CHITCHAT", "SourceMac:" + msg.sourceMac + " Received mac:" + received_mac);
        boolean flag = false;
        if (msg.sourceMac.equals(received_mac)) {
            flag = true;
            return flag;
        } else {
            Log.d("Chitchat", "Inside valid checking:" + msg.destAddr);
            if (msg.destAddr != null && msg.destAddr.length() > 0) {
                String[] intermediaries = msg.destAddr.split("\\|");
                Log.d("chitchat", "size:" + intermediaries.length);
                if (intermediaries != null && intermediaries.length > 0) {
                    boolean flag1 = false;
                    for (String inter_mac : intermediaries) {
                        Log.d("ChitchatAlgo", inter_mac);
                        if (inter_mac.equals(received_mac)) {
                            flag1 = true;
                            break;
                        }
                    }
                    if (flag1)
                        flag = true;
                }

            }
        }
        Log.d("CHITAHCAT", "Checkvalid conclusion:" + flag);
        return flag;
    }

    public Object deepCopy(Object input) {

        Object output = null;
        try {
            // Writes the object
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(input);

            // Reads the object
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            output = objectInputStream.readObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

}
