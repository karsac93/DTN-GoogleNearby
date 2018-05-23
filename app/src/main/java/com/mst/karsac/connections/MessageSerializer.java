package com.mst.karsac.connections;

import com.mst.karsac.GlobalApp;
import com.mst.karsac.interest.Interest;
import com.mst.karsac.ratings.RatingPOJ;

import java.io.Serializable;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;

public class MessageSerializer implements Serializable {
    public static final String INTEREST_MODE = "interest_mode";
    public static final String MESSAGE_MODE = "message";
    public static final String RECEIVED_MODE = "received";

    public List<Interest> my_interests;
    public List<ImageMessage> my_mesages;
    public String mode;
    public String my_macaddress;
    public Mode mode_type;
    public List<RatingPOJ> ratingPOJList;
    public HashMap<String, String> msgUUIDList;
    public int incentive;

    public MessageSerializer(List<Interest> my_interests, String mode) {
        this.my_interests = my_interests;
        this.mode = mode;
        this.my_macaddress = GlobalApp.source_mac;
    }

    public MessageSerializer(String mode, List<ImageMessage> my_mesages, int incentive) {
        this.incentive = incentive;
        this.mode = mode;
        this.my_mesages = my_mesages;
    }

    public MessageSerializer(String mode){
        this.mode = mode;
    }

    public MessageSerializer() {
    }
}
