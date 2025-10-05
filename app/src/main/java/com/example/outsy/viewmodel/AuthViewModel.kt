package com.example.outsy.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.outsy.data.repository.AuthRepository

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableLiveData<Pair<Boolean, String?>>()
    val authState: LiveData<Pair<Boolean, String?>> = _authState

    fun login(email: String, password: String) {
        repository.login(email, password) { success, error ->
            _authState.value = Pair(success, error)
        }
    }

    fun register(
        email: String,
        password: String,
        phone: String,
        profileBitmap: Bitmap?
    ) {
        repository.register(email, password, phone, profileBitmap) { success, error ->
            _authState.value = Pair(success, error)
        }
    }

    fun logout() {
        repository.logout()
    }
}
