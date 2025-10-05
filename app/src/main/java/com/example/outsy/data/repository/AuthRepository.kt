package com.example.outsy.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onResult(true, null)
                else onResult(false, task.exception?.message)
            }
    }

//    fun register(
//        email: String,
//        password: String,
//        phone: String,
//        profileBitmap: Bitmap?,
//        onResult: (Boolean, String?) -> Unit
//    ) {
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val uid = auth.currentUser?.uid
//                    if (uid != null) {
//                        val user = hashMapOf(
//                            "email" to email,
//                            "phone" to phone,
//                            "photoBase64" to profileBitmap?.let { bitmapToBase64(it) }
//                        )
//
//                        firestore.collection("users").document(uid)
//                            .set(user)
//                            .addOnSuccessListener { onResult(true, null) }
//                            .addOnFailureListener { e -> onResult(false, e.message) }
//                    } else {
//                        onResult(false, "UID is null")
//                    }
//                } else {
//                    onResult(false, task.exception?.message)
//                }
//            }
//    }

    fun register(
        email: String,
        password: String,
        phone: String,
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

                val user = hashMapOf(
                    "email" to email,
                    "phone" to phone,
                    "photoBase64" to profileBitmap?.let { bitmapToBase64(it) }
                )

                firestore.collection("users").document(uid)
                    .set(user)
                    .addOnSuccessListener {
                        Log.d("Register", "Firestore write successful")
                        onResult(true, null)
                    }
                    .addOnFailureListener { e ->
                        Log.e("RegisterError", "Firestore failed: ${e.message}", e)
                        onResult(false, e.message)
                    }
            }
    }


    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun saveUserData(
        uid: String,
        email: String,
        phone: String,
        photoBase64: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = hashMapOf(
            "email" to email,
            "phone" to phone,
            "photoBase64" to photoBase64
        )

        firestore.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun getUserProfileImage(base64String: String?): Bitmap? {
        return base64String?.let {
            val decodedBytes = Base64.decode(it, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
