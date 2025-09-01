package com.example.fernfreunde.data.local.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.fernfreunde.data.local.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // ***************************************************************** //
    // INSERT/UPDATE USERS                                               //
    // ***************************************************************** //

    @Upsert
    suspend fun upsert(user: User)

    @Upsert
    suspend fun upsertAll(users: List<User>)

    // ***************************************************************** //
    // GET/OBSERVE USERS                                                 //
    // ***************************************************************** //

    // für einmalige lesende Zugriffe (wird asynchron in Coroutine ausgeführt, da suspend fun)
    // ---> für Worker, Repository, UseCase (keine UI!)
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE userId IN (:ids)")
    suspend fun getUsersByIds(ids: List<String>): List<User>

    // User nach username finden, z.B. bei Registration
    // ---> für Repository, UseCase
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findUserByUsername(username: String): User?

    // für automatische Updates wenn sich was in der DB ändert, liefert kontinuierlich aktuelle Daten (wegen Flow<>)
    // ---> für ViewModel (UI), z.B. ProfileScreen
    @Query("SELECT * FROM users WHERE userid = :userId LIMIT 1")
    fun observeUserById(userId: String): Flow<User?>

    @Query("SELECT * FROM users ORDER BY username")
    fun observeAllUsers(): Flow<List<User>>

    // ***************************************************************** //
    // DELETE USERS                                                 //
    // ***************************************************************** //

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUser(userId: String)
}