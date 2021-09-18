package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.responses.PlacesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface GetPlaces {

    @GET("/api/watchman/places")
    Call<PlacesResponse> get(@Header("Authorization") String authToken);
}
