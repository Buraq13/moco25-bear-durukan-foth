package com.example.fernfreunde.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.fernfreunde.data.local.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Upsert
    suspend fun upsertUser(user: User)

    // für automatische Updates wenn sich was in der DB ändert, liefert kontinuierlich aktuelle Daten (wegen Flow<>)
    // ---> für ViewModel (UI), z.B. ProfileScreen
    @Query("SELECT * FROM users WHERE userid = :userId LIMIT 1")
    fun observeUser(userId: String): Flow<User?>

    // für einmalige lesende Zugriffe (wird asynchron in Coroutine ausgeführt, da suspend fun)
    // ---> für Worker, Repository, UseCase (keine UI!)
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserSync(userId: String): User?

    // User nach username finden, z.B. bei Registration
    // ---> für Repository, UseCase
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?
}