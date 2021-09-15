package com.azarpark.watchman.retrofit_remote;

import com.azarpark.watchman.retrofit_remote.interfaces.Test;
import com.azarpark.watchman.retrofit_remote.responses.TestResponse;

import retrofit2.Callback;


public class RetrofitAPIRepository {

    public void test (Callback<TestResponse> responseCallback, int id){

        Test test = RetrofitAPIClient.getClient().create(Test.class);

        test.test(id).enqueue(responseCallback);

    }

}
