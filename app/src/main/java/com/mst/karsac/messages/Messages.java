package com.mst.karsac.messages;

import java.io.Serializable;

/**
 * Created by ks2ht on 3/25/2018.
 */

public class Messages implements Serializable {

    public static final String MY_MESSAGE_TABLE_NAME = "mymessages";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_IMG_PATH = "imgPath";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TAGS = "tags";
    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_FORMAT = "format";
    public static final String COLUMN_SRCMAC = "srcmac";
    public static final String COLUMN_DESTADDR = "destaddr";
    public static final String COLUMN_SIZE = "size";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LON = "lon";
    public static final String COLUMN_TYPE = "type";


    String imgPath, timestamp, tagsForCurrentImg, fileName, format, sourceMac, destAddr;
    int rating, type, id;
    long size;
    float lat, lon;

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public static final String CREATE_TABLE_MESSAGE = "CREATE TABLE " + MY_MESSAGE_TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_IMG_PATH + " imgPath TEXT,"
            + COLUMN_TIMESTAMP + " timestamp TEXT," +  COLUMN_TAGS + " TEXT,"
            + COLUMN_FILENAME + " TXT," + COLUMN_FORMAT + " TXT," + COLUMN_SRCMAC + " TXT,"
            + COLUMN_DESTADDR + " TXT," +  COLUMN_SIZE + " INTEGER," + COLUMN_RATING + " INTEGER,"
            + COLUMN_LAT + " REAL," + COLUMN_LON + " REAL," + COLUMN_TYPE + " INTEGER)";

    public String getImgPath() {
        return imgPath;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getTagsForCurrentImg() {
        return tagsForCurrentImg;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFormat() {
        return format;
    }

    public String getSourceMac() {
        return sourceMac;
    }

    public String getDestAddr() {
        return destAddr;
    }

    public long getSize() {
        return size;
    }

    public int getRating() {
        return rating;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }
}
