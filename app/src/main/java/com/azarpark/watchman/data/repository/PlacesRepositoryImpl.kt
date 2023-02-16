package com.azarpark.watchman.data.repository

import com.azarpark.watchman.data.remote.WatchmanAPI
import com.azarpark.watchman.domain.repository.PlacesRepository

class PlacesRepositoryImpl (val api : WatchmanAPI) : PlacesRepository {
    override suspend fun getPlaces() {
        TODO("Not yet implemented")
    }
}