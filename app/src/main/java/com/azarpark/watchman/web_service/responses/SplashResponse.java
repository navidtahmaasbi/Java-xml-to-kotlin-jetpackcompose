package com.azarpark.watchman.web_service.responses;

import com.azarpark.watchman.models.KeyValueModel;
import com.azarpark.watchman.models.TicketMessage;
import com.azarpark.watchman.models.Update;
import com.azarpark.watchman.models.Watchman;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class SplashResponse {

    public int success;
    public String msg, description;
    public String qr_url;
    public int refresh_time;
    public String telephone;
    public String pricing;
    public String sms_number;
    public String rules_url;
    public String about_us_url;
    public String guide_url;
    public Update update;
    public String print_description2;
    public ArrayList<KeyValueModel> watchman_detail;
    public Watchman watchman;
    public List<TicketMessage> messages;
}
