package info.androidhive.cardview.interest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ks2ht on 3/23/2018.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "interests_db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Interest.CREATE_TABLE_SELF);
        sqLiteDatabase.execSQL(Interest.CREATE_TABLE_TRANSIENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Interest.TABLE_NAME_SELF);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Interest.TABLE_NAME_TRANSIENT);
        onCreate(sqLiteDatabase);
    }

    public long insertSelfInterest(String interest, String type, float value) {
        long id = 0;
        String table_name = getTableName(type);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Interest.COLUMN_INTEREST, interest);
        values.put(Interest.INTEREST_VALUE, value);
        id = db.insert(table_name, null, values);
        return id;
    }

    public String getTableName(String type) {
        String table_name = null;
        if (type.contains("self"))
            table_name = Interest.TABLE_NAME_SELF;
        else
            table_name = Interest.TABLE_NAME_TRANSIENT;
        return table_name;
    }

    public List<Interest> getInterests(String type) {
        List<Interest> interestList = new ArrayList<>();
        String table_name = getTableName(type);

        String selectQuery = "SELECT * FROM " + table_name + " ORDER BY " + Interest.COLUMN_ID;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Interest interest = new Interest();
                interest.setId(cursor.getInt(cursor.getColumnIndex(Interest.COLUMN_ID)));
                interest.setInterest(cursor.getString(cursor.getColumnIndex(Interest.COLUMN_INTEREST)));
                interest.setTimestamp(cursor.getString(cursor.getColumnIndex(Interest.COLUMN_TIMESTAMP)));
                interest.setValue(cursor.getFloat(cursor.getColumnIndex(Interest.INTEREST_VALUE)));
                interestList.add(interest);
            } while (cursor.moveToNext());
        }
        db.close();
        return interestList;
    }

    public int getCount(String type) {
        String table_name = getTableName(type);
        String countQuery = "SELECT * FROM "+ table_name;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor =  db.rawQuery(countQuery, new String[]{Interest.COLUMN_ID});
        int count = cursor.getCount();
        return count;
    }

    public void deleteInterest(Interest interest, String type){
        String table_name = getTableName(type);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table_name, Interest.COLUMN_ID  + "=?", new String[]{String.valueOf(interest.getId())});
        db.close();
    }

}
