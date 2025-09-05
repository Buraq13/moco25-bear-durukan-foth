package com.example.fernfreunde.data.remote.dataSources

import android.net.Uri
import com.example.fernfreunde.data.other.Constants.USER_COLLECTION
import com.example.fernfreunde.data.remote.dtos.UserDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.firestore.DocumentSnapshot
import com.example.fernfreunde.data.remote.utils.getMillis
import com.example.fernfreunde.data.remote.utils.getStringSafe

class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : IFirestoreUserDataSource {

    private val usersCollection = firestore.collection(USER_COLLECTION)

    // ***************************************************************** //
    // HELPER: sichere Mapping-Funktion                                  //
    // -> wandelt DocumentSnapshot defensiv in UserDto um, ohne zu      //
    //    crashen, wenn Timestamp/Feld-Typen anders sind                 //
    // ***************************************************************** //
    private fun mapSnapshotToDto(doc: DocumentSnapshot): UserDto? {
        return try {
            // userId: Feld oder doc.id als Fallback
            val userId = doc.getStringSafe("userId") ?: doc.id.takeIf { it.isNotBlank() } ?: return null

            // Pflicht-/Optionale Felder (robust lesen)
            val username = doc.getStringSafe("username") ?: ""
            val displayName = doc.getStringSafe("displayName")
            val profileImageUrl = doc.getStringSafe("profileImageUrl")
            val bio = doc.getStringSafe("bio")

            // updatedAt: Timestamp | Long | Double | Int | Date -> Long millis
            val updatedAt = doc.getMillis("updatedAt")

            UserDto(
                userId = userId,
                displayName = displayName,
                username = username,
                profileImageUrl = profileImageUrl,
                bio = bio,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            // defensive: falls etwas unerwartet ist, nicht crashen, sondern null zurückgeben
            e.printStackTrace()
            null
        }
    }

    // ***************************************************************** //
    // READ OPERATIONS                                                   //
    // ***************************************************************** //

    override suspend fun getUser(userId: String): UserDto? {
        val snapshot = usersCollection.document(userId).get().await()
        return if (snapshot.exists()) {
            mapSnapshotToDto(snapshot)
        } else {
            null
        }
    }

    override suspend fun getUsers(userIds: List<String>): List<UserDto> {
        if (userIds.isEmpty()) return emptyList()

        // da Firestore für whereIn-Anfragen ein Limit von 10 hat, wird die Anfrage in chunks aufgeteilt
        val chunks = userIds.chunked(10)
        val results = mutableListOf<UserDto>()
        for (chunk in chunks) {
            val snapshot = usersCollection.whereIn("userId", chunk).get().await()
            results += snapshot.documents.mapNotNull { mapSnapshotToDto(it) }
        }
        return results
    }

    override suspend fun getAllUsers(): List<UserDto> {
        val snapshot = usersCollection.limit(100).get().await()
        return snapshot.documents.mapNotNull { mapSnapshotToDto(it) }
    }

    // ***************************************************************** //
    // WRITE OPERATIONS                                                  //
    // ***************************************************************** //

    override suspend fun createOrUpdateUser(user: UserDto) {
        val data = hashMapOf<String, Any?>(
            "userId" to user.userId,
            "username" to user.username,
            "displayName" to user.displayName,
            "profileImageUrl" to user.profileImageUrl,
            "bio" to user.bio,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        usersCollection.document(user.userId).set(data, SetOptions.merge()).await()
    }

    override suspend fun uploadProfileImage(userId: String, mediaUri: Uri): String {

        val ref = storage.reference.child("profile_images/$userId.jpg")
        ref.putFile(mediaUri).await()

        val downloadUrl = ref.downloadUrl.await().toString()

        usersCollection.document(userId).update("profileImageUrl", downloadUrl).await()

        return downloadUrl
    }
    // ***************************************************************** //
    // SHOW REALTIME CHANGES                                             //
    // ***************************************************************** //

    // zeigt Echtzeit-Änderungen, z.B. wenn usxername geändert oder Profilbild aktualisiert wurde
    override fun listenUser(userId: String) = callbackFlow<UserDto?> {
        // registriert einen listener, der bei jeder Änderung automatisch aufgerufen wird
        val registration = usersCollection.document(userId).addSnapshotListener { snapshot, error ->
            // wenn ein Fehler auftritt, wird der Flow geschlossen, ansonsten wird ein Ergebnis zurückgegeben
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                try {
                    trySend(mapSnapshotToDto(snapshot))
                } catch (e: Exception) {
                    // defensive: falls Mapping fehlschlägt -> sende null oder schließe
                    e.printStackTrace()
                    trySend(null)
                }
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }
}
