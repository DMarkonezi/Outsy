package com.example.outsy.data.models

import com.google.firebase.firestore.GeoPoint

data class Place(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val category: String = "",
    val description: String = "",
    val rating: Double = 0.0,
    val imageUrl: String = ""
)