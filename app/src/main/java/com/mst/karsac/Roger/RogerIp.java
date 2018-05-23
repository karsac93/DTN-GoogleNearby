package com.mst.karsac.Roger;

import com.mst.karsac.connections.ImageMessage;


import java.io.Serializable;
import java.util.ArrayList;

public class RogerIp implements Serializable{
    public String my_macaddress;
    public ArrayList<ImageMessage> imageMessages;

    public RogerIp(String my_macaddress, ArrayList<ImageMessage> imageMessages) {
        this.my_macaddress = my_macaddress;
        this.imageMessages = imageMessages;
    }
}
