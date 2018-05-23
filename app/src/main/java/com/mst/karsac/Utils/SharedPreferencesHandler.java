package com.mst.karsac.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SharedPreferencesHandler {

    public static final String STATUS = "status";
    public static final String LAST_IPADDRESS = "ip_address";

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setStringPreferences(Context context, String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setIntPreference(Context context, String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setFloatPreference(Context context, String key, float value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static int getIntPreferences(Context context, String key) {
        return getSharedPreferences(context).getInt(key, 0);
    }

    public static String getStringPreferences(Context context, String key) {
        return getSharedPreferences(context).getString(key, "");
    }

    public static float getRadiusPreferences(Context context, String key) {
        return getSharedPreferences(context).getFloat(key, 1.0f);
    }

    public static int getIncentive(Context context, String key) {
        return getSharedPreferences(context).getInt(key, 300);
    }

}
