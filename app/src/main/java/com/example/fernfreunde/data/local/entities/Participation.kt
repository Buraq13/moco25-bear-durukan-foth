package com.example.fernfreunde.data.local.entities

import androidx.room.Entity

@Entity(
    tableName = "participations",
    primaryKeys = ["userId", "date"]
)
data class Participation(
    val userId: String,           // FK-Reference to User.userId
    val date: String,             // format: "yyyy-MM-dd", FK-Reference to DailyChallenge.date (as Id)
    val postLocalId: String?,     // local reference
    val createdAtClient: Long,
    val synced: Boolean
)
