package com.example.fernfreunde.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fernfreunde.data.local.entities.Friendship
import com.example.fernfreunde.data.local.entities.User
import com.example.fernfreunde.data.repositories.FriendshipRepository
import com.example.fernfreunde.data.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FriendsListViewModel @Inject constructor(
    private val friendshipRepository: FriendshipRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
): ViewModel() {
    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _pending = MutableStateFlow<List<User>>(emptyList())
    val pending: StateFlow<List<User>> = _pending.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var observeJob: Job? = null

    // ***************************************************************** //
    // SHOWING FRIENDS OF USER                                           //
    // ***************************************************************** //

    init {
        startObservingFriendsForUser()
    }

    private fun startObservingFriendsForUser() {
        observeJob?.cancel()

        observeJob = viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _friends.value = emptyList()
                _pending.value = emptyList()
                _allUsers.value = emptyList()
                return@launch
            }

            launch {
                friendshipRepository.observeFriendsForUser(userId)
                    .collect { users ->
                        _friends.value = users
                    }
            }

            launch {
                _pending.value = friendshipRepository.getIncomingFriendshipRequest(userId)
            }

            launch {
                loadAllUsers()
            }

            launch {
                try {
                    _isRefreshing.value = true
                    friendshipRepository.syncFriendshipsFromRemote(userId)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isRefreshing.value = false
                }
            }
        }
    }

    fun refresh() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                friendshipRepository.syncFriendshipsFromRemote(userId)
                loadAllUsers()
            } catch (e:Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // ***************************************************************** //
    // SENDING/ACCEPTING FRIENDSHIP-REQUESTS                             //
    // ***************************************************************** //

    fun sendFriendshipRequest(otherUserId: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                friendshipRepository.sendFriendshipRequest(userId, otherUserId)
                friendshipRepository.syncFriendshipsFromRemote(userId)
                loadAllUsers()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun acceptFriendshipRequest(userIdA: String, userIdB: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                friendshipRepository.acceptFriendshipRequest(userIdA, userIdB)
                friendshipRepository.syncFriendshipsFromRemote(userId)
                loadAllUsers()
            } catch (e: Exception) {
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // ***************************************************************** //
    // LOADING ALL USERS                                                 //
    // ***************************************************************** //

    private suspend fun loadAllUsers() {
        val userId = auth.currentUser?.uid ?: return

        try {
            val users = withContext(Dispatchers.IO) {
                userRepository.getAllUsers()
            }

            val friendsIds = friends.value.map { it.userId }.toSet()
            _allUsers.value = users.filter { it.userId != userId && !friendsIds.contains(it.userId) }
        } catch (e:Exception) {
            _allUsers.value = emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }
}