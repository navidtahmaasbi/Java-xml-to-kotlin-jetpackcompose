package com.azarpark.watchman.retrofit_remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitAPIClient {

    public static String INITIAL_BASE_URL = "https://backend.iranademo.ir";
    public static String BASE_URL = "https://backend.iranademo.ir";
    private static Retrofit retrofit = null;
    private static Retrofit initialRetrofit = null;

    public static Retrofit getInitialClient(){

        if (initialRetrofit == null)
            initialRetrofit = new Retrofit.Builder()
                    .baseUrl(INITIAL_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        return initialRetrofit;

    }
    public static Retrofit getClient(){

        if (retrofit == null)
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        return retrofit;

    }

    public static void setBaseUrl(String url) {
        BASE_URL = url;
    }
}
