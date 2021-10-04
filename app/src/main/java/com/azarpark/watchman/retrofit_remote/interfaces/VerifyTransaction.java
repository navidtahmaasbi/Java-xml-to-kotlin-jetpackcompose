package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.responses.DebtHistoryResponse;
import com.azarpark.watchman.retrofit_remote.responses.VerifyTransactionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface VerifyTransaction {

    @GET("/api/watchman/verify/{amount}/{our_token}/{bank_token}/{place_id}")
    Call<VerifyTransactionResponse> verify(@Header("Authorization") String authToken,
                                           @Path("amount") String amount,
                                           @Path("our_token") String our_token,
                                           @Path("bank_token") String bank_token,
                                           @Path("place_id") int place_id
    );

}
