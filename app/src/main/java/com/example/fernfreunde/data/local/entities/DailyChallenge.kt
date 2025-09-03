package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_challenges",
    indices = [Index(value = ["challengeId"])]
)
data class DailyChallenge(
    @PrimaryKey
    val challengeId: String,
    val date: String? = null,               // format: "yyyy-MM-dd"
    val title: String,
    val text: String,
    val startAt: Long,
    val expiresAt: Long,
    val maxPostsPerUser: Int? = 1   // defaultmäßig 1 Post per User, aber falls wir mehrere Posts per
                                    // User ermöglichen wollen; bei null = unlimited
)