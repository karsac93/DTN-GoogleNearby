package com.mst.karsac.Algorithm;

import com.mst.karsac.DbHelper.DbHelper;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.interest.Interest;

import java.util.ArrayList;
import java.util.List;

public class ChitchatAlgo {

    public List<Interest> decayingFunction(int timestamp){
        DbHelper dbHelper = GlobalApp.dbHelper;
        List<Interest> self_interest;
        List<Interest> transient_interest;
        self_interest = dbHelper.getInterests(GlobalApp.SELF_INTEREST);
        transient_interest = dbHelper.getInterests(GlobalApp.TRANSIENT_INTEREST);
        self_interest = decayFormula(self_interest, GlobalApp.SELF_INTEREST, timestamp);
        transient_interest = decayFormula(transient_interest, GlobalApp.TRANSIENT_INTEREST, timestamp);
        //self_interest.addAll(transient_interest);
        return self_interest;

    }

    private List<Interest> decayFormula(List<Interest> interests, int type, int timestamp) {
        if(type == GlobalApp.TRANSIENT_INTEREST)
        {
            for(Interest interest : interests){
                float value = interest.getValue() / (timestamp - interest.getTimestamp());
                interest.setValue(value);
            }
        }
        else{
            for(Interest interest : interests) {
                float value = ((interest.getValue() - 0.5f) / (timestamp - interest.getTimestamp())) + 0.5f;
                interest.setValue(value);
            }
        }
        return interests;
    }
}
