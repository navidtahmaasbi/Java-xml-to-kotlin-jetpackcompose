package com.azarpark.watchman.models;

import androidx.annotation.NonNull;

public class MyDate {
    public int year, month,day;

    public MyDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    @NonNull
    public String toString(){
        return year + "-" + month +"-"+day;
    }
}
