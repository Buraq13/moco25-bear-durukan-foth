package com.example.fernfreunde

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fernfreunde.data.mappers.SyncStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

// Anpassung: Pakete / Klassennamen prüfen - passe ggf. an dein Projekt an
@RunWith(AndroidJUnit4::class)
class FirestoreIntegrationTest {

    // App-Kontext
    private val context = ApplicationProvider.getApplicationContext<Context>()

    // In-Memory Room DB (keine persistente Daten)
    private lateinit var db: com.example.fernfreunde.data.local.database.AppDatabase
    private lateinit var postDao: com.example.fernfreunde.data.local.daos.PostDao

    // Firebase (emulator)
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    @Before
    fun setup() {
        // 1) Stelle sicher, dass die Firebase SDK auf den Emulator zeigt (nochmals, zur Sicherheit)
        firestore = FirebaseFirestore.getInstance().apply {
            useEmulator("10.0.2.2", 8080)
        }
        storage = FirebaseStorage.getInstance().apply {
            useEmulator("10.0.2.2", 9199)
        }
        auth = FirebaseAuth.getInstance().apply {
            useEmulator("10.0.2.2", 9099)
        }

        // 2) In-Memory Room DB, Testmodus
        db = Room.inMemoryDatabaseBuilder(context, com.example.fernfreunde.data.local.database.AppDatabase::class.java)
            .allowMainThreadQueries() // in Tests ok
            .build()
        postDao = db.postDao()
    }

    @After
    fun teardown() = runBlocking {
        // close Room DB
        db.close()
        // optional: CLEANUP in Firestore/Storage - falls noch gefunden, aber wir löschen explizit im Test
    }

    @Test
    fun createPost_writesToFirestoreAndCleansUp() = runBlocking {
        // 1) Erstelle lokalen Post in Room (in-memory)
        val postId = UUID.randomUUID().toString()
        val localPost = com.example.fernfreunde.data.local.entities.Post(
            localId = postId,
            remoteId = null,
            userId = "test_user_123",
            userName = "TestUser",
            challengeDate = null,
            challengeId = null,
            description = "Integration test post - no media",
            mediaLocalPath = null,
            mediaRemoteUrl = null,
            createdAtClient = System.currentTimeMillis(),
            createdAtServer = null,
            syncStatus = com.example.fernfreunde.data.mappers.SyncStatus.PENDING
        )
        postDao.insert(localPost)

        // 2) Erzeuge DTO für Firestore (einfach, minimal)
        val postDto = mapOf(
            "postId" to postId,
            "authorId" to localPost.userId,
            "authorName" to localPost.userName,
            "description" to localPost.description,
            "mediaUrl" to null,
            "createdAtClient" to localPost.createdAtClient,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        // 3) Schreibe direkt in Firestore (wir testen Firestore-Write, nicht WorkManager)
        firestore.collection("posts").document(postId).set(postDto).await()

        // 4) Prüfe, ob das Dokument existiert
        val snapshot = firestore.collection("posts").document(postId).get().await()
        assertTrue("Firestore document should exist", snapshot.exists())

        // 5) Cleanup: lösche das Document wieder (damit Emulator sauber bleibt)
        firestore.collection("posts").document(postId).delete().await()

        // 6) Optional: Verifiziere, dass es gelöscht wurde
        val after = firestore.collection("posts").document(postId).get().await()
        assertTrue("Document should be deleted", !after.exists())
    }
}
