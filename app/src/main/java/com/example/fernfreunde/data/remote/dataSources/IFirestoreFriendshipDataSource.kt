package com.example.fernfreunde.data.remote.dataSources

import com.example.fernfreunde.data.remote.dtos.FriendshipDto
import kotlinx.coroutines.flow.Flow

interface IFirestoreFriendshipDataSource {
    suspend fun getFriendshipsForUser(userId: String): List<FriendshipDto>
    suspend fun getFriendIds(userId: String): List<String>

    suspend fun sendFriendRequest(fromUserId: String, toUserId: String)
    suspend fun acceptFriendRequest(aUserId: String, bUserId: String)
    suspend fun getIncomingRequests(userId: String): List<FriendshipDto>

    suspend fun removeFriend(aUserId: String, bUserId: String)
}
