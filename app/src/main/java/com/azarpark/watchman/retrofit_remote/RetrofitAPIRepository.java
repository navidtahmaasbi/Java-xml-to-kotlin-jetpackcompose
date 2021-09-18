package com.azarpark.watchman.retrofit_remote;

import com.azarpark.watchman.retrofit_remote.bodies.LoginBody;
import com.azarpark.watchman.retrofit_remote.interfaces.GetPlaces;
import com.azarpark.watchman.retrofit_remote.interfaces.Login;
import com.azarpark.watchman.retrofit_remote.responses.LoginResponse;
import com.azarpark.watchman.retrofit_remote.responses.PlacesResponse;
import com.azarpark.watchman.retrofit_remote.responses.TestResponse;

import retrofit2.Callback;


public class RetrofitAPIRepository {

    public void login (LoginBody body, Callback<LoginResponse> responseCallback){

        Login request = RetrofitAPIClient.getClient().create(Login.class);

        request.login(body).enqueue(responseCallback);

    }

    public void getPlaces (String token,Callback<PlacesResponse> responseCallback){

        GetPlaces request = RetrofitAPIClient.getClient().create(GetPlaces.class);

        request.get(token).enqueue(responseCallback);

    }

}
