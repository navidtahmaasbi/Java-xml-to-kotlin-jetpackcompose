package com.azarpark.watchman.retrofit_remote;

import com.azarpark.watchman.retrofit_remote.bodies.LoginBody;
import com.azarpark.watchman.retrofit_remote.interfaces.Login;
import com.azarpark.watchman.retrofit_remote.responses.LoginResponse;
import com.azarpark.watchman.retrofit_remote.responses.TestResponse;

import retrofit2.Callback;


public class RetrofitAPIRepository {

    public void login (LoginBody body, Callback<LoginResponse> responseCallback){

        Login login = RetrofitAPIClient.getClient().create(Login.class);

        login.login(body).enqueue(responseCallback);

    }

}
