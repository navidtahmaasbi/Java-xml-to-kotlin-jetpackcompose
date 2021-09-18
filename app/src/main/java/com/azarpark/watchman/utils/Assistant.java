package com.azarpark.watchman.utils;

import android.service.autofill.RegexValidator;

public class Assistant {

    public boolean isMobile(String mobile){

        return mobile.matches("^09\\d{9}$");

    }

    public boolean isPassword(String password){

        return password.length() < 4;

    }

}
