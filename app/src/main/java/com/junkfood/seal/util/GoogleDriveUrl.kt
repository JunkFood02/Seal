package com.junkfood.seal.util

import android.content.Context
import com.junkfood.seal.R
import java.net.URI
import java.net.URLDecoder

/**
 * Extracts Google Drive targets from every common share-link shape.
 *
 * Port of the proven web backend parser (`vidtoaui/backend/src/utils/driveUrl.js`):
 *   https://drive.google.com/file/d/FILE_ID/view[?usp=sharing][&resourcekey=...]
 *   https://drive.google.com/file/d/FILE_ID/preview
 *   https://drive.google.com/open?id=FILE_ID
 *   https://drive.google.com/uc?id=FILE_ID  (any export=)
 *   https://drive.usercontent.google.com/download?id=FILE_ID&export=download
 *   https://drive.google.com/drive/folders/FOLDER_ID
 *   https://drive.google.com/drive/u/0/folders/FOLDER_ID
 *   bare FILE_ID
 *
 * Only the id/shape is parsed here; permissions are never bypassed downstream.
 */
object GoogleDriveUrl {

    data class Target(
        val id: String,
        val type: Type,
        val resourceKey: String? = null,
        val bare: Boolean = false,
    ) {
        enum class Type {
            FILE,
            FOLDER
        }
    }

    private val fileIdRegex = Regex("^[A-Za-z0-9_-]{10,}$")

    private val allowedHosts =
        setOf(
            "drive.google.com",
            "www.drive.google.com",
            "drive.usercontent.google.com",
            "www.drive.usercontent.google.com",
        )

    private val filePathRegex = Regex("/file/d/([^/]+)")
    private val folderPathRegex = Regex("/folders/([^/]+)")

    fun extractDriveTarget(input: String?): Target? {
        val raw = input?.trim().orEmpty()
        if (raw.isEmpty()) return null

        val uri = runCatching { URI(raw) }.getOrNull()
        val host = uri?.host?.lowercase()

        if (host == null) {
            // Bare file id pasted with no URL at all
            return if (fileIdRegex.matches(raw)) {
                Target(raw, Target.Type.FILE, null, bare = true)
            } else {
                null
            }
        }

        if (host !in allowedHosts && !host.endsWith(".drive.google.com")) return null

        val path = uri.path.orEmpty()
        val params = parseQuery(uri.query)

        filePathRegex.find(path)?.let { match ->
            val id = match.groupValues[1]
            if (fileIdRegex.matches(id)) {
                return Target(id, Target.Type.FILE, params.resourceKey)
            }
        }

        folderPathRegex.find(path)?.let { match ->
            val id = match.groupValues[1]
            if (fileIdRegex.matches(id)) {
                return Target(id, Target.Type.FOLDER, params.resourceKey)
            }
        }

        params.id?.let { id ->
            if (fileIdRegex.matches(id)) {
                return Target(id, Target.Type.FILE, params.resourceKey)
            }
        }

        return null
    }

    /** Back-compat helper: file ids only (folders return null). */
    fun extractFileId(input: String?): String? =
        extractDriveTarget(input)?.takeIf { it.type == Target.Type.FILE }?.id

    fun isDriveUrl(input: String?): Boolean = extractDriveTarget(input) != null

    /** Case-insensitive query-string parameters. */
    internal class QueryParams {
        private val map = LinkedHashMap<String, String>()

        val id: String?
            get() = map["id"]

        val resourceKey: String?
            get() = map.entries.firstOrNull { it.key.equals("resourcekey", true) }?.value
                ?.takeIf { it.isNotEmpty() }

        fun put(key: String, value: String) {
            map[key] = value
        }

        fun entries(): Set<Map.Entry<String, String>> = map.entries

        fun containsKeyCaseInsensitive(key: String): Boolean = map.keys.any { it.equals(key, true) }

        fun toQueryString(encode: (String) -> String): String =
            map.entries.joinToString("&") { "${encode(it.key)}=${encode(it.value)}" }
    }

    internal fun parseQuery(query: String?): QueryParams {
        val params = QueryParams()
        if (query.isNullOrEmpty()) return params
        for (pair in query.split('&')) {
            if (pair.isEmpty()) continue
            val idx = pair.indexOf('=')
            val rawKey = if (idx >= 0) pair.substring(0, idx) else pair
            val rawValue = if (idx >= 0) pair.substring(idx + 1) else ""
            val key = decode(rawKey)
            if (key.isEmpty()) continue
            params.put(key, decode(rawValue))
        }
        return params
    }

    private fun decode(value: String): String =
        runCatching { URLDecoder.decode(value, "UTF-8") }.getOrElse { value }
}

/**
 * Typed failures of the Google Drive resolver. The English [message] is the fallback shown
 * verbatim; [userMessage] returns the localized variant for the UI.
 */
sealed class DriveError(message: String) : Exception(message) {
    object NotAccessible :
        DriveError(
            "File isn't publicly accessible. In Drive set General access to 'Anyone with the link'."
        )

    object NotFound : DriveError("File doesn't exist or the link is wrong.")

    object QuotaExceeded :
        DriveError("Google Drive quota exceeded for this file. Try again later.")

    object Folder : DriveError("This is a folder link, not a video.")

    data class Failed(val detail: String = "") :
        DriveError("Google Drive refused the download — check the link is public and points to a video.")

    data class Network(val causeMessage: String) :
        DriveError("Couldn't reach Google Drive: $causeMessage")

    fun userMessage(context: Context): String =
        when (this) {
            NotAccessible -> context.getString(R.string.drive_not_public)
            NotFound -> context.getString(R.string.drive_not_found)
            QuotaExceeded -> context.getString(R.string.drive_quota)
            Folder -> context.getString(R.string.drive_folder)
            is Failed -> context.getString(R.string.drive_failed)
            is Network -> context.getString(R.string.drive_network, causeMessage)
        }
}
