package com.example.fernfreunde.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fernfreunde.data.repositories.DailyChallengeRepository
import com.example.fernfreunde.data.repositories.FriendshipRepository
import com.example.fernfreunde.data.repositories.PostRepository
import com.example.fernfreunde.data.repositories.UserRepository
import com.example.fernfreunde.ui.components.feed.PostDisplay
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val friendshipRepository: FriendshipRepository,
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val auth: FirebaseAuth
): ViewModel() {

    private val _feed = MutableStateFlow<List<PostDisplay>>(emptyList())
    val feed = _feed.asStateFlow()

    private var observeJob: Job? = null

    fun startObservingFeedForCurrentUser() {
        observeJob?.cancel()

        observeJob = viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                _feed.value = emptyList()
                return@launch
            }

            val friendIds = try {
                friendshipRepository.getFriendIdsForUser(uid)
            } catch (e: Exception) {
                _feed.value = emptyList()
                return@launch
            }

            if (friendIds.isEmpty()) {
                _feed.value = emptyList()
                return@launch
            }

            val currentChallengeId = try {
                dailyChallengeRepository.getCurrentChallengeId()
            } catch (e: Exception) {
                _feed.value = emptyList()
                return@launch
            }
            val postsFlow = postRepository.observeFeedForUser(currentChallengeId!!, friendIds)

            postsFlow.collect { posts ->
                val userIds = posts.map { it.userId }.distinct()
                val usersById = withContext(Dispatchers.IO) {
                    try {
                        userRepository.getUsersByIds(userIds).associateBy { it.userId }
                    } catch (e: Exception) {
                        emptyMap()
                    }
                }

                val mapped = posts.map { p ->
                    val user = usersById[p.userId]
                    val created = p.createdAtServer ?: p.createdAtClient
                    PostDisplay(
                        postId = p.localId,
                        userId = p.userId,
                        userName = user?.displayName,
                        userProfileUrl = user?.profileImageUrl,
                        description = p.description,
                        mediaUrl = p.mediaRemoteUrl ?: p.mediaLocalPath,
                        createdAt = created
                    )
                }

                _feed.value = mapped
            }
        }
    }

    fun refresh() {
        observeJob?.cancel()
        startObservingFeedForCurrentUser()
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }
}