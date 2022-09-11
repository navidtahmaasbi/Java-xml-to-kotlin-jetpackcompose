package com.azarpark.watchman.web_service;

import android.content.Context;

import com.azarpark.watchman.BuildConfig;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebService {

    private final API api;

    public WebService() {
        OkHttpClient okHttpClient = getOkHttpClient();

        String baseURL = "https://tabriz.backend1.azarpark.irana.app";
        if (Constants.SELECTED_PAYMENT == Constants.SAMAN || Constants.SELECTED_PAYMENT == Constants.BEH_PARDAKHT)
            baseURL = "https://tabriz.backend1.azarpark.irana.app";
        else if (Constants.SELECTED_PAYMENT == Constants.PASRIAN)
            baseURL = "https://sarab.backend1.azarpark.irana.app";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        api = retrofit.create(API.class);
    }

    public API getClient(Context context) {
//        if (api == null){
//            okHttpClient = getOkHttpClient();
//
//            String baseURL = "https://tabriz.backend1.azarpark.irana.app";
//            if (Constants.SELECTED_PAYMENT == Constants.SAMAN)
//                baseURL = "https://tabriz.backend1.azarpark.irana.app";
//            else if (Constants.SELECTED_PAYMENT == Constants.PASRIAN)
//                baseURL = "https://sarab.backend1.azarpark.irana.app";
//
//            retrofit = new Retrofit.Builder()
//                    .baseUrl(baseURL)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .client(okHttpClient)
//                    .build();
//
//            api = retrofit.create(API.class);
//        }
        return api;
    }

//    public static void changeClientURL(String subDomain) {
//
//        String baseURL = Constants.BASE_URL_FIRST_PART + subDomain + Constants.BASE_URL_SECOND_PART;
//
//        if (okHttpClient == null)
//            okHttpClient = getOkHttpClient();
//
//        retrofit = new Retrofit.Builder()
//                .baseUrl(baseURL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(okHttpClient)
//                .build();
//
//        api = retrofit.create(API.class);
//
//    }

    private static OkHttpClient getOkHttpClient() {

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

            builder.retryOnConnectionFailure(false);

            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(interceptor);
            }

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
