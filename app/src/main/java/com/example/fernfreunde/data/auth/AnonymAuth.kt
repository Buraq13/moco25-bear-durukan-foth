package com.example.fernfreunde.data.auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

suspend fun ensureAnonymousSignedIn(): FirebaseUser? {
    val auth = FirebaseAuth.getInstance()
    auth.currentUser?.let { return it }
    return try {
        val result = auth.signInAnonymously().await()
        result.user
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}