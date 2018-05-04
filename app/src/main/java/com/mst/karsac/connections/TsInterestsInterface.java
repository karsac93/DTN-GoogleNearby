package com.mst.karsac.connections;


import com.mst.karsac.messages.Messages;

public interface TsInterestsInterface {
    public MessageSerializer getTsInterests();

    void setMessageSerializer(MessageSerializer msg);

    public void notifyComplete();

    public void notifyCompleteClient();
}
