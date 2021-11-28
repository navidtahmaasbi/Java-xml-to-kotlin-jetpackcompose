package com.azarpark.watchman.web_service;

import com.azarpark.watchman.retrofit_remote.responses.GetCitiesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface API {

    @GET("/api/cities")
    Call<GetCitiesResponse> getCities();

}
