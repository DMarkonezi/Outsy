package com.example.outsy.data.models

import com.google.firebase.Timestamp

data class Review (
    val id: String = "",
    val userId: String = "",
    val username: String = "", // UI logic
    val placeId: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val createdAt: Timestamp = Timestamp.now(),
)