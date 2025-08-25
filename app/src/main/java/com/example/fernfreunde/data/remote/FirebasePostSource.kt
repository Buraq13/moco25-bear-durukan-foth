package com.example.fernfreunde.data.remote

import com.example.fernfreunde.data.local.entities.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebasePostSource @Inject constructor(
    private val remote: FirestoreDataSource
){

    var posts = emptyList<Post>()

    // einmalig alle Posts von Firebase laden
    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        try {
            state = State.STATE_INITIALIZING
            val allPosts = remote.getAllPosts()
            posts = allPosts.sortedByDescending { it.createdAtServer ?: it.createdAtClient }
            state = State.STATE_INITIALIZED
        } catch (e: Exception) {
            state = State.STATE_ERROR
            throw e
        }
    }

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = State.STATE_CREATED
        set(value) {
            if (value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == State.STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if (state == State.STATE_CREATED || state == State.STATE_INITIALIZING) {
            onReadyListeners += action
            return false
        } else {
            action(state == State.STATE_INITIALIZED)
            return true
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}