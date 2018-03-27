package com.mst.karsac.messages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mst.karsac.interest.DbHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ks2ht on 3/25/2018.
 */

public class MessageDbHelper extends SQLiteOpenHelper {


    public MessageDbHelper(Context context) {
        super(context, DbHelper.DATABASE_NAME, null, DbHelper.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Messages.CREATE_TABLE_MESSAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Messages.MY_MESSAGE_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public long insertImageRecord(Messages message){
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
        return id;
    }

    public List<Messages> getAllMessages(int type){
        List<Messages> messagesList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + Messages.MY_MESSAGE_TABLE_NAME + " WHERE type=" + type;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                Messages messages = new Messages();
                messages.imgPath = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_IMG_PATH));
                messages.timestamp = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_TIMESTAMP));
                messages.tagsForCurrentImg = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_TAGS));
                messages.fileName = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_FILENAME));
                messages.format = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_FORMAT));
                messages.sourceMac = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_SRCMAC));
                messages.destAddr = cursor.getString(cursor.getColumnIndex(Messages.COLUMN_DESTADDR));
                messages.size = cursor.getLong(cursor.getColumnIndex(Messages.COLUMN_SIZE));
                messages.rating = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_RATING));
                messages.lat = cursor.getFloat(cursor.getColumnIndex(Messages.COLUMN_LAT));
                messages.lon = cursor.getFloat(cursor.getColumnIndex(Messages.COLUMN_LON));
                messages.type = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_TYPE));
                messages.id = cursor.getInt(cursor.getColumnIndex(Messages.COLUMN_ID));
                messagesList.add(messages);
            }while (cursor.moveToNext());
        }
        return messagesList;
    }

    public int getMsgCount(int type){
        int count = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        String countQuery = "SELECT * FROM " + Messages.MY_MESSAGE_TABLE_NAME + " WHERE type=" + type;
        Cursor cursor = db.rawQuery(countQuery, new String[]{Messages.COLUMN_ID});
        count = cursor.getCount();
        return count;
    }

    public void deleteMsg(Messages msg){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Messages.MY_MESSAGE_TABLE_NAME, Messages.COLUMN_ID + "=?", new String[]{String.valueOf(msg.getId())});
    }
}
