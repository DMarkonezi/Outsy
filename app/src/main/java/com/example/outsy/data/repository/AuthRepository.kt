package com.example.outsy.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.outsy.data.models.PlaceOwner
import com.example.outsy.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import com.google.firebase.firestore.GeoPoint

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    fun login(email: String, password: String, onResult: (Boolean, String?, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid
                if (userId != null) {
                    firestore.collection("placeOwners").document(userId).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                onResult(true, null, "placeowner")
                            } else {
                                onResult(true, null, "user")
                            }
                        }
                        .addOnFailureListener {
                            onResult(true, null, "user")
                        }
                } else {
                    onResult(false, "User not found", null)
                }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message, null)
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

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("RegisterError", "Auth failed: ${task.exception?.message}")
                    onResult(false, task.exception?.message)
                    return@addOnCompleteListener
                }

                val uid = auth.currentUser?.uid

                if (uid == null) {
                    Log.e("RegisterError", "UID is null")
                    onResult(false, "UID is null")
                    return@addOnCompleteListener
                }

                Log.d("Register", "Creating user document for UID: $uid")

                if (profileBitmap != null) {
                    uploadProfileImage(uid, profileBitmap) { imageUrl ->
                        saveUserToFirestore(uid, username, firstName, lastName, email, phoneNumber, location, imageUrl, onResult)
                    }
                } else {
                    saveUserToFirestore(uid, username, firstName, lastName, email, phoneNumber, location, null, onResult)
                }
            }
    }

    fun registerPlaceOwner(
        businessName: String,
        email: String,
        password: String,
        phoneNumber: String,
        onResult: (Boolean, String?) -> Unit
    )
    {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("Register Error - Place Owner", "Auth failed: ${task.exception?.message}")
                    onResult(false, task.exception?.message)
                    return@addOnCompleteListener
                }

                val uid = auth.currentUser?.uid

                if (uid == null) {
                    Log.e("Register Error - Place Owner", "UID is null")
                    onResult(false, "UID is null")
                    return@addOnCompleteListener
                }

                Log.d("Register - Place Owner", "Creating user document for UID: $uid")

                savePlaceOwnerToFirestore(uid, businessName, email, phoneNumber, onResult);
            }
    }

    private fun uploadProfileImage(uid: String, bitmap: Bitmap, onComplete: (String?) -> Unit) {
        val ref = storage.reference.child("profile_images/$uid.jpg")
        val baos = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val data = baos.toByteArray()

        ref.putBytes(data)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("Register", "Image uploaded successfully: $uri")
                    onComplete(uri.toString())
                }
            }
            .addOnFailureListener {
                Log.e("Storage", "Image upload failed: ${it.message}")
                onComplete(null)
            }
    }

    private fun saveUserToFirestore(
        uid: String,
        username: String,
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        location: GeoPoint?,
        imageUrl: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = User(
            uid = uid,
            username = username,
            firstName = firstName,
            lastName = lastName,
            email = email,
            phoneNumber = phoneNumber,
            photoUrl = imageUrl?: "",
            status = "Not going out",
            points = 0,
            location = location?.let { GeoPoint(it.latitude, it.longitude) }
        )

        firestore.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                Log.d("Register - User", "User saved successfully")
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("Register - User", "Firestore write failed: ${e.message}")
                onResult(false, e.message)
            }
    }

    private fun savePlaceOwnerToFirestore(
        id: String,
        businessName: String,
        email: String,
        phoneNumber: String,
        onResult: (Boolean, String?) -> Unit
    )
    {
        val placeOwner = PlaceOwner(
            id = id,
            businessName = businessName,
            email = email,
            phoneNumber = phoneNumber
        )

        firestore.collection("placeOwners")
            .document(id)
            .set(placeOwner)
            .addOnSuccessListener {
                Log.d("Register - Place Owner", "Place Owner saved successfully")
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("Register - Place Owner", "Firestore write failed: ${e.message}")
                onResult(false, e.message)
            }
    }

    fun logout() {
        auth.signOut()
    }
}
