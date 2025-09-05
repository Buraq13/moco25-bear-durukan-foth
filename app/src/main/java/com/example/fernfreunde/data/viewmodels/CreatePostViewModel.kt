package com.example.fernfreunde.data.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fernfreunde.data.auth.AnonymAuth
import com.example.fernfreunde.data.repositories.DailyChallengeRepository
import com.example.fernfreunde.data.repositories.PostRepository
import com.example.fernfreunde.data.repositories.UserRepository
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
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val userRepository: UserRepository,
    private val anonymAuth: AnonymAuth,
    private val auth: FirebaseAuth
): ViewModel() {

    private val _isPosting = MutableStateFlow(false)
    val isPosting = _isPosting.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError = _lastError.asStateFlow()

    private val _createdPostId = MutableStateFlow<String?>(null)
    val createdPostId = _createdPostId.asStateFlow()

    private var createJob: Job? = null

    fun onImageChosen(uri: Uri) {
        createPostWithMedia(uri, "")
    }

    fun onPhotoCaptured(uri: Uri) {
        createPostWithMedia(uri, "")
    }

    fun createPostWithMedia(mediaUri: Uri?, description: String?) {

        if (mediaUri == null) {
            _lastError.value = "Kein Bild/Video ausgewählt."
            return
        }

        createJob?.cancel()

        createJob = viewModelScope.launch {
            _isPosting.value = true
            _lastError.value = null
            _createdPostId.value = null

            try {
                // ***** angemeldeten User prüfen *****
                val user = anonymAuth.ensureSignedIn()
                    ?: throw IllegalStateException("Anmeldung fehlgeschlagen")

                val uid = user.uid
                val userName = auth.currentUser?.displayName ?: "Anon"

                // ***** aktuelle ChallengeId abrufen *****
                val challengeId = withContext(Dispatchers.IO) {
                    dailyChallengeRepository.getCurrentChallengeId()
                }
                if (challengeId.isNullOrBlank()) {
                    _lastError.value = "Keine aktive Challenge verfügbar."
                    return@launch
                }

                // ***** prüfen ob User posten darf (Limit pro Challenge) *****
                val allowed = withContext(Dispatchers.IO) {
                    postRepository.canCreatePost(uid, challengeId)
                }
                if (!allowed) {
                    _lastError.value = "Limit für Posts in dieser Challenge erreicht."
                    return@launch
                }

                // ***** Post erstellen (lokal + enqueue upload) *****
                val postId = withContext(Dispatchers.IO) {
                    postRepository.createPost(
                        userId = uid,
                        userName = userName,
                        challengeId = challengeId,
                        description = description,
                        mediaUri = mediaUri
                    )
                }
                Log.i("WM_STATUS", "Post created")

                _createdPostId.value = postId
            } catch (e: Exception) {
                e.printStackTrace()
                _lastError.value = e.message ?: "Fehler beim Erstellen des Posts"
            } finally {
                _isPosting.value = false
            }
        }
    }

    fun clearError() { _lastError.value = null }

    fun clearCreatedPostEvent() { _createdPostId.value = null }

    override fun onCleared() {
        super.onCleared()
        createJob?.cancel()
    }
}
