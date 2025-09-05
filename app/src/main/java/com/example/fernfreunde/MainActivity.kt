package com.example.fernfreunde

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.fernfreunde.data.auth.AnonymAuth
import com.example.fernfreunde.data.local.entities.User
import com.example.fernfreunde.data.remote.dtos.UserDto
import com.example.fernfreunde.data.repositories.UserRepository
import com.example.fernfreunde.ui.navigation.AppNavHost
import com.example.fernfreunde.ui.theme.FernfreundeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var anonymAuth: AnonymAuth

    @Inject lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Falls diese Funktion fehlt: die nächste Zeile einfach löschen.
        enableEdgeToEdge()

        lifecycleScope.launchWhenCreated {

            try {
                val firebaseUser = anonymAuth.ensureSignedIn()
                firebaseUser?.let { user ->
                    val uid = user.uid
                    // create a default DTO/entity and persist it remotely + locally
                    val defaultUsername = "anon_${uid.take(6)}"
                    val defaultDisplayName = "Anonymous"

                    // If you have a UserRepository with createOrUpdateUser(UserDto) -> use it:
                    try {
                        // userRepository should expose a createOrUpdateUser(userDto) method.
                        userRepository.upsertLocalUser(
                            User(
                                userId = uid,
                                username = defaultUsername,
                                displayName = defaultDisplayName,
                                profileImageUrl = null,
                                bio = null,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    Log.i("MainActivity", "Anon sign-in done, uid=$uid")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.w("MainActivity", "Anon sign-in failed: ${e.message}")
            }

            setContent {
                FernfreundeTheme {
                    AppNavHost()
                }
            }
        }
    }
}