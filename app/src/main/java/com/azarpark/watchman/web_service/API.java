package com.azarpark.watchman.web_service;

import com.azarpark.watchman.models.AddMobieToPlateResponse;
import com.azarpark.watchman.models.CreateImpressedResponse;
import com.azarpark.watchman.models.CreateTicketMessageResponse;
import com.azarpark.watchman.models.CreateTicketResponse;
import com.azarpark.watchman.models.CreateVacationResponse;
import com.azarpark.watchman.models.GetImprestsResponse;
import com.azarpark.watchman.models.GetTicketResponse;
import com.azarpark.watchman.models.GetTicketsResponse;
import com.azarpark.watchman.models.GetVacationsResponse;
import com.azarpark.watchman.models.IncomeStatisticsResponse;
import com.azarpark.watchman.models.IncomeStatisticsResponse02;
import com.azarpark.watchman.models.RemoveImpressedResponse;
import com.azarpark.watchman.models.RemoveVacationResponse;
import com.azarpark.watchman.models.WatchmanTimeResponse;
import com.azarpark.watchman.web_service.bodies.LoginBody;
import com.azarpark.watchman.web_service.bodies.ParkBody;
import com.azarpark.watchman.web_service.responses.CreateTransactionResponse;
import com.azarpark.watchman.web_service.responses.DebtHistoryResponse;
import com.azarpark.watchman.web_service.responses.DeleteExitRequestResponse;
import com.azarpark.watchman.web_service.responses.DiscountsResponse;
import com.azarpark.watchman.web_service.responses.EstimateParkPriceResponse;
import com.azarpark.watchman.web_service.responses.ExitParkResponse;
import com.azarpark.watchman.web_service.responses.ExitRequestResponse;
import com.azarpark.watchman.web_service.responses.GetCitiesResponse;
import com.azarpark.watchman.web_service.responses.LoginResponse;
import com.azarpark.watchman.web_service.responses.LogoutResponse;
import com.azarpark.watchman.web_service.responses.ParkResponse;
import com.azarpark.watchman.web_service.responses.ParkedPlatePlaceIDResponse;
import com.azarpark.watchman.web_service.responses.PlacesResponse;
import com.azarpark.watchman.web_service.responses.SendExitCodeResponse;
import com.azarpark.watchman.web_service.responses.SplashResponse;
import com.azarpark.watchman.web_service.responses.VerifyTransactionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface API {

    @GET("/api/cities")
    Call<GetCitiesResponse> getCities();

    @GET("/api/watchman/splash")
    Call<SplashResponse> getSplash(
            @Header("Authorization") String authToken,
            @Query("version_code") int versionCode,
            @Query("serial_number") String serialNumber
    );

    @POST("oauth/token")
    Call<LoginResponse> login(@Body LoginBody body);

    @GET("/api/watchman/places")
    Call<PlacesResponse> getPlaces(@Header("Authorization") String authToken);

    @GET("/api/watchman/discount")
    Call<DiscountsResponse> getDiscounts(@Header("Authorization") String authToken);

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

    @GET("/api/watchman/times/submit/{type}/{latitude}/{longitude}")
    Call<WatchmanTimeResponse> setWatchmanTime(@Header("Authorization") String authToken,
                                               @Path("type") String type,
                                               @Path("latitude") String latitude,
                                               @Path("longitude") String longitude
    );

    @GET("/api/watchman/parking_status/{plate_type}/{tag1}/{tag2}/{tag3}/{tag4}")
    Call<ParkedPlatePlaceIDResponse> getParkedPlatePlaceId(@Header("Authorization") String authToken,
                                                           @Path("plate_type") String plateType,
                                                           @Path("tag1") String tag1,
                                                           @Path("tag2") String tag2,
                                                           @Path("tag3") String tag3,
                                                           @Path("tag4") String tag4
    );

    @GET("/api/watchman/income/statistics")
    Call<IncomeStatisticsResponse> getIncomeStatistics(@Header("Authorization") String authToken);

    @GET("/api/watchman/income/statistics2")
    Call<IncomeStatisticsResponse02> getIncomeStatistics02(@Header("Authorization") String authToken);

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

    //api/watchman/create_transaction/simple/62/%D9%85/791/35/300000?flag=10&object_id=6&object_type=App\Models\Discount

    @GET("/api/watchman/create_transaction/{plate_type}/{tag1}/{tag2}/{tag3}/{tag4}/{amount}")
    Call<CreateTransactionResponse> createTransactionForDiscount(@Header("Authorization") String authToken,
                                                                 @Path("plate_type") String plateType,
                                                                 @Path("tag1") String tag1,
                                                                 @Path("tag2") String tag2,
                                                                 @Path("tag3") String tag3,
                                                                 @Path("tag4") String tag4,
                                                                 @Path("amount") int amount,
                                                                 @Query("flag") int transactionType, //10
                                                                 @Query("object_id") int discountId,
                                                                 @Query("object_type") String type // App\Models\Discount
    );

    @GET("/api/watchman/exit_request/delete/{place_id}")
    Call<DeleteExitRequestResponse> deleteExitRequest(@Header("Authorization") String authToken,
                                                      @Path("place_id") int place_id);

    @GET("/api/watchman/park/estimate/{id}")
    Call<EstimateParkPriceResponse> estimatePArkPrice(@Header("Authorization") String authToken, @Path("id") int id);

    @GET("/api/watchman/imprest/add2/{amount}/{bank_account_type}/{bank_account_number}/{bank_account_name}")
    Call<CreateImpressedResponse> createImprest(@Header("Authorization") String authToken,
                                                @Path("amount") String amount,
                                                @Path("bank_account_type") String type,
                                                @Path("bank_account_number") String bankAccountNumber,
                                                @Path("bank_account_name") String bankAccountName);

    @FormUrlEncoded
    @POST("/api/watchman/watchman_ticket/store")
    Call<CreateTicketResponse> createTicket(@Header("Authorization") String authToken,
                                            @Field("subject") String title,
                                            @Field("description") String description
    );

    @FormUrlEncoded
    @POST("/api/watchman/watchman_ticket/detail/store/{ticket_id}")
    Call<CreateTicketMessageResponse> createTicketMessage(@Header("Authorization") String authToken,
                                                          @Path("ticket_id") int ticket_id,
                                                          @Field("description") String description
    );

    @GET("/api/watchman/imprest/remove/{id}")
    Call<RemoveImpressedResponse> deleteImprest(@Header("Authorization") String authToken, @Path("id") int id);

    @GET("/api/watchman/imprests/1000/0")
    Call<GetImprestsResponse> getImprests(@Header("Authorization") String authToken);

    @GET("/api/watchman/watchman_ticket/1000/0")
    Call<GetTicketsResponse> getTickets(@Header("Authorization") String authToken);

    @GET("/api/watchman/watchman_ticket/{ticket_id}")
    Call<GetTicketResponse> getTicket(@Header("Authorization") String authToken,
                                      @Path("ticket_id") int ticket_id);


    @GET("/api/watchman/vacation/add/{date}/{type}/{start}/{end}/{vacation_type}")
    Call<CreateVacationResponse> createVacation(@Header("Authorization") String authToken,
                                                @Path("date") String date,
                                                @Path("type") String timeType,
                                                @Path("start") String start,
                                                @Path("end") String end,
                                                @Path("vacation_type") String type,
                                                @Query("description") String beduneHugugReason
    );

    @GET("/api/watchman/vacation/remove/{id}")
    Call<RemoveVacationResponse> deleteVacation(@Header("Authorization") String authToken, @Path("id") int id);

    @GET("/api/watchman/vacation/1000/0")
    Call<GetVacationsResponse> getVacations(@Header("Authorization") String authToken);


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


          //api/watchman/verify/300000/5391785732/1111/-1/1
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


    @GET("/api/watchman/phone/add/{car_type}/{tag1}/{tag2}/{tag3}/{tag4}/{phone}")
    Call<AddMobieToPlateResponse> addMobileToPlate(@Header("Authorization") String authToken,
                                                   @Path("car_type") String plateType,
                                                   @Path("tag1") String tag1,
                                                   @Path("tag2") String tag2,
                                                   @Path("tag3") String tag3,
                                                   @Path("tag4") String tag4,
                                                   @Path("phone") String phone);

    @GET("/api/watchman/phone/add/{car_type}/{tag1}/{tag2}/{tag3}/{tag4}/{phone}")
    Call<AddMobieToPlateResponse> addMobileToPlate(@Header("Authorization") String authToken,
                                                   @Path("car_type") String plateType,
                                                   @Path("tag1") String tag1,
                                                   @Path("tag2") String tag2,
                                                   @Path("tag3") String tag3,
                                                   @Path("tag4") String tag4,
                                                   @Path("phone") String phone,
                                                   @Query("is_wage") int isWage);
}
