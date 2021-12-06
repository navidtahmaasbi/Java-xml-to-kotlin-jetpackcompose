package com.azarpark.watchman.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.service.autofill.RegexValidator;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.azarpark.watchman.R;
import com.azarpark.watchman.activities.SplashActivity;
import com.azarpark.watchman.download_utils.DownloadController;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.Place;
import com.yandex.metrica.YandexMetrica;

import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import retrofit2.http.Url;
import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

public class Assistant {

    public static long generateResNum() {

        return System.currentTimeMillis();

    }

    public boolean isMobile(String mobile) {

        return mobile.matches("^09\\d{9}$");

    }

    public boolean isPassword(String password) {

        return password.length() >= 4;

    }

    public boolean isPersianAlphabet(String s) {

        Pattern RTL_CHARACTERS =
                Pattern.compile("[\u0600-\u06FF\u0750-\u077F\u0590-\u05FF\uFE70-\uFEFF]");
        Matcher matcher = RTL_CHARACTERS.matcher(s);
        return matcher.find();


    }

    public void showSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void hideSoftKeyboard(Activity activity) {

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);

    }

    public static void hideKeyboard(Activity activity, View view) {

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    public Bitmap qrGenerator(String value) {

        System.out.println("----------> value : " + value);

        // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
        QRGEncoder qrgEncoder = new QRGEncoder(value, null, QRGContents.Type.TEXT, 512);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);
        Bitmap bitmap = qrgEncoder.getBitmap();

        return bitmap;


    }

    public Bitmap qrGenerator(String url, int placeID,String tag1,String tag2,String tag3,String tag4) {

        Uri uri = Uri.parse(url);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(uri.getScheme())
                .authority(uri.getAuthority())
                .appendPath(uri.getPath())
                /*.fragment("section-name")*/;

        for (String p : uri.getPathSegments())
            builder.appendPath(p);

        for (String q : uri.getQueryParameterNames())
            builder.appendQueryParameter(q,uri.getQueryParameter(q));

        builder.appendQueryParameter(Constants.tag1, tag1);
        builder.appendQueryParameter(Constants.tag2, tag2);
        builder.appendQueryParameter(Constants.tag3, tag3);
        builder.appendQueryParameter(Constants.tag4, tag4);

        builder.appendQueryParameter(Constants.place_id, Integer.toString(placeID));
        String myUrl = builder.build().toString();

        QRGEncoder qrgEncoder = new QRGEncoder(myUrl, null, QRGContents.Type.TEXT, 512);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);
        Bitmap bitmap = qrgEncoder.getBitmap();

        return bitmap;


    }

    public boolean isNumber(String amount) {

        amount = amount.replace(",", "");

        try {

            int a = Integer.parseInt(amount);
        } catch (Exception e) {

            return false;
        }

        return true;
    }

    public String getTime() {

        PersianDate pdate = new PersianDate();

        StringBuilder sb = new StringBuilder();
        sb.append(pdate.getShYear());
        sb.append("-");
        if (pdate.getShMonth() < 10)
            sb.append("0");
        sb.append(pdate.getShMonth());
        sb.append("-");
        if (pdate.getShDay() < 10)
            sb.append("0");
        sb.append(pdate.getShDay());
        sb.append(" ");
        sb.append(pdate.getHour());
        sb.append(":");
        sb.append(pdate.getMinute());
//        sb.append(":");
//        sb.append(pdate.getSecond());
//        sb.append(":");

        return sb.toString();

    }

    public String toJalali(String date) {

        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        int day = Integer.parseInt(date.substring(8, 10));
        int hour = Integer.parseInt(date.substring(11, 13));
        int minute = Integer.parseInt(date.substring(14, 16));
        int second = Integer.parseInt(date.substring(17, 19));

        PersianDate pdate = new PersianDate();

        pdate.setGrgYear(year);
        pdate.setGrgMonth(month);
        pdate.setGrgDay(day);

        String s = pdate.getShYear() + "-" + pdate.getShMonth() + "-" + pdate.getShDay() + " " + hour + ":" + minute + ":" + second;

        PersianDateFormat pdformater = new PersianDateFormat("Y-m-j g-i");
        return pdformater.format(pdate);

    }

    public String formatAmount(int num) {
        DecimalFormat decimalFormat = new DecimalFormat();
        DecimalFormatSymbols decimalFormateSymbol = new DecimalFormatSymbols();
        decimalFormateSymbol.setGroupingSeparator(',');
        decimalFormat.setDecimalFormatSymbols(decimalFormateSymbol);
        return decimalFormat.format(num);
    }

    public boolean VPNEnabled(Context mContext) {

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = cm.getAllNetworks();

        boolean vpnIsOpen = false;

        Log.i("TAG", "Network count: " + networks.length);
        for (int i = 0; i < networks.length; i++) {

            NetworkCapabilities caps = cm.getNetworkCapabilities(networks[i]);

            Log.i("TAG", "Network " + i + ": " + networks[i].toString());
            Log.i("TAG", "VPN transport is: " + caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
            Log.i("TAG", "NOT_VPN capability is: " + caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN));

            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                vpnIsOpen = true;

        }

        return vpnIsOpen;

    }

    public PlateType getPlateType(Place place) {

        if (place.tag2 == null || place.tag2.isEmpty() || place.tag2.equals("0"))
            return PlateType.old_aras;

        if (place.tag3 == null || place.tag3.isEmpty() || place.tag3.equals("0"))
            return PlateType.new_aras;

        return PlateType.simple;

    }

    public PlateType getPlateType(String tag1, String tag2, String tag3, String tag4) {

        if (tag2 == null || tag2.isEmpty() || tag2.equals("0"))
            return PlateType.old_aras;

        if (tag3 == null || tag3.isEmpty() || tag3.equals("0"))
            return PlateType.new_aras;

        return PlateType.simple;

    }

    public boolean simplePlateIsValid(String tag1, String tag2, String tag3, String tag4) {


        if (tag1.isEmpty() || tag2.isEmpty() || tag3.isEmpty() || tag4.isEmpty())
            return false;

        if (tag1.length() != 2 || tag2.length() != 1 || tag3.length() != 3 || tag4.length() != 2)
            return false;

        if (tag1.charAt(0) == '0' || tag3.charAt(0) == '0' || tag4.charAt(0) == '0')
            return false;

        if (tag1.contains(".") || tag3.contains(".") || tag4.contains("."))
            return false;

        if (!tag2IsValid(tag2))
            return false;

        return true;

    }

    private boolean tag2IsValid(String s) {

        String validChars = "ضصثقفغعهخحجچپگکمنتالبیسشظطزرذدو";

        for (int i = 0; i < s.length(); i++)
            if (!validChars.contains(s.charAt(i) + ""))
                return false;

        return true;


    }

    private boolean isNotNumber(String s) {

        String ignoreChars = "1234567890";

        for (int i = 0; i < s.length(); i++)
            if (!ignoreChars.contains(s.charAt(i) + ""))
                return true;

        return false;


    }

    public void saveTags(String tag1, String tag2, String tag3, String tag4, Context context) {

        SharedPreferencesRepository.setValue(Constants.TAG1, tag1);
        SharedPreferencesRepository.setValue(Constants.TAG2, tag2);
        SharedPreferencesRepository.setValue(Constants.TAG3, tag3);
        SharedPreferencesRepository.setValue(Constants.TAG4, tag4);

    }

    public static void loginEvent(String username) {


        Map<String, Object> eventParameters = new HashMap<String, Object>();
        eventParameters.put("username", username);

        YandexMetrica.reportEvent("Login", eventParameters);

    }

    public static void eventByMobile(String username, String action) {


        Map<String, Object> eventParameters = new HashMap<String, Object>();
        eventParameters.put(username, action);

        YandexMetrica.reportEvent("events", eventParameters);

    }

    public String getTimeDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();


        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        if (elapsedHours == 0)
            return elapsedMinutes + " دقیقه";

        return elapsedHours + " ساعت " + elapsedMinutes + " دقیقه";
    }

    public static String getUnixTime(){

       return Long.toString(System.currentTimeMillis() / 1000L);

    }

    @SuppressLint("HardwareIds")
    public static String getSerialNumber(){

        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.O)
            return android.os.Build.SERIAL;

        return "";

    }

    public static void createNotification(Context context,String title,String message){

        Intent intent = new Intent(context, SplashActivity.class);

        // Assign channel ID
        String channel_id = "notification_channel";
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT
        );

        NotificationCompat.Builder builder
                = new NotificationCompat.Builder(
                context,
                channel_id)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder = builder.setContent(getCustomDesign(context,title, message));
        } else {
            builder = builder.setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_launcher);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    channel_id,
                    "web_app",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(0, builder.build());

    }

    private static RemoteViews getCustomDesign(Context context,String title,
                                        String message) {
        @SuppressLint("RemoteViewLayout") RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.title, title);
        remoteViews.setTextViewText(R.id.message, message);
        remoteViews.setImageViewResource(R.id.icon, R.drawable.ic_launcher);
        return remoteViews;
    }

}
