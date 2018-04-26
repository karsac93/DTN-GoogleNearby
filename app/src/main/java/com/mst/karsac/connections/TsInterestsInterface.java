package com.mst.karsac.connections;


import com.mst.karsac.messages.Messages;

public interface TsInterestsInterface {
    public MessageSerializer getTsInterests();

    public void notifyComplete();
}
