package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.TypeConverters
import com.example.fernfreunde.data.mappers.Converters

@Entity(
    tableName = "friendships",
    primaryKeys = ["userIdA", "userIdB"],
    indices = [Index(value = ["userIdA"]), Index(value = ["userIdB"])]
)
@TypeConverters(Converters::class)
data class Friendship(
    val userIdA: String,
    val userIdB: String,
    val status: FriendshipStatus,  // PENDING/ACCEPTED/BLOCKED
    val createdAt: Long,
    val lastInteractionAt: Long? = null  // epoch millis
)
