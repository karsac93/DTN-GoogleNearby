package com.mst.karsac.interest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ks2ht on 3/23/2018.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "dtndatabase";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("DbHelper", "OnCreate");
        Log.d("DbHelper-create", Interest.CREATE_TABLE_INTEREST);
        sqLiteDatabase.execSQL(Interest.CREATE_TABLE_INTEREST);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Interest.TABLE_NAME_INTEREST);
        onCreate(sqLiteDatabase);
    }

    public long insertInterest(String interest, int type, float value) {
        long id = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Interest.COLUMN_INTEREST, interest);
        values.put(Interest.INTEREST_VALUE, value);
        values.put(Interest.TYPE, type);
        id = db.insert(Interest.TABLE_NAME_INTEREST, null, values);
        return id;
    }


    public List<Interest> getInterests(int type) {
        List<Interest> interestList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + Interest.TABLE_NAME_INTEREST + " WHERE type="+ type + " ORDER BY " + Interest.COLUMN_ID;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Interest interest = new Interest();
                interest.setId(cursor.getInt(cursor.getColumnIndex(Interest.COLUMN_ID)));
                interest.setInterest(cursor.getString(cursor.getColumnIndex(Interest.COLUMN_INTEREST)));
                interest.setTimestamp(formatDate(cursor.getString(cursor.getColumnIndex(Interest.COLUMN_TIMESTAMP))));
                interest.setValue(cursor.getFloat(cursor.getColumnIndex(Interest.INTEREST_VALUE)));
                interestList.add(interest);
            } while (cursor.moveToNext());
        }
        db.close();
        return interestList;
    }

    public int getCount(int type) {
        String countQuery = "SELECT * FROM "+ Interest.TABLE_NAME_INTEREST + " WHERE type=" + type;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor =  db.rawQuery(countQuery, new String[]{Interest.COLUMN_ID});
        int count = cursor.getCount();
        return count;
    }

    public void deleteInterest(Interest interest, int type){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Interest.TABLE_NAME_INTEREST, Interest.COLUMN_ID  + "=? AND " + Interest.TYPE + "=?", new String[]{String.valueOf(interest.getId()), String.valueOf(type)});
        db.close();
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

}
