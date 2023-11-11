package com.azarpark.watchman.models;

public class Place {
    public int id;
    public int number;
    public int street_id;
    public int is_active;
    public String status;
    public int has_discount = 0;
    public int watchman_id;
    public Object user_id;
    public int car_id;
    public String start;
    public String end;
    public int price;
    public int period;
    public Object is_requested;
    public Object requested_person;
    public Object requested_id;
    public String tag1;
    public String tag2;
    public String tag3;
    public String tag4;
    public EstimatePrice estimate_price;
    public ExitRequest exit_request;
    public Car car;
    public Watchman watchman;

    public void addTag(String tag1,String tag2,String tag3,String tag4){
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.tag4 = tag4;
    }

    public String getPlateString() {

        if (tag2 == null || tag2.isEmpty())
            return tag1 + "ارس";

        if (tag3 == null || tag3.isEmpty())
            return tag1 + "ارس" + tag2;

        return tag1 + tag2 + tag3 +"ایران"+ tag4;


    }
}
