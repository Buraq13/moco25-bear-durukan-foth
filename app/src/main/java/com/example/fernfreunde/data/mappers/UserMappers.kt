package com.example.fernfreunde.data.mappers

import com.example.fernfreunde.data.local.entities.User
import com.example.fernfreunde.data.remote.dtos.UserDto
import java.util.UUID

fun UserDto.toEntity(): User {
    val id = this.userId.ifBlank { UUID.randomUUID().toString() }

    return User(
        userId = id,
        username = this.username,
        displayName = this.displayName ?: "",
        profileImageUrl = this.profileImageUrl,
        bio = this.bio,
        createdAt = this.updatedAt
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        userId = this.userId,
        displayName = this.displayName,
        username = this.username,
        profileImageUrl = this.profileImageUrl,
        bio = this.bio
    )
}