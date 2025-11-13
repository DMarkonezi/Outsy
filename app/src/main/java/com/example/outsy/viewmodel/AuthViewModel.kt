package com.example.outsy.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.outsy.data.repository.AuthRepository
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    // States
    private val _authState = MutableStateFlow<Pair<Boolean, String?>>(Pair(false, null))
    val authState: StateFlow<Pair<Boolean, String?>> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userType = MutableStateFlow<String?>(null)
    val userType: StateFlow<String?> = _userType.asStateFlow()

    fun login(email: String, password: String) {
        _isLoading.value = true
        repository.login(email, password) { success, error, type ->
            _authState.value = Pair(success, error)
            _isLoading.value = false
            _userType.value = type
        }
    }

    fun registerUser(
        username: String,
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        location: GeoPoint?,
        profileBitmap: Bitmap?,
        onResult: (Boolean, String?) -> Unit
    ) {
        _isLoading.value = true
        repository.registerUser(username, firstName, lastName, email, password, phoneNumber, location, profileBitmap, onResult)
    }

    fun registerPlaceOwner(
        businessName: String,
        email: String,
        password: String,
        phoneNumber: String,
        onResult: (Boolean, String?) -> Unit
    )
    {
        _isLoading.value = true;
        repository.registerPlaceOwner(businessName, email, password, phoneNumber, onResult);
    }

    fun logout() {
        repository.logout()
        _userType.value = null
        _authState.value = Pair(false, null)
    }
}
