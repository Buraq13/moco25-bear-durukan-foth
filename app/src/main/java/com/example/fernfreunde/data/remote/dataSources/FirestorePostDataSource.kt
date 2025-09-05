package com.example.fernfreunde.data.remote.dataSources

import android.net.Uri
import com.example.fernfreunde.data.remote.dtos.PostDto
import com.example.fernfreunde.data.other.Constants.POST_COLLECTION
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.example.fernfreunde.data.remote.utils.getMillis
import com.example.fernfreunde.data.remote.utils.getStringSafe

@Singleton
class FirestorePostDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val postsCollection = firestore.collection(POST_COLLECTION)

    // ***************************************************************** //
    // READ OPERATIONS                                                   //
    // ***************************************************************** //

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

                results += snapshot.documents.mapNotNull { doc ->
                    try {
                        val postId = doc.getStringSafe("postId") ?: doc.id
                        val userId = doc.getStringSafe("userId") ?: ""
                        val userName = doc.getStringSafe("userName")
                        val challengeDate = doc.getStringSafe("challengeDate")
                        val challengeIdField = doc.getStringSafe("challengeId")
                        val description = doc.getStringSafe("description")
                        val mediaRemoteUrl = doc.getStringSafe("mediaRemoteUrl")

                        val createdAtClient = doc.getMillis("createdAtClient")
                            ?: doc.getMillis("createdAt")
                            ?: System.currentTimeMillis()

                        val createdAtRaw = doc.get("createdAt")

                        PostDto(
                            postId = postId,
                            userId = userId,
                            userName = userName,
                            challengeDate = challengeDate,
                            challengeId = challengeIdField,
                            description = description,
                            mediaRemoteUrl = mediaRemoteUrl,
                            createdAtClient = createdAtClient,
                            createdAt = createdAtRaw
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }

            val sorted = results.sortedByDescending { dto ->
                val server = when (val s = dto.createdAt) {
                    is Timestamp -> s.toDate().time
                    is Long -> s
                    is Number -> s.toLong()
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
        return snapshot.documents.mapNotNull { doc ->
            try {
                val postId = doc.getStringSafe("postId") ?: doc.id
                val userId = doc.getStringSafe("userId") ?: ""
                val userName = doc.getStringSafe("userName")
                val challengeDate = doc.getStringSafe("challengeDate")
                val challengeIdField = doc.getStringSafe("challengeId")
                val description = doc.getStringSafe("description")
                val mediaRemoteUrl = doc.getStringSafe("mediaRemoteUrl")
                val createdAtClient = doc.getMillis("createdAtClient")
                    ?: doc.getMillis("createdAt")
                    ?: System.currentTimeMillis()
                val createdAtRaw = doc.get("createdAt")

                PostDto(
                    postId = postId,
                    userId = userId,
                    userName = userName,
                    challengeDate = challengeDate,
                    challengeId = challengeIdField,
                    description = description,
                    mediaRemoteUrl = mediaRemoteUrl,
                    createdAtClient = createdAtClient,
                    createdAt = createdAtRaw
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // ***************************************************************** //
    // WRITE OPERATIONS                                                  //
    // ***************************************************************** //

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
            "userName" to postDto.userName,
            "challengeDate" to postDto.challengeDate,
            "challengeId" to postDto.challengeId,
            "description" to postDto.description,
            "mediaRemoteUrl" to downloadUrl,
            "createdAtClient" to postDto.createdAtClient,
            "createdAt" to FieldValue.serverTimestamp()
        )

        postsCollection.document(postId).set(map).await()

        downloadUrl
    }
}
