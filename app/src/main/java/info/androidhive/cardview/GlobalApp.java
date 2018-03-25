package info.androidhive.cardview;

import android.app.Application;
import android.util.Log;

import info.androidhive.cardview.interest.DbHelper;

/**
 * Created by ks2ht on 3/25/2018.
 */

public class GlobalApp extends Application {
    static boolean hasStarted = false;
    public static DbHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("GLOBAL", "Inside");
        hasStarted = true;
        dbHelper = new DbHelper(this);
    }
}
