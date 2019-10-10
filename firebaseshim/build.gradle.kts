import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
    }
}

dependencies {
    api(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    api("com.jakewharton.timber:timber:4.7.1")
    compileOnly("com.crashlytics.sdk.android:crashlytics:2.10.1")
}
