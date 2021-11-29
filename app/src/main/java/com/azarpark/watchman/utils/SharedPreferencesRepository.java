package com.azarpark.watchman.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.azarpark.watchman.models.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SharedPreferencesRepository {

    private String MY_PREFS_NAME = "app_needs";

    SharedPreferences.Editor editor;
    SharedPreferences prefs;

    public static String
            ACCESS_TOKEN = "ACCESS_TOKEN",
            REFRESH_TOKEN = "refreshToken",
            USERNAME = "username",
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
            SUB_DOMAIN = "SUB_DOMAIN",
            CITY_ID = "city_id";
    public static String qr_url = "qr_url";
    public static String refresh_time = "refresh_time";
    public static String telephone = "telephone";
    public static String pricing = "pricing";
    public static String sms_number = "sms_number";
    public static String rules_url = "rules_url";
    public static String about_us_url = "about_us_url";
    public static String guide_url = "guide_url";

    private String UNSYCNCED_RES_NUMS = "unsynced_res_nums";

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

    public void addToTransactions(Transaction transaction) {

        String arrayString = getString(UNSYCNCED_RES_NUMS, "[]");
        Gson gson = new Gson();
        ArrayList<Transaction> transactions = gson.fromJson(arrayString, new TypeToken<List<Transaction>>() {
        }.getType());

        transactions.add(transaction);

        saveString(UNSYCNCED_RES_NUMS, gson.toJson(transactions));

    }

    public void updateTransactions(Transaction transaction) {

        String arrayString = getString(UNSYCNCED_RES_NUMS, "[]");
        Gson gson = new Gson();
        ArrayList<Transaction> transactions = gson.fromJson(arrayString, new TypeToken<List<Transaction>>() {
        }.getType());


        for (Transaction t : transactions)
            if (t.getOur_token().equals(transaction.getOur_token())) {
                transactions.remove(t);
                transactions.add(transaction);
                break;
            }


        saveString(UNSYCNCED_RES_NUMS, gson.toJson(transactions));

    }

    public void checkTransactions() {


        String arrayString = getString(UNSYCNCED_RES_NUMS, "[]");
        Gson gson = new Gson();
        ArrayList<Transaction> transactions = gson.fromJson(arrayString, new TypeToken<List<Transaction>>() {
        }.getType());


        for (Transaction t : transactions) {
            if (t.getStatus() == 0 && timeDifferenceInSeconds(Long.parseLong(t.getCreateTime()), Long.parseLong(Assistant.getUnixTime())) > 10) {
                t.setStatus(-1);
                updateTransactions(t);
            }
        }

        saveString(UNSYCNCED_RES_NUMS, gson.toJson(transactions));


    }

    public long timeDifferenceInSeconds(long d1, long d2) {


        long diff = Math.abs(d2 - d1);

        return diff;

    }

    public void removeFromTransactions(Transaction transaction) {


        String arrayString = getString(UNSYCNCED_RES_NUMS, "[]");
        Gson gson = new Gson();
        ArrayList<Transaction> transactions = gson.fromJson(arrayString, new TypeToken<List<Transaction>>() {
        }.getType());


        if (transactions.size() != 0)
            for (int i = 0; i < transactions.size(); i++)
                if (transactions.get(i).getOur_token().equals(transaction.getOur_token()))
                    transactions.remove(i);

        saveString(UNSYCNCED_RES_NUMS, gson.toJson(transactions));

    }

    public ArrayList<Transaction> getTransactions() {

        String arrayString = getString(UNSYCNCED_RES_NUMS, "[]");
        Gson gson = new Gson();
        ArrayList<Transaction> transactions = gson.fromJson(arrayString, new TypeToken<List<Transaction>>() {
        }.getType());

        return transactions;

    }

    //----------------------------------------------------------------------------------------------------

    private static SharedPreferences sharedPreferences;

    public static void create(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.SHAREDPREFERNCE, context.MODE_PRIVATE);
    }

    public static String getValue(String key, String defaultValue) {

        return sharedPreferences.getString(key, defaultValue);
    }

    public static void setValue(String key, String value) {

        sharedPreferences.edit().putString(key, value).apply();

    }

    public static String getToken() {

        return sharedPreferences.getString(Constants.ACCESS_TOKEN, "");
    }

    public static String getTokenWithPrefix() {

        return "Bearer " + sharedPreferences.getString(Constants.ACCESS_TOKEN, "");
    }

    public static void setToken(String token) {

        sharedPreferences.edit().putString(Constants.ACCESS_TOKEN, token).apply();

    }

    public static void removeToken() {

        sharedPreferences.edit().putString(Constants.ACCESS_TOKEN, "").apply();

    }

}
