package com.azarpark.watchman.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.azarpark.watchman.models.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesRepository {

    private String MY_PREFS_NAME = "app_needs";

    SharedPreferences.Editor editor;
    SharedPreferences prefs;

    public static String
            ACCESS_TOKEN = "accessToken",
            REFRESH_TOKEN = "refreshToken",
            PLATE_TYPE = "PLATE_TYPE",
            TAG1 = "TAG1",
            TAG2 = "TAG2",
            TAG3 = "TAG3",
            TAG4 = "TAG4",
            AMOUNT = "AMOUNT",
            PLACE_ID = "PLACE_ID",
            OUR_TOKEN = "our_token",
            TRANSACTION_ID = "TRANSACTION_ID",
            REF_NUM = "REF_NUM",
            SUB_DOMAIN = "SUB_DOMAIN";
    public static String qr_url = "qr_url";
    public static String refresh_time="refresh_time";
    public static String telephone ="telephone";
    public static String pricing="pricing";
    public static String sms_number="sms_number";
    public static String rules_url="rules_url";
    public static String about_us_url="about_us_url";
    public static String guide_url="guide_url";

    private String  UNSYCNCED_RES_NUMS = "unsynced_res_nums";

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

    public String getString(String key, String defaultString) {


        return prefs.getString(key, defaultString);

    }

    public void addToTransactions(Transaction transaction){

        String arrayString = getString(UNSYCNCED_RES_NUMS,"[]");
        Gson gson = new Gson();
        ArrayList<Transaction> transactions = gson.fromJson(arrayString, new TypeToken<List<Transaction>>(){}.getType());

        transactions.add(transaction);

        saveString(UNSYCNCED_RES_NUMS,gson.toJson(transactions));

    }

    public void removeFromTransactions(Transaction transaction){

        String arrayString = getString(UNSYCNCED_RES_NUMS,"[]");
        Gson gson = new Gson();
        ArrayList<Transaction> transactions = gson.fromJson(arrayString, new TypeToken<List<Transaction>>(){}.getType());

        transactions.remove(transaction);

        saveString(UNSYCNCED_RES_NUMS,gson.toJson(transactions));

    }

}
