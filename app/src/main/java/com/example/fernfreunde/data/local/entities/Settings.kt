package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "settings"
)
data class Settings(
    @PrimaryKey
    val userId: String,                     // FK-Reference to User.userId -> one-to-one
    val pushEnabled: Boolean = true,
    val reminderEnabled: Boolean = true,
    val reminderOffsetMinutes: Int = 60,
)
