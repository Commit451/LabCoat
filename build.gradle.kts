buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://maven.fabric.io/public")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.3")
        classpath(kotlin("gradle-plugin", version = "1.3.61"))
        classpath("io.fabric.tools:gradle:1.31.2")
        classpath("com.google.gms:google-services:4.3.3")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.27.0"
}

allprojects {
    repositories {
        google()
        maven(url = "https://jitpack.io")
        mavenCentral()
        jcenter()
        maven(url = "https://maven.fabric.io/public")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
