plugins {
    id("com.android.application") version "7.4.2" // 이 부분을 수정합니다.
    id("org.jetbrains.kotlin.android") version "1.8.0" // Kotlin 플러그인 버전을 추가합니다.
}

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
    }
}
//
//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}