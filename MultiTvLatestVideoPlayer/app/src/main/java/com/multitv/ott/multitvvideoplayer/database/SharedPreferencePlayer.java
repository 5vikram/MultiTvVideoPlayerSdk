package com.multitv.ott.multitvvideoplayer.database;
/**
 * Created by Lenovo on 03-02-2017.
 */


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class SharedPreferencePlayer {

    public String PREFS_NAME = "SharedPreferencePlayer";

    public SharedPreferencePlayer() {
        super();
    }

    public void setPreferencesUri(Context context, String key, Uri value){
        if (context == null){
            return;
        }

        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key,value.toString());
        editor.commit();
    }

    public Uri getPreferencesUri(Context context, String key){
        if (context == null){
            return null;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uriStr = prefs.getString(key, "");
        return Uri.parse(uriStr);
    }

    public void setPreferencesString(Context context, String key, String value) {
        if (context == null)
            return;

        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void setPreferenceBoolean(Context context, String key, boolean value) {
        if (context == null)
            return;
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }


    public String getPreferencesString(Context context, String key) {
        if (context == null)
            return null;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String position = prefs.getString(key, "");
        return position;
    }


    public void setPreferenceInt(Context context, String key, int value) {
        if (context == null)
            return;
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.commit();
    }


    public int getPreferencesInt(Context context, String key, int defaultValue) {
        if (context == null)
            return 0;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int position = prefs.getInt(key, defaultValue);
        return position;
    }

    public boolean getPreferenceBoolean(Context _context, String key) {
        if (_context == null)
            return false;
        SharedPreferences prefs = _context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean b = prefs.getBoolean(key, false);
        return b;
    }


    public String getRecentList(Context context, String key) {
        if (context == null)
            return null;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public void setRecentList(Context context, String data, String key) {
        if (context == null)
            return;
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key, data);
        editor.apply();
    }

    public void clearPrefs(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }


    public String getCategories(Context context, String key) {
        if (context == null)
            return null;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public void setCategories(Context context, String data, String key) {
        if (context == null)
            return;
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key, data);
        editor.apply();
    }

}
