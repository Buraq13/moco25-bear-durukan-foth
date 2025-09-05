package com.example.fernfreunde.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.fernfreunde.data.mappers.Converters

@Entity(
    tableName = "pending_uploads"
)
@TypeConverters(Converters::class)
data class PendingUpload(
    @PrimaryKey
    val id: String,
    val postLocalId: String,
    val filePaths: List<String>,    // local file paths (TypeConverter)
    val attempts: Int = 0,
    val nextRetryAt: Long? = null,
    val createdAt: Long
)
