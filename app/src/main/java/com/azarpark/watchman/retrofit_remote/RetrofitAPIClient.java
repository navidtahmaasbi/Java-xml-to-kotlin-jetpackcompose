package com.azarpark.watchman.retrofit_remote;

import android.content.Context;

import com.azarpark.watchman.utils.SharedPreferencesRepository;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitAPIClient {

//    public static String INITIAL_BASE_URL = "https://backend.iranademo.ir";
    public static String INITIAL_BASE_URL = "https://backend1.azarpark.irana.app";
    public static String BASE_URL = "https://backend1.azarpark.irana.app";
//    public static String BASE_URL = "https://backend.iranademo.ir";
    private static Retrofit retrofit = null;
    private static Retrofit initialRetrofit = null;

    public static Retrofit getInitialClient() {

        if (initialRetrofit == null)
            initialRetrofit = new Retrofit.Builder()
                .baseUrl(INITIAL_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getUnsafeOkHttpClient())
                .build();

        return initialRetrofit;

    }

    public static Retrofit getClient(Context context) {

        SharedPreferencesRepository sh_p = new SharedPreferencesRepository(context);

        BASE_URL = "https://" + sh_p.getString(SharedPreferencesRepository.SUB_DOMAIN) + ".backend1.azarpark.irana.app";
//        BASE_URL = "https://" + sh_p.getString(SharedPreferencesRepository.SUB_DOMAIN) + ".backend.iranademo.ir";

        if (retrofit == null)
            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getUnsafeOkHttpClient())
                .build();

        return retrofit;

    }

    public static void setBaseUrl(String url) {
        BASE_URL = url;

        retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getUnsafeOkHttpClient())
            .build();

    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

