package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "userId"],
    indices = [Index(value = ["userId"]), Index(value = ["groupId"])]
)
data class GroupMember (
    val groupId: String,    // FK-Reference to Group.groupId
    val userId: String,     // FK-Reference to User.userId
    val joinedAt: Long
    // optional: val status: String
)