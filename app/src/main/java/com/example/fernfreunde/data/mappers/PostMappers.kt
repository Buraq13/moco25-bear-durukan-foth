package com.example.fernfreunde.data.mappers

import com.example.fernfreunde.data.local.entities.Post
import com.example.fernfreunde.data.remote.dtos.PostDto
import com.google.firebase.Timestamp
import java.util.UUID

fun PostDto.toEntity(): Post {
    val createdServerMillis: Long? = when (val s = this.createdAt) {
        is Timestamp -> s.toDate().time
        is Long -> s
        is Number -> s.toLong()
        else -> null
    }

    val id = this.postId.ifBlank { UUID.randomUUID().toString() }
    val remoteIdResolved = this.postId.ifBlank { id }

    return Post(
        localId = id,
        remoteId = remoteIdResolved,
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
        postId = this.remoteId ?: this.localId,
        userId = this.userId,
        userName = this.userName,
        description = this.description,
        challengeDate = this.challengeDate,
        challengeId = this.challengeId,
        mediaRemoteUrl = this.mediaRemoteUrl,
        createdAtClient = this.createdAtClient,
        createdAt = this.createdAtServer
    )
}
