package com.example.fernfreunde.data.mappers

import com.example.fernfreunde.data.local.entities.Post
import com.example.fernfreunde.data.remote.dtos.PostDto
import com.google.firebase.Timestamp
import java.util.UUID

fun PostDto.toEntity(): Post {
    val createdServerMillis: Long? = when (val s = this.createdAtServer) {
        is Timestamp -> s.toDate().time
        is Long -> s
        else -> null
    }

    val id = this.postId.ifBlank { UUID.randomUUID().toString() }
    return Post(
        localId = this.postId,
        remoteId = this.postId,                 // localId == remoteId für Idemptotenz
        userId = this.userId,
        userName = this.userName,
        challengeDate = this.challengeDate,
        challengeId = this.challengeId,
        description = this.description,
        mediaLocalPath = null,
        mediaRemoteUrl = this.mediaRemoteUrl,
        createdAtClient = this.createdAtClient,
        createdAtServer = createdServerMillis,
        syncStatus = SyncStatus.SYNCED
    )
}

fun Post.toDto(): PostDto {
    return PostDto(
        postId = this.remoteId ?: this.localId,    // localId == remoteId für Idemptotenz
        userId = this.userId,
        userName = this.userName,
        description = this.description,
        challengeDate = this.challengeDate,
        mediaRemoteUrl = this.mediaRemoteUrl,
        createdAtClient = this.createdAtClient,
        createdAtServer = this.createdAtServer      // or null
    )
}