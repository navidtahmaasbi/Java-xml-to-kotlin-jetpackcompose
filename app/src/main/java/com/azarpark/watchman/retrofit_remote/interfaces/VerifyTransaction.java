package com.azarpark.watchman.retrofit_remote.interfaces;

import com.azarpark.watchman.retrofit_remote.bodies.VerifyTransactionBody;
import com.azarpark.watchman.retrofit_remote.responses.DebtHistoryResponse;
import com.azarpark.watchman.retrofit_remote.responses.VerifyTransactionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface VerifyTransaction {

    @GET("/api/watchman/verify/{amount}/{our_token}/{bank_token}/{place_id}/{status}")
    Call<VerifyTransactionResponse> verify(@Header("Authorization") String authToken,
                                           @Path("amount") String amount,
                                           @Path("our_token") String our_token,
                                           @Path("bank_token") String bank_token,
                                           @Path("place_id") int place_id,
                                           @Path("status") int status,
                                           @Query("bank_type") String bank_type,
                                           @Query("state") String state,
                                           @Query("card_number") String card_number,
                                           @Query("bank_datetime") String bank_datetime,
                                           @Query("trace_number") String trace_number,
                                           @Query("result_message") String result_message
                                           );

}


