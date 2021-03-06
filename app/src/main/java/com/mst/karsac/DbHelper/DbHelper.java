package com.mst.karsac.DbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mst.karsac.GlobalApp;
import com.mst.karsac.Utils.SharedPreferencesHandler;
import com.mst.karsac.interest.Interest;
import com.mst.karsac.messages.Messages;
import com.mst.karsac.ratings.DeviceRating;
import com.mst.karsac.ratings.MessageRatings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ks2ht on 3/23/2018.
 */

public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dtndatabase";
    private static final String TAG = "DBHelper";
    Context context;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("DbHelper", "OnCreate");
        Log.d("DbHelper-create", Interest.CREATE_TABLE_INTEREST);
        sqLiteDatabase.execSQL(Interest.CREATE_TABLE_INTEREST);
        sqLiteDatabase.execSQL(Messages.CREATE_TABLE_MESSAGE);
        sqLiteDatabase.execSQL(MessageRatings.CREATE_MESSAGE_RATING_TABLE);
        sqLiteDatabase.execSQL(DeviceRating.CREATE_DEVICE_RATING_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Interest.TABLE_NAME_INTEREST);
        onCreate(sqLiteDatabase);
    }

    public int updateInterest(String interest, float value, int type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Interest.INTEREST_VALUE, value);
        return db.update(Interest.TABLE_NAME_INTEREST, contentValues, Interest.COLUMN_INTEREST + "=? AND " + Interest.TYPE + "=?", new String[]{interest, String.valueOf(type)});
    }

    public long insertInterest(String interest, int type, float value) {
        long id = 0;
        int count;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Interest.COLUMN_INTEREST, interest);
        values.put(Interest.INTEREST_VALUE, value);
        values.put(Interest.TYPE, type);
        values.put(Interest.COLUMN_TIMESTAMP, SharedPreferencesHandler.getIntPreferences(context, GlobalApp.TIMESTAMP));
        String selectQuery = "SELECT * FROM " + Interest.TABLE_NAME_INTEREST + " WHERE " +
                Interest.COLUMN_INTEREST + "='" + interest + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        count = cursor.getCount();
        if (count == 0)
            id = db.insert(Interest.TABLE_NAME_INTEREST, null, values);
        else if (count == 1) {
            Log.d(TAG, "Already exists");
            if (cursor.moveToFirst()) {
                do {
                    Interest interest1 = new Interest();
                    interest1.setId(cursor.getInt(cursor.getColumnIndex(Interest.COLUMN_ID)));
                    interest1.setInterest(cursor.getString(cursor.getColumnIndex(Interest.COLUMN_INTEREST)));
                    interest1.setTimestamp(cursor.getInt(cursor.getColumnIndex(Interest.COLUMN_TIMESTAMP)));
                    interest1.setValue(cursor.getFloat(cursor.getColumnIndex(Interest.INTEREST_VALUE)));
                    interest1.setType(cursor.getInt(cursor.getColumnIndex(Interest.TYPE)));
                    Log.d(TAG, "present interest type:" + interest1.getType() + "insert type:" + type + " interest name:" + interest1.getInterest());
                    if (type != interest1.getType() && type == 0) {
                        Log.d(TAG, "different types!");
                        float val = 0.5f;
                        if (interest1.getValue() > 0.5f) {
                            val = interest1.getValue();
                        }
                        deleteInterest(interest1, 1);
                        insertInterest(interest, 0, val);
                    }

                } while (cursor.moveToNext());
            }
        } else
            Log.d(TAG, "Something wrong, two exists?");
        return id;
    }


    public HashMap<String, String> getMsgUUID() {
        HashMap<String, String> uuidList = new HashMap<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + Messages.MY_MESSAGE_TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                String uuid = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_UUID));
                String tags = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_TAGS));
                uuidList.put(uuid, tags);
            } while (cursor.moveToNext());
        }
        return uuidList;
    }

    public List<Interest> getInterests(int type) {
        List<Interest> interestList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + Interest.TABLE_NAME_INTEREST + " WHERE type=" + type + " ORDER BY " + Interest.COLUMN_ID;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Interest interest = new Interest();
                interest.setId(cursor.getInt(cursor.getColumnIndex(Interest.COLUMN_ID)));
                interest.setInterest(cursor.getString(cursor.getColumnIndex(Interest.COLUMN_INTEREST)));
                interest.setTimestamp(cursor.getInt(cursor.getColumnIndex(Interest.COLUMN_TIMESTAMP)));
                interest.setValue(cursor.getFloat(cursor.getColumnIndex(Interest.INTEREST_VALUE)));
                interest.setType(type);
                interestList.add(interest);
            } while (cursor.moveToNext());
        }
        db.close();
        return interestList;
    }

    public int getCount(int type) {
        String countQuery = "SELECT * FROM " + Interest.TABLE_NAME_INTEREST + " WHERE type=" + type;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, new String[]{Interest.COLUMN_ID});
        int count = cursor.getCount();
        return count;
    }

    public void deleteInterest(Interest interest, int type) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Interest.TABLE_NAME_INTEREST, Interest.COLUMN_ID + "=? AND " + Interest.TYPE + "=?", new String[]{String.valueOf(interest.getId()), String.valueOf(type)});
        db.close();
    }


    public long insertImageRecord(Messages message) {
        long id;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Messages.COLUMN_IMG_PATH, message.getImgPath());
        contentValues.put(Messages.COLUMN_TIMESTAMP, message.getTimestamp());
        contentValues.put(Messages.COLUMN_TAGS, message.getTagsForCurrentImg());
        contentValues.put(Messages.COLUMN_FILENAME, message.getFileName());
        contentValues.put(Messages.COLUMN_FORMAT, message.getFormat());
        contentValues.put(Messages.COLUMN_SRCMAC, message.getSourceMac());
        contentValues.put(Messages.COLUMN_DESTADDR, message.getDestAddr());
        contentValues.put(Messages.COLUMN_SIZE, message.getSize());
        contentValues.put(Messages.COLUMN_RATING, message.getRating());
        contentValues.put(Messages.COLUMN_LAT, message.getLat());
        contentValues.put(Messages.COLUMN_LON, message.getLon());
        contentValues.put(Messages.COLUMN_TYPE, message.getType());
        contentValues.put(Messages.COLUMN_UUID, message.getUuid());
        contentValues.put(Messages.COLUMN_PROMISED, message.incentive_promised);
        contentValues.put(Messages.COLUMN_PAID, message.incentive_paid);
        contentValues.put(Messages.COLUMN_RECEIVED, message.incentive_received);
        id = db.insert(Messages.MY_MESSAGE_TABLE_NAME, null, contentValues);
        Log.d("ID", "ID" + id);
        return id;
    }

    public Messages getSingleMessage(String msgUUID){
        String selectQuery = "SELECT * FROM " + Messages.MY_MESSAGE_TABLE_NAME + " WHERE " + Messages.COLUMN_UUID + "= '" + msgUUID + "'";
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        Messages messages = null;
        if (cursor.moveToFirst()) {
            do {
                String imgPath = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_IMG_PATH));
                String timestamp = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_TIMESTAMP));
                String tagsForCurrentImg = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_TAGS));
                String fileName = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_FILENAME));
                String format = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_FORMAT));
                String sourceMac = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_SRCMAC));
                String destAddr = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_DESTADDR));
                long size = cursor.getLong(cursor.getColumnIndex(Messages.COLUMN_SIZE));
                int rating = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_RATING));
                double lat = cursor.getDouble(cursor.getColumnIndex(Messages.COLUMN_LAT));
                double lon = cursor.getDouble(cursor.getColumnIndex(Messages.COLUMN_LON));
                int type_msg = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_TYPE));
                int id = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_ID));
                int incetive_paid = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_PAID));
                int incetive_received = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_RECEIVED));
                int incetive_promised = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_PROMISED));
                String uuid = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_UUID));
                messages = new Messages(imgPath, timestamp, tagsForCurrentImg, fileName,
                        format, sourceMac, destAddr, rating, type_msg, size, lat, lon, incetive_paid,
                        incetive_promised, incetive_received, uuid);
            } while (cursor.moveToNext());
        }
        return messages;
    }

    public List<Messages> getAllMessages(int type) {
        List<Messages> messagesList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + Messages.MY_MESSAGE_TABLE_NAME + " WHERE type=" + type;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                String imgPath = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_IMG_PATH));
                String timestamp = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_TIMESTAMP));
                String tagsForCurrentImg = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_TAGS));
                String fileName = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_FILENAME));
                String format = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_FORMAT));
                String sourceMac = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_SRCMAC));
                String destAddr = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_DESTADDR));
                long size = cursor.getLong(cursor.getColumnIndex(Messages.COLUMN_SIZE));
                int rating = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_RATING));
                double lat = cursor.getDouble(cursor.getColumnIndex(Messages.COLUMN_LAT));
                double lon = cursor.getDouble(cursor.getColumnIndex(Messages.COLUMN_LON));
                int type_msg = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_TYPE));
                int id = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_ID));
                int incetive_paid = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_PAID));
                int incetive_received = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_RECEIVED));
                int incetive_promised = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_PROMISED));
                String uuid = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_UUID));
                Messages messages = new Messages(imgPath, timestamp, tagsForCurrentImg, fileName,
                        format, sourceMac, destAddr, rating, type_msg, size, lat, lon, incetive_paid,
                        incetive_promised, incetive_received, uuid);
                messages.id = String.valueOf(id);
                messagesList.add(messages);
            } while (cursor.moveToNext());
        }
        return messagesList;
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("MMM d");
            return fmtOut.format(date);
        } catch (ParseException e) {

        }

        return "";
    }

    public int getMsgCount(int type) {
        int count = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        String countQuery = "SELECT * FROM " + Messages.MY_MESSAGE_TABLE_NAME + " WHERE type=" + type;
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();
        return count;
    }

    public void truncate(String tableName) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String selectQuery = "DELETE FROM " + tableName;
        sqLiteDatabase.execSQL(selectQuery);
    }

    public void deleteMsg(Messages msg) {
        int count = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("DELETE", msg.id + msg.fileName);
        db.delete(Messages.MY_MESSAGE_TABLE_NAME, Messages.COLUMN_ID + "=? AND " + Interest.TYPE + "=?", new String[]{String.valueOf(msg.getId()), String.valueOf(msg.type)});
        Log.d("SIZE in DB", getMsgCount(msg.type) + " ");
    }

    public int updateMsg(Messages message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Messages.COLUMN_IMG_PATH, message.getImgPath());
        contentValues.put(Messages.COLUMN_TIMESTAMP, message.getTimestamp());
        contentValues.put(Messages.COLUMN_TAGS, message.getTagsForCurrentImg());
        contentValues.put(Messages.COLUMN_FILENAME, message.getFileName());
        contentValues.put(Messages.COLUMN_FORMAT, message.getFormat());
        contentValues.put(Messages.COLUMN_SRCMAC, message.getSourceMac());
        contentValues.put(Messages.COLUMN_DESTADDR, message.getDestAddr());
        contentValues.put(Messages.COLUMN_SIZE, message.getSize());
        contentValues.put(Messages.COLUMN_RATING, message.getRating());
        contentValues.put(Messages.COLUMN_LAT, message.getLat());
        contentValues.put(Messages.COLUMN_LON, message.getLon());
        contentValues.put(Messages.COLUMN_TYPE, message.getType());
        contentValues.put(Messages.COLUMN_UUID, message.getUuid());
        contentValues.put(Messages.COLUMN_PROMISED, message.incentive_promised);
        contentValues.put(Messages.COLUMN_PAID, message.incentive_paid);
        contentValues.put(Messages.COLUMN_RECEIVED, message.incentive_received);

        int id = db.update(Messages.MY_MESSAGE_TABLE_NAME, contentValues, Messages.COLUMN_ID + "=?", new String[]{String.valueOf(message.id)});
        return id;
    }

    public int updateMsgTags(String msgUUID, String tags) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Messages.COLUMN_TAGS, tags);

        int id = db.update(Messages.MY_MESSAGE_TABLE_NAME, contentValues, Messages.COLUMN_UUID + "=?", new String[]{msgUUID});
        return id;
    }

    public long insertMessageRating(MessageRatings messageRatings) {
        long id = 0;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageRatings.MESSAGE_UNIQUE_ID_COLUMN, messageRatings.getMessage_unique_id());
        contentValues.put(MessageRatings.INTERMEDIARIES_COLUMN, messageRatings.getIntermediary());
        contentValues.put(MessageRatings.TAG_RATE_COLUMN, messageRatings.getTag_rate());
        contentValues.put(MessageRatings.CONFIDENCE_RATE_COLUMN, messageRatings.getConfidence_rate());
        contentValues.put(MessageRatings.QUALITY_RATE_COLUMN, messageRatings.getQuality_rate());
        contentValues.put(MessageRatings.INTERMEDIARY_TYPE_COLUMN, messageRatings.getInter_type());
        contentValues.put(MessageRatings.LOCAL_AVERAGE_COLUMN, messageRatings.getLocal_average());
        id = sqLiteDatabase.insert(MessageRatings.MESSAGE_RATING_TABLE, null, contentValues);
        return id;
    }

    public int updateRatings(MessageRatings messageRatings){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageRatings.TAG_RATE_COLUMN, messageRatings.getTag_rate());
        contentValues.put(MessageRatings.CONFIDENCE_RATE_COLUMN, messageRatings.getConfidence_rate());
        contentValues.put(MessageRatings.QUALITY_RATE_COLUMN, messageRatings.getQuality_rate());
        contentValues.put(MessageRatings.INTERMEDIARY_TYPE_COLUMN, messageRatings.getInter_type());
        contentValues.put(MessageRatings.LOCAL_AVERAGE_COLUMN, messageRatings.getLocal_average());
        int id = db.update(MessageRatings.MESSAGE_RATING_TABLE, contentValues,
                MessageRatings.MESSAGE_UNIQUE_ID_COLUMN + "=? AND " + MessageRatings.INTERMEDIARIES_COLUMN +
        "=?", new String[]{messageRatings.getMessage_unique_id(), messageRatings.getIntermediary()});
        return id;
    }

    public List<MessageRatings> getRatingsMessage(String msgUUID, String intermediary) {
        List<MessageRatings> messageRatingsList = new ArrayList<>();
        String selectQuery = "";
        if (msgUUID != null && intermediary != null)
            selectQuery = "SELECT * FROM " + MessageRatings.MESSAGE_RATING_TABLE + " WHERE " + MessageRatings.MESSAGE_UNIQUE_ID_COLUMN + "='" + msgUUID + "' AND "
                    + MessageRatings.INTERMEDIARIES_COLUMN + "'" + intermediary + "'";
        else if (msgUUID != null && intermediary == null)
            selectQuery = "SELECT * FROM " + MessageRatings.MESSAGE_RATING_TABLE + " WHERE " + MessageRatings.MESSAGE_UNIQUE_ID_COLUMN + "='" + msgUUID + "'";
        else if (msgUUID == null && intermediary == null)
            selectQuery = "SELECT * FROM " + MessageRatings.MESSAGE_RATING_TABLE;
        else if (msgUUID == null && intermediary != null)
            selectQuery = "SELECT * FROM " + MessageRatings.MESSAGE_RATING_TABLE + " WHERE " + MessageRatings.INTERMEDIARIES_COLUMN + "='" + intermediary + "'";;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                String msgUUID_local = cursor.getString(cursor.getColumnIndex(MessageRatings.MESSAGE_UNIQUE_ID_COLUMN));
                String intermediaries = cursor.getString(cursor.getColumnIndex(MessageRatings.INTERMEDIARIES_COLUMN));
                float tag_rate = cursor.getFloat(cursor.getColumnIndex(MessageRatings.TAG_RATE_COLUMN));
                float confidence_rate = cursor.getFloat(cursor.getColumnIndex(MessageRatings.CONFIDENCE_RATE_COLUMN));
                float quality_rate = cursor.getFloat(cursor.getColumnIndex(MessageRatings.CONFIDENCE_RATE_COLUMN));
                String inter_type = cursor.getString(cursor.getColumnIndex(MessageRatings.INTERMEDIARY_TYPE_COLUMN));
                float local_average = cursor.getFloat(cursor.getColumnIndex(MessageRatings.LOCAL_AVERAGE_COLUMN));
                MessageRatings singleMessageRatings = new MessageRatings(msgUUID_local, tag_rate,
                        confidence_rate, quality_rate, intermediaries, inter_type, local_average);
                messageRatingsList.add(singleMessageRatings);
            } while (cursor.moveToNext());
        }
        return messageRatingsList;
    }

    public List<String> getDistinctIntermediaries(){
        List<String> distinctInter = new ArrayList<>();
        String selectQuery = "SELECT DISTINCT " + MessageRatings.INTERMEDIARIES_COLUMN + " FROM " + MessageRatings.MESSAGE_RATING_TABLE;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                String intermediary = cursor.getString(cursor.getColumnIndex(MessageRatings.INTERMEDIARIES_COLUMN));
                distinctInter.add(intermediary);
            }while(cursor.moveToNext());
        }
        return distinctInter;
    }

    public void deleteMessageRatings(String msgUUID){
        Log.d("DELETE msg Ratings", msgUUID);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MessageRatings.MESSAGE_RATING_TABLE, MessageRatings.MESSAGE_UNIQUE_ID_COLUMN + "=?", new String[]{msgUUID});
    }

    public DeviceRating getDeviceRating(String deviceUUID){
        String selectQuery = "SELECT * FROM " + DeviceRating.DEVICE_RATING_TABLE + " WHERE " + DeviceRating.DEVICE_UUID_COLUMN + " ='" + deviceUUID + "'";
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        DeviceRating deviceRating = null;
        if(cursor.moveToFirst()){
            do{
                String deviceId = cursor.getString(cursor.getColumnIndex(DeviceRating.DEVICE_UUID_COLUMN));
                float device_average = cursor.getFloat(cursor.getColumnIndex(DeviceRating.DEVICE_AVERAGE));
                deviceRating = new DeviceRating(deviceId, device_average);
            }while(cursor.moveToNext());
        }
        return deviceRating;
    }

    public List<DeviceRating> getAllDeviceRatings(){
        List<DeviceRating> deviceRatingList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DeviceRating.DEVICE_RATING_TABLE;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        DeviceRating deviceRating = null;
        if(cursor.moveToFirst()){
            do{
                String deviceId = cursor.getString(cursor.getColumnIndex(DeviceRating.DEVICE_UUID_COLUMN));
                float device_average = cursor.getFloat(cursor.getColumnIndex(DeviceRating.DEVICE_AVERAGE));
                deviceRating = new DeviceRating(deviceId, device_average);
                deviceRatingList.add(deviceRating);
            }while(cursor.moveToNext());
        }
        return deviceRatingList;
    }

    public void insertOrUpdateDeviceRatings(DeviceRating deviceRating){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DeviceRating.DEVICE_UUID_COLUMN, deviceRating.getDevice_uuid());
        contentValues.put(DeviceRating.DEVICE_AVERAGE, deviceRating.getDevice_average());
        DeviceRating check_present = getDeviceRating(deviceRating.getDevice_uuid());
        if(check_present == null){
            sqLiteDatabase.insert(DeviceRating.DEVICE_RATING_TABLE, null, contentValues);
        }
        else{
            float average = (check_present.getDevice_average() + deviceRating.getDevice_average()) / 2.0f;
            contentValues.put(DeviceRating.DEVICE_AVERAGE, average);
            sqLiteDatabase.update(DeviceRating.DEVICE_RATING_TABLE, contentValues, DeviceRating.DEVICE_UUID_COLUMN + "=?", new String[]{deviceRating.getDevice_uuid()});
        }
    }

}
