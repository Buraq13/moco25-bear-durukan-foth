plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services") // Firebase
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.fernfreunde"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fernfreunde"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- AndroidX Basics ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // --- Jetpack Compose ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    //implementation(libs.androidx.camera.core)

    // --- CameraX (über BOM) ---
    val camerax = "1.3.4"
    implementation(platform("androidx.camera:camera-bom:$camerax"))
    implementation("androidx.camera:camera-core")
    implementation("androidx.camera:camera-camera2")
    implementation("androidx.camera:camera-lifecycle")
// ✅ NEU hinzugefügt
    implementation("androidx.camera:camera-video")
// ⚠️ HIER: bei dir war noch "camera-view:1.3.0-alpha02" → durch BOM-Version ersetzt
    implementation("androidx.camera:camera-view")
// ✅ NEU hinzugefügt
    implementation("androidx.camera:camera-extensions")

// Futures + Guava (wird von ProcessCameraProvider genutzt)
    implementation("androidx.concurrent:concurrent-futures-ktx:1.2.0")
    implementation("com.google.guava:guava:32.1.3-android")

// Lifecycle Compose (richtiges Paket für LocalLifecycleOwner!)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    // --- Navigation ---
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // --- WorkManager ---
    implementation(libs.androidx.work.runtime.ktx)
    implementation("androidx.hilt:hilt-work:1.0.0")

    // --- DataStore ---
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // --- Room ---
    val room_version = "2.7.2"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // --- Firebase (über BOM gemanaged) ---
    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // --- Credentials API ---
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")

    // --- Coroutines + Play Services ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // --- Lifecycle ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    // --- Coil (inkl. GIF) ---
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil-gif:2.4.0")

    // --- Dagger Hilt ---
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}