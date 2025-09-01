package com.example.fernfreunde

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fernfreunde.data.local.database.AppDatabase
import com.example.fernfreunde.data.local.entities.User
import com.example.fernfreunde.data.mappers.SyncStatus
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class FirestoreIntegrationTest {

    // App-Kontext (Instrumentation)
    private val context = ApplicationProvider.getApplicationContext<Context>()

    // In-Memory Room DB (keine persistente Daten)
    private lateinit var db: com.example.fernfreunde.data.local.database.AppDatabase
    private lateinit var postDao: com.example.fernfreunde.data.local.daos.PostDao
    private lateinit var userDao: com.example.fernfreunde.data.local.daos.UserDao

    // Firebase (emulator)
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    @Before
    fun setup() {
        try {
            // 1) Sicherstellen: FirebaseApp initialisiert (explizit mit minimalen Options).
            //    In Tests ist google-services.json manchmal nicht korrekt gelesen, darum explizit setzen.
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    // PROJECT-ID: falls du einen bestimmten Projekt-Id im Emulator verwendest,
                    // setze sie hier. Für Emulator-Tests reicht oft ein Dummy.
                    .setProjectId("moco25-fernfreunde2") // <- optional an dein Projekt anpassen
                    // ApplicationId und API-Key sind Pflichtfelder, dürfen für Tests dummy sein:
                    .setApplicationId("1:000:android:test") // irgend-eindeutiger String
                    .setApiKey("test-key")
                    .build()

                FirebaseApp.initializeApp(context, options)
            }

            // 2) Firestore: Instanz holen und auf Emulator zeigen
            firestore = FirebaseFirestore.getInstance().apply {
                // Zeige auf den lokal laufenden Firestore-Emulator (Android emulator -> 10.0.2.2)
                useEmulator("10.0.2.2", 8080)
                // Deaktiviere lokale Persistence für Integrationstests, um Test-Races zu verhindern
                firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build()
            }

            // 3) Storage: Instanz holen und auf Emulator zeigen (falls du Storage-Emulator laufen hast)
            storage = FirebaseStorage.getInstance().apply {
                // Storage-Emulator-Port üblicherweise 9199 (prüfe deinen Emulator)
                useEmulator("10.0.2.2", 9199)
            }

            // 4) Auth: Instanz holen und auf Auth-Emulator zeigen
            auth = FirebaseAuth.getInstance().apply {
                // Auth-Emulator-Port z.B. 9099
                useEmulator("10.0.2.2", 9099)
                // Optional: signInAnonymously um sicherzustellen, dass Auth ready ist
                // (kann weggelassen werden wenn nicht gebraucht)
                // runCatching { signInAnonymously().await() }
            }

            // 5) In-memory Room DB für Tests (keine persistenten Änderungen)
            db = Room.inMemoryDatabaseBuilder(
                context,
                com.example.fernfreunde.data.local.database.AppDatabase::class.java
            )
                .allowMainThreadQueries() // in Tests OK
                .build()
            postDao = db.postDao()
            userDao = db.userDao()

        } catch (e: Exception) {
            // Wenn setup fehlschlägt, explizit failen - dann läuft @After nicht auf nicht-initialisierten Variablen
            fail("Test setup failed: ${e.message}")
        }
    }

    @After
    fun teardown() = runBlocking {
        // Wenn setup abgebrochen wurde, ist db evtl. nicht initialisiert => safe exit
        if (!::db.isInitialized) return@runBlocking

        try {
            // DB schließen
            db.close()
        } catch (e: Exception) {
            println("teardown: closing DB failed: ${e.message}")
        }
    }

        @Test
        fun createPost_writesToFirestoreAndCleansUp() = runBlocking {
            val userId = UUID.randomUUID().toString()
            val user = User(
                userId = userId,
                displayName = "TestUser",
                username = "testuser",
                profileImageUrl = null,
                bio = null,
                createdAt = System.currentTimeMillis()
            )
            userDao.upsert(user)

            // 1) Erstelle lokalen Post in Room (in-memory)
            val postId = UUID.randomUUID().toString()
            val localPost = com.example.fernfreunde.data.local.entities.Post(
                localId = postId,
                remoteId = null,
                userId = userId,
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
                "userId" to localPost.userId,
                "userName" to localPost.userName,
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
