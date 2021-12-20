package com.azarpark.watchman.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.azarpark.watchman.models.LocalNotification;
import com.azarpark.watchman.models.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SharedPreferencesRepository {


    public static void checkTransactions() {


        String arrayString = getValue(Constants.UNSYCNCED_RES_NUMS, "[]");
        Gson gson = new Gson();
        ArrayList<Transaction> transactions = gson.fromJson(arrayString, new TypeToken<List<Transaction>>() {
        }.getType());


        for (Transaction t : transactions) {
            if (t.getStatus() == 0 && timeDifferenceInSeconds(Long.parseLong(t.getCreateTime()), Long.parseLong(Assistant.getUnixTime())) > 10) {
                t.setStatus(-1);
                updateTransactions02(t);
            }
        }

        setValue(Constants.UNSYCNCED_RES_NUMS, gson.toJson(transactions));


    }

    private static long timeDifferenceInSeconds(long d1, long d2) {


        long diff = Math.abs(d2 - d1);

        return diff;

    }

    public static ArrayList<Transaction> getTransactions() {

        String arrayString = getValue(Constants.UNSYCNCED_RES_NUMS, "[]");
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

    public static String getValue(String key) {

        return sharedPreferences.getString(key, "");
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

    public static void updateTransactions02(Transaction transaction) {

        String arrayString = getValue(Constants.UNSYCNCED_RES_NUMS, "[]");
        Gson gson = new Gson();
        ArrayList<Transaction> transactions = gson.fromJson(arrayString, new TypeToken<List<Transaction>>() {
        }.getType());

        for (Transaction t : transactions)
            if (t.getOur_token().equals(transaction.getOur_token())) {
                transactions.remove(t);
                transactions.add(transaction);
                break;
            }

        setValue(Constants.UNSYCNCED_RES_NUMS, gson.toJson(transactions));

    }

    public static void addToTransactions02(Transaction transaction) {

        String arrayString = getValue(Constants.UNSYCNCED_RES_NUMS, "[]");
        Gson gson = new Gson();
        ArrayList<Transaction> transactions = gson.fromJson(arrayString, new TypeToken<List<Transaction>>() {
        }.getType());

        transactions.add(transaction);

        setValue(Constants.UNSYCNCED_RES_NUMS, gson.toJson(transactions));

    }

    public static void removeFromTransactions02(Transaction transaction) {

        String arrayString = getValue(Constants.UNSYCNCED_RES_NUMS, "[]");
        Gson gson = new Gson();
        ArrayList<Transaction> transactions = gson.fromJson(arrayString, new TypeToken<List<Transaction>>() {
        }.getType());


        if (transactions.size() != 0)
            for (int i = 0; i < transactions.size(); i++)
                if (transactions.get(i).getOur_token().equals(transaction.getOur_token()))
                    transactions.remove(i);

        setValue(Constants.UNSYCNCED_RES_NUMS, gson.toJson(transactions));

    }

    public static ArrayList<LocalNotification> getLocalNotifications() {

        String arrayString = getValue(Constants.LOCAL_NOTIFICATIONS, "[]");
        Gson gson = new Gson();
        ArrayList<LocalNotification> notifications = gson.fromJson(arrayString, new TypeToken<List<LocalNotification>>() {
        }.getType());

        return notifications;

    }

    public static void addToLocalNotifications(LocalNotification notification,Context context) {

        Assistant.makeSoundAndVibrate(context);

        String arrayString = getValue(Constants.LOCAL_NOTIFICATIONS, "[]");
        Gson gson = new Gson();
        ArrayList<LocalNotification> notifications = gson.fromJson(arrayString, new TypeToken<List<LocalNotification>>() {
        }.getType());

        notifications.add(notification);

        setValue(Constants.LOCAL_NOTIFICATIONS, gson.toJson(notifications));

    }

    public static void removeFromLocalNotifications(LocalNotification notification) {

        String arrayString = getValue(Constants.LOCAL_NOTIFICATIONS, "[]");
        Gson gson = new Gson();
        ArrayList<LocalNotification> notifications = gson.fromJson(arrayString, new TypeToken<List<LocalNotification>>() {
        }.getType());

        for (LocalNotification n : notifications)
            if (n.id.equals(notification.id))
                notifications.remove(n);

        setValue(Constants.LOCAL_NOTIFICATIONS, gson.toJson(notifications));

    }

}
