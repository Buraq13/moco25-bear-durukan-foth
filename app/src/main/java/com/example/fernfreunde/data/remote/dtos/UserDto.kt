package com.example.fernfreunde.data.remote.dtos

class UserDto (
    val userId: String = "",
    val displayName: String? = null,
    val username: String = "",
    val profileImageUrl: String? = null,
    val bio: String? = null,
    val updatedAt: Long? = null // can be Timestamp or Long/null
)