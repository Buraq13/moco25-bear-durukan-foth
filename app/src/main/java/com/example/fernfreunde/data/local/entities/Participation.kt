package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "participations",
    primaryKeys = ["userId", "date", "challengeId"],
    foreignKeys = [
        ForeignKey(
            entity = DailyChallenge::class,
            parentColumns = ["date","challengeId"],
            childColumns = ["date","challengeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Post::class,
            parentColumns = ["postLocalId"],
            childColumns = ["postLocalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["date"]),
        Index(value = ["challengeId"]),
        Index(value = ["postLocalId"])
    ]
)
data class Participation(
    val userId: String,           // FK-Reference to User.userId
    val date: String,             // format: "yyyy-MM-dd", FK-Reference to DailyChallenge.date
    val challengeId: String,      // FK-Reference to DailyChallenge.challengeId
    val postLocalId: String?,     // local reference
    val createdAtClient: Long = System.currentTimeMillis()
)
