package com.example.fernfreunde.data.repositories

import android.net.Uri
import androidx.room.withTransaction
import com.example.fernfreunde.data.local.entities.User
import com.example.fernfreunde.data.mappers.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: com.example.fernfreunde.data.local.daos.UserDao,
    private val appDatabase: com.example.fernfreunde.data.local.database.AppDatabase,
    private val remote: com.example.fernfreunde.data.remote.dataSources.FirestoreUserDataSource? = null
) {

    // ***************************************************************** //
    // READ/OBSERVE USERS                                                //
    // ***************************************************************** //

    // liefert einen Flow für einen User (wird automatisch geupdatet, falls sich was in Room ändert)
    // ---> für Viewmodel (z.B. Profile Screen, um Details für User anzuzeigen)
    fun observeUser(userId: String): Flow<User?> {
        return userDao.observeUserById(userId)
    }

    // User einmalig aus Room holen, falls nicht vorhanden von Firebase holen und in Room speichern
    // ---> für ViewModel, z.B. um Profildaten abzufragen
    suspend fun getUser(userId: String): User? = withContext(Dispatchers.IO) {
        val local = userDao.getUserById(userId)
        if (local != null) return@withContext local

        val remoteDto = remote?.getUser(userId) ?: return@withContext null
        val entity = remoteDto.toEntity()
        userDao.upsert(entity)
        entity
    }

    // mehrere User aus Room holen, falls einige fehlen und remote vorhanden sind -> aus Firebase holen
    // ---> für FrienshipRepository.observeFriends, um Freundesliste zu erstellen
    suspend fun getUsersByIds(userIds: List<String>): List<User> = withContext(Dispatchers.IO) {
        if (userIds.isEmpty()) return@withContext emptyList()
        val local = userDao.getUsersByIds(userIds)
        val missing = userIds.filterNot { id -> local.any { it.userId == id } }
        if (missing.isEmpty() || remote == null) return@withContext local

        val remoteList = remote.getUsers(missing)
        val entities = remoteList.map { it.toEntity() }

        appDatabase.withTransaction {
            userDao.upsertAll(entities)
        }

        userDao.getUsersByIds(userIds)
    }

    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        if (remote == null) return@withContext emptyList()
        val remoteUsers = remote.getAllUsers()
        return@withContext remoteUsers.map { it.toEntity() }
    }

    // ***************************************************************** //
    // CREATE/UPDATE USERS                                               //
    // ***************************************************************** //

    // muss noch implementiert werden

    // ***************************************************************** //
    // HELPER: SYNCHRONICE ROOM <-> FIREBASE                             //
    // -> holt alle User von Firebase und speichert sie lokal in Room    //
    // ***************************************************************** //

    suspend fun syncUsersFromRemote(userIds: List<String>) = withContext(Dispatchers.IO) {
        if (userIds.isEmpty() || remote == null) return@withContext
        val dtos = remote.getUsers(userIds)
        val entities = dtos.map { it.toEntity() }
        appDatabase.withTransaction {
            userDao.upsertAll(entities)
        }
    }

    // ***************************************************************** //
    // DELETE USERS                                                      //
    // ***************************************************************** //

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        userDao.deleteUser(userId)
        // optional User auch in Firebase löschen:
        // remote?.deleteUser(userId)
    }
    suspend fun uploadProfilePicture(userId: String, uri: Uri): String {
        val downloadUrl = remote?.uploadProfileImage(userId, uri) ?: ""
        return downloadUrl}
}
