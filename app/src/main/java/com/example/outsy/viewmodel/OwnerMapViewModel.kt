package com.example.outsy.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outsy.data.models.Place
import com.example.outsy.data.repository.PlaceRepository
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OwnerMapViewModel(
    private val repository: PlaceRepository = PlaceRepository()
) : ViewModel() {

    private val _ownerLocation = MutableStateFlow<LatLng?>(null)
    val ownerLocation: StateFlow<LatLng?> = _ownerLocation

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _placeAdded = MutableStateFlow(false)
    val placeAdded: StateFlow<Boolean> = _placeAdded

    private val _ownerPlaces = MutableStateFlow<List<Place>>(emptyList())
    val ownerPlaces: StateFlow<List<Place>> = _ownerPlaces

    init {
        loadOwnerPlaces()
    }

    fun setOwnerLocation(latLng: LatLng?) {
        _ownerLocation.value = latLng
    }

    fun setSelectedLocation(latLng: LatLng?) {
        _selectedLocation.value = latLng
    }

    fun addPlace(
        name: String,
        category: String,
        description: String,
        imageBitmap: Bitmap?
    ) {
        val location = _selectedLocation.value ?: return

        val ownerId = repository.getCurrentUserId()
        if (ownerId == null) {
            _errorMessage.value = "User not logged in."
            return
        }

        val place = Place(
            id = "",
            ownerId = ownerId,
            name = name,
            location = GeoPoint(location.latitude, location.longitude),
            category = category,
            description = description,
            rating = 0.0,
            imageUrl = ""
        )

        _isLoading.value = true
        _placeAdded.value = false
        _errorMessage.value = null

        viewModelScope.launch {
            repository.addPlace(place, imageBitmap) { success, error, newId ->
                _isLoading.value = false

                if (success) {
                    _placeAdded.value = true
                    _selectedLocation.value = null
                    loadOwnerPlaces()
                } else {
                    _errorMessage.value = error ?: "Unknown error."
                }
            }
        }
    }

    fun loadOwnerPlaces() {
        viewModelScope.launch {
            repository.getOwnerPlaces(
                onResult = { places ->
                    _ownerPlaces.value = places
                },
                onError = { error ->
                    _errorMessage.value = error.message ?: "Failed to load places"
                }
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearPlaceAdded() {
        _placeAdded.value = false
    }
}