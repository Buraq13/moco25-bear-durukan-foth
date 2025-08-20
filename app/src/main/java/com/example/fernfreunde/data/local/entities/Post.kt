package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Group::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["groupId"]), Index(value = ["createdAtClient"])]
)
data class Post(
    @PrimaryKey
    val localId: String,               // client-side UUID; later set remoteId
    val remoteId: String?,             // Firestore doc id after sync
    val userId: String,
    val userName: String?,
    // ???
    val groupId: String?,
    val challengeDate: String?,          // format: "yyyy-MM-dd"
    val description: String?,
    val mediaLocalPaths: List<String>,   // TypeConverter -> JSON
    val mediaRemoteUrls: List<String>,   // TypeConverter -> JSON
    val thumbnailLocalPath: String?,
    val thumbnailRemoteUrl: String?,
    val createdAtClient: Long,
    val createdAtServer: Long?,          // nullable until server provides
    // val status: SyncStatus
)
