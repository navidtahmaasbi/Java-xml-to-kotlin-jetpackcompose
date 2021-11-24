package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.responses.GetCitiesResponse;
import com.azarpark.watchman.retrofit_remote.responses.SplashResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface Splash {

    @GET("/api/watchman/splash")
    Call<SplashResponse> get(@Header("Authorization") String authToken, @Query("version_code") int versionCode);

}
