package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.responses.CreateTransactionResponse;
import com.azarpark.watchman.retrofit_remote.responses.SplashResponse;
import com.azarpark.watchman.retrofit_remote.responses.VerifyTransactionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CreateTransaction {

    @GET("/api/watchman/create_transaction/{plate_type}/{tag1}/{tag2}/{tag3}/{tag4}/{amount}?flag={transaction_type}")
    Call<CreateTransactionResponse> create(@Header("Authorization") String authToken,
                                           @Path("plate_type") String plateType,
                                           @Path("tag1") String tag1,
                                           @Path("tag2") String tag2,
                                           @Path("tag3") String tag3,
                                           @Path("tag4") String tag4,
                                           @Path("amount") int amount,
                                           @Query("transaction_type") int transactionType
    );

}
