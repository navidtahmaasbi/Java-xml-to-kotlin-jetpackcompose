package com.azarpark.watchman.utils;

import android.service.autofill.RegexValidator;

import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assistant {

    public static int SAMAN = 1, PASRIAN = 2 , SELECTED_PAYMENT = 1;

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

}
