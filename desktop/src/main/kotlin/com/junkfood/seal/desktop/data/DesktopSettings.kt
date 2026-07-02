package com.junkfood.seal.desktop.data

import com.junkfood.seal.desktop.download.DownloadPreferences
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class DesktopSettings(
    val downloadDirectory: String = defaultDownloadDirectory(),
    /** Default download options; each download dialog starts from these. */
    val downloadPreferences: DownloadPreferences = DownloadPreferences(),
    /** Set after the bundled binaries dir has been added to the user PATH (Windows only). */
    val addedToPath: Boolean = false,
    /**
     * Set after the one-time Windows shell integration (seal:// protocol, URL associations for
     * shared links, uninstaller registration) has been applied. See WindowsIntegration.
     */
    val shellIntegrated: Boolean = false,
) {
    companion object {
        fun defaultDownloadDirectory(): String =
            File(System.getProperty("user.home"), "Downloads/Seal").path
    }
}

/** Minimal JSON-file-backed settings store. No DI/Room yet — see the Windows port plan. */
object SettingsStore {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val file = File(System.getProperty("user.home"), ".seal/settings.json")

    fun load(): DesktopSettings {
        if (!file.exists()) return DesktopSettings()
        return runCatching { json.decodeFromString<DesktopSettings>(file.readText()) }
            .getOrDefault(DesktopSettings())
    }

    fun save(settings: DesktopSettings) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(settings))
    }
}
