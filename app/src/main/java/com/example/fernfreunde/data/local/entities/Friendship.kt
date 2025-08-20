package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "friendships",
    primaryKeys = ["userIdA", "userIdB"],
    indices = [Index(value = ["userIdA"]), Index(value = ["userIdB"])]
)
data class Friendship(
    val userIdA: String,
    val userIdB: String,
    val status: FriendshipStatus,
    val createdAt: Long,
    val lastInteractionAt: Long    // epoch millis
)
