package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey
    val userId: String,             // UUID-String
    var username: String,
    var displayName: String,
    var profileImageUrl: String?,   // remote URL (Firebase Storage)
    var bio: String?,
    val createdAt: Long
)