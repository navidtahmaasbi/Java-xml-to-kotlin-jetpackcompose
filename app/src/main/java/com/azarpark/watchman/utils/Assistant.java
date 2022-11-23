package com.azarpark.watchman.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import com.azarpark.watchman.R;
import com.azarpark.watchman.activities.NotificationsActivity;
import com.azarpark.watchman.activities.SplashActivity;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.MyDate;
import com.azarpark.watchman.models.MyTime;
import com.azarpark.watchman.models.Place;
import com.yandex.metrica.YandexMetrica;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import saman.zamani.persiandate.PersianDate;

public class Assistant {

    public static long generateResNum() {

        return System.currentTimeMillis();

    }

    public long getDateDifferenceInMinutes(Date d1, Date d2) {

        long diff = d2.getTime() - d1.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;

        return minutes;

    }

    public static String persianToEnglishNumbers(String mobile) {


        return mobile;

    }

    public int charCountInString(String string, char ch) {

        int count = 0;
        for (int i = 0; i < string.length(); i++)
            if (string.charAt(i) == ch)
                count++;

        return count;
    }

    public boolean isMobile(String mobile) {

        for (int i = 0; i < mobile.length(); i++)
            if (charCountInString(mobile, mobile.charAt(i)) > 5)
                return false;

        return mobile.matches("^09\\d{9}$");

    }

    public boolean isPassword(String password) {

        return password.length() >= 4;

    }

    public boolean isValidCharForTag2(String s) {

        String validChars = Constants.VALID_CHARS.replace(" ", "");
        return validChars.contains(s);

    }

    public boolean isPersianAlphabet(String s) {

        Pattern RTL_CHARACTERS =
                Pattern.compile("[\u0600-\u06FF\u0750-\u077F\u0590-\u05FF\uFE70-\uFEFF]");
        Matcher matcher = RTL_CHARACTERS.matcher(s);
        return matcher.find();


    }

    public void hideSoftKeyboard(Activity activity) {

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm.isAcceptingText()) {
            imm.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_SHOWN, InputMethodManager.HIDE_NOT_ALWAYS);
        }


    }

    public static void hideKeyboard(Activity activity) {

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm.isAcceptingText()) {
            imm.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_SHOWN, InputMethodManager.HIDE_NOT_ALWAYS);
        }


    }

    public void showSoftKeyboard(Activity activity) {

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (!imm.isAcceptingText()) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }


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

    public Bitmap qrGenerator(String url, int placeID, String tag1, String tag2, String tag3, String tag4) {

        Uri uri = Uri.parse(url);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(uri.getScheme())
                .authority(uri.getAuthority())
                .appendPath(uri.getPath())
        /*.fragment("section-name")*/;

        for (String p : uri.getPathSegments())
            builder.appendPath(p);

        for (String q : uri.getQueryParameterNames())
            builder.appendQueryParameter(q, uri.getQueryParameter(q));

        builder.appendQueryParameter(Constants.tag1, tag1);
        if (tag2 != null)
            builder.appendQueryParameter(Constants.tag2, tag2);
        if (tag3 != null)
            builder.appendQueryParameter(Constants.tag3, tag3);
        if (tag4 != null)
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

            long a = Long.parseLong(amount);
        } catch (Exception e) {

            System.out.println("---------> eee : " );
            e.printStackTrace();
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

        return sb.toString();

    }

    public static MyDate getDate() {

        PersianDate pDate = new PersianDate();
        return new MyDate(pDate.getShYear(), pDate.getShMonth(), pDate.getShDay());

    }

    public static MyTime getMyTime() {

        PersianDate pDate = new PersianDate();
        return new MyTime(pDate.getHour(), pDate.getMinute());

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

        return pdate.getShYear() + "-" + pdate.getShMonth() + "-" + pdate.getShDay() + " " + hour + ":" + minute + ":" + second;

    }

    public String toJalaliWithoutTime(String date) {

        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        int day = Integer.parseInt(date.substring(8, 10));

        PersianDate pdate = new PersianDate();

        pdate.setGrgYear(year);
        pdate.setGrgMonth(month);
        pdate.setGrgDay(day);

        String s = pdate.getShYear() + "-" + pdate.getShMonth() + "-" + pdate.getShDay();

        return s;

//        System.out.println("---------> s : " + s);

//        PersianDateFormat pdformater = new PersianDateFormat("Y-m-j g-i");
//
//        System.out.println("---------> pdformater.format(pdate) : " + pdformater.format(pdate));
//
//        return pdformater.format(pdate);

    }

    public String jalaliToMiladi(int year, int month, int day) {

        PersianDate pdate = new PersianDate();
        pdate.setShYear(year);
        pdate.setShMonth(month);
        pdate.setShDay(day);

        String s =
                pdate.getGrgYear() +
                "-" +
                (pdate.getGrgMonth() < 10 ? "0" : "") +
                pdate.getGrgMonth() +
                "-" +
                (pdate.getGrgDay() < 10 ? "0" : "") +
                pdate.getGrgDay();

        return s;
    }

    public static String miladiToJalali(String s) {

        int year = Integer.parseInt(s.substring(0, 4));
        int month = Integer.parseInt(s.substring(5, 7));
        int day = Integer.parseInt(s.substring(8, 10));

        System.out.println("----------> " + year + " - " + month + " - " + day);

        PersianDate persianDate = new PersianDate();
        persianDate.setGrgYear(year);
        persianDate.setGrgMonth(month);
        persianDate.setGrgDay(day);

        String[] timeParts = s.split(" ");

        return persianDate.getShYear() + "/" + persianDate.getShMonth() + "/" + persianDate.getShDay() + " " + timeParts[1];

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

    public static void eventForDuplicateTransactions(String username, String action) {


        Map<String, Object> eventParameters = new HashMap<String, Object>();
        eventParameters.put(username, action);

        YandexMetrica.reportEvent("duplicate transactions", eventParameters);

    }

    public static void printerEvent(String key, String value) {


        Map<String, Object> eventParameters = new HashMap<String, Object>();
        eventParameters.put(key, value);

        YandexMetrica.reportEvent("printer issue", eventParameters);

    }

    public String getTimeDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();
        System.out.println("---------> endDate.getTime() : " + endDate.getTime());
        System.out.println("---------> startDate.getTime() : " + startDate.getTime());
        System.out.println("---------> different : " + different);

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

    public static boolean checkIfVerifyIsPermittedNow() {

        long lastVerifyRequestTime = -100;
        try {
            lastVerifyRequestTime = Long.parseLong(SharedPreferencesRepository.getValue(Constants.LAST_VERIFY_REQUEST_TIME, "-100"));
        } catch (Exception e) {
            e.printStackTrace();
            Assistant.eventForDuplicateTransactions(SharedPreferencesRepository.getValue(Constants.USERNAME), "error: lastVerifyRequestTime111");
        }

        long now = Long.parseLong(String.valueOf(new Date().getTime()));
        long difference = now - lastVerifyRequestTime;

        boolean canVerify = lastVerifyRequestTime == -100 || difference > 3000;

        if (!canVerify) {
            Assistant.eventForDuplicateTransactions(SharedPreferencesRepository.getValue(Constants.USERNAME), "verify happened in " + difference + " milli second");
        }

        return canVerify;

    }

    public static void updateLastVerifyRequestTime() {
        SharedPreferencesRepository.setValue(Constants.LAST_VERIFY_REQUEST_TIME, String.valueOf(new Date().getTime()));
    }

    public static void updateLastBankResultTime() {
        SharedPreferencesRepository.setValue(Constants.LAST_BANK_RESULT_TIME, String.valueOf(new Date().getTime()));
    }

    public static void checkLastBankResultTime() {

        long lastBankResultTime = -100;
        try {
            lastBankResultTime = Long.parseLong(SharedPreferencesRepository.getValue(Constants.LAST_BANK_RESULT_TIME, "0"));
        } catch (Exception e) {
            e.printStackTrace();
            Assistant.eventForDuplicateTransactions(SharedPreferencesRepository.getValue(Constants.USERNAME), "error: lastVerifyRequestTime222");
        }

        long now = Long.parseLong(String.valueOf(new Date().getTime()));
        long a = (now - lastBankResultTime);

        if (lastBankResultTime != -100 && a < 3000) {
            Assistant.eventForDuplicateTransactions(SharedPreferencesRepository.getValue(Constants.USERNAME), "bank result happend in " + (now - lastBankResultTime) + " mili second");
        }

    }

    public static String getUnixTime() {

        return Long.toString(System.currentTimeMillis() / 1000L);

    }

    @SuppressLint("HardwareIds")
    public static String getSerialNumber() {

        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.O)
            return android.os.Build.SERIAL;

        return "serial";

    }

    public static void createNotification(Context context, String title, String message) {

        Intent intent = new Intent(context, SplashActivity.class);

        // Assign channel ID
        String channel_id = "notification_channel" + new Date().getTime();
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
            builder = builder.setContent(getCustomDesign(context, title, message));
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

    private static RemoteViews getCustomDesign(Context context, String title,
                                               String message) {
        @SuppressLint("RemoteViewLayout") RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.title, title);
        remoteViews.setTextViewText(R.id.message, message);
        remoteViews.setImageViewResource(R.id.icon, R.drawable.ic_launcher);
        return remoteViews;
    }

    public static void makeSoundAndVibrate(Context context) {

//         Vibrator vib;
        MediaPlayer mp;

        mp = MediaPlayer.create(context, R.raw.ding);
//        vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//        vib.vibrate(500);
        mp.start();

    }

    public static boolean isValidMobile(String inputString) {

        return true;

//        HashMap<Character, Integer> charCountMap = new HashMap();
//
//        char[] strArray = inputString.toCharArray();
//
//        for (char c : strArray) {
//            if (charCountMap.containsKey(c))
//                charCountMap.put(c, charCountMap.get(c) + 1);
//            else
//                charCountMap.put(c, 1);
//        }
//
//        for (Map.Entry entry : charCountMap.entrySet())
//            if (((int) entry.getValue()) > 5)
//                return false;
//        return true;
    }

    public static String getImprestStatus(String englishStatus) {
        switch (englishStatus) {
            case "watchman_added":
                return "ثبت شده توسط پارکبان";
            case "supervisor_accepted":
                return "درانتظار تایید";
            case "supervisor_rejected":
            case "financial_rejected":
                return "رد شده";
            case "financial_accepted":
                return "تایید شده";
            default:
                return englishStatus;
        }
    }

    public static String getVacationStatus(String englishStatus) {
        switch (englishStatus) {
            case "watchman_added":
                return "ثبت شده توسط پارکبان";
            case "supervisor_accepted":
                return "درانتظار تایید";
            case "supervisor_rejected":
            case "main_supervisor_rejected":
                return "رد شده";
            case "main_supervisor_accepted":
                return "تایید شده";
            default:
                return englishStatus;
        }
    }

    public String numberFormat(String s) {
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        return formatter.format(Integer.parseInt(s));
    }
}
