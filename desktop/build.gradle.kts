import java.net.URI
import java.util.zip.ZipInputStream
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin { jvmToolchain(21) }

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.animation)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.serialization.json)
}

// Fetches the standalone yt-dlp and ffmpeg binaries for the host OS so they can be bundled into
// the installer (see YtDlpDownloader). Only runs at packaging time, not on every build/run, so it
// doesn't slow down day-to-day development.
val ytDlpBinDir = layout.buildDirectory.dir("ytdlp-bin")

// Compose Desktop only bundles app resources from OS-specific subfolders of appResourcesRootDir
// ("windows", "macos", "linux", or "common") — files placed directly in the root are ignored at
// packaging time.
//
// All host-OS decisions are made here at configuration time and captured as plain strings, so the
// doLast actions don't reference the build script itself (a configuration-cache requirement).
val hostIsWindows = OperatingSystem.current().isWindows
val hostIsMac = OperatingSystem.current().isMacOsX
val osResourceDirName =
    when {
        hostIsWindows -> "windows"
        hostIsMac -> "macos"
        else -> "linux"
    }

val downloadYtDlp by
    tasks.registering {
        description = "Downloads the yt-dlp standalone binary for the host OS to bundle into the installer"

        val outputDirProvider = ytDlpBinDir
        outputs.dir(outputDirProvider)

        val isWindows = hostIsWindows
        val osDirName = osResourceDirName
        val (assetName, targetName) =
            when {
                hostIsWindows -> "yt-dlp.exe" to "yt-dlp.exe"
                hostIsMac -> "yt-dlp_macos" to "yt-dlp"
                else -> "yt-dlp_linux" to "yt-dlp"
            }

        doLast {
            val outputDir = outputDirProvider.get().asFile.resolve(osDirName).apply { mkdirs() }
            val target = outputDir.resolve(targetName)

            if (!target.exists()) {
                val url = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/$assetName"
                logger.lifecycle("Downloading yt-dlp binary from $url")
                URI(url).toURL().openStream().use { input -> target.outputStream().use { input.copyTo(it) } }
                if (!isWindows) target.setExecutable(true)
            }
        }
    }

// ffmpeg is needed by yt-dlp for merging separate video+audio streams, audio extraction (-x),
// and embedding thumbnails/metadata — i.e. most of the download options in the UI. Bundle the
// static Windows build published by the yt-dlp project so those features work out of the box.
val downloadFfmpeg by
    tasks.registering {
        description = "Downloads a static ffmpeg build (Windows only) to bundle into the installer"

        val isWindows = hostIsWindows
        onlyIf { isWindows }

        val outputDirProvider = ytDlpBinDir
        outputs.dir(outputDirProvider)

        val osDirName = osResourceDirName

        doLast {
            val outputDir = outputDirProvider.get().asFile.resolve(osDirName).apply { mkdirs() }
            val wanted = setOf("ffmpeg.exe", "ffprobe.exe")
            if (wanted.all { outputDir.resolve(it).exists() }) return@doLast

            val url =
                "https://github.com/yt-dlp/FFmpeg-Builds/releases/latest/download/" +
                    "ffmpeg-master-latest-win64-gpl.zip"
            logger.lifecycle("Downloading ffmpeg from $url")
            ZipInputStream(URI(url).toURL().openStream().buffered()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name.substringAfterLast('/')
                    if (!entry.isDirectory && name in wanted) {
                        outputDir.resolve(name).outputStream().use { zip.copyTo(it) }
                    }
                    entry = zip.nextEntry
                }
            }
        }
    }

// Seal's own uninstaller (see WindowsIntegration): shipped next to the bundled binaries so the
// app can register it as the "Uninstall Seal" Start-menu entry on first launch. The script also
// cleans up everything the installer doesn't know about (PATH entry, registry, app data).
val bundleUninstallScript by
    tasks.registering {
        description = "Copies the Windows uninstall script into the app resources dir"

        val isWindows = hostIsWindows
        onlyIf { isWindows }

        val scriptFile = layout.projectDirectory.file("packaging/windows/uninstall.ps1").asFile
        inputs.file(scriptFile)
        val outputDirProvider = ytDlpBinDir
        outputs.dir(outputDirProvider)

        val osDirName = osResourceDirName

        doLast {
            val outputDir = outputDirProvider.get().asFile.resolve(osDirName).apply { mkdirs() }
            scriptFile.copyTo(outputDir.resolve("uninstall.ps1"), overwrite = true)
        }
    }

compose.desktop {
    application {
        mainClass = "com.junkfood.seal.desktop.MainKt"

        // Cap the heap and use a modern low-pause collector so the app stays light and snappy.
        jvmArgs += listOf("-Xms64m", "-Xmx512m", "-XX:+UseZGC")

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "Seal"
            packageVersion = "1.0.0"
            description = "Seal — video/audio downloader powered by yt-dlp"
            vendor = "Seal"

            windows {
                menuGroup = "Seal"
                // Start-menu entry, desktop shortcut, and an install-location chooser in the
                // installer UI. Per-user install means no UAC prompt and snappier setup.
                menu = true
                shortcut = true
                dirChooser = true
                perUserInstall = true
                // Stable upgrade GUID so newer installers cleanly replace older versions.
                upgradeUuid = "9c3a8f5e-1d4b-4c6a-9b7e-2f0d8a51c3b7"
            }

            appResourcesRootDir.set(ytDlpBinDir)
        }
    }
}

// prepareAppResources is the Compose plugin's Sync task that copies appResourcesRootDir into the
// packaged app image; it must run after the binaries have been downloaded into that dir.
tasks.matching { it.name == "prepareAppResources" || it.name.startsWith("package") }.configureEach {
    dependsOn(downloadYtDlp, downloadFfmpeg, bundleUninstallScript)
}
