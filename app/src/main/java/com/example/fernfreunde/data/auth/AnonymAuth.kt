package com.example.fernfreunde.data.auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AnonymAuth @Inject constructor(
    private val auth: FirebaseAuth
) {
//    suspend fun signInAnonymously(): FirebaseUser? {
//        return try {
//            val result = auth.signInAnonymously().await()
//            result.user
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }

    suspend fun ensureSignedIn(): FirebaseUser? {
        // bereits angemeldet?
        auth.currentUser?.let { return it }

        return try {
            val result = auth.signInAnonymously().await()
            result.user
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // optional: Hilfsfunktion um nur die uid zu bekommen
    suspend fun ensureUid(): String? {
        return ensureSignedIn()?.uid
    }
}