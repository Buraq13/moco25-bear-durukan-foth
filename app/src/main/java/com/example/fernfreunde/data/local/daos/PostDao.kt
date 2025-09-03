package com.example.fernfreunde.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fernfreunde.data.local.entities.Post
import com.example.fernfreunde.data.mappers.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    // ***************************************************************** //
    // INSERT POSTS                                                      //
    // ***************************************************************** //

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: Post)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<Post>)

    // ***************************************************************** //
    // GET/OBSERVE POSTS                                                 //
    // ***************************************************************** //

    @Query("SELECT * FROM posts WHERE localId = :localId LIMIT 1")
    suspend fun getPostSync(localId: String): Post?

    @Query("""
        SELECT COUNT(*) FROM posts
        WHERE userId = :userId
          AND challengeId = :challengeId
    """)
    suspend fun countPostsForUserAndChallenge(userId: String, challengeId: String?): Int

    @Query(
        """
    SELECT * FROM posts
    WHERE challengeId = :challengeId
    AND userId IN (:userIds)
    ORDER BY COALESCE(createdAtServer, createdAtClient) DESC
    """
    )
    fun observePostsForChallengeByUsers(challengeId: String?, userIds: List<String>): Flow<List<Post>>

    // ***************************************************************** //
    // UPDATE SYNCSTATUS                                                 //
    // ***************************************************************** //

    @Query("SELECT * FROM posts WHERE syncStatus = :status")
    suspend fun getPostsBySyncStatus(status: SyncStatus): List<Post>

    @Query("""
        UPDATE posts
        SET remoteId = :remoteId,
            mediaRemoteUrl = :mediaUrl,
            syncStatus = :status,
            createdAtServer = :createdAtServer
        WHERE localId = :localId
    """)
    suspend fun updateAfterSync(localId: String, remoteId: String?, mediaUrl: String?, status: SyncStatus, createdAtServer: Long?)

    @Query("UPDATE posts SET syncStatus = :status WHERE localId = :localId")
    suspend fun updateStatus(localId: String, status: SyncStatus)

    // ***************************************************************** //
    // DELETE                                                            //
    // ***************************************************************** //

    @Query("DELETE FROM posts WHERE localId = :localId")
    suspend fun deletePost(localId: String)
}