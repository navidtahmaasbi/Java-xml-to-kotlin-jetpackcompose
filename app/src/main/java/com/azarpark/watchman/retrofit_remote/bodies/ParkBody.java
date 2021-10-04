package com.azarpark.watchman.retrofit_remote.bodies;

public class ParkBody {

    String tag1 = null, tag2 = null, tag3 = null, tag4 = null, tag_type = null, name = null, color = null;
    int place_id = 0, street_id = 0;

    public ParkBody(String tag1, String tag2, String tag3, String tag4, String tag_type, int place_id, int street_id) {
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.tag4 = tag4;
        this.tag_type = tag_type;
        this.place_id = place_id;
        this.street_id = street_id;
    }

    public ParkBody(String tag1, String tag2, String tag_type, int place_id, int street_id) {
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag_type = tag_type;
        this.place_id = place_id;
        this.street_id = street_id;
    }

    public ParkBody(String tag1, String tag_type, int place_id, int street_id) {
        this.tag1 = tag1;
        this.tag_type = tag_type;
        this.place_id = place_id;
        this.street_id = street_id;
    }

    public String getTag1() {
        return tag1;
    }

    public String getTag2() {
        return tag2;
    }

    public String getTag3() {
        return tag3;
    }

    public String getTag4() {
        return tag4;
    }

    public String getTag_type() {
        return tag_type;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getPlace_id() {
        return place_id;
    }

    public int getStreet_id() {
        return street_id;
    }
}
