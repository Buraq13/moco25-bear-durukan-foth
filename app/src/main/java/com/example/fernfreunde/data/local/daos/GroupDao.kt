package com.example.fernfreunde.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.fernfreunde.data.local.entities.Group
import com.example.fernfreunde.data.local.entities.GroupMember
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Upsert
    suspend fun upsertGroup(group: Group)

    // Group-Details live anzeigen ---> für ViewModel (GroupDetailsScreen)
    @Query("SELECT * FROM groups WHERE groupId = :groupId LIMIT 1")
    fun observeGroup(groupId: String): Flow<Group?>

    // einmaliger lesender Zugriff auf Group-Details ---> für Worker, Repository, UseCase
    @Query("SELECT * FROM groups WHERE groupId = :groupId LIMIT 1")
    suspend fun getGroupSync(groupId: String): Group?

    // Liste aller Gruppen des angemeldeten Users ---> für ViewModel (GroupList oder so?)
    @Query("""SELECT g.* FROM groups g 
            INNER JOIN group_members gm ON g.groupId = gm.groupId 
            WHERE gm.userId = :userId
            ORDER BY g.createdAt DESC""")
    fun observeGroupsForUser(userId: String): Flow<List<Group>>

    // Gruppenmitglied hinzufügen ---> für Repository
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: GroupMember)

    // mehrere Mitglieder auf einmal zu Gruppe hinzufügen, z.B. beim Erstellen ---> für Repository
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMembers(members: List<GroupMember>)

    // Gruppenmitglied löschen
    @Query("DELETE FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun removeMember(groupId: String, userId: String)


    // alle Gruppenmitglieder anzeigen
    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    fun observeMembersForGroup(groupId: String): Flow<List<GroupMember>>

    // anzeigen, in welchen Gruppen ein User ist
    @Query("SELECT * FROM group_members WHERE userId = :userId")
    fun observeMembershipsForUser(userId: String): Flow<List<GroupMember>>

    // bestimmten User aus der Gruppe holen, um z.B. zu prüfen ob er Mitglied ist ---> für Repo/Use Case
    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND userId = :userId LIMIT 1")
    suspend fun getMember(groupId: String, userId: String): GroupMember?


    // ---- Transactional helper: create group + members (atomic locally) ----
    @Transaction
    suspend fun createGroupWithMembers(group: Group, members: List<GroupMember>) {
        upsertGroup(group)
        if (members.isNotEmpty()) insertGroupMembers(members)
    }

    // ---- Transactional helper: remove group and members ----
    @Transaction
    suspend fun deleteGroupAndMembers(groupId: String) {
        // delete all members
        removeAllMembersForGroup(groupId)
        // delete the group
        deleteGroup(groupId)
    }

    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun removeAllMembersForGroup(groupId: String)

    @Query("DELETE FROM groups WHERE groupId = :groupId")
    suspend fun deleteGroup(groupId: String)

}