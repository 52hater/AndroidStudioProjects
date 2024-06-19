plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.project.mfp2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.project.mfp2"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation(libs.androidx.ui.graphics.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.material3.android)
}