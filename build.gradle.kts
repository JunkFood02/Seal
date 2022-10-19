buildscript {
    val composeVersion by extra("1.3.0-rc01")
    val lifecycleVersion by extra("2.6.0-alpha02")
    val navigationVersion by extra("2.5.2")
    val roomVersion by extra("2.4.3")
    val accompanistVersion by extra("0.25.1")
    val composeMd3Version by extra("1.0.0-rc01")
    val coilVersion by extra("2.2.2")
    val youtubedlAndroidVersion by extra("reduce-python-SNAPSHOT")
    val okhttpVersion by extra("5.0.0-alpha.10")
    val kotlinVersion by extra("1.7.20")
    val hiltVersion by extra("2.44")

    repositories {
        mavenCentral()
    }
    dependencies {

        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlinVersion"]}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${project.extra["hiltVersion"]}")
    }
}
plugins {
    id("com.android.application") version "7.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.7.20" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

