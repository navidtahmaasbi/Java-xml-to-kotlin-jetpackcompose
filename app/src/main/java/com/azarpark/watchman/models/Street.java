package com.azarpark.watchman.models;

import java.util.ArrayList;
import java.util.List;

public class Street {
    public int id;
    public String name;
    public String area;
    public Object latitude;
    public Object longitude;
    public int price1;
    public int price2;
    public int radius;
    public Pivot pivot;
    public ArrayList<Place> places;

    public class Pivot{
        public int watchman_id;
        public int street_id;
    }

}
