package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.responses.GetCitiesResponse;
import com.azarpark.watchman.retrofit_remote.responses.SplashResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface Splash {

    @GET("/api/watchman/splash")
    Call<SplashResponse> get(@Header("Authorization") String authToken);

}
