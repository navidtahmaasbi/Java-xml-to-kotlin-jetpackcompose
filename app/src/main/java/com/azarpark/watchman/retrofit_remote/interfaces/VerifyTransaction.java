package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.responses.DebtHistoryResponse;
import com.azarpark.watchman.retrofit_remote.responses.VerifyTransactionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface VerifyTransaction {

    @GET("/api/watchman/verify/{plate_type}/{tag1}/{tag2}/{tag3}/{tag4}/{amount}/{transaction_id}/{place_id}")
    Call<VerifyTransactionResponse> verify(@Header("Authorization") String authToken,
                                        @Path("plate_type") String plateType,
                                        @Path("tag1") String tag1,
                                        @Path("tag2") String tag2,
                                        @Path("tag3") String tag3,
                                        @Path("tag4") String tag4,
                                        @Path("amount") String amount,
                                        @Path("transaction_id") String transaction_id,
                                        @Path("place_id") int place_id
    );

}
