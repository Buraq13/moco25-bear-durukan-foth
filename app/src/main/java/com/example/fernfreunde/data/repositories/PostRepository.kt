package com.example.fernfreunde.data.repositories

import android.content.Context
import android.net.Uri
import androidx.work.*
import androidx.work.NetworkType

import com.example.fernfreunde.data.local.daos.PostDao
import com.example.fernfreunde.data.local.entities.Post
import com.example.fernfreunde.data.mappers.SyncStatus
import com.example.fernfreunde.data.mappers.toEntity
import com.example.fernfreunde.data.remote.FirestoreDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Singleton
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
) {

    companion object {
        const val KEY_POST_ID = "key_post_id"
        const val KEY_MEDIA_URI = "key_media_uri"
        const val UNIQUE_WORK_PREFIX = "upload_post_"
    }

    // ***************************************************************** //
    // CREATE NEW POSTS                                                  //
    // ***************************************************************** //

    // lokal neuen Post erstellen und der Warteschlange eines Upload Workers hinzufügen ---> für ViewModel
    suspend fun createPost(userId: String, userName: String?, text: String?, mediaUri: Uri?) {
        val postId = UUID.randomUUID().toString()

        val localEntity = Post(
            localId = postId,
            remoteId = null,
            userId = userId,
            userName = userName,
            description = text,
            challengeDate = null,
            mediaLocalPath = mediaUri.toString(),
            mediaRemoteUrl = null,
            createdAtClient = System.currentTimeMillis(),
            createdAtServer = null,
            syncStatus = SyncStatus.PENDING
        )

        // Post sofort lokal persistieren, so dass er direkt in der UI angezeigt werden kann
        postDao.insert(localEntity)

        // In die Warteschlange des opload Workers (WorkManager) hinzufügen
        enqueueUploadWork(postId = postId, mediaUri = mediaUri)
    }

    // ***************************************************************** //
    // UPLOAD POSTS                                                      //
    // ***************************************************************** //

    // WorkManager um Posts hochzuladen ---> für PostRepository & syncPendingPosts
    fun enqueueUploadWork(postId: String, mediaUri: Uri?) {
        val dataBuilder = Data.Builder()
            .putString(KEY_POST_ID, postId)
        mediaUri?.let { dataBuilder.putString(KEY_MEDIA_URI, it.toString()) }

        val inputData = dataBuilder.build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadWork = OneTimeWorkRequestBuilder<com.example.fernfreunde.worker.UploadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork("$UNIQUE_WORK_PREFIX$postId", ExistingWorkPolicy.REPLACE, uploadWork)
    }

    // ***************************************************************** //
    // OBSERVE POSTS                                             //
    // ***************************************************************** //

    // Posts aller Freunde im Feed anzeigen ---> für ViewModel
    fun observeFeedForUser(challengeDate: String, friendIds: List<String>): Flow<List<Post>> {
        if (friendIds.isEmpty()) {
            return flowOf(emptyList())
        }
        return postDao.observePostsForChallengeByUsers(challengeDate, friendIds)
    }

    // ***************************************************************** //
    // SYNCHRONICE REMOTE <=> LOCAL                                      //
    // ***************************************************************** //

    // One-Shot Synchronisierung remote <=> local ---> kann manuell von einem SyncService oder beim AppStart aufgerufen werden (optional)
    suspend fun syncFriendsPostsRemoteToLocal(friendIds: List<String>) = withContext(Dispatchers.IO) {
        val remotePosts = firestoreDataSource.getFriendsPosts(friendIds)
        val entities = remotePosts.map { it.toEntity() }
        if (entities.isNotEmpty()) {
            postDao.insertAll(entities)
        }
    }

    // lokale Datenbank nach Posts mit status "PENDING" überprüfen & in die Warteschlange für Upload packen
    suspend fun syncPendingPosts() = withContext(Dispatchers.IO) {
        val pending = postDao.getPostsBySyncStatus("PENDING")
        pending.forEach { post ->
            enqueueUploadWork(post.localId, null /* mediaUri unknown here */)
        }
    }
}
