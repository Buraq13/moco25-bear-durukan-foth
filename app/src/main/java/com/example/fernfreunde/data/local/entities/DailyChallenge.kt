package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.fernfreunde.data.mappers.Converters

@Entity(
    tableName = "daily_challenges",
    indices = [Index(value = ["challengeId"])]
)
@TypeConverters(Converters::class)
data class DailyChallenge(
    @PrimaryKey
    val challengeId: String = "",
    val date: String? = null,               // format: "yyyy-MM-dd"
    val title: String = "",
    val text: String = "",
    val challengeType: ChallengeType = ChallengeType.ANY,
    val startAt: Long = 0,
    val expiresAt: Long = 0,
    val maxPostsPerUser: Int? = 1   // defaultmäßig 1 Post per User, aber falls wir mehrere Posts per
                                    // User ermöglichen wollen; bei null = unlimited
)