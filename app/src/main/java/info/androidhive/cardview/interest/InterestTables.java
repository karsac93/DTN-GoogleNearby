package info.androidhive.cardview.interest;

/**
 * Created by ks2ht on 3/23/2018.
 */

public class InterestTables {

    public static final String TABLE_NAME_SELF = "interestself";
    public static final String TABLE_NAME_TRANSIENT = "interesttransient";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_INTEREST = "note";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private int id;
    private String note;
    private String timestamp;
    private String type;


    public static final String CREATE_TABLE_SELF =
            "CREATE TABLE " + TABLE_NAME_SELF + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_INTEREST + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";

    public static final String CREATE_TABLE_TRANSIENT =
            "CREATE TABLE " + TABLE_NAME_TRANSIENT + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_INTEREST + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";

    public InterestTables(int id, String note, String timestamp, String type) {
        this.id = id;
        this.note = note;
        this.timestamp = timestamp;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

}
