package com.example.fernfreunde

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fernfreunde.ui.navigation.AppNavHost
import com.example.fernfreunde.ui.theme.FernfreundeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Falls diese Funktion fehlt: die nächste Zeile einfach löschen.
        enableEdgeToEdge()

        setContent {
            FernfreundeTheme {
                AppNavHost()
            }
        }
    }
}