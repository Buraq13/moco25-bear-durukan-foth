package com.example.fernfreunde.worker
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fernfreunde.data.local.daos.PostDao
import com.example.fernfreunde.data.mappers.toDto
import com.example.fernfreunde.data.remote.FirestoreDataSource
import com.google.firebase.storage.FirebaseStorage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import androidx.core.net.toUri

// @HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val postDao: PostDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_POST_ID = "key_post_id"
        const val KEY_MEDIA_URI = "key_media_uri"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        val postId = inputData.getString(KEY_POST_ID)
            ?: return@withContext Result.failure()

        val mediaUriString = inputData.getString(KEY_MEDIA_URI)
        val mediaUri = mediaUriString?.toUri()

        val local = postDao.getPostSync(postId) ?: return@withContext Result.failure()

        try {
            if (mediaUri != null) {
                val downloadUrl = firestoreDataSource.createPost(local.toDto(), mediaUri)
                postDao.updateAfterSync(postId, postId, downloadUrl, "SYNCED", System.currentTimeMillis())
            }
            Result.success()
        } catch (e: Exception) {
            return@withContext Result.retry()
        }
    }
}