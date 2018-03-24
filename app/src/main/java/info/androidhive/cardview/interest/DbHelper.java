package info.androidhive.cardview.interest;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        sqLiteDatabase.execSQL(InterestTables.CREATE_TABLE_SELF);
        sqLiteDatabase.execSQL(InterestTables.CREATE_TABLE_TRANSIENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + InterestTables.TABLE_NAME_SELF);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + InterestTables.TABLE_NAME_TRANSIENT);
        onCreate(sqLiteDatabase);
    }
}
