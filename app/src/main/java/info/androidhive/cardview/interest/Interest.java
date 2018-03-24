package info.androidhive.cardview.interest;

import android.content.Intent;

/**
 * Created by ks2ht on 3/23/2018.
 */

public class Interest {

    public static final String TABLE_NAME_SELF = "interestself";
    public static final String TABLE_NAME_TRANSIENT = "interesttransient";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_INTEREST = "interest";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String INTEREST_VALUE = "value";

    private int id;
    private String interest;
    private String timestamp;
    private float value;

    public static final String CREATE_TABLE_SELF =
            "CREATE TABLE " + TABLE_NAME_SELF + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_INTEREST + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + INTEREST_VALUE + "REAL"
                    + ")";

    public static final String CREATE_TABLE_TRANSIENT =
            "CREATE TABLE " + TABLE_NAME_TRANSIENT + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_INTEREST + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + INTEREST_VALUE + "REAL"
                    + ")";

    public Interest(){

    }

    public Interest(int id, String interest, String timestamp) {
        this.id = id;
        this.interest = interest;
        this.timestamp = timestamp;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

}
