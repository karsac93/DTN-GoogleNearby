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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ks2ht on 3/23/2018.
 */

public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dtndatabase";
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Interest.TABLE_NAME_INTEREST);
        onCreate(sqLiteDatabase);
    }

    public int updateInterest(String interest, float value, int type){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Interest.INTEREST_VALUE, value);
        return db.update(Interest.TABLE_NAME_INTEREST, contentValues, Interest.COLUMN_INTEREST + "=? AND " + Interest.TYPE + "=?", new String[]{interest, String.valueOf(type)});
    }

    public long insertInterest(String interest, int type, float value) {
        long id = 0;
        int count = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Interest.COLUMN_INTEREST, interest);
        values.put(Interest.INTEREST_VALUE, value);
        values.put(Interest.TYPE, type);
        values.put(Interest.COLUMN_TIMESTAMP, SharedPreferencesHandler.getTimestamp(context, GlobalApp.TIMESTAMP));
        String selectQuery = "SELECT * FROM " + Interest.TABLE_NAME_INTEREST + " WHERE " +
                Interest.COLUMN_INTEREST + "='" + interest + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        count = cursor.getCount();
        if (count == 0)
            id = db.insert(Interest.TABLE_NAME_INTEREST, null, values);
        return id;
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
        long id = 0;
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
        id = db.insert(Messages.MY_MESSAGE_TABLE_NAME, null, contentValues);
        Log.d("ID", "ID" + id);
        return id;
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
                float incetive_paid = cursor.getFloat(cursor.getColumnIndex(Messages.COLUMN_PAID));
                float incetive_received = cursor.getFloat(cursor.getColumnIndex(Messages.COLUMN_RECEIVED));
                float incetive_promised = cursor.getFloat(cursor.getColumnIndex(Messages.COLUMN_PROMISED));
                Messages messages = new Messages(imgPath, timestamp, tagsForCurrentImg, fileName,
                        format, sourceMac, destAddr, rating, type_msg, size, lat, lon, incetive_paid,
                        incetive_promised, incetive_received);
                messages.id = id;
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

    public void deleteMsg(Messages msg) {
        int count = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("DELETE", msg.id + msg.fileName);
        db.delete(Messages.MY_MESSAGE_TABLE_NAME, Messages.COLUMN_ID + "=? AND " + Interest.TYPE + "=?", new String[]{String.valueOf(msg.getId()), String.valueOf(msg.type)});
        Log.d("SIZE in DB", getMsgCount(msg.type) + " ");
    }

    public int updateMsg(Messages msg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Messages.COLUMN_TAGS, msg.tagsForCurrentImg);
        return db.update(Messages.MY_MESSAGE_TABLE_NAME, contentValues, Messages.COLUMN_ID + "=?", new String[]{String.valueOf(msg.id)});
    }

}
