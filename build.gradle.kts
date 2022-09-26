buildscript {
    val composeVersion by extra("1.2.1")
    val lifecycleVersion by extra("2.6.0-alpha02")
    val navigationVersion by extra("2.5.2")
    val roomVersion by extra("2.4.3")
    val accompanistVersion by extra("0.25.1")
    val composeMd3Version by extra("1.0.0-beta03")
    val coilVersion by extra("2.2.1")
    val youtubedlAndroidVersion by extra("68e633ff07")
    val okhttpVersion by extra("5.0.0-alpha.10")
    val kotlinVersion by extra("1.7.10")
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
    id("com.android.application") version "7.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.7.10" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

