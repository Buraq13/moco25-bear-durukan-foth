package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Challenge in Challenge-Pool
@Entity(
    tableName = "challenges"
)
data class Challenge (
    @PrimaryKey
    val challengeId: String,                        // UUID-String
    val title: String?,
    val text: String,
    val type: ChallengeType = ChallengeType.ANY     // PHOTO, VIDEO, ANY; defaultmäßig ANY
)

// konkrete Instanz einer Challenge
@Entity(
    tableName = "daily_challenges",
    primaryKeys = ["date", "challengeId"],
    foreignKeys = [
        ForeignKey(
            entity = Challenge::class,
            parentColumns = ["challengeId"],
            childColumns = ["challengeId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["challengeId"])]
)
data class DailyChallenge(
    val date: String,               // format: "yyyy-MM-dd"
    val challengeId: String,
    val startAt: Long,
    val expiresAt: Long,
    val maxPostsPerUser: Int? = 1   // defaultmäßig 1 Post per User, aber falls wir mehrere Posts per
                                    // User ermöglichen wollen; bei null = unlimited
)