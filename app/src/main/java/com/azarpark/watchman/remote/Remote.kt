package com.azarpark.watchman.remote

import com.azarpark.watchman.BuildConfig
import com.azarpark.watchman.utils.Constants
import com.azarpark.watchman.web_service.API
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.lang.RuntimeException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class Remote {

    companion object{
        var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://tabriz.backend1.azarpark.irana.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        private var api: API = retrofit.create(API::class.java)
        fun getAPI() : API = api
    }

}