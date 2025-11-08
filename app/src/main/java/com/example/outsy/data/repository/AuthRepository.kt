package com.example.outsy.data.repository

import android.graphics.Bitmap
import android.util.Log
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

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onResult(true, null)
                else onResult(false, task.exception?.message)
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
                        saveUserToFirestore(uid, firstName, lastName, email, phoneNumber, location, imageUrl, onResult)
                    }
                } else {
                    saveUserToFirestore(uid, firstName, lastName, email, phoneNumber, location, null, onResult)
                }
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
                Log.d("Register", "User saved successfully")
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("Register", "Firestore write failed: ${e.message}")
                onResult(false, e.message)
            }
    }

    // TODO dodati neki email check da li je validan i slicno

    fun logout() {
        auth.signOut()
    }
}
