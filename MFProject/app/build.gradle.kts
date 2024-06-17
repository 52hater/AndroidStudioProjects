plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.example.screencaptureapp"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.screencaptureapp"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation(libs.androidx.constraintlayout)
    implementation("com.google.android.material:material:1.5.0")
}