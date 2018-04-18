package com.mst.karsac.messages;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ks2ht on 3/25/2018.
 */

public class Messages implements Parcelable{

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

    public String imgPath;
    public String timestamp;
    public String tagsForCurrentImg;
    public String fileName;
    public String format;
    public String sourceMac;
    public String destAddr;
    public int rating;
    public int type;
    public int id;
    public long size;
    public double lat;
    public double lon;
    public float incentive_promised;
    public float incentive_received;
    public float incentive_paid;

    public Messages(String imgPath, String timestamp, String tagsForCurrentImg, String fileName,
                    String format, String sourceMac, String destAddr, int rating, int type,
                    long size, double lat, double lon, float incentive_paid, float incentive_promised,
                    float incentive_received) {
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
    }

    public static final String CREATE_TABLE_MESSAGE = "CREATE TABLE " + MY_MESSAGE_TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_IMG_PATH + " imgPath TEXT,"
            + COLUMN_TIMESTAMP + " TEXT," +  COLUMN_TAGS + " TEXT,"
            + COLUMN_FILENAME + " TEXT," + COLUMN_FORMAT + " TEXT," + COLUMN_SRCMAC + " TEXT,"
            + COLUMN_DESTADDR + " TEXT," +  COLUMN_SIZE + " INTEGER," + COLUMN_RATING + " INTEGER,"
            + COLUMN_LAT + " REAL," + COLUMN_LON + " REAL," + COLUMN_TYPE + " INTEGER, "
            + COLUMN_PAID + " REAL," + COLUMN_RECEIVED + " REAL," + COLUMN_PROMISED + " REAL)";

    public static final Creator<Messages> CREATOR = new Creator<Messages>() {
        @Override
        public Messages createFromParcel(Parcel in) {
            return new Messages(in);
        }

        @Override
        public Messages[] newArray(int size) {
            return new Messages[size];
        }
    };

    protected Messages(Parcel in) {
        imgPath = in.readString();
        timestamp = in.readString();
        tagsForCurrentImg = in.readString();
        fileName = in.readString();
        format = in.readString();
        sourceMac = in.readString();
        destAddr = in.readString();
        rating = in.readInt();
        type = in.readInt();
        id = in.readInt();
        size = in.readLong();
        lat = in.readDouble();
        lon = in.readDouble();
        incentive_received = in.readFloat();
        incentive_paid = in.readFloat();
        incentive_promised = in.readFloat();
    }

    public int getType() {
        return type;
    }

    public int getId() {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imgPath);
        parcel.writeString(timestamp);
        parcel.writeString(tagsForCurrentImg);
        parcel.writeString(fileName);
        parcel.writeString(format);
        parcel.writeString(sourceMac);
        parcel.writeString(destAddr);
        parcel.writeInt(rating);
        parcel.writeInt(type);
        parcel.writeInt(id);
        parcel.writeLong(size);
        parcel.writeDouble(lat);
        parcel.writeDouble(lon);
        parcel.writeFloat(incentive_received);
        parcel.writeFloat(incentive_paid);
        parcel.writeFloat(incentive_promised);
    }
}
