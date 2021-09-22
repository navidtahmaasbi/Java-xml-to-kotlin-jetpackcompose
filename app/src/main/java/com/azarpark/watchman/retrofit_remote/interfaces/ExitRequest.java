package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.responses.DebtHistoryResponse;
import com.azarpark.watchman.retrofit_remote.responses.ExitRequestResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface ExitRequest {

    @GET("/api/watchman/exit_request/{plate_type}/{tag1}/{tag2}/{tag3}/{tag4}")
    Call<ExitRequestResponse> submit(@Header("Authorization") String authToken,
                                  @Path("plate_type") String plateType,
                                  @Path("tag1") String tag1,
                                  @Path("tag2") String tag2,
                                  @Path("tag3") String tag3,
                                  @Path("tag4") String tag4);

}
