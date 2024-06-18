plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.project.mfp"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.project.mfp"
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
    // AndroidX Test libraries
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // JUnit
    testImplementation("junit:junit:4.13.2")

    testImplementation(kotlin("test"))  // Kotlin 테스트 라이브러리
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")  // JUnit Jupiter API
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")  // JUnit Jupiter 엔진
}

tasks.withType<Test> {
    useJUnitPlatform()  // JUnit Platform을 사용하여 테스트를 실행합니다.
}