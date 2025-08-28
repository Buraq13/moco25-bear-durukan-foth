package com.example.fernfreunde.worker
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fernfreunde.data.local.daos.PostDao
import com.example.fernfreunde.data.mappers.toDto
import com.example.fernfreunde.data.remote.FirestorePostDataSource
import com.google.firebase.storage.FirebaseStorage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import androidx.core.net.toUri
import com.example.fernfreunde.data.mappers.SyncStatus

// @HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val postDao: PostDao,
    private val firestorePostDataSource: FirestorePostDataSource,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_POST_ID = "key_post_id"
        const val KEY_MEDIA_URI = "key_media_uri"
    }

    // main-method eines Coroutine-Workers, wird vom WorkManager im Hintergrund aufgerufen
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        // inputData ist das in setInputData(inputData) übergebene DataObjekt
        val postId = inputData.getString(KEY_POST_ID)
            ?: return@withContext Result.failure()

        val mediaUriString = inputData.getString(KEY_MEDIA_URI)
        val mediaUri = mediaUriString?.toUri()

        // ließt den lokalen Datensatz aus Room
        // falls kein lokaler Datensatz mit der entsprechenden postId existiert, wird ein Fehler zurückgegeben
        val local = postDao.getPostSync(postId) ?: return@withContext Result.failure()

        // wenn mediaUri existiert, wird Upload in FirebaseStorage durchgeführt und die downloadUrl zurückgegeben
        // mit postDao.updateAfterSync(...) wird der lokale Eintrag in Room aktualisiert und die dowloadUrl als
        // mediaRemoteUrl hinzugefügt
        try {
            if (mediaUri != null) {
                val downloadUrl = firestorePostDataSource.createPost(local.toDto(), mediaUri)
                postDao.updateAfterSync(postId, postId, downloadUrl, SyncStatus.SYNCED, System.currentTimeMillis())
            }
            Result.success()
        } catch (e: Exception) {
            return@withContext Result.retry()
        }
    }
}