// file: com/example/fernfreunde/data/remote/FirebaseUserRemoteDataSource.kt
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

class FirestoreUserDataSource(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : IFirestoreUserDataSource {

    private val users = firestore.collection(USER_COLLECTION)

    // ***************************************************************** //
    // READ OPERATIONS                                                   //
    // ***************************************************************** //

    override suspend fun getUser(userId: String): UserDto? {
        val snapshot = users.document(userId).get().await()
        return if (snapshot.exists()) {
            snapshot.toObject(UserDto::class.java)
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
            val snapshot = users.whereIn("userId", chunk).get().await()
            results += snapshot.documents.mapNotNull { it.toObject(UserDto::class.java) }
        }
        return results
    }

    override suspend fun searchUsers(query: String, limit: Int): List<UserDto> {
        // prefix search on username and displayName using startAt/endAt trick
        val q = query.trim()
        if (q.isEmpty()) return emptyList()

        val end = "$q\uF8FF" // unicode trick for prefix-range

        val byUsername = users
            .orderBy("username")
            .startAt(q)
            .endAt(end)
            .limit(limit.toLong())
            .get()
            .await()
            .documents.mapNotNull { it.toObject(UserDto::class.java) }

        // also search displayName (merge results, remove duplicates by userId)
        val byDisplayName = users
            .orderBy("displayName")
            .startAt(q)
            .endAt(end)
            .limit(limit.toLong())
            .get()
            .await()
            .documents.mapNotNull { it.toObject(UserDto::class.java) }

        val all = (byUsername + byDisplayName).distinctBy { it.userId }
        return all.take(limit)
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
        users.document(user.userId).set(data, SetOptions.merge()).await()
    }

    override suspend fun uploadProfileImage(userId: String, mediaUri: Uri): String {

        val ref = storage.reference.child("profile_images/$userId.jpg")
        ref.putFile(mediaUri).await()

        val downloadUrl = ref.downloadUrl.await().toString()

        users.document(userId).update("profileImageUrl", downloadUrl).await()

        return downloadUrl
    }
    // ***************************************************************** //
    // SHOW REALTIME CHANGES                                             //
    // ***************************************************************** //

    // zeigt Echtzeit-Änderungen, z.B. wenn usxername geändert oder Profilbild aktualisiert wurde
    override fun listenUser(userId: String) = callbackFlow<UserDto?> {
        // registriert einen listener, der bei jeder Änderung automatisch aufgerufen wird
        val registration = users.document(userId).addSnapshotListener { snapshot, error ->
            // wenn ein Fehler auftritt, wird der Flow geschlossen, ansonsten wird ein Ergebnis zurückgegeben
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject(UserDto::class.java))
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }
}
