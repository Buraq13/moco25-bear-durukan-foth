package com.example.fernfreunde.data.remote.dtos

import com.example.fernfreunde.data.local.entities.FriendshipStatus

data class FriendshipDto (
    val userIdA: String = "",
    val userIdB: String = "",
    var status: FriendshipStatus = FriendshipStatus.PENDING,  // PENDING/ACCEPTED/BLOCKED
    val requestedBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)