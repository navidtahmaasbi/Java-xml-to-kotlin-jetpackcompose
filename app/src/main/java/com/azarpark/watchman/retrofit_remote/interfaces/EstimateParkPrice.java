package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.responses.EstimateParkPriceResponse;
import com.azarpark.watchman.retrofit_remote.responses.PlacesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface EstimateParkPrice {

    @GET("/api/watchman/park/estimate/{id}")
    Call<EstimateParkPriceResponse> get(@Header("Authorization") String authToken, @Path("id") int id);

}
