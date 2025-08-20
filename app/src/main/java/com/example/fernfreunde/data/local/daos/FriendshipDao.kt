package com.example.fernfreunde.data.local.daos

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fernfreunde.data.local.entities.Friendship
import kotlinx.coroutines.flow.Flow

interface FriendshipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(friendship: Friendship)

    @Query("DELETE FROM friendships WHERE userIdA = :a AND userIdB = :b")
    suspend fun deleteFriendship(a: String, b: String)

    // einamlig überprüfen, ob eine Freundschaft existiert ---> für Repo/UseCase
    @Query("SELECT * FROM friendships WHERE userIdA = :a AND userIdB = :b LIMIT 1")
    suspend fun getFriendshipSync(a: String, b: String): Friendship?

    // alle Freunde eines Nutzers anzeigen, bei Änderungen automatisch UI updaten ---> für ViewModel (FriendslistScreen)
    @Query("""
    SELECT * FROM friendships 
    WHERE (userIdA = :userId OR userIdB = :userId) 
      AND status = 'ACCEPTED' 
    ORDER BY lastInteractionAt DESC
  """)
    fun observeFriendsForUser(userId: String): Flow<List<Friendship>>

    // einmalig alle Freunde eines nutzers zählen ---> für Repo/ViewModel
    @Query("""
    SELECT COUNT(*) FROM friendships 
    WHERE (userIdA = :userId OR userIdB = :userId) 
      AND status = 'ACCEPTED'
  """)
    suspend fun countFriends(userId: String): Int

    // Timestamp in >>lastInteractionAt<< einer friendship updaten, wenn die User interagieren ---> für Repository on interaction event
    @Query("UPDATE friendships SET lastInteractionAt = :ts WHERE userIdA = :a AND userIdB = :b")
    suspend fun updateLastInteraction(a: String, b: String, ts: Long)

    // optional: Freundschaftanfragen anzeigen (wenn status == PENDING) ---> für ViewModel
    @Query("""
    SELECT * FROM friendships 
    WHERE (userIdA = :userId OR userIdB = :userId) 
      AND status = 'PENDING'
    ORDER BY createdAt DESC
  """)
    fun observePendingForUser(userId: String): Flow<List<Friendship>>
}