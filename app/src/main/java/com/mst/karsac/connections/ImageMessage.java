package com.mst.karsac.connections;

import com.mst.karsac.messages.Messages;
import com.mst.karsac.ratings.MessageRatings;

import java.io.Serializable;
import java.util.List;

public class ImageMessage implements Serializable{
    public Messages messages;
    public String img_path;
    public List<MessageRatings> messageRatings;

    public ImageMessage(Messages messages, String img_path, List<MessageRatings> messageRatings) {
        this.messages = messages;
        this.img_path = img_path;
        this.messageRatings = messageRatings;
    }
}
