package com.azarpark.watchman.web_service;

import com.azarpark.watchman.models.AddMobieToPlateResponse;
import com.azarpark.watchman.web_service.bodies.LoginBody;
import com.azarpark.watchman.web_service.bodies.ParkBody;
import com.azarpark.watchman.web_service.responses.CreateTransactionResponse;
import com.azarpark.watchman.web_service.responses.DebtHistoryResponse;
import com.azarpark.watchman.web_service.responses.DeleteExitRequestResponse;
import com.azarpark.watchman.web_service.responses.EstimateParkPriceResponse;
import com.azarpark.watchman.web_service.responses.ExitParkResponse;
import com.azarpark.watchman.web_service.responses.ExitRequestResponse;
import com.azarpark.watchman.web_service.responses.GetCitiesResponse;
import com.azarpark.watchman.web_service.responses.LoginResponse;
import com.azarpark.watchman.web_service.responses.LogoutResponse;
import com.azarpark.watchman.web_service.responses.ParkResponse;
import com.azarpark.watchman.web_service.responses.PlacesResponse;
import com.azarpark.watchman.web_service.responses.SendExitCodeResponse;
import com.azarpark.watchman.web_service.responses.SplashResponse;
import com.azarpark.watchman.web_service.responses.VerifyTransactionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface API {

    @GET("/api/cities")
    Call<GetCitiesResponse> getCities();

    @GET("/api/watchman/splash")
    Call<SplashResponse> getSplash(@Header("Authorization") String authToken, @Query("version_code") int versionCode);

    @POST("oauth/token")
    Call<LoginResponse> login(@Body LoginBody body);

    @GET("/api/watchman/places")
    Call<PlacesResponse> getPlaces(@Header("Authorization") String authToken);

    @POST("/api/watchman/park")
    Call<ParkResponse> parkCar(@Header("Authorization") String authToken, @Body ParkBody body);

    @GET("/api/watchman/car/history/{plate_type}/{tag1}/{tag2}/{tag3}/{tag4}/{limit}/{offset}")
    Call<DebtHistoryResponse> getCarDebtHistory(@Header("Authorization") String authToken,
                                  @Path("plate_type") String plateType,
                                  @Path("tag1") String tag1,
                                  @Path("tag2") String tag2,
                                  @Path("tag3") String tag3,
                                  @Path("tag4") String tag4,
                                  @Path("limit") int limit,
                                  @Path("offset") int offset
    );

    @GET("/api/watchman/create_transaction/{plate_type}/{tag1}/{tag2}/{tag3}/{tag4}/{amount}")
    Call<CreateTransactionResponse> createTransaction(@Header("Authorization") String authToken,
                                           @Path("plate_type") String plateType,
                                           @Path("tag1") String tag1,
                                           @Path("tag2") String tag2,
                                           @Path("tag3") String tag3,
                                           @Path("tag4") String tag4,
                                           @Path("amount") int amount,
                                           @Query("flag") int transactionType
    );

    @GET("/api/watchman/exit_request/delete/{place_id}")
    Call<DeleteExitRequestResponse> deleteExitRequest(@Header("Authorization") String authToken,
                                           @Path("place_id") int place_id);

    @GET("/api/watchman/park/estimate/{id}")
    Call<EstimateParkPriceResponse> estimatePArkPrice(@Header("Authorization") String authToken, @Path("id") int id);


    @GET("/api/watchman/park/free/{placeID}")
    Call<ExitParkResponse> exitPark(@Header("Authorization") String authToken, @Path("placeID") int placeID);


    @GET("/api/watchman/exit_request/{plate_type}/{tag1}/{tag2}/{tag3}/{tag4}")
    Call<ExitRequestResponse> exitRequest(@Header("Authorization") String authToken,
                                     @Path("plate_type") String plateType,
                                     @Path("tag1") String tag1,
                                     @Path("tag2") String tag2,
                                     @Path("tag3") String tag3,
                                     @Path("tag4") String tag4);

    @GET("/api/watchman/logout")
    Call<LogoutResponse> logout(@Header("Authorization") String authToken);


    @GET("api/watchman/lock/sms/{code}")
    Call<SendExitCodeResponse> sendExitCode(@Header("Authorization") String authToken,
                                    @Path("code") int code);


    @GET("/api/watchman/verify/{amount}/{our_token}/{bank_token}/{place_id}/{status}")
    Call<VerifyTransactionResponse> verifyTransaction(@Header("Authorization") String authToken,
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


//    @GET("/api/watchman/plate_add_mobile")
//    Call<AddMobieToPlateResponse> addMobileToPlate(@Header("Authorization") String authToken,
//                                                   @Query("phone") String mobile,
//                                                   @Query("tag1") String tag1,
//                                                   @Query("tag2") String tag2,
//                                                   @Query("tag3") String tag3,
//                                                   @Query("tag4") String tag4
//    );

    @GET("/api/watchman/phone/add/{car_type}/{tag1}/{tag2}/{tag3}/{tag4}/{phone}")
    Call<AddMobieToPlateResponse> addMobileToPlate(@Header("Authorization") String authToken,
                                                      @Path("car_type") String plateType,
                                                      @Path("tag1") String tag1,
                                                      @Path("tag2") String tag2,
                                                      @Path("tag3") String tag3,
                                                      @Path("tag4") String tag4,
                                                      @Path("phone") String phone);






}
