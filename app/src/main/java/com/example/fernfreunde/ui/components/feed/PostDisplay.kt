package com.example.fernfreunde.ui.components.feed

data class PostDisplay (
    val postId: String,
    val userId: String,
    val userName: String?,
    val userProfileUrl: String?,
    val description: String?,
    val mediaUrl: String?,
    val createdAt: Long
)