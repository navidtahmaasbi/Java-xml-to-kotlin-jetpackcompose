package com.azarpark.watchman.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesRepository {

    private String MY_PREFS_NAME = "app_needs";

    SharedPreferences.Editor editor;
    SharedPreferences prefs;

    public static String
            ACCESS_TOKEN = "accessToken",
            REFRESH_TOKEN = "refreshToken";

    public SharedPreferencesRepository(Context context) {

        editor = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
    }

    public void saveString(String key, String value) {

        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {

        String defaultString = "";

        return prefs.getString(key, defaultString);

    }
}
