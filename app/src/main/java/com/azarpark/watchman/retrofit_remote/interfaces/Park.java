package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;
import com.azarpark.watchman.retrofit_remote.responses.ParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.PlacesResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface Park {

    @POST("/api/watchman/park")
    Call<ParkResponse> park(@Header("Authorization") String authToken, @Body ParkBody body);
}
