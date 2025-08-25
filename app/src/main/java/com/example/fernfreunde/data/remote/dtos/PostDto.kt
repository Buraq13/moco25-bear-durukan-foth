package com.example.fernfreunde.data.remote.dtos

data class PostDto (
    val postId: String = "",
    val userId: String = "",
    val userName: String? = null,
    val challengeDate: String? = null,
    val description: String? = null,
    val mediaRemoteUrl: String? = null,           // remoteUrl von Firebase!!!
    val createdAtClient: Long = System.currentTimeMillis(),
    val createdAtServer: Any? = null
)