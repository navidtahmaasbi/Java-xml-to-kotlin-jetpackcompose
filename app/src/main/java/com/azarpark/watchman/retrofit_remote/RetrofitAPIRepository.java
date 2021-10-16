package com.azarpark.watchman.retrofit_remote;

import android.content.Context;

import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.retrofit_remote.bodies.LoginBody;
import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;
import com.azarpark.watchman.retrofit_remote.bodies.VerifyTransactionBody;
import com.azarpark.watchman.retrofit_remote.interfaces.CarDebtHistory;
import com.azarpark.watchman.retrofit_remote.interfaces.CreateTransaction;
import com.azarpark.watchman.retrofit_remote.interfaces.DeleteExitRequest;
import com.azarpark.watchman.retrofit_remote.interfaces.EstimateParkPrice;
import com.azarpark.watchman.retrofit_remote.interfaces.ExitPark;
import com.azarpark.watchman.retrofit_remote.interfaces.ExitRequest;
import com.azarpark.watchman.retrofit_remote.interfaces.GetCities;
import com.azarpark.watchman.retrofit_remote.interfaces.GetPlaces;
import com.azarpark.watchman.retrofit_remote.interfaces.Login;
import com.azarpark.watchman.retrofit_remote.interfaces.Logout;
import com.azarpark.watchman.retrofit_remote.interfaces.Park;
import com.azarpark.watchman.retrofit_remote.interfaces.Splash;
import com.azarpark.watchman.retrofit_remote.interfaces.VerifyTransaction;
import com.azarpark.watchman.retrofit_remote.responses.CreateTransactionResponse;
import com.azarpark.watchman.retrofit_remote.responses.DebtHistoryResponse;
import com.azarpark.watchman.retrofit_remote.responses.DeleteExitRequestResponse;
import com.azarpark.watchman.retrofit_remote.responses.EstimateParkPriceResponse;
import com.azarpark.watchman.retrofit_remote.responses.ExitParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.ExitRequestResponse;
import com.azarpark.watchman.retrofit_remote.responses.GetCitiesResponse;
import com.azarpark.watchman.retrofit_remote.responses.LoginResponse;
import com.azarpark.watchman.retrofit_remote.responses.LogoutResponse;
import com.azarpark.watchman.retrofit_remote.responses.ParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.PlacesResponse;
import com.azarpark.watchman.retrofit_remote.responses.SplashResponse;
import com.azarpark.watchman.retrofit_remote.responses.TestResponse;
import com.azarpark.watchman.retrofit_remote.responses.VerifyTransactionResponse;

import retrofit2.Callback;

public class RetrofitAPIRepository {

    Context context;

    public RetrofitAPIRepository(Context context) {
        this.context = context;
    }

    public void login(LoginBody body, Callback<LoginResponse> responseCallback) {

        Login request = RetrofitAPIClient.getClient(context).create(Login.class);

        request.login(body).enqueue(responseCallback);

    }

    public void getPlaces(String token, Callback<PlacesResponse> responseCallback) {

        GetPlaces request = RetrofitAPIClient.getClient(context).create(GetPlaces.class);

        request.get(token).enqueue(responseCallback);

    }

    public void getCities(String token, Callback<GetCitiesResponse> responseCallback) {

        GetCities request = RetrofitAPIClient.getInitialClient().create(GetCities.class);

        request.get(token).enqueue(responseCallback);

    }

    public void getSplashData(String token, Callback<SplashResponse> responseCallback) {

        Splash request = RetrofitAPIClient.getClient(context).create(Splash.class);

        request.get(token).enqueue(responseCallback);

    }

    public void createTransaction(String token, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int amount, Callback<CreateTransactionResponse> responseCallback) {

        System.out.println("---------> createTransaction in repo");

        CreateTransaction request = RetrofitAPIClient.getClient(context).create(CreateTransaction.class);

        if (tag2 == null)
            tag2 = "null";
        if (tag3 == null)
            tag3 = "null";
        if (tag4 == null)
            tag4 = "null";

        request.create(token, plateType.toString(), tag1, tag2, tag3, tag4, amount).enqueue(responseCallback);

    }

    public void park(String token, ParkBody body, Callback<ParkResponse> responseCallback) {

        Park request = RetrofitAPIClient.getClient(context).create(Park.class);

        request.park(token, body).enqueue(responseCallback);

    }

    public void estimateParkPrice(String token, int placeID, Callback<EstimateParkPriceResponse> responseCallback) {

        EstimateParkPrice request = RetrofitAPIClient.getClient(context).create(EstimateParkPrice.class);

        request.get(token, placeID).enqueue(responseCallback);

    }

    public void exitPark(String token, int placeID, Callback<ExitParkResponse> responseCallback) {

        ExitPark request = RetrofitAPIClient.getClient(context).create(ExitPark.class);

        request.exit(token, placeID).enqueue(responseCallback);

    }

    public void getCarDebtHistory(String token, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int limit, int offset, Callback<DebtHistoryResponse> responseCallback) {

        CarDebtHistory request = RetrofitAPIClient.getClient(context).create(CarDebtHistory.class);

        request.get(token, plateType.toString(), tag1, tag2, tag3, tag4, limit, offset).enqueue(responseCallback);

    }

    public void verifyTransaction(String token, Transaction transaction, Callback<VerifyTransactionResponse> responseCallback) {

        VerifyTransaction request = RetrofitAPIClient.getClient(context).create(VerifyTransaction.class);

        request.verify(token,transaction.getAmount(), transaction.getOur_token(), transaction.getBank_token(), transaction.getPlaceID(), transaction.getStatus(), transaction.getBank_type(), transaction.getState(), transaction.getCard_number(), transaction.getBank_datetime(), transaction.getTrace_number(), transaction.getResult_message()).enqueue(responseCallback);

    }

    public void exitRequest(String token, PlateType plateType, String tag1, String tag2, String tag3, String tag4, Callback<ExitRequestResponse> responseCallback) {

        ExitRequest request = RetrofitAPIClient.getClient(context).create(ExitRequest.class);

        request.submit(token, plateType.toString(), tag1, tag2, tag3, tag4).enqueue(responseCallback);

    }

    public void deleteExitRequest(String token, int place_id, Callback<DeleteExitRequestResponse> responseCallback) {

        DeleteExitRequest request = RetrofitAPIClient.getClient(context).create(DeleteExitRequest.class);

        request.delete(token, place_id).enqueue(responseCallback);

    }

    public void logout(String token, Callback<LogoutResponse> responseCallback) {

        Logout request = RetrofitAPIClient.getClient(context).create(Logout.class);

        request.submit(token).enqueue(responseCallback);

    }

}
