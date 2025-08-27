package com.example.fernfreunde.data.repositories

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.*
import androidx.work.NetworkType
import com.example.fernfreunde.data.local.daos.DailyChallengeDao
import com.example.fernfreunde.data.local.daos.ParticipationDao

import com.example.fernfreunde.data.local.daos.PostDao
import com.example.fernfreunde.data.local.entities.Participation
import com.example.fernfreunde.data.local.entities.Post
import com.example.fernfreunde.data.mappers.SyncStatus
import com.example.fernfreunde.data.mappers.toEntity
import com.example.fernfreunde.data.remote.FirestoreDataSource
import com.google.firebase.auth.FirebaseAuth
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
    private val dailyChallengeDao: DailyChallengeDao,
    private val participationDao: ParticipationDao,
    private val firestoreDataSource: FirestoreDataSource,
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
        date: String,
        challengeId: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val dailyChallenge = if (challengeId != null) {
            dailyChallengeDao.getDailyChallengeByDateAndId(date, challengeId)
        } else {
            dailyChallengeDao.getDefaultForDate(date)
        }

        val maxAllowed = dailyChallenge?.maxPostsPerUser ?: 1

        if (maxAllowed == null) {
            return@withContext true
        }

        val currentCount = postDao.countPostsForUserInChallenge(userId, date, challengeId)
        currentCount < maxAllowed
    }


    // ***************************************************************** //
    // CREATE NEW POSTS                                                  //
    // ***************************************************************** //

    // lokal neuen Post erstellen und der Warteschlange eines Upload Workers hinzufügen
    // ---> wird von CreatePostViewModel aufgerufen, nachdem Eingabe validiert wurde
    suspend fun createPost(userId: String, userName: String?, date: String, challengeId: String, text: String?, mediaUri: Uri?) {
        val postId = UUID.randomUUID().toString()

        val localEntity = Post(
            localId = postId,
            remoteId = null,
            userId = userId,
            userName = userName,
            description = text,
            challengeDate = date,
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

        // Participation lokal persestieren um doppelte Teilnahme zu verhindern
        val participation = Participation(
            userId = userId,
            date = date,
            challengeId = challengeId,
            postLocalId = postId,
            createdAtClient = System.currentTimeMillis()
        )
        participationDao.insertParticipation(participation)

        // Post in die Warteschlange des upload Workers (WorkManager) hinzufügen
        enqueueUploadWork(postId = postId, mediaUri = mediaUri)
    }

    // ***************************************************************** //
    // UPLOAD POSTS (WORK MANAGER)                                       //
    // ***************************************************************** //

    // WorkManager um Posts in Firebase hochzuladen ---> für PostRepository & syncPendingPosts
    fun enqueueUploadWork(postId: String, mediaUri: Uri?) {

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
    }

    // ***************************************************************** //
    // OBSERVE POSTS                                             //
    // ***************************************************************** //

    // Posts aller Freunde im Feed anzeigen ---> für ViewModel
    fun observeFeedForUser(challengeDate: String, challengeId: String, friendIds: List<String>): Flow<List<Post>> {
        if (friendIds.isEmpty()) {
            return flowOf(emptyList())
        }
        return postDao.observePostsForChallengeByUsers(challengeDate, challengeId, friendIds)
    }

    // ***************************************************************** //
    // SYNCHRONICE REMOTE <=> LOCAL HELPERS                              //
    // ***************************************************************** //

    // One-Shot Synchronisierung remote <=> local ---> kann manuell von einem SyncService oder beim AppStart aufgerufen werden (optional)
    // holt einmalig alle Posts der angegebenen friendsIds aus Firestore und schreibt die in die lokale Room Datenbank
    suspend fun syncFriendsPostsRemoteToLocal(friendIds: List<String>) = withContext(Dispatchers.IO) {
        val remotePosts = firestoreDataSource.getFriendsPosts(friendIds)
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
            enqueueUploadWork(post.localId, post.mediaLocalPath?.toUri())
        }
    }
}
