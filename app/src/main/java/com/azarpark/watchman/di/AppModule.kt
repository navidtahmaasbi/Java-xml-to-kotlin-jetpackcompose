package com.azarpark.watchman.di

import com.azarpark.watchman.data.remote.WatchmanAPI
import com.azarpark.watchman.core.AppConfig
import com.azarpark.watchman.data.repository.PlacesRepositoryImpl
import com.azarpark.watchman.domain.repository.PlacesRepository
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
        return PlacesRepositoryImpl()
    }

}