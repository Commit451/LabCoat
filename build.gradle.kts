buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://maven.fabric.io/public")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.0-rc01")
        classpath(kotlin("gradle-plugin", version = "1.3.72"))
        classpath("io.fabric.tools:gradle:1.31.2")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("com.apollographql.apollo:apollo-gradle-plugin:2.0.3")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.28.0"
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
