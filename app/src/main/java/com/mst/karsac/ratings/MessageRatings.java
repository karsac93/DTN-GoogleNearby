package com.mst.karsac.ratings;

public class MessageRatings {

    public static final String MESSAGE_RATING_TABLE = "message_ratings";
    public static final String MESSAGE_UNIQUE_ID_COLUMN = "msg_uuid";
    public static final String TAG_RATE_COLUMN = "tags_column";
    public static final String CONFIDENCE_RATE_COLUMN = "confidence_rate";
    public static final String QUALITY_RATE_COLUMN = "quality_rate";
    public static final String INTERMEDIARIES_COLUMN = "intermediaries";
    public static final String INTERMEDIARY_TYPE = "intermediary_type";

    public static final String CREATE_MESSAGE_RATING_TABLE = "CREATE TABLE " + MESSAGE_RATING_TABLE + " ("
            + MESSAGE_UNIQUE_ID_COLUMN + " TEXT," + TAG_RATE_COLUMN + " REAL," +  CONFIDENCE_RATE_COLUMN
            + " REAL," + QUALITY_RATE_COLUMN + " REAL," + INTERMEDIARIES_COLUMN + " TEXT," + INTERMEDIARY_TYPE + " TEXT, PRIMARY KEY (" +
            MESSAGE_UNIQUE_ID_COLUMN + ", " + INTERMEDIARIES_COLUMN + "))";

    String message_unique_id;
    float tag_rate, confidence_rate, quality_rate;
    String intermediary;
    String inter_type;


    public MessageRatings(String message_unique_id, float tag_rate, float confidence_rate, float quality_rate, String intermediary, String inter_type) {
        this.message_unique_id = message_unique_id;
        this.tag_rate = tag_rate;
        this.confidence_rate = confidence_rate;
        this.quality_rate = quality_rate;
        this.intermediary = intermediary;
        this.inter_type = inter_type;
    }

    public String getMessage_unique_id() {
        return message_unique_id;
    }

    public void setMessage_unique_id(String message_unique_id) {
        this.message_unique_id = message_unique_id;
    }

    public float getTag_rate() {
        return tag_rate;
    }

    public void setTag_rate(float tag_rate) {
        this.tag_rate = tag_rate;
    }

    public float getConfidence_rate() {
        return confidence_rate;
    }

    public void setConfidence_rate(float confidence_rate) {
        this.confidence_rate = confidence_rate;
    }

    public float getQuality_rate() {
        return quality_rate;
    }

    public void setQuality_rate(float quality_rate) {
        this.quality_rate = quality_rate;
    }

    public String getIntermediary() {
        return intermediary;
    }

    public void setIntermediary(String intermediary) {
        this.intermediary = intermediary;
    }

    public String getInter_type() {
        return inter_type;
    }

    public void setInter_type(String inter_type) {
        this.inter_type = inter_type;
    }
}
