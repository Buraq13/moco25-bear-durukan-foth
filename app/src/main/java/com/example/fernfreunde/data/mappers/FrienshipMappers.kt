package com.example.fernfreunde.data.mappers

import com.example.fernfreunde.data.local.entities.Friendship
import com.example.fernfreunde.data.remote.dtos.FriendshipDto
import java.util.UUID

fun FriendshipDto.toEntity(): Friendship {
    return Friendship(
        userIdA = this.userIdA,
        userIdB = this.userIdB,
        status = this.status,
        createdAt = this.createdAt,
        lastInteractionAt = this.lastInteractionAt
    )
}

fun Friendship.toDto(): FriendshipDto {
    return FriendshipDto(
        userIdA = this.userIdA,
        userIdB = this.userIdB,
        status = this.status,
        createdAt = this.createdAt,
        lastInteractionAt = this.lastInteractionAt
    )
}