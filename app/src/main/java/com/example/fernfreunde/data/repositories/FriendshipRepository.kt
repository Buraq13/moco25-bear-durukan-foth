package com.example.fernfreunde.data.repositories

import androidx.room.withTransaction
import com.example.fernfreunde.data.local.daos.FriendshipDao
import com.example.fernfreunde.data.local.daos.UserDao
import com.example.fernfreunde.data.local.database.AppDatabase
import com.example.fernfreunde.data.local.entities.Friendship
import com.example.fernfreunde.data.local.entities.FriendshipStatus
import com.example.fernfreunde.data.local.entities.User
import com.example.fernfreunde.data.mappers.toEntity
import com.example.fernfreunde.data.remote.dataSources.FirestoreFriendshipDataSource
import com.example.fernfreunde.data.remote.dataSources.FirestoreUserDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendshipRepository @Inject constructor(
    private val friendshipDao: FriendshipDao,
    private val userDao: UserDao,
    private val appDatabase: AppDatabase,
    private val firestoreFriendshipDataSource: FirestoreFriendshipDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource

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

    fun observeFriendsForUser(userId: String, status: FriendshipStatus = FriendshipStatus.ACCEPTED): Flow<List<User>> {
        return friendshipDao.observeFriendshipsForUser(userId, status)
            .map { friendships ->
                friendships.map { f -> if (f.userIdA == userId) f.userIdB else f.userIdA }
            }
            .flatMapLatest { ids ->
                if (ids.isEmpty()) flowOf(emptyList())
                else flow {
                    val users = userDao.getUsersByIds(ids)
                    if (users.size < ids.size) {
                        val missing = ids.filterNot { id -> users.any { it.userId == id } }
                        val remoteList = firestoreUserDataSource.getUsers(missing)
                        val entities = remoteList.map { it.toEntity() }
                        appDatabase.withTransaction {
                            userDao.upsertAll(entities)
                        }
                        emit(userDao.getUsersByIds(ids))
                    } else {
                        emit(users)
                    }
                }.flowOn(Dispatchers.IO)
            }
    }

    suspend fun getFriendIdsForUser(userId: String): List<String> =
        friendshipDao.getFriendIdsForUser(userId, FriendshipStatus.ACCEPTED)

    // ***************************************************************** //
    // SENDING & ACCEPTING FRIENDSHIPS                                   //
    // ***************************************************************** //

    suspend fun sendFriendshipRequest(fromUserId: String, toUserId: String) = withContext(Dispatchers.IO) {
        firestoreFriendshipDataSource.sendFriendRequest(fromUserId, toUserId)
        val (a, b) = canonicalPair(fromUserId, toUserId)
        val entity = Friendship(
            userIdA = a,
            userIdB = b,
            status = FriendshipStatus.PENDING,
            requestedBy = fromUserId,
            createdAt = System.currentTimeMillis())
        friendshipDao.insert(entity)
    }

    suspend fun acceptFriendshipRequest(userIdA: String, userIdB: String) = withContext(Dispatchers.IO) {
        firestoreFriendshipDataSource.acceptFriendRequest(userIdA, userIdB)
        val (a, b) = canonicalPair(userIdA, userIdB)
        val entity = Friendship(
            userIdA = a,
            userIdB = b,
            status = FriendshipStatus.ACCEPTED,
            requestedBy = userIdB,
            createdAt = System.currentTimeMillis())
        friendshipDao.insert(entity)
    }

    suspend fun getIncomingFriendshipRequest(userId: String): List<User> {
        val remote = firestoreFriendshipDataSource.getIncomingRequests(userId)
        val otherIds = remote.map { if (it.userIdA == userId) it.userIdB else it.userIdA }.distinct()
        val cached = userDao.getUsersByIds(otherIds)
        val missing = otherIds.filterNot { id -> cached.any { it.userId == id } }
        if (missing.isNotEmpty()) {
            val remoteUsers = firestoreUserDataSource.getUsers(missing)
            val entities = remoteUsers.map { it.toEntity() }
            appDatabase.withTransaction { userDao.upsertAll(entities) }
            return userDao.getUsersByIds(otherIds)
        }
        return cached
    }

    suspend fun removeFriend(userIdA: String, userIdB: String) = withContext(Dispatchers.IO) {
        firestoreFriendshipDataSource.removeFriend(userIdA, userIdB)
        val (a, b) = canonicalPair(userIdA, userIdB)
        friendshipDao.deleteFriendship(a, b)
    }
    // ***************************************************************** //
    // HELPER: SYNCHRONICE ROOM <-> FIREBASE                             //
    // -> holt alle Friendships von Firebase und speichert sie lokal in  //
    // Room                                                              //
    // ***************************************************************** //

    suspend fun syncFriendshipsFromRemote(myUserId: String) = withContext(Dispatchers.IO) {
        val remoteList = firestoreFriendshipDataSource.getFriendshipsForUser(myUserId)
        val entities = remoteList.map { it.toEntity() }
        appDatabase.withTransaction {
            friendshipDao.insertAll(entities)
        }

        val friendIds = entities.map { if (it.userIdA == myUserId) it.userIdB else it.userIdA }.distinct()
        val cached = userDao.getUsersByIds(friendIds)
        val missing = friendIds.filterNot { id -> cached.any { it.userId == id } }
        if (missing.isNotEmpty()) {
            val remoteUsersDtos = firestoreUserDataSource.getUsers(missing)
            val userEntities = remoteUsersDtos.map { it.toEntity() }
            appDatabase.withTransaction {
                userDao.upsertAll(userEntities)
            }
        }
    }
}
