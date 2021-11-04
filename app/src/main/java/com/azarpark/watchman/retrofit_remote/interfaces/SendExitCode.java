package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.responses.ExitRequestResponse;
import com.azarpark.watchman.retrofit_remote.responses.SendExitCodeResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface SendExitCode {

    @GET("api/watchman/lock/sms/{code}")
    Call<SendExitCodeResponse> send(@Header("Authorization") String authToken,
                                    @Path("code") int code);

}
