@file:Suppress("UnstableApiUsage")

buildscript {

    repositories {
        mavenCentral()
        google()
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.gradlePlugin) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

