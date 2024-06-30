package com.azarpark.watchman.web_service.bodies;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

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

    private RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("multipart/form-data"), value);
    }

    public Map<String, RequestBody> toPartMap() {
        Map<String, RequestBody> partMap = new HashMap<>();

        if (tag1 != null) partMap.put("tag1", createPartFromString(tag1));
        if (tag2 != null) partMap.put("tag2", createPartFromString(tag2));
        if (tag3 != null) partMap.put("tag3", createPartFromString(tag3));
        if (tag4 != null) partMap.put("tag4", createPartFromString(tag4));
        if (tag_type != null) partMap.put("tag_type", createPartFromString(tag_type));
        partMap.put("place_id", createPartFromString(String.valueOf(place_id)));
        partMap.put("street_id", createPartFromString(String.valueOf(street_id)));
        if (latitude != null) partMap.put("latitude", createPartFromString(latitude));
        if (longitude != null) partMap.put("longitude", createPartFromString(longitude));

        return partMap;
    }
}
