package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.responses.DebtHistoryResponse;
import com.azarpark.watchman.retrofit_remote.responses.GetCitiesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface GetCities {

    @GET("api/cities")
    Call<GetCitiesResponse> get(@Header("Authorization") String authToken);

}
