package com.azarpark.watchman.retrofit_remote;

import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.retrofit_remote.bodies.LoginBody;
import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;
import com.azarpark.watchman.retrofit_remote.interfaces.CarDebtHistory;
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

    public void login(LoginBody body, Callback<LoginResponse> responseCallback) {

        Login request = RetrofitAPIClient.getClient().create(Login.class);

        request.login(body).enqueue(responseCallback);

    }

    public void getPlaces(String token, Callback<PlacesResponse> responseCallback) {

        GetPlaces request = RetrofitAPIClient.getClient().create(GetPlaces.class);

        request.get(token).enqueue(responseCallback);

    }

    public void getCities(String token, Callback<GetCitiesResponse> responseCallback) {

        GetCities request = RetrofitAPIClient.getInitialClient().create(GetCities.class);

        request.get(token).enqueue(responseCallback);

    }

    public void getSplashData(String token, Callback<SplashResponse> responseCallback) {

        Splash request = RetrofitAPIClient.getInitialClient().create(Splash.class);

        request.get(token).enqueue(responseCallback);

    }

    public void park(String token, ParkBody body, Callback<ParkResponse> responseCallback) {

        Park request = RetrofitAPIClient.getClient().create(Park.class);

        request.park(token, body).enqueue(responseCallback);

    }

    public void estimateParkPrice(String token, int placeID, Callback<EstimateParkPriceResponse> responseCallback) {

        EstimateParkPrice request = RetrofitAPIClient.getClient().create(EstimateParkPrice.class);

        request.get(token,placeID).enqueue(responseCallback);

    }

    public void exitPark(String token, int placeID, Callback<ExitParkResponse> responseCallback) {

        ExitPark request = RetrofitAPIClient.getClient().create(ExitPark.class);

        request.exit(token,placeID).enqueue(responseCallback);

    }

    public void getCarDebtHistory(String token, PlateType plateType,String tag1,String tag2,String tag3,String tag4,int limit, int offset , Callback<DebtHistoryResponse> responseCallback) {

        CarDebtHistory request = RetrofitAPIClient.getClient().create(CarDebtHistory.class);

        request.get(token,plateType.toString(),tag1,tag2,tag3,tag4,limit,offset).enqueue(responseCallback);

    }

    public void verifyTransaction(String token, PlateType plateType,String tag1,String tag2,String tag3,String tag4,String amount, String transaction_id, int placeID , Callback<VerifyTransactionResponse> responseCallback) {

        VerifyTransaction request = RetrofitAPIClient.getClient().create(VerifyTransaction.class);

        request.verify(token,plateType.toString(),tag1,tag2,tag3,tag4,amount,transaction_id,placeID).enqueue(responseCallback);

    }

    public void exitRequest(String token, PlateType plateType,String tag1,String tag2,String tag3,String tag4 , Callback<ExitRequestResponse> responseCallback) {

        ExitRequest request = RetrofitAPIClient.getClient().create(ExitRequest.class);

        request.submit(token,plateType.toString(),tag1,tag2,tag3,tag4).enqueue(responseCallback);

    }

    public void deleteExitRequest(String token, int place_id , Callback<DeleteExitRequestResponse> responseCallback) {

        DeleteExitRequest request = RetrofitAPIClient.getClient().create(DeleteExitRequest.class);

        request.delete(token,place_id).enqueue(responseCallback);

    }

    public void logout(String token, Callback<LogoutResponse> responseCallback) {

        Logout request = RetrofitAPIClient.getClient().create(Logout.class);

        request.submit(token).enqueue(responseCallback);

    }

}
