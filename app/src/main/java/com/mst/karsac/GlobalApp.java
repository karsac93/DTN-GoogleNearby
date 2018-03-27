package com.mst.karsac;

import android.app.Application;
import android.util.Log;

import com.mst.karsac.interest.DbHelper;
import com.mst.karsac.messages.MessageDbHelper;

/**
 * Created by ks2ht on 3/25/2018.
 */

public class GlobalApp extends Application {
    public static DbHelper dbHelper;
    public static MessageDbHelper msgDb;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("GLOBAL", "Inside");
        dbHelper = new DbHelper(this);
        msgDb = new MessageDbHelper(this);
    }
}
