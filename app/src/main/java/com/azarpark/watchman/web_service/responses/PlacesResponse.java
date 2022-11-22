package com.azarpark.watchman.web_service.responses;

import com.azarpark.watchman.models.Notification;
import com.azarpark.watchman.models.Update;
import com.azarpark.watchman.models.Watchman;

import java.util.ArrayList;

public class PlacesResponse {

    public int success;
    public String msg;
    public String description;
    public Watchman watchman;
    public String can_detect;
    public Update update;
    public String qr_url;
    public int refresh_time;
    public String telephone;
    public String pricing;
    public String sms_number;
    public ArrayList<Notification> notifications;

}
