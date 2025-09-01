// file: com/example/fernfreunde/data/remote/FirebaseFriendshipRemoteDataSource.kt
package com.example.fernfreunde.data.remote.dataSources

import com.example.fernfreunde.data.other.Constants.FRIENDSHIP_COLLECTION
import com.example.fernfreunde.data.remote.dtos.FriendshipDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreFriendshipDataSource(
    private val firestore: FirebaseFirestore
) : IFirestoreFriendshipDataSource {

    private val friendships = firestore.collection(FRIENDSHIP_COLLECTION)

    private fun canonicalId(a: String, b: String): Pair<String, String> {
        return if (a <= b) Pair(a, b) else Pair(b, a)
    }

    override suspend fun getFriendshipsForUser(userId: String): List<FriendshipDto> {
        val queryUserA = friendships.whereEqualTo("userIdA", userId).get()
        val queryUserB = friendships.whereEqualTo("userIdB", userId).get()
        val snapshotA = queryUserA.await()
        val snapshotB = queryUserB.await()
        val listA = snapshotA.documents.mapNotNull { it.toObject(FriendshipDto::class.java) }
        val listB = snapshotB.documents.mapNotNull { it.toObject(FriendshipDto::class.java) }
        // vereinigt die beiden Listen und entfernt Duplikate
        val combined = (listA + listB).distinctBy { listOf(it.userIdA, it.userIdB).sorted().joinToString("_") }
        return combined
    }

    override suspend fun getFriendIds(userId: String): List<String> {
        val snapshotA = friendships.whereEqualTo("userIdA", userId).get().await()
        val snapshotB = friendships.whereEqualTo("userIdB", userId).get().await()
        val ids = mutableSetOf<String>()
        snapshotA.documents.mapNotNullTo(ids) { it.getString("userIdB") }
        snapshotB.documents.mapNotNullTo(ids) { it.getString("userIdA") }
        return ids.toList()
    }

    override suspend fun sendFriendRequest(fromUserId: String, toUserId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun acceptFriendRequest(aUserId: String, bUserId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeFriend(aUserId: String, bUserId: String) {
        val (a, b) = canonicalId(aUserId, bUserId)
        val docId = "${a}_$b"
        friendships.document(docId).delete().await()
    }

    override suspend fun blockUser(aUserId: String, bUserId: String) {
        TODO("Not yet implemented")
    }

    override fun listenFriendships(userId: String) = callbackFlow<List<FriendshipDto>> {
        // Helper: query both sides in a suspend function
        suspend fun queryBoth(): List<FriendshipDto> {
            val snapA = friendships.whereEqualTo("userIdA", userId).get().await()
            val snapB = friendships.whereEqualTo("userIdB", userId).get().await()
            val listA = snapA.documents.mapNotNull { it.toObject(FriendshipDto::class.java) }
            val listB = snapB.documents.mapNotNull { it.toObject(FriendshipDto::class.java) }
            return (listA + listB).distinctBy { listOf(it.userIdA, it.userIdB).sorted().joinToString("_") }
        }

        // register listeners for both queries (one for each side)
        val regA = friendships.whereEqualTo("userIdA", userId)
            .addSnapshotListener { _, _ ->
                // launch a coroutine in callbackFlow's scope
                launch {
                    try {
                        val combined = queryBoth()
                        trySend(combined).isSuccess
                    } catch (e: Exception) {
                        close(e)
                    }
                }
            }

        val regB = friendships.whereEqualTo("userIdB", userId)
            .addSnapshotListener { _, _ ->
                launch {
                    try {
                        val combined = queryBoth()
                        trySend(combined).isSuccess
                    } catch (e: Exception) {
                        close(e)
                    }
                }
            }

        // initial emission (best-effort)
        launch {
            try {
                val initial = queryBoth()
                trySend(initial).isSuccess
            } catch (e: Exception) {
                // ignore or close(e)
            }
        }

        awaitClose {
            regA.remove()
            regB.remove()
        }
    }
}
