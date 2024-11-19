package com.azarpark.cunt.di

import com.azarpark.cunt.data.remote.WatchmanAPI
import com.azarpark.cunt.core.AppConfig
import com.azarpark.cunt.data.repository.PlacesRepositoryImpl
import com.azarpark.cunt.domain.repository.PlacesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideWatchmanAPI() : WatchmanAPI {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl(AppConfig.selectedConfig.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(WatchmanAPI::class.java)
    }

    @Singleton
    @Provides
    fun providePlacesRepository(api : WatchmanAPI) : PlacesRepository{
        return PlacesRepositoryImpl(api = api)
    }

}