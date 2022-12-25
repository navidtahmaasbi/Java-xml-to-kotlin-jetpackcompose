package com.azarpark.watchman.web_service.bodies;

public class ParkBody {

    String tag1 = null, tag2 = null, tag3 = null, tag4 = null, tag_type = null, name = null, color = null;
    int place_id = 0, street_id = 0;
    String latitude, longitude;

    public ParkBody(String tag1, String tag2, String tag3, String tag4, String tag_type, int place_id, int street_id) {
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.tag4 = tag4;
        this.tag_type = tag_type;
        this.place_id = place_id;
        this.street_id = street_id;
    }

    public ParkBody(String tag1, String tag2, String tag3, String tag4, String tag_type, int place_id, int street_id, String latitude, String longitude) {
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.tag4 = tag4;
        this.tag_type = tag_type;
        this.place_id = place_id;
        this.street_id = street_id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ParkBody(String tag1, String tag2, String tag_type, int place_id, int street_id) {
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag_type = tag_type;
        this.place_id = place_id;
        this.street_id = street_id;
    }

    public ParkBody(String tag1, String tag2, String tag_type, int place_id, int street_id, String latitude, String longitude) {
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag_type = tag_type;
        this.place_id = place_id;
        this.street_id = street_id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ParkBody(String tag1, String tag_type, int place_id, int street_id) {
        this.tag1 = tag1;
        this.tag_type = tag_type;
        this.place_id = place_id;
        this.street_id = street_id;
    }

    public ParkBody(String tag1, String tag_type, int place_id, int street_id, String latitude, String longitude) {
        this.tag1 = tag1;
        this.tag_type = tag_type;
        this.place_id = place_id;
        this.street_id = street_id;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
