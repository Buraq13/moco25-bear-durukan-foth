package com.example.fernfreunde.data.remote.dataSources

import android.net.Uri
import com.example.fernfreunde.data.remote.dtos.PostDto
import com.example.fernfreunde.data.other.Constants.POST_COLLECTION
import com.example.fernfreunde.data.remote.dtos.UserDto
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePostDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val postsCollection = firestore.collection(POST_COLLECTION)

    // ***************************************************************** //
    // READ OPERATIONS                                                   //
    // ***************************************************************** //

    // die Posts von allen Freunden des eingeloggten Users holen
    // da Firebase nur maximal 10 Einträge pro Anfrage erlaubt, chunked die Anfrage
    suspend fun getFriendsPosts(friendIds: List<String>, challengeId: String): List<PostDto> = withContext(Dispatchers.IO) {
        if (friendIds.isEmpty()) return@withContext emptyList()
        try {
            val results = mutableListOf<PostDto>()
            val chunks = friendIds.chunked(10)
            for (chunk in chunks) {
                var query = postsCollection.whereIn("userId", chunk)

                query = query.whereEqualTo("challengeId", challengeId)

                val snapshot = query
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                results += snapshot.documents.mapNotNull {
                    it.toObject(PostDto::class.java)
                }
            }

            val sorted = results.sortedByDescending { dto ->
                val server = when (val s = dto.createdAtServer) {
                    is Timestamp -> s.toDate().time
                    is Long -> s
                    else -> null
                }
                server ?: dto.createdAtClient
            }
            sorted
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAllPosts(): List<PostDto> {
        val snapshot = postsCollection.limit(100).get().await()
        return snapshot.documents.mapNotNull { it.toObject(PostDto::class.java) }
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