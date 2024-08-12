package com.azarpark.watchman.web_service.responses;

import androidx.annotation.NonNull;

import java.util.List;

public class ChargeItem {
    public int id;
    public int show_property;
    public String subject;
    public String value;
    public String type;


    public static String encode(List<ChargeItem> items){
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (ChargeItem item : items) {
            if(first){
                first = false;
            }
            else {
                builder.append(",");
            }
            builder.append(item.value);
        }

        return builder.toString();
    }
    public static String[] decode(String values){
        return values.split(",");
    }
}

