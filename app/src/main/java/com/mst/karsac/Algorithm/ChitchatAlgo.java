package com.mst.karsac.Algorithm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.mst.karsac.DbHelper.DbHelper;
import com.mst.karsac.GlobalApp;

import com.mst.karsac.connections.ImageMessage;
import com.mst.karsac.connections.MessageSerializer;
import com.mst.karsac.interest.Interest;
import com.mst.karsac.messages.Messages;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.List;

public class ChitchatAlgo {

    DbHelper dbHelper = GlobalApp.dbHelper;

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

    public void growthAlgorithm(List<Interest> obtained_interest, List<Interest> my_self_interests) {
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

    public MessageSerializer RoutingProtocol(List<Interest> obtained_interest, List<Interest> my_interest, String recevied_mac) {
        List<ImageMessage> imageList = new ArrayList<>();
        List<Messages> my_self_Messages = dbHelper.getAllMessages(0);
        List<Messages> my_transient_messages = dbHelper.getAllMessages(1);
        my_self_Messages.addAll(my_transient_messages);
        for (Messages my_msg : my_self_Messages) {
            boolean flag = checkValid(my_msg, recevied_mac);
            if(flag == false) {
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
                            Log.d("Chitchat", tags + " - " + obtained);
                            neighbor_value = neighbor_value + obtained.getValue();
                        }
                    }
                }
                Log.d("chitchat", "Comparison of tag values:" + my_value + " - " + neighbor_value);
                if (neighbor_value >= my_value) {
                    String msg_string = getBase64String(my_msg.imgPath);
                    ImageMessage img_exchange = new ImageMessage(my_msg, msg_string);
                    imageList.add(img_exchange);
                }
            }
        }
        MessageSerializer final_messages = new MessageSerializer(MessageSerializer.MESSAGE_MODE, imageList);
        return final_messages;

    }

    private String getBase64String(String filePath) {
        String img_string = null;
        // give your image file url in mCurrentPhotoPath
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // In case you want to compress your image, here it's at 40%
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            img_string = Base64.encodeToString(byteArray, Base64.DEFAULT);
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
        } else {
            if (msg.destAddr != null && msg.destAddr.length() > 0) {
                String[] intermediaries = msg.destAddr.split("|");
                if(intermediaries != null && intermediaries.length > 0){
                    boolean flag1 = false;
                    for(String inter_mac : intermediaries){
                        if(inter_mac.equals(received_mac)){
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

}
