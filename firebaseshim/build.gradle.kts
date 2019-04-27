plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdkVersion(28)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(28)
    }
}

dependencies {
    api("com.jakewharton.timber:timber:4.7.1")
    compileOnly("com.crashlytics.sdk.android:crashlytics:2.9.9")
}
