package com.example.fernfreunde.data.remote.dtos

import com.example.fernfreunde.data.local.entities.FriendshipStatus

data class FriendshipDto (
    val userIdA: String = "",
    val userIdB: String = "",
    val status: FriendshipStatus = FriendshipStatus.PENDING,  // PENDING/ACCEPTED/BLOCKED
    val createdAt: Long = System.currentTimeMillis(),
    val lastInteractionAt: Long? = null
)