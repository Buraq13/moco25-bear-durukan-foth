package com.example.fernfreunde.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fernfreunde.data.local.entities.Friendship
import com.example.fernfreunde.data.local.entities.FriendshipStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendshipDao {

    // ***************************************************************** //
    // INSERT FRIENDSHIPS                                                //
    // ***************************************************************** //

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(friendship: Friendship)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Friendship>)

    // ***************************************************************** //
    // GET/OBSERVE FRIENDSHIPS                                           //
    // ***************************************************************** //

    // einmalig überprüfen, ob eine Freundschaft existiert ---> für Repo/UseCase
    @Query("SELECT * FROM friendships WHERE userIdA = :a AND userIdB = :b LIMIT 1")
    suspend fun getFriendshipSync(a: String, b: String): Friendship?

    // alle Freunde eines Nutzers anzeigen, bei Änderungen automatisch UI updaten ---> für ViewModel (FriendslistScreen)
    @Query("""
      SELECT * FROM friendships 
      WHERE (userIdA = :userId OR userIdB = :userId) 
        AND status = :status
      ORDER BY createdAt DESC
    """)
    fun observeFriendsForUserWithStatus(userId: String, status: FriendshipStatus = FriendshipStatus.ACCEPTED): Flow<List<Friendship>>

    // ***************************************************************** //
    // GET FRIEND-IDS FOR USER                                           //
    // call getUsersById in UserDao to get User-Entities                 //
    // ***************************************************************** //

    @Query("""
      SELECT CASE
        WHEN userIdA = :userId THEN userIdB
        ELSE userIdA
      END
      FROM friendships
      WHERE (userIdA = :userId OR userIdB = :userId)
        AND status = :status
    """)
    suspend fun getFriendIdsForUser(userId: String, status: FriendshipStatus = FriendshipStatus.ACCEPTED): List<String>

    // ***************************************************************** //
    // COUNT FRIENDS FOR USER                                            //
    // ***************************************************************** //

    // einmalig alle Freunde eines Nutzers zählen ---> für Repo/ViewModel
    @Query("""
    SELECT COUNT(*) FROM friendships 
    WHERE (userIdA = :userId OR userIdB = :userId) 
      AND status = 'ACCEPTED'
  """)
    suspend fun countFriends(userId: String): Int

    // ***************************************************************** //
    // DELETE                                                            //
    // ***************************************************************** //

    @Query("DELETE FROM friendships WHERE userIdA = :a AND userIdB = :b")
    suspend fun deleteFriendship(a: String, b: String)
}