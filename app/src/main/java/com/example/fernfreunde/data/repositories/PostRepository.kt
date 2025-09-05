package com.example.fernfreunde.data.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Log.*
import androidx.core.net.toUri
import androidx.work.*
import androidx.work.NetworkType
import com.example.fernfreunde.data.local.daos.DailyChallengeDao
import com.example.fernfreunde.data.local.daos.PostDao
import com.example.fernfreunde.data.local.entities.Post
import com.example.fernfreunde.data.mappers.SyncStatus
import com.example.fernfreunde.data.mappers.toDto
import com.example.fernfreunde.data.mappers.toEntity
import com.example.fernfreunde.data.remote.dataSources.FirestorePostDataSource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Singleton
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val dailyChallengeDao: DailyChallengeDao,
    private val firestorePostDataSource: FirestorePostDataSource,
    private val workManager: WorkManager,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {

    companion object {
        const val KEY_POST_ID = "key_post_id"
        const val KEY_MEDIA_URI = "key_media_uri"
        const val UNIQUE_WORK_PREFIX = "upload_post_"
    }

    private fun currentUserId(): String? = auth.currentUser?.uid

    // ***************************************************************** //
    // PARTICIPATION CHECK                                               //
    // ***************************************************************** //

    // Prüft, ob der aktuelle Nutzer noch einen Post für die gegebene Challenge erstellen darf
    suspend fun canCreatePost(
        userId: String = currentUserId() ?: throw IllegalStateException("no user signed in"),
        challengeId: String
    ): Boolean = withContext(Dispatchers.IO) {
        val currentChallenge = dailyChallengeDao.getCached()
        val maxAllowed = currentChallenge?.maxPostsPerUser ?: return@withContext true
        val currentCount = postDao.countPostsForUserAndChallenge(userId, challengeId)
        currentCount < maxAllowed
    }

    // ***************************************************************** //
    // CREATE NEW POSTS                                                  //
    // ***************************************************************** //

    // lokal neuen Post erstellen und der Warteschlange eines Upload Workers hinzufügen
    // ---> für CreatePostViewModel, aber vorher Eingabe validieren!
    suspend fun createPost(
        userId: String,
        userName: String?,
        challengeId: String,
        description: String?,
        mediaUri: Uri?): String = withContext(Dispatchers.IO) {

        val postId = UUID.randomUUID().toString()

        val localEntity = Post(
            localId = postId,
            remoteId = null,
            userId = userId,
            userName = userName,
            description = description,
            challengeDate = null,
            challengeId = challengeId,
            mediaLocalPath = mediaUri.toString(),
            mediaRemoteUrl = null,
            createdAtClient = System.currentTimeMillis(),
            createdAtServer = null,
            syncStatus = SyncStatus.PENDING
        )

        // Post sofort lokal persistieren, so dass er direkt in der UI angezeigt werden kann
        // z.B. für Vorschau, bevor der User auf "Posten" drückt
        postDao.insert(localEntity)

        // laternativ WorkManager nutzen
        GlobalScope.launch {
            try {
                uploadPostImmediately(postId, mediaUri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return@withContext postId
    }

    // ***************************************************************** //
    // UPLOAD POSTS (WORK MANAGER)                                       //
    // ***************************************************************** //

    fun enqueueUploadWork(postId: String, mediaUri: Uri?) {

        Log.i("PostRepository", "enqueueUploadWork called for postId=$postId mediaUri=$mediaUri - currently disabled (direct upload mode).")

        /*
        // dataBuilder erstellt DataObject, das aus Key-Value-Paaren besteht
        // in dem Fall enthält es PostId & MediaUri (wenn nicht null), damit der Worker weiß, welches Post-Objekt er bearbeiten soll
        val dataBuilder = Data.Builder()
            .putString(KEY_POST_ID, postId)
        mediaUri?.let { dataBuilder.putString(KEY_MEDIA_URI, it.toString()) }

        val inputData = dataBuilder.build()

        // constaints definiert die Vorbedingen welche erfüllt sein müssen, bevor der WorkManager den job ausführen darf
        // in dem Fall NetworkType.CONNECTED, heißt: nur bei bestehender Internetverbindung
        // weitere mögliche constraints: setRequiresBatteryNotLow(true), setRequiresStorageNotLow(true)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // OneTimeWorkRequestBuilder erstellt Auftragstyp für genau einen Lauf des Upload-Workers
        // dabei muss die Klasse des UploadWorkers übergeben werden, damit sie auch intanziiert werden kann,
        // nachdem die App-Prozesse beendet wurden (wichtig für Background-Uploads)
        val uploadWork = OneTimeWorkRequestBuilder<com.example.fernfreunde.worker.UploadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            // BackoffPolicy.EXPONENTIAL bedeutet, die Wartezeit für Neuversuche bei Fehlern steigt exponentiell
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        // enqueueUniqueWork(...) fügt eindeutigen Auftrag in die Warteschlange ein, falls schon ein Auftrag mit
        // exakt dem gleichen Namen existiert, wird er ersetzt -> verhindert doppelte Uploads
        workManager.enqueueUniqueWork("$UNIQUE_WORK_PREFIX$postId", ExistingWorkPolicy.REPLACE, uploadWork)
        */
    }

    // ***************************************************************** //
    // OBSERVE POSTS                                                     //
    // ***************************************************************** //

    // Posts aller Freunde im Feed anzeigen
    // ---> für FeedViewModel, liefert automatische Updates wenn ein Freund einen neuen Post hochlädt
    fun observeFeedForUser(challengeId: String, friendIds: List<String>): Flow<List<Post>> {
        if (friendIds.isEmpty()) {
            return flowOf(emptyList())
        }

        return flow {
            try {
                GlobalScope.launch {
                    try {
                        syncFriendsPostsRemoteToLocal(friendIds, challengeId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            emitAll(postDao.observePostsForChallengeByUsers(challengeId, friendIds))
        }
    }

    suspend fun getAll(): List<Post> = withContext(Dispatchers.IO) {
        if (firestorePostDataSource == null) return@withContext emptyList()
        val posts = firestorePostDataSource.getAllPosts()
        return@withContext posts.map { it.toEntity() }

    }

    // ***************************************************************** //
    // SYNCHRONICE REMOTE <=> LOCAL HELPERS                              //
    // ***************************************************************** //

    // One-Shot Synchronisierung remote <=> local ---> kann manuell von einem SyncService oder beim AppStart aufgerufen werden (optional)
    // holt einmalig alle Posts der angegebenen friendsIds aus Firestore und schreibt die in die lokale Room Datenbank
    suspend fun syncFriendsPostsRemoteToLocal(friendIds: List<String>, challengeId: String) = withContext(Dispatchers.IO) {
        val remotePosts = firestorePostDataSource.getFriendsPosts(friendIds, challengeId)
        val entities = remotePosts.map { it.toEntity() }
        if (entities.isNotEmpty()) {
            postDao.insertAll(entities)
        }
    }

    // lokale Datenbank nach Posts mit status "PENDING" überprüfen & in die Warteschlange für Upload packen
    // stellt sicher, dass nach Neustart der App/Reconnect alle noch nicht hochgeladenen Posts wieder
    // in die Warteschlange des Upload Workers kommen
    // ---> idealerweise einmal bei App-Start aufrufen
    suspend fun syncPendingPosts() = withContext(Dispatchers.IO) {
        val pending = postDao.getPostsBySyncStatus(SyncStatus.PENDING)
        pending.forEach { post ->
            // früher: enqueueUploadWork(post.localId, post.mediaLocalPath?.toUri())
            // jetzt: starte direkten Upload asynchron (ergebnis irrelevant für die Aufrufer-Logik)
            GlobalScope.launch {
                try {
                    uploadPostImmediately(post.localId, post.mediaLocalPath?.toUri())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // ***************************************************************** //
    // HILFSFUNKTIONEN                                                    //
    // ***************************************************************** //

    // Ersatz für Uplolad-Worker, da der nicht funktioniert hat: liest den lokalen Post, lädt die Mediendatei hoch
    // und aktualisiert den lokalen DB-Eintrag nach erfolgreichem Upload.
    private suspend fun uploadPostImmediately(postId: String, mediaUri: Uri?) = withContext(Dispatchers.IO) {
        val local = postDao.getPostSync(postId) ?: return@withContext

        try {
            if (mediaUri != null) {
                val downloadUrl = firestorePostDataSource.createPost(local.toDto(), mediaUri)
                postDao.updateAfterSync(postId, postId, downloadUrl, SyncStatus.SYNCED, System.currentTimeMillis())
                Log.i("PostRepository", "uploadPostImmediately success for postId=$postId url=$downloadUrl")
            } else {
                Log.i("PostRepository", "uploadPostImmediately: no mediaUri for postId=$postId; leaving as PENDING")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PostRepository", "uploadPostImmediately failed for postId=$postId: ${e.message}")
        }
    }
}
