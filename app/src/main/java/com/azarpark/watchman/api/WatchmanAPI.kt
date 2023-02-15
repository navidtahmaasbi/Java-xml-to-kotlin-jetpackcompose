package com.azarpark.watchman.api

import com.azarpark.watchman.web_service.responses.PlacesResponse
import retrofit2.Response
import retrofit2.http.GET

interface WatchmanAPI {

    @GET("/api/watchman/places")
    suspend fun getPlaces() : Response<PlacesResponse>

}