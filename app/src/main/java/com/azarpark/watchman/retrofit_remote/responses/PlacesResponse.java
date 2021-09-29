package com.azarpark.watchman.retrofit_remote.responses;

import com.azarpark.watchman.models.Update;
import com.azarpark.watchman.models.Watchman;

public class PlacesResponse {

    public String success;
    public String msg;
    public Watchman watchman;
    public Update update;
    public String qr_url;
    public int refresh_time;

}
