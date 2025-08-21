plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Room
    val room_version = "2.7.2"
    implementation("androidx.room:room-runtime:${room_version}")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:${room_version}")

    // Firebase
    implementation("com.google.firebase:firebase-storage:22.0.0")

    implementation(platform("com.google.firebase:firebase-bom:34.1.0")) // BOM verwaltet kompatible Firebase-Vers.
    implementation("com.google.firebase:firebase-auth-ktx")           // Firebase Auth (optional, aber praktisch)
    implementation("com.google.firebase:firebase-firestore-ktx")      // Firestore KTX (leicht zu benutzen)
    implementation("com.google.firebase:firebase-storage-ktx")        // Storage KTX (Datei-Upload)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") // .await() für Tasks
}
