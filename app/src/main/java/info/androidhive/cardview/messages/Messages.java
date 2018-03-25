package info.androidhive.cardview.messages;

/**
 * Created by ks2ht on 3/25/2018.
 */

public class Messages {

    public static final String MY_MESSAGE_TABLE_NAME = "mymessages";
    public static final String INBOX_TABLE_NAME = "inbox";

    String imgPath, timestamp, tagsForCurrentImg, fileName, format, sourceMac, destAddr;
    int size, rating;
    float lat, lon;


    public static final String CREATE_TABLE_MESSAGE = "CREATE TABLE " + MY_MESSAGE_TABLE_NAME + "("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, imgPath TEXT, timestamp TEXT, tags TEXT," +
            " filename TXT," + " format TXT, sourceMac TXT, destAddr TXT, size INTEGER, " +
            "rating INTEGER, lat REAL, lon REAL, type int";


}
