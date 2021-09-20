package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;
import com.azarpark.watchman.retrofit_remote.responses.ExitParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.ParkResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ExitPark {

    @GET("/api/watchman/park/free/{placeID}")
    Call<ExitParkResponse> exit(@Header("Authorization") String authToken, @Path("placeID") int placeID);
}
