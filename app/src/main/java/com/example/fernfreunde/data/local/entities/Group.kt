package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "groups",
    indices = [Index(value = ["groupName"])]
)
data class Group (
    @PrimaryKey
    val groupId: String,            // UUID-String
    var groupName: String,
    var groupImageUrl: String?,     // remote URL (Firebase Storage)
    var description: String?,
    val ownerId: String,            // FK-Reference to User.userId
    val createdAt: Long
)