package com.example.outsy.data.models

import com.google.firebase.firestore.GeoPoint

data class User(
    val uid: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val photoUrl: String,
    val points: Int,
    val status: String? = null,
    val location: GeoPoint? = null
)
