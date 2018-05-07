package com.mst.karsac.Algorithm;

import com.mst.karsac.messages.Messages;

public class MessageClassification {
    Messages messages;
    boolean type;

    public MessageClassification(Messages messages, boolean type) {
        this.messages = messages;
        this.type = type;
    }
}
