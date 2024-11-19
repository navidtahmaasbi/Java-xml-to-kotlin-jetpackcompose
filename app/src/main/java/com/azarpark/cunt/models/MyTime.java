package com.azarpark.cunt.models;

import androidx.annotation.NonNull;

public class MyTime {
    public int hour, minute;

    public MyTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    @NonNull
    public String toString(){
        return hour + ":" + minute;
    }
}
