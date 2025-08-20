package com.example.fernfreunde.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fernfreunde.data.local.entities.Post
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: Post)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<Post>)

    @Query("SELECT * FROM posts WHERE localId = :localId LIMIT 1")
    fun observePost(localId: String): Flow<Post?>

    @Query("SELECT * FROM posts WHERE localId = :localId LIMIT 1")
    suspend fun getPostSync(localId: String): Post?

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAtClient DESC")
    fun observePostsForAuthor(userId: String): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE groupId = :groupId ORDER BY createdAtClient DESC")
    fun observePostsForGroup(groupId: String): Flow<List<Post>>

    // PagingSource for large feeds
    @Query("SELECT * FROM posts WHERE groupId = :groupId ORDER BY createdAtServer DESC")
    fun postsPagingSourceForGroup(groupId: String): PagingSource<Int, Post>

    @Query("UPDATE posts SET status = :status WHERE localId = :localId")
    suspend fun updateStatus(localId: String, status: com.example.fernfreunde.data.local.SyncStatus)

    @Query("DELETE FROM posts WHERE localId = :localId")
    suspend fun deletePost(localId: String)
}