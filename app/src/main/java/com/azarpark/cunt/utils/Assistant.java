package com.azarpark.cunt.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.azarpark.cunt.R;
import com.azarpark.cunt.activities.SplashActivity;
import com.azarpark.cunt.databinding.PrintContentPartBinding;
import com.azarpark.cunt.enums.PlateType;
import com.azarpark.cunt.models.MyDate;
import com.azarpark.cunt.models.MyTime;
import com.azarpark.cunt.models.Place;
import com.azarpark.cunt.models.Plate;
import com.yandex.metrica.YandexMetrica;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import kotlin.random.Random;
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

        if (!mobile.matches("^09\\d{9}$"))
            return false;

        if (!mobile.startsWith("09"))
            return false;

        return true;

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
    public static boolean isNumber02(String amount) {

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

    public static String formatAmount02(int num) {
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

    public static String translateToTomanInWords(String amount){
        String minValExceptionMessage = "مبلغ نمیتواند کمتر از " + formatAmount02(Constants.MIN_PRICE_FOR_PAYMENT) +" تومان باشد.";
        String maxValExceptionMessage = "مبلغ نمیتواند بیشتر از " + formatAmount02(Constants.MAX_PRICE_FOR_PAYMENT) +" تومان باشد.";
        String formatExceptionMessage = "مقدار صحیح وارد کنید";

        String stringVal = amount.replaceAll(",","");

        if (stringVal.isEmpty())
            return "";
        if (!isNumber02(stringVal)){
            return formatExceptionMessage;
        }
        int intVal = Integer.parseInt(stringVal);
        if (intVal < Constants.MIN_PRICE_FOR_PAYMENT){
            return minValExceptionMessage;
        }
        if (intVal > Constants.MAX_PRICE_FOR_PAYMENT){
            return maxValExceptionMessage;
        }
        StringBuilder sb = new StringBuilder();
        if (intVal == 1000)
            sb.append("هزار");
        else if ( intVal == 1000000)
            sb.append("یک میلیون");
        else {
            sb.append(numToWords(intVal/1000));
            sb.append(" هزار");
            if (intVal%1000 != 0){
                sb.append(" و ");
                sb.append(numToWords(intVal%1000));
            }
        }

        sb.append(" تومان");

        return sb.toString();

    }

    public static String numToWords(int num){
        StringBuilder sb = new StringBuilder("");
        int sadgan = num/100;
        int dahgan = (num - (sadgan * 100))/10;
        int yekan = (num - ((sadgan * 100) + (dahgan * 10)));
        if (sadgan != 0){
            sb.append(translateNumber(sadgan*100));
            if (dahgan != 0 || yekan != 0)
                sb.append(" و ");
        }
        if (dahgan != 0 && dahgan != 1){
            sb.append(translateNumber(dahgan*10));
            if (yekan != 0)
                sb.append(" و ");
        }else if (dahgan == 1)
            sb.append(translateNumber((dahgan*10) + yekan));
        if (yekan != 0 && dahgan != 1)
            sb.append(translateNumber(yekan));

        return sb.toString();

    }

    private static String translateNumber(int number){
        Map<Integer,String> dictionary = new HashMap<>();
        dictionary.put(1,"یک");
        dictionary.put(2,"دو");
        dictionary.put(3,"سه");
        dictionary.put(4,"چهار");
        dictionary.put(5,"پنج");
        dictionary.put(6,"شش");
        dictionary.put(7,"هفت");
        dictionary.put(8,"هشت");
        dictionary.put(9,"نه");
        dictionary.put(10,"ده");
        dictionary.put(11,"یازده");
        dictionary.put(12,"دوازده");
        dictionary.put(13,"سیزده");
        dictionary.put(14,"چهارده");
        dictionary.put(15,"پانزده");
        dictionary.put(16,"شانزده");
        dictionary.put(17,"هفده");
        dictionary.put(18,"هجده");
        dictionary.put(19,"نوزده");
        dictionary.put(20,"بیست");
        dictionary.put(30,"سی");
        dictionary.put(40,"چهل");
        dictionary.put(50,"پنجاه");
        dictionary.put(60,"شصت");
        dictionary.put(70,"هفتاد");
        dictionary.put(80,"هشتاد");
        dictionary.put(90,"نود");
        dictionary.put(100,"صد");
        dictionary.put(200,"دویست");
        dictionary.put(300,"سیصد");
        dictionary.put(400,"چهارصد");
        dictionary.put(500,"پانصد");
        dictionary.put(600,"ششصد");
        dictionary.put(700,"هفتصد");
        dictionary.put(800,"هشتصد");
        dictionary.put(900,"نهصد");

        return dictionary.get(number);
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

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O)
            return Build.SERIAL;

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
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
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


    public static String arrToStr(List arr)
    {
        if(arr == null)
            return "";

        StringBuilder sb = new StringBuilder();
        boolean firstItem = true;
        for (Object o : arr) {
            if(firstItem)
            {
                firstItem = false;
            }
            else
            {
                sb.append("\n");
            }
            sb.append(o.toString());
        }

        return sb.toString();
    }

    public static Bitmap viewToBitmap(View v)
    {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
    }

    public static boolean inflateHTML(String head, String body, Context context, ViewGroup parent, boolean attachToParent) {
        PrintContentPartBinding layout = PrintContentPartBinding.inflate(LayoutInflater.from(context), parent, attachToParent);

        if (head != null && !head.isEmpty())
        {
            layout.title.setVisibility(View.VISIBLE);
            layout.title.setText(head);
        }

        if (body != null && !body.isEmpty()) {
            layout.bodyWv.setVisibility(View.VISIBLE);
            layout.bodyWv.getSettings().setJavaScriptEnabled(false);
            layout.bodyWv.loadData(body, "text/html; charset=utf-8", "UTF-8");
            return true;
        }
        return false;
    }

    /**
     * example 12-b-345-67
     *
     * @param plateString
     * @return
     */
    public static boolean isIranPlate(String plateString){
        String pattern = "\\d{2}[a-zA-Z]\\d{5}";
        return plateString.matches(pattern);
    }

    /**
     * example: 12345
     *
     * @param plateString
     * @return
     */
    public static boolean isOldAras(String plateString){
        if(plateString.length() == 5)
        {
            try{
                Integer.parseInt(plateString);
                return true;
            }
            catch (Exception ignored){}
        }
        return false;
    }

    /**
     * example: 12345-67
     *
     * @param plateString
     * @return
     */
    public static boolean isNewAras(String plateString){
        if(plateString.length() == 7)
        {
            try{
                Integer.parseInt(plateString);
                return true;
            }
            catch (Exception ignored){}
        }
        return false;
    }

    public static Plate parse(String plateString){
        PlateType plateType = PlateType.simple;
        String tag1 = "", tag2 = "", tag3 = "", tag4 = "";
        boolean isPlateValid = false;

        if (Assistant.isIranPlate(plateString)) {
            Dictionary_char dictionary_char = new Dictionary_char();
            tag1 = plateString.substring(0, 2);
            tag2 = dictionary_char.get_persian_string(plateString.charAt(2));
            tag3 = plateString.substring(3, 6);
            tag4 = plateString.substring(6, 8);
            isPlateValid = true;
            plateType = PlateType.simple;
        }
        else if(Assistant.isNewAras(plateString))
        {
            tag1 = plateString.substring(0, 5);
            tag2 = plateString.substring(5);
            isPlateValid = true;
            plateType = PlateType.new_aras;
        }
        else if(Assistant.isOldAras(plateString))
        {
            tag1 = plateString;
            isPlateValid = true;
            plateType = PlateType.old_aras;
        }


        if(isPlateValid)
            return new Plate(plateType, tag1, tag2, tag3, tag4);

        return null;
    }

    public static String generateFilename(String format){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        return timeStamp + "_" + (Random.Default.nextInt(10000)) + "." + format;
    }

    public static File createCacheFile(Context context, String format){
        return new File(context.getCacheDir(), generateFilename(format));
    }

    public static File writeBitmapToFile(Bitmap bitmap, File file) throws IOException {
        //create a file to write bitmap data
        file.createNewFile();

        //Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();

        return file;
    }

    public static int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    // delays the execution of the function by the specified time
    public static void delay(long millis, Runnable task){
        new Handler().postDelayed(task, millis);
    }
}
