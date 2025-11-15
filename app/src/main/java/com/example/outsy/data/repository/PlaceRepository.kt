package com.example.outsy.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.outsy.data.models.Place
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

data class SearchFilter(
    val searchQuery: String = "",
    val category: String? = null,
    val radiusKm: Double? = null,
    val userLocation: LatLng? = null
)

class PlaceRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val placesCollection = firestore.collection("places")

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun addPlace(
        place: Place,
        imageBitmap: Bitmap?,
        onResult: (Boolean, String?, String?) -> Unit
    ) {
        val userId = getCurrentUserId()

        if (userId == null) {
            Log.e("PlaceError", "User is not logged in")
            onResult(false, "User is not logged in", null)
            return
        }

        val newPlaceRef = placesCollection.document()
        val placeId = newPlaceRef.id
        val placeWithId = place.copy(id = placeId)

        if (imageBitmap != null) {
            uploadPlaceImage(placeId, imageBitmap) { imageUrl ->
                val placeWithImage = placeWithId.copy(imageUrl = imageUrl ?: "")
                savePlaceToFirestore(placeWithImage, onResult, placeId)
            }
        } else {
            savePlaceToFirestore(placeWithId, onResult, placeId)
        }
    }

    private fun uploadPlaceImage(placeId: String, bitmap: Bitmap, onComplete: (String?) -> Unit) {
        val ref = storage.reference.child("places/$placeId/image.jpg")
        val baos = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val data = baos.toByteArray()

        ref.putBytes(data)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("PlaceStorage", "Image uploaded successfully: $uri")
                    onComplete(uri.toString())
                }
            }
            .addOnFailureListener {
                Log.e("PlaceStorage", "Image upload failed: ${it.message}")
                onComplete(null)
            }
    }

    private fun savePlaceToFirestore(
        place: Place,
        onResult: (Boolean, String?, String?) -> Unit,
        placeId: String
    ) {
        placesCollection
            .document(placeId)
            .set(place)
            .addOnSuccessListener {
                Log.d("Place Firestore", "Place saved successfully with ID: $placeId")
                onResult(true, null, placeId)
            }
            .addOnFailureListener { e ->
                Log.e("Place Firestore", "Firestore write failed: ${e.message}")
                onResult(false, e.message, null)
            }
    }

    fun getAllPlaces(onResult: (List<Place>) -> Unit, onError: (Exception) -> Unit) {
        placesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val places = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Place::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                onResult(places)
            }
    }

    fun getOwnerPlaces(onResult: (List<Place>) -> Unit, onError: (Exception) -> Unit) {
        val currentOwnerId = getCurrentUserId() ?: run {
            onResult(emptyList())
            return
        }

        placesCollection
            .whereEqualTo("ownerId", currentOwnerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val places = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Place::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                onResult(places)
            }
    }

    // Filtering

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    fun filterPlaces(allPlaces: List<Place>, filter: SearchFilter): List<Place> {
        var result = allPlaces

        if (filter.searchQuery.isNotEmpty()) {
            result = result.filter { place ->
                place.name.contains(filter.searchQuery, ignoreCase = true) ||
                        place.category.contains(filter.searchQuery, ignoreCase = true) ||
                        place.description.contains(filter.searchQuery, ignoreCase = true)
            }
        }

        // Filter po kategoriji
        filter.category?.let { category ->
            result = result.filter { it.category == category }
        }

        // Filter po radijusu
        if (filter.radiusKm != null && filter.userLocation != null) {
            result = result.filter { place ->
                val distance = calculateDistance(
                    filter.userLocation.latitude,
                    filter.userLocation.longitude,
                    place.location.latitude,
                    place.location.longitude
                )
                distance <= filter.radiusKm
            }
        }

        return result
    }
}