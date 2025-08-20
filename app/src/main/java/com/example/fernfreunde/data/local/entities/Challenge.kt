package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Challenge in Challenge-Pool
@Entity(
    tableName = "challenges"
)
data class Challenge (
    @PrimaryKey
    val challengeId: String,       // UUID-String
    val title: String?,
    val text: String,
    val type: ChallengeType
)


@Entity(
    tableName = "daily_challenges"
)
data class DailyChallenge(
    @PrimaryKey
    val date: String,           // format: "yyyy-MM-dd"
    val challengeId: String,
    val startAt: Long,
    val expiresAt: Long
)