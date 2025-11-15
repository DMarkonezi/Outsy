package com.example.outsy.data.repository

import com.example.outsy.data.models.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ReviewRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val placesCollection = firestore.collection("places")

    fun addReview(review: Review, onResult: (Boolean, String?) -> Unit) {
        val reviewRef = firestore.collection("reviews").document()
        val reviewWithId = review.copy(id = reviewRef.id)

        reviewRef.set(reviewWithId)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

//    fun getPlaceReviews(placeId: String, onResult: (List<Review>) -> Unit, onError: (Exception) -> Unit) {
//        firestore.collection("reviews")
//            .whereEqualTo("placeId", placeId)
//            .orderBy("createdAt", Query.Direction.DESCENDING)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    onError(error)
//                    return@addSnapshotListener
//                }
//
//                val reviews = snapshot?.documents?.mapNotNull { doc ->
//                    doc.toObject(Review::class.java)?.copy(id = doc.id)
//                } ?: emptyList()
//
//                onResult(reviews)
//            }
//    }
}