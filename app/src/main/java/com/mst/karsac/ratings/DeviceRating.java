package com.mst.karsac.ratings;

import java.io.Serializable;

public class DeviceRating implements Serializable {

    public static final String DEVICE_RATING_TABLE = "device_rating";

    public static final String DEVICE_UUID_COLUMN = "device_uuid";
    public static final String DEVICE_AVERAGE = "device_average";

    public static final String CREATE_DEVICE_RATING_TABLE = "CREATE TABLE " + DEVICE_RATING_TABLE
            + " (" + DEVICE_UUID_COLUMN + " TEXT, " + DEVICE_AVERAGE + " REAL, PRIMARY KEY(" + DEVICE_UUID_COLUMN + "))";

    String device_uuid;
    float device_average;

    public DeviceRating(String device_uuid, float device_average) {
        this.device_uuid = device_uuid;
        this.device_average = device_average;
    }

    public String getDevice_uuid() {
        return device_uuid;
    }

    public void setDevice_uuid(String device_uuid) {
        this.device_uuid = device_uuid;
    }

    public float getDevice_average() {
        return device_average;
    }

    public void setDevice_average(float device_average) {
        this.device_average = device_average;
    }
}
