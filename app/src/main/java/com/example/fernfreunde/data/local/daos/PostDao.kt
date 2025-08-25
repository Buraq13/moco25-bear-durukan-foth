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
    fun observePost(localId: String): Flow<Post?>

    @Query("SELECT * FROM posts WHERE localId = :localId LIMIT 1")
    suspend fun getPostSync(localId: String): Post?

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAtClient DESC")
    fun observePostsForUser(userId: String): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE userId = :userId AND challengeDate = :challengeDate LIMIT 1")
    suspend fun getPostForUserAndChallenge(userId: String, challengeDate: String): Post?

    @Query(
        """
    SELECT * FROM posts
    WHERE challengeDate = :challengeDate
    AND userId IN (:userIds)
    ORDER BY COALESCE(createdAtServer, createdAtClient) DESC
    """
    )
    fun observePostsForChallengeByUsers(challengeDate: String, userIds: List<String>): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE syncStatus = :status")
    suspend fun getPostsBySyncStatus(status: String): List<Post>

    // ***************************************************************** //
    // UPDATE SYNCSTATUS                                                 //
    // ***************************************************************** //

    @Query("""
        UPDATE posts
        SET remoteId = :remoteId,
            mediaRemoteUrl = :mediaUrl,
            syncStatus = :status,
            createdAtServer = :createdAtServer
        WHERE localId = :localId
    """)
    suspend fun updateAfterSync(localId: String, remoteId: String?, mediaUrl: String?, status: String, createdAtServer: Long?)

    @Query("UPDATE posts SET syncStatus = :status WHERE localId = :localId")
    suspend fun updateStatus(localId: String, status: SyncStatus)

    // ***************************************************************** //
    // DELETE                                                            //
    // ***************************************************************** //

    @Query("DELETE FROM posts WHERE localId = :localId")
    suspend fun deletePost(localId: String)
}