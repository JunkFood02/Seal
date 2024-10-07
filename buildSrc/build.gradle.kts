plugins { `kotlin-dsl` }

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
}

kotlin { jvmToolchain(21) }
