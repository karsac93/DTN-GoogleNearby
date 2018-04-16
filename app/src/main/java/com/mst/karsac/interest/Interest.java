package com.mst.karsac.interest;

/**
 * Created by ks2ht on 3/23/2018.
 */

public class Interest {

    public static final String TABLE_NAME_INTEREST = "interest";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_INTEREST = "interest";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String INTEREST_VALUE = "value";
    public static final String TYPE = "type";

    private int id;
    private String interest;
    private int timestamp;
    private float value;
    private int type;

    public static final String CREATE_TABLE_INTEREST =
            "CREATE TABLE " + TABLE_NAME_INTEREST + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_INTEREST + " TEXT,"
                    + COLUMN_TIMESTAMP + " INTEGER,"
                    + INTEREST_VALUE + " REAL,"
                    + TYPE + " INTEGER"
                    + ")";

    public Interest(){

    }

    public Interest(int id, String interest, int timestamp, int type) {
        this.id = id;
        this.interest = interest;
        this.timestamp = timestamp;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String note) {
        this.interest = note;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
