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
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.ui.graphics.android)
    // AndroidX Test libraries
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // JUnit
    testImplementation("junit:junit:4.13.2")

    testImplementation(kotlin("test"))  // Kotlin 테스트 라이브러리
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")  // JUnit Jupiter API
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")  // JUnit Jupiter 엔진

    implementation ("androidx.compose.ui:ui:1.4.3")
    implementation ("androidx.compose.material3:material3:1.1.0")

    implementation ("androidx.startup:startup-runtime:1.1.1")
    implementation ("androidx.profileinstaller:profileinstaller:1.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()  // JUnit Platform을 사용하여 테스트를 실행합니다.
}