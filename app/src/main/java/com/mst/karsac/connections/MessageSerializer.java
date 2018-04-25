package com.mst.karsac.connections;

import com.mst.karsac.interest.Interest;

import java.io.Serializable;
import java.net.ServerSocket;
import java.util.List;

public class MessageSerializer implements Serializable {
    public static final String INTEREST_MODE = "interest_mode";
    public static final String MESSAGE_MODE = "message";

    List<Interest> my_interests;
    String mode;

    List<ImageMessage> my_mesages;

    public MessageSerializer(List<Interest> my_interests, String mode) {
        this.my_interests = my_interests;
        this.mode = mode;
    }

    public MessageSerializer(String mode, List<ImageMessage> my_mesages) {
        this.mode = mode;
        this.my_mesages = my_mesages;
    }
}
