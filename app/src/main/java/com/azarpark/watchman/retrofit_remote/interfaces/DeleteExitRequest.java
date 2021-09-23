package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.responses.DeleteExitRequestResponse;
import com.azarpark.watchman.retrofit_remote.responses.ExitRequestResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface DeleteExitRequest {

    @GET("/api/watchman/exit_request/delete/{place_id}")
    Call<DeleteExitRequestResponse> delete(@Header("Authorization") String authToken,
                                           @Path("place_id") int place_id);

}
