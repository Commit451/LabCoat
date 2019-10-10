buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://maven.fabric.io/public")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.1")
        classpath(kotlin("gradle-plugin", version = "1.3.50"))
        classpath("io.fabric.tools:gradle:1.31.1")
        classpath("com.google.gms:google-services:4.3.2")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.25.0"
}

allprojects {
    repositories {
        google()
        maven(url = "https://jitpack.io")
        mavenCentral()
        jcenter()
        maven(url = "https://maven.fabric.io/public")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
