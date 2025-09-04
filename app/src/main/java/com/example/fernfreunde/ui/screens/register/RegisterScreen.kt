package com.example.fernfreunde.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.fernfreunde.R
import com.example.fernfreunde.ui.theme.FernfreundeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String, Uri?) -> Unit = { _, _, _, _ -> },
    onLoginRedirect: () -> Unit = {}
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profilbild Upload
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .clickable { launcher.launch("image/*") },
                shape = RoundedCornerShape(50.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Image(
                    painter = if (selectedImageUri != null)
                        rememberAsyncImagePainter(selectedImageUri)
                    else
                        painterResource(R.drawable.placeholder),
                    contentDescription = "Profilbild",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Eingabefelder
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Benutzername") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-Mail") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Passwort") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Register Button
            Button(
                onClick = { onRegisterClick(username, email, password, selectedImageUri) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onLoginRedirect) {
                Text("Schon ein Konto? → Login")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    FernfreundeTheme {
        RegisterScreen()
    }
}
