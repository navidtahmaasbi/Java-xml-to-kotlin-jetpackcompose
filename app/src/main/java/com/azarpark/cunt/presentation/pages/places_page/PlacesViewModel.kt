package com.azarpark.cunt.presentation.pages.places_page

import androidx.lifecycle.ViewModel
import com.azarpark.cunt.domain.repository.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val repository: PlacesRepository 
): ViewModel() {

}