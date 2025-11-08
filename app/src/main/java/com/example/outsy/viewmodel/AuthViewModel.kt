package com.example.outsy.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.outsy.data.repository.AuthRepository
import com.google.firebase.firestore.GeoPoint

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableLiveData<Pair<Boolean, String?>>()
    val authState: LiveData<Pair<Boolean, String?>> = _authState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        _isLoading.value = true
        repository.login(email, password) { success, error ->
            _authState.value = Pair(success, error)
            _isLoading.value = false
        }
    }

    fun register(
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

        repository.register(firstName, lastName, email, password, phoneNumber, location, profileBitmap, onResult)
    }

    fun logout() {
        repository.logout()
    }
}
