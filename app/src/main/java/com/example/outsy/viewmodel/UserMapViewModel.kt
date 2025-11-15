package com.example.outsy.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outsy.data.models.Place
import com.example.outsy.data.repository.PlaceRepository
import com.example.outsy.data.repository.SearchFilter
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserMapViewModel(
    private val repository: PlaceRepository = PlaceRepository()
) : ViewModel() {
    private val useDistanceFilter = true

    private val _allPlaces = MutableStateFlow<List<Place>>(emptyList())
    val allPlaces: StateFlow<List<Place>> = _allPlaces.asStateFlow()

    private val _displayedPlaces = MutableStateFlow<List<Place>>(emptyList())
    val displayedPlaces: StateFlow<List<Place>> = _displayedPlaces.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilter())
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    init {
        loadAllPlaces()
    }

    private fun loadAllPlaces() {
        _isLoading.value = true
        _errorMessage.value = null

        repository.getAllPlaces(
            onResult = { places ->
                _allPlaces.value = places
                applyFilter()
                _isLoading.value = false
            },
            onError = { error ->
                Log.e("UserMapViewModel", "Error loading places", error)
                _errorMessage.value = error.message ?: "Error loading places"
                _isLoading.value = false
            }
        )
    }

    private fun applyFilter() {
        val filtered = if (useDistanceFilter && _userLocation.value != null) {
            repository.filterPlaces(_allPlaces.value, _searchFilter.value)
        } else {
            _allPlaces.value
        }

        _displayedPlaces.value = filtered
    }

    fun setUserLocation(location: LatLng) {
        _userLocation.value = location
        _searchFilter.value = _searchFilter.value.copy(userLocation = location)
        applyFilter()
    }

    fun setSearchQuery(query: String) {
        _searchFilter.value = _searchFilter.value.copy(searchQuery = query)
        applyFilter()
    }

    fun setCategory(category: String?) {
        _searchFilter.value = _searchFilter.value.copy(category = category)
        applyFilter()
    }

    fun searchNearby(userLocation: LatLng, radiusKm: Double) {
        _userLocation.value = userLocation
        _searchFilter.value = _searchFilter.value.copy(
            radiusKm = radiusKm,
            userLocation = userLocation
        )
        applyFilter()
    }

    fun clearFilters() {
        _searchFilter.value = SearchFilter()
        applyFilter()
    }

    fun refreshPlaces() {
        loadAllPlaces()
    }

    fun getAvailableCategories(): List<String> {
        return _allPlaces.value.map { it.category }.distinct()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}