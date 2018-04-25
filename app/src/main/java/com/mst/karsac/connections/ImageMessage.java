package com.mst.karsac.connections;

import com.mst.karsac.messages.Messages;

import java.io.Serializable;
import java.util.List;

public class ImageMessage implements Serializable{
    Messages messages;
    String img_path;

    public ImageMessage(Messages messages, String img_path) {
        this.messages = messages;
        this.img_path = img_path;
    }
}
