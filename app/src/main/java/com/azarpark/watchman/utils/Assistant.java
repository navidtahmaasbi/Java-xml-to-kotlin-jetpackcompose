package com.azarpark.watchman.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.service.autofill.RegexValidator;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.azarpark.watchman.download_utils.DownloadController;

import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import retrofit2.http.Url;
import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

public class Assistant {

    public static int SAMAN = 1, PASRIAN = 2, SELECTED_PAYMENT = 2;
    public static int MIN_PRICE_FOR_PAYMENT = 100;
    public static String NON_CHARGE_SHABA = "IR540550100470106230710001", CHARGE_SHABA = "IR270550100470106230710002";

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
        return matcher.find();  // it's RTL

//        int c = s.codePointAt(0);
//        if (c >= 0x0600 && c <= 0x06E0)
//            return true;

    }

    public void showSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void hideSoftKeyboard(Activity activity) {

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);

    }

    public Bitmap qrGenerator(String value) {

        // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
        QRGEncoder qrgEncoder = new QRGEncoder(value, null, QRGContents.Type.TEXT, 512);
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
        sb.append(pdate.getShMonth());
        sb.append("-");
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

    public boolean VPNEnabled (Context mContext){

        ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = cm.getAllNetworks();

        boolean vpnIsOpen = false;

        Log.i("TAG", "Network count: " + networks.length);
        for(int i = 0; i < networks.length; i++) {

            NetworkCapabilities caps = cm.getNetworkCapabilities(networks[i]);

            Log.i("TAG", "Network " + i + ": " + networks[i].toString());
            Log.i("TAG", "VPN transport is: " + caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
            Log.i("TAG", "NOT_VPN capability is: " + caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN));

            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                vpnIsOpen = true;

        }

        return  vpnIsOpen;

    }

}
