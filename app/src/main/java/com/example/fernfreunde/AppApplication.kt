package com.example.fernfreunde

import android.app.Application
import com.google.firebase.FirebaseApp

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this) // Initialisiert Firebase anhand der google-services.json
                                                // und sorgt dafür, dass die Firebase SDK korrekt startet sobald die App läuft
    }
}