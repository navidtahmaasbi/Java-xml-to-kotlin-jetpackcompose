package com.azarpark.cunt.data.repository

import com.azarpark.cunt.data.remote.WatchmanAPI
import com.azarpark.cunt.domain.repository.PlacesRepository

class PlacesRepositoryImpl (val api : WatchmanAPI) : PlacesRepository {
    override suspend fun getPlaces() {
        TODO("Not yet implemented")
    }
}