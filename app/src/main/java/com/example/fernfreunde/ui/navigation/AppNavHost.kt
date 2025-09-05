package com.example.fernfreunde.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fernfreunde.feature.media.MediaType
import com.example.fernfreunde.feature.media.rememberMediaPicker
import com.example.fernfreunde.feature.media.rememberTakePhoto
import com.example.fernfreunde.feature.permissions.Permission
import com.example.fernfreunde.feature.permissions.PermissionRequester
import com.example.fernfreunde.ui.screens.friends.FriendsListScreen
import com.example.fernfreunde.ui.screens.main.MainScreen
import com.example.fernfreunde.ui.screens.mission.MissionDetailsScreen
import com.example.fernfreunde.ui.screens.profile.ProfileScreen
import com.example.fernfreunde.ui.screens.profile.EditProfileScreen
import com.example.fernfreunde.ui.screens.settings.SettingsScreen
import com.example.fernfreunde.ui.screens.upload.UploadScreen
import com.example.fernfreunde.ui.theme.FernfreundeTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import android.net.Uri
import com.example.fernfreunde.feature.media.rememberRecordVideo

@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Routes.MAIN
    ) {
        composable(Routes.MAIN) {
            MainScreen(
                onFriendsClick = { nav.go(Routes.FRIENDS) },
                onUploadClick  = { nav.go(Routes.UPLOAD)  },
                onProfileClick = { nav.go(Routes.PROFILE) },
                onMissionClick = { nav.go(Routes.MISSION) }
            )
        }

        composable(Routes.FRIENDS) {
            FriendsListScreen(
                onFriendsClick = { nav.go(Routes.MAIN) },
                onUploadClick  = { nav.go(Routes.UPLOAD)  },
                onProfileClick = { nav.go(Routes.PROFILE) }
            )
        }

        composable(Routes.UPLOAD) {
            val inPreview = LocalInspectionMode.current

            // Unten-links Thumbnail-State (kannst du vorerst behalten – wird im v4-Screen nicht angezeigt)
            var lastMedia by rememberSaveable { mutableStateOf<String?>(null) }

            // Galerie (Photo Picker)
            val pickImage =
                if (inPreview) ({})
                else rememberMediaPicker(MediaType.Image) { uri: Uri? ->
                    lastMedia = uri?.toString()
                }

            // Foto aufnehmen (System-Intent)
            val takePhoto =
                if (inPreview) ({})
                else rememberTakePhoto { uri: Uri? ->
                    lastMedia = uri?.toString()
                }

            // Video aufnehmen (System-Intent)
            val recordVideo =
                if (inPreview) ({})
                else rememberRecordVideo { uri: Uri? ->
                    lastMedia = uri?.toString()
                }

            // Runtime-Permissions (nur außerhalb der Preview initialisieren)
            val cameraPerm = if (inPreview) null else PermissionRequester(Permission.CAMERA)
            val audioPerm  = if (inPreview) null else PermissionRequester(Permission.RECORD_AUDIO)

            UploadScreen(
                onFriendsClick = { nav.go(Routes.FRIENDS) },
                onUploadClick  = { nav.go(Routes.MAIN) },
                onProfileClick = { nav.go(Routes.PROFILE) },

                onOpenGallery  = pickImage,

                // Tap = Foto
                onShutter = {
                    if (inPreview) {
                        takePhoto()
                    } else {
                        if (cameraPerm?.granted == true) takePhoto() else cameraPerm?.request()
                    }
                },

                // Long-press gibt es im v4-UploadScreen NICHT mehr → Record UI bleibt in AppNavHost
                onSwitchCamera    = { /* CameraX wechselt aktuell direkt im Screen */ },
                onFlashModeChange = { /* optional: an VM melden */ }
            )

            // Hinweis: recordVideo() bleibt verfügbar – wenn ihr später Long-Press wieder einführt,
            // könnt ihr es dort aufrufen. Aktuell nur vorbereitet.
        }


        composable(Routes.PROFILE) {
            ProfileScreen(
                onFriendsClick = { nav.go(Routes.FRIENDS) },
                onUploadClick  = { nav.go(Routes.UPLOAD)  },
                onProfileClick = { nav.go(Routes.MAIN) },
                onEditProfileClick = { nav.navigate(Routes.EDIT_PROFILE) },
                onSettingsClick = { nav.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                onSaveClick = { username, bio, email, imageUri ->
                    // TODO: Daten speichern (z.B. Firebase / lokale DB)
                    nav.popBackStack() // zurück zum Profil
                },
                onCancelClick = {
                    nav.popBackStack()
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onFriendsClick = { nav.go(Routes.FRIENDS) },
                onUploadClick  = { nav.go(Routes.UPLOAD)  },
                onProfileClick = { nav.go(Routes.PROFILE) },
                onBackClick = { nav.popBackStack() },  // 👈 zurück zum Profil
                onSaveClick = { push, requests ->
                    // TODO: später speichern (Firebase / DB)
                    nav.popBackStack() // nach Save zurück zum Profil
                }
            )
        }

        composable(Routes.MISSION) {
            MissionDetailsScreen(
                onBack = { nav.popBackStack() }
            )
        }
    }
}

private fun NavHostController.go(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AppNavHostPreview() {
    FernfreundeTheme { AppNavHost() }
}

