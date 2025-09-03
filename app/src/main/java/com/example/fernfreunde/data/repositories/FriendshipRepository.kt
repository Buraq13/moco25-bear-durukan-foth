package com.example.fernfreunde.data.repositories

import androidx.room.withTransaction
import com.example.fernfreunde.data.local.entities.FriendshipStatus
import com.example.fernfreunde.data.mappers.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendshipRepository @Inject constructor(
    private val friendshipDao: com.example.fernfreunde.data.local.daos.FriendshipDao,
    private val userDao: com.example.fernfreunde.data.local.daos.UserDao,
    private val appDatabase: com.example.fernfreunde.data.local.database.AppDatabase,
    private val remoteFriendships: com.example.fernfreunde.data.remote.dataSources.FirestoreFriendshipDataSource? = null,
    private val remoteUsers: com.example.fernfreunde.data.remote.dataSources.FirestoreUserDataSource? = null

) {

    // ***************************************************************** //
    // HELPER: CANONICAL PAIR                                            //
    // -> stellt sicher, dass (a,b) immer in derselben Reihenfolge       //
    // gespeichert wird (um zu verhindern, dass eine doppelte Friendship //
    // mit FreundA, FreundB und FreundB, FreundA gespeichert wird)       //
    // ***************************************************************** //

    private fun canonicalPair(a: String, b: String): Pair<String, String> {
        return if (a <= b) Pair(a, b) else Pair(b, a)
    }

    // ***************************************************************** //
    // OBSERVE FRIENDS                                                   //
    // -> für ViewModel (z.B. FriendsSceen) -> Darstellung in UI         //
    // ***************************************************************** //

    // liefert eine vollständige Liste aller Freunde als User-Objekte
    fun observeFriendUsers(userId: String): Flow<List<com.example.fernfreunde.data.local.entities.User>> {
        return friendshipDao.observeFriendsForUserWithStatus(userId, FriendshipStatus.ACCEPTED)
            .map { friendships ->
                // friendIds bestimmen (d.h. immer die Id des jewseiligen anderen User nehmen)
                friendships.map { f -> if (f.userIdA == userId) f.userIdB else f.userIdA }
            }
            .flatMapLatest { ids ->
                if (ids.isEmpty()) flowOf(emptyList())
                else flow {
                    // User lokal aus Room holen (schneller), falls welche fehlen im Hintergrund mit Firebase synchronisieren
                    val users = userDao.getUsersByIds(ids)
                    if (users.size < ids.size && remoteUsers != null) {
                        // Fehlende Ids im Hintergrund fetchen
                        val missing = ids.filterNot { id -> users.any { it.userId == id } }
                        val remoteList = remoteUsers.getUsers(missing)
                        val entities = remoteList.map { it.toEntity() }
                        appDatabase.withTransaction {
                            userDao.upsertAll(entities)
                        }
                        // gepudatete Liste aus Room holen
                        emit(userDao.getUsersByIds(ids))
                    } else {
                        emit(users)
                    }
                }.flowOn(Dispatchers.IO)
            }
    }

    // liefert alle Ids der Freunde
    suspend fun getFriendIdsForUser(userId: String): List<String> = friendshipDao.getFriendIdsForUser(userId, FriendshipStatus.ACCEPTED)

    // ***************************************************************** //
    // HELPER: SYNCHRONICE ROOM <-> FIREBASE                             //
    // -> holt alle Friendships von Firebase und speichert sie lokal in  //
    // Room                                                              //
    // ***************************************************************** //

    suspend fun syncFriendshipsFromRemote(myUserId: String) = withContext(Dispatchers.IO) {
        if (remoteFriendships == null) return@withContext
        val remoteList = remoteFriendships.getFriendshipsForUser(myUserId)
        val entities = remoteList.map { it.toEntity() }
        appDatabase.withTransaction {
            friendshipDao.insertAll(entities)
        }
    }
}
