package com.example.fernfreunde.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.Uri
import com.example.fernfreunde.data.repositories.UserRepository
import com.example.fernfreunde.data.local.entities.User
import com.google.firebase.auth.FirebaseAuth

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    // ********** Flow für User-Daten **********
    val userFlow: Flow<User?> = flow {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            emit(userRepository.getUser(uid))   // initiales Laden
            emitAll(userRepository.observeUser(uid)) // realtime updates
        } else {
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    // ********** Profilbild hochladen **********
    fun uploadProfilePicture(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                userRepository.uploadProfilePicture(uid, uri)
            } catch (e: Exception) {
                e.printStackTrace()
                // Optional: Snackbar/Toast für Fehler
            }
        }
    }

    // ********** Logout **********
    fun logout() {
        auth.signOut()
    }
}