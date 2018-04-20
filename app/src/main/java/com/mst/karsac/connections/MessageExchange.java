package com.mst.karsac.connections;

import android.os.Parcel;
import android.os.Parcelable;

import com.mst.karsac.interest.Interest;

import java.io.Serializable;
import java.util.ArrayList;

public class MessageExchange implements Serializable {
    ArrayList<Interest> interestArrayList;

    public MessageExchange(ArrayList<Interest> interestArrayList) {
        this.interestArrayList = interestArrayList;
    }



    public ArrayList<Interest> getInterestArrayList() {
        return interestArrayList;
    }
}
