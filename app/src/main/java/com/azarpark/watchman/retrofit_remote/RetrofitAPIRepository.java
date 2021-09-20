package com.azarpark.watchman.retrofit_remote;

import com.azarpark.watchman.retrofit_remote.bodies.LoginBody;
import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;
import com.azarpark.watchman.retrofit_remote.interfaces.EstimateParkPrice;
import com.azarpark.watchman.retrofit_remote.interfaces.ExitPark;
import com.azarpark.watchman.retrofit_remote.interfaces.GetPlaces;
import com.azarpark.watchman.retrofit_remote.interfaces.Login;
import com.azarpark.watchman.retrofit_remote.interfaces.Park;
import com.azarpark.watchman.retrofit_remote.responses.EstimateParkPriceResponse;
import com.azarpark.watchman.retrofit_remote.responses.ExitParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.LoginResponse;
import com.azarpark.watchman.retrofit_remote.responses.ParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.PlacesResponse;
import com.azarpark.watchman.retrofit_remote.responses.TestResponse;

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

}
