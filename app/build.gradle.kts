import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
}
apply(plugin = "dagger.hilt.android.plugin")

val versionMajor = 1
val versionMinor = 0
val versionPatch = 5
val versionBuild = 0
val isStable = true

val composeVersion = "1.2.1"
val lifecycleVersion = "2.6.0-alpha01"
val navigationVersion = "2.5.0"
val roomVersion = "2.4.2"
val accompanistVersion = "0.25.1"
val kotlinVersion = "1.6.21"
val hiltVersion = "2.43.2"
val composeMd3Version = "1.0.0-alpha16"
val coilVersion = "2.2.0"
val youtubedlAndroidVersion = "0.13.3"

val keystorePropertiesFile = rootProject.file("keystore.properties")

val splitApks = !project.hasProperty("noSplits")

android {
    if (keystorePropertiesFile.exists()) {
        val keystoreProperties = Properties()
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
        signingConfigs {
            getByName("debug")
            {
                keyAlias = keystoreProperties["keyAlias"].toString()
                keyPassword = keystoreProperties["keyPassword"].toString()
                storeFile = file(keystoreProperties["storeFile"]!!)
                storePassword = keystoreProperties["storePassword"].toString()
            }
        }
    }

    compileSdk = 33
    defaultConfig {
        applicationId = "com.junkfood.seal"
        minSdk = 23
        targetSdk = 33
        versionCode = 10050
        versionName = if (isStable) {
            "${versionMajor}.${versionMinor}.${versionPatch}"
        } else {
            "${versionMajor}.${versionMinor}.${versionPatch}-beta.${versionBuild}"
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
        if (!splitApks)
            ndk {
                (properties["ABI_FILTERS"] as String).split(';').forEach {
                    abiFilters.add(it)
                }
            }
    }
    if (splitApks)
        splits {
            abi {
                isEnable = !project.hasProperty("noSplits")
                reset()
                include("arm64-v8a", "x86_64", "armeabi-v7a")
                isUniversalApk = false
            }
        }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists())
                signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            if (keystorePropertiesFile.exists())
                signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }

    lint {
        disable.addAll(listOf("MissingTranslation", "ExtraTranslation"))
    }

    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Seal-${defaultConfig.versionName}-${name}.apk"
        }
    }

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.0"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs.useLegacyPackaging = true
    }
    namespace = "com.junkfood.seal"
}

dependencies {


    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.6.0-beta01")
    implementation("com.google.android.material:material:1.7.0-beta01")
    implementation("androidx.activity:activity-compose:1.6.0-beta01")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material3:material3:$composeMd3Version")
    implementation("androidx.compose.material3:material3-window-size-class:$composeMd3Version")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.navigation:navigation-compose:$navigationVersion")
    implementation("androidx.compose.animation:animation-graphics:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("com.google.accompanist:accompanist-navigation-animation:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")

    implementation("io.coil-kt:coil-compose:$coilVersion")
    implementation("io.coil-kt:coil-video:$coilVersion")

    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    implementation("com.github.yausername.youtubedl-android:library:$youtubedlAndroidVersion")
    implementation("com.github.yausername.youtubedl-android:ffmpeg:$youtubedlAndroidVersion")
//    implementation ("com.github.xibr.youtubedl-android:library:set-ffmpeg-location-SNAPSHOT")
//    implementation ("com.github.xibr.youtubedl-android:ffmpeg:set-ffmpeg-location-SNAPSHOT")

//    implementation("com.github.JunkFood02.youtubedl-android:ffmpeg:-SNAPSHOT")
//    implementation("com.github.JunkFood02.youtubedl-android:library:-SNAPSHOT")

    implementation("com.tencent:mmkv:1.2.14")

//    implementation("androidx.palette:palette-ktx:1.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
}
