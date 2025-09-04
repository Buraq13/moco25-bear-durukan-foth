package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.fernfreunde.data.mappers.Converters
import com.example.fernfreunde.data.mappers.SyncStatus

@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = DailyChallenge::class,
            parentColumns = ["challengeId"],
            childColumns = ["challengeId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["challengeId"]),
        Index(value = ["createdAtClient"])
    ]
)
@TypeConverters(Converters::class)      // Convertiert automatisch SyncStatus <=> String
data class Post(
    @PrimaryKey
    val localId: String,                // lokale eindeutige Id (client-seitig erzeugt)
    val remoteId: String?,              // Id in Firestore, wird erst gesetzt wenn Post erfolgreich in Firestore gespichert wurde (server-seitig erzeugt)
                                        // localId == remoteId für Idempotenz
    val userId: String,
    val userName: String?,
    val challengeDate: String?,         // format: "yyyy-MM-dd", FK-Reference to DailyChallenge.date
    val challengeId: String?,           // FK-Reference to DailyChallenge.challengeId
    val description: String?,
    val mediaLocalPath: String?,        // Pfad zur lokalen Datei (vor Upload, für Preview vorm Posten und als Uploadquelle)
    val mediaRemoteUrl: String?,        // Pfad zur Url in Firebase
    val createdAtClient: Long,
    val createdAtServer: Long?,          // null bis vom Server bereitgestellt
    val syncStatus: SyncStatus           // zur Überprüfung der Synchronisierung zwischen Room <=> Firebase
)
