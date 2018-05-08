package com.mst.karsac.messages;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by ks2ht on 3/25/2018.
 */

public class Messages implements Serializable{

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
    public static final String COLUMN_PROMISED = "promised";
    public static final String COLUMN_RECEIVED = "received";
    public static final String COLUMN_PAID = "paid";
    public static final String COLUMN_UUID = "uuid";

    public String imgPath;
    public String timestamp;
    public String tagsForCurrentImg;
    public String fileName;
    public String format;
    public String sourceMac;
    public String destAddr;
    public int rating;
    public int type;
    public String id;
    public long size;
    public double lat;
    public double lon;
    public int incentive_promised;
    public int incentive_received;
    public int incentive_paid;
    public String uuid;

    public Messages(String imgPath, String timestamp, String tagsForCurrentImg, String fileName,
                    String format, String sourceMac, String destAddr, int rating, int type,
                    long size, double lat, double lon, int incentive_paid, int incentive_promised,
                    int incentive_received, String uuid) {
        this.imgPath = imgPath;
        this.timestamp = timestamp;
        this.tagsForCurrentImg = tagsForCurrentImg;
        this.fileName = fileName;
        this.format = format;
        this.sourceMac = sourceMac;
        this.destAddr = destAddr;
        this.rating = rating;
        this.type = type;
        this.size = size;
        this.lat = lat;
        this.lon = lon;
        this.incentive_promised = incentive_promised;
        this.incentive_paid = incentive_paid;
        this.incentive_received = incentive_received;
        this.uuid = uuid;
    }

    public static final String CREATE_TABLE_MESSAGE = "CREATE TABLE " + MY_MESSAGE_TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +COLUMN_UUID + " TEXT," + COLUMN_IMG_PATH + " imgPath TEXT,"
            + COLUMN_TIMESTAMP + " TEXT," +  COLUMN_TAGS + " TEXT,"
            + COLUMN_FILENAME + " TEXT," + COLUMN_FORMAT + " TEXT," + COLUMN_SRCMAC + " TEXT,"
            + COLUMN_DESTADDR + " TEXT," +  COLUMN_SIZE + " INTEGER," + COLUMN_RATING + " INTEGER,"
            + COLUMN_LAT + " REAL," + COLUMN_LON + " REAL," + COLUMN_TYPE + " INTEGER, "
            + COLUMN_PAID + " INTEGER," + COLUMN_RECEIVED + " INTEGER," + COLUMN_PROMISED + " INTEGER)";


    public int getType() {
        return type;
    }

    public String getId() {
        return id;
    }

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

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getUuid() {return uuid;}
}
