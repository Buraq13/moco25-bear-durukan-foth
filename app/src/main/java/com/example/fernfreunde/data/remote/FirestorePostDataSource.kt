package com.example.fernfreunde.data.remote

import android.net.Uri
import com.example.fernfreunde.data.remote.dtos.PostDto
import com.example.fernfreunde.data.other.Constants.POST_COLLECTION
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class FirestorePostDataSource {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val postsCollection = firestore.collection(POST_COLLECTION)

    // ***************************************************************** //
    // READ OPERATIONS                                                   //
    // ***************************************************************** //

    // für one-shot: einmalig alle Posts holen (nur für debug/Admin, sonst gefilterte Abfragen)
    suspend fun getAllPosts(): List<PostDto> {
        return try {
            // postCollection.get().await().toObjects(Post::class.java) // toObject benötigt default Konstruktor
            val snapshot = postsCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(PostDto::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // für Realtime-Updates: alle Posts holen (nur für debug/Admin, sonst gefilterte Abfragen)
    fun listenAllPostsAsFlow(): Flow<List<PostDto>> = callbackFlow {
        val registration = postsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { it.toObject(PostDto::class.java) }
                trySend(list)
            }
        }
        awaitClose { registration.remove() }
    }

    // nur die eigenen Posts des eingeloggten Users holen, sortiert nach createdAt
    suspend fun getPostsFromUser(userId: String): List<PostDto> = withContext(Dispatchers.IO) {
        try {
            // snapshot = Firestore-Query um alle Dokumente nach userId zu filtern
            val snapshot = postsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            // wandelt jedes snapshot-document in ein PostDto um -> PostDto benötigt dafür default values im Konstuktor!
            snapshot.documents.mapNotNull { it.toObject(PostDto::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // die Posts von allen Freunden des eingeloggten Users holen
    // da Firebase nur maximal 10 Einträge pro Anfrage erlaubt, chunked die Anfrage
    suspend fun getFriendsPosts(friendIds: List<String>): List<PostDto> = withContext(Dispatchers.IO) {
        if (friendIds.isEmpty()) return@withContext emptyList()
        try {
            val results = mutableListOf<PostDto>()
            val chunks = friendIds.chunked(10)
            for (chunk in chunks) {
                val snapshot = postsCollection
                    .whereIn("userId", chunk)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                results += snapshot.documents.mapNotNull { it.toObject(PostDto::class.java) }
            }
            // sortiere global nach Zeit (server oder client)
            val sorted = results.sortedByDescending { dto ->
                // dto.createdAtServer kann ein Timestamp oder Long oder null sein -> sichere Umwandlung
                val server = when (val s = dto.createdAtServer) {
                    is Timestamp -> s.toDate().time
                    is Long -> s
                    else -> null
                }
                server ?: dto.createdAtClient
            }
            sorted
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ***************************************************************** //
    // WRITE OPERATIONS                                                  //
    // ***************************************************************** //

    // Post in Firebase speichern
    suspend fun createPost(postDto: PostDto, mediaUri: Uri): String = withContext(Dispatchers.IO) {
        val postId = if (postDto.postId.isNotBlank()) postDto.postId else UUID.randomUUID().toString()

        val filename = mediaUri.lastPathSegment ?: "${System.currentTimeMillis()}"
        val storagePath = "posts/$postId/$filename"
        val storageRef = storage.reference.child(storagePath)

        storageRef.putFile(mediaUri).await()

        val downloadUrl = storageRef.downloadUrl.await().toString()

        val map = hashMapOf<String, Any?>(
            "postId" to postId,
            "userId" to postDto.userId,
            "user" to postDto.userName,
            "challengeDate" to postDto.challengeDate,
            "description" to postDto.description,
            "mediaRemoteUrl" to downloadUrl,
            "createdAtClient" to postDto.createdAtClient,
            "createdAt" to FieldValue.serverTimestamp()
        )

        postsCollection.document(postId).set(map).await()

        downloadUrl
    }

}