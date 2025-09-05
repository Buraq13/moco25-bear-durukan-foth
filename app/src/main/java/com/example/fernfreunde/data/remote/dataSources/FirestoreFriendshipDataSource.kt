// file: com/example/fernfreunde/data/remote/FirebaseFriendshipRemoteDataSource.kt
package com.example.fernfreunde.data.remote.dataSources

import com.example.fernfreunde.data.local.entities.FriendshipStatus
import com.example.fernfreunde.data.other.Constants.FRIENDSHIP_COLLECTION
import com.example.fernfreunde.data.remote.dtos.FriendshipDto
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreFriendshipDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : IFirestoreFriendshipDataSource {

    private val friendshipsCollection = firestore.collection(FRIENDSHIP_COLLECTION)

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
    // READ OPERATIONS                                                   //
    // ***************************************************************** //

    override suspend fun getFriendshipsForUser(userId: String): List<FriendshipDto> {
        val snapshotA = friendshipsCollection.whereEqualTo("userIdA", userId).get().await()
        val snapshotB = friendshipsCollection.whereEqualTo("userIdB", userId).get().await()
        val listA = snapshotA.documents.mapNotNull { it.toObject(FriendshipDto::class.java) }
        val listB = snapshotB.documents.mapNotNull { it.toObject(FriendshipDto::class.java) }
        // vereinigt die beiden Listen und entfernt Duplikate
        return (listA + listB)
            .distinctBy { listOf(it.userIdA, it.userIdB).sorted().joinToString("_") }
    }

    override suspend fun getFriendIds(userId: String): List<String> {
        val snapshotA = friendshipsCollection.whereEqualTo("userIdA", userId).get().await()
        val snapshotB = friendshipsCollection.whereEqualTo("userIdB", userId).get().await()
        val ids = mutableSetOf<String>()
        snapshotA.documents.mapNotNullTo(ids) { it.getString("userIdB") }
        snapshotB.documents.mapNotNullTo(ids) { it.getString("userIdA") }
        return ids.toList()
    }

    // ***************************************************************** //
    // FRIENDSHIP-REQUESTS & REMOVING FRIENDS                            //
    // ***************************************************************** //

    override suspend fun sendFriendRequest(fromUserId: String, toUserId: String) {
        val (a, b) = canonicalPair(fromUserId, toUserId)
        val docId = "${a}_$b"
        val payload = hashMapOf<String, Any>(
            "userIdA" to a,
            "userIdB" to b,
            "status" to FriendshipStatus.PENDING,
            "requestedBy" to fromUserId,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        friendshipsCollection.document(docId).set(payload, SetOptions.merge()).await()
    }

    override suspend fun acceptFriendRequest(aUserId: String, bUserId: String) {
        val (a, b) = canonicalPair(aUserId, bUserId)
        val docId = "${a}_$b"
        val payload = mapOf(
            "status" to FriendshipStatus.ACCEPTED
        )
        friendshipsCollection.document(docId).update(payload).await()
    }

    override suspend fun getIncomingRequests(userId: String): List<FriendshipDto> {
        val snapshotA = friendshipsCollection.whereEqualTo("userIdA", userId).get().await()
        val snapshotB = friendshipsCollection.whereEqualTo("userIdB", userId).get().await()
        val listA = snapshotA.documents.mapNotNull { it.toObject(FriendshipDto::class.java) }
        val listB = snapshotB.documents.mapNotNull { it.toObject(FriendshipDto::class.java) }
        return (listA + listB)
            .filter { it.status == FriendshipStatus.PENDING && it.requestedBy != userId }
            .distinctBy { listOf(it.userIdA, it.userIdB).sorted().joinToString { "_" } }
    }

    override suspend fun removeFriend(aUserId: String, bUserId: String) {
        val (a, b) = canonicalPair(aUserId, bUserId)
        val docId = "${a}_$b"
        friendshipsCollection.document(docId).delete().await()
    }
}
