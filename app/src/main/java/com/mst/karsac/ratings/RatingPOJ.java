package com.mst.karsac.ratings;

import java.io.Serializable;

public class RatingPOJ implements Serializable{

    public static final String RATINGS_TABLENAME = "ratings";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MAC_ADDRESS = "mac_address";
    public static final String COLUMN_AVERAGE_RATING = "average_rating";

    public String type = "";

    public static final String CREATE_TABLE_RATING = "CREATE TABLE " + RATINGS_TABLENAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_MAC_ADDRESS + " TEXT,"
            + COLUMN_AVERAGE_RATING + " REAL)";

    public String mac_address = null;
    public float average = 0.0f;

    public RatingPOJ(String mac_address, float average) {
        this.mac_address = mac_address;
        this.average = average;
    }

    public RatingPOJ() {
    }
}
