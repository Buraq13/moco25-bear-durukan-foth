package com.example.fernfreunde.data.remote.dataSources

import android.net.Uri
import com.example.fernfreunde.data.remote.dtos.UserDto

interface IFirestoreUserDataSource {
    suspend fun getUser(userId: String): UserDto?
    suspend fun getUsers(userIds: List<String>): List<UserDto>
    suspend fun getAllUsers(): List<UserDto>

    suspend fun createOrUpdateUser(user: UserDto)
    suspend fun uploadProfileImage(userId: String, mediaUri: Uri): String

    fun listenUser(userId: String): kotlinx.coroutines.flow.Flow<UserDto?>
}
