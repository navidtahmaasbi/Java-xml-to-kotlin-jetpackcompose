package com.azarpark.watchman.web_service.responses;

import com.azarpark.watchman.models.City;

import java.util.ArrayList;

public class GetCitiesResponse {

    public String success;
    public String msg;
    public String description;
    public ArrayList<City> items;
}
