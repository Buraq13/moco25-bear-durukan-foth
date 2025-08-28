package com.example.fernfreunde.data.remote.dtos

class UserDto (
    val userId: String = "",
    val displayName: String? = null,
    val username: String? = null,
    val photoUrl: String? = null,
    val bio: String? = null,
    val updatedAt: Any? = null // can be Timestamp or Long/null
)