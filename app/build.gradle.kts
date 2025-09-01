plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Material 3
    implementation("androidx.compose.material3:material3:1.3.1")

    //Icons
    implementation ("androidx.compose.material:material-icons-extended")
    implementation ("androidx.compose.material:material-icons-extended:1.3.0-alpha03")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // DataStore
    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // Room Stuff
    /*val room_version = "2.6.1"
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    */
    implementation ("androidx.room:room-runtime:2.6.0")
    annotationProcessor ("androidx.room:room-compiler:2.6.0")
    kapt ("androidx.room:room-compiler:2.6.0")
    implementation ("androidx.room:room-ktx:2.6.0")


    //Dagger-Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")//-android


    //Navigation
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    //GIF
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil-gif:2.4.0")

    //Coroutine
    implementation ("io.coil-kt:coil-compose:1.3.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation ("androidx.navigation:navigation-compose:2.7.2")

    // ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0-alpha01")
    // LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.0-alpha01")


}
kapt{
    correctErrorTypes = true
}
