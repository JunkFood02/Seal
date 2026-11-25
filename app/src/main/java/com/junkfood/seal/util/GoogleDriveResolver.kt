package com.junkfood.seal.util

import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * Resolves Google Drive share links to a direct download URL that yt-dlp can fetch with its
 * generic extractor.
 *
 * Port of the proven web backend flow (`vidtoaui/backend/src/services/googleDrive.js`):
 *  1. GET `uc?export=download&id=...[&resourcekey=...]`
 *  2. If the response is HTML: detect login-wall / quota pages, otherwise follow the modern
 *     virus-scan `<form action=".../download">` (hidden inputs: confirm, uuid, ...) or fall
 *     back to the legacy `confirm=` token, carrying an in-memory cookie jar.
 *  3. Return the final URL as soon as a non-HTML response starts; the body is deliberately
 *     discarded (yt-dlp performs the actual download).
 *
 * Permissions are never bypassed: private files fail with [DriveError.NotAccessible].
 */
object GoogleDriveResolver {

    private const val DEFAULT_UC_BASE = "https://drive.google.com/uc?export=download"
    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Mobile Safari/537.36"
    private const val MAX_FOLLOW_HOPS = 3

    /** Overridable for tests, mirroring the backend's `DRIVE_UC_BASE` env variable. */
    @Volatile var ucBase: String = DEFAULT_UC_BASE

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    internal val loginRegex =
        Regex("ServiceLogin|accounts\\.google\\.com|You need access|Request access")

    internal val quotaRegex =
        Regex(
            "Quota exceeded|Too many users|downloadQuotaExceeded|rate.?limit",
            RegexOption.IGNORE_CASE,
        )

    private val downloadFormRegex =
        Regex("<form[^>]*action=\"([^\"]*)\"[^>]*>([\\s\\S]*?)</form>", RegexOption.IGNORE_CASE)
    private val inputRegex = Regex("<input[^>]*>", RegexOption.IGNORE_CASE)
    private val inputNameRegex = Regex("name=\"([^\"]*)\"", RegexOption.IGNORE_CASE)
    private val inputValueRegex = Regex("value=\"([^\"]*)\"", RegexOption.IGNORE_CASE)
    private val confirmTokenRegex1 = Regex("confirm=([0-9A-Za-z_-]+)")
    private val confirmTokenRegex2 = Regex("\"confirm\"\\s*:\\s*\"([^\"]+)\"")
    private val confirmTokenRegex3 = Regex("confirm_([0-9A-Za-z_]+)")

    /**
     * Returns a direct download URL for [url]. Non-Drive URLs pass through unchanged, so this
     * is safe to call for every download. Throws [DriveError] for Drive links that cannot be
     * downloaded (private, quota, folder, not found, network).
     */
    fun resolve(url: String): String {
        val target = GoogleDriveUrl.extractDriveTarget(url) ?: return url
        return resolveTarget(target)
    }

    fun resolveTarget(target: GoogleDriveUrl.Target): String {
        if (target.type == GoogleDriveUrl.Target.Type.FOLDER) throw DriveError.Folder

        val cookieJar = mutableListOf<String>()
        var currentUrl = buildFirstUrl(target)
        var hops = 0

        while (true) {
            val response = execute(currentUrl, cookieJar)
            try {
                val contentType = response.header("Content-Type").orEmpty()
                if (!response.isSuccessful) throw errorForStatus(response.code)
                if (!contentType.contains("text/html", ignoreCase = true)) {
                    // Real file bytes: done. The body is discarded here; yt-dlp downloads it.
                    return currentUrl
                }

                val html = response.body?.string().orEmpty()
                if (loginRegex.containsMatchIn(html)) throw DriveError.NotAccessible
                if (quotaRegex.containsMatchIn(html)) throw DriveError.QuotaExceeded

                hops += 1
                if (hops > MAX_FOLLOW_HOPS) throw DriveError.Failed()

                val next =
                    parseDownloadForm(html, target.resourceKey)
                        ?: buildLegacyConfirmUrl(target, html)
                if (next == null) {
                    throw if (hops == 1) DriveError.NotFound else DriveError.Failed()
                }
                currentUrl = next
            } finally {
                response.close()
            }
        }
    }

    /** Follows the modern virus-scan form, returning its absolute action URL. */
    internal fun parseDownloadForm(html: String, resourceKey: String? = null): String? {
        val formMatch = downloadFormRegex.find(html) ?: return null
        val action = formMatch.groupValues[1]
        if (!action.contains("download", ignoreCase = true)) return null

        val formParams = LinkedHashMap<String, String>()
        inputRegex.findAll(formMatch.groupValues[2]).forEach { input ->
            val name = inputNameRegex.find(input.value)?.groupValues?.get(1) ?: return@forEach
            val value = inputValueRegex.find(input.value)?.groupValues?.get(1) ?: ""
            formParams[name] = value
        }
        if (formParams.isEmpty()) return null

        val resolved =
            runCatching { URI("https://drive.google.com/").resolve(action) }.getOrNull()
                ?: return null

        val params = GoogleDriveUrl.parseQuery(resolved.query)
        formParams.forEach { (key, value) -> params.put(key, value) }
        if (resourceKey != null && !params.containsKeyCaseInsensitive("resourcekey")) {
            params.put("resourcekey", resourceKey)
        }

        val base =
            runCatching { URI(resolved.scheme, resolved.authority, resolved.path, null, null) }
                .getOrNull() ?: return null
        return "$base?${params.toQueryString(::encodeQuery)}"
    }

    /** Legacy large-file flow: confirm token embedded in the warning page. */
    internal fun extractConfirmToken(html: String): String? =
        confirmTokenRegex1.find(html)?.groupValues?.get(1)
            ?: confirmTokenRegex2.find(html)?.groupValues?.get(1)
            ?: confirmTokenRegex3.find(html)?.groupValues?.get(1)

    private fun buildFirstUrl(target: GoogleDriveUrl.Target): String =
        buildUcUrl(target, confirm = null)

    private fun buildLegacyConfirmUrl(target: GoogleDriveUrl.Target, html: String): String? {
        val token = extractConfirmToken(html) ?: return null
        return buildUcUrl(target, confirm = token)
    }

    private fun buildUcUrl(target: GoogleDriveUrl.Target, confirm: String?): String {
        val questionIndex = ucBase.indexOf('?')
        val base = if (questionIndex >= 0) ucBase.substring(0, questionIndex) else ucBase
        val baseQuery = if (questionIndex >= 0) ucBase.substring(questionIndex + 1) else ""
        val params = GoogleDriveUrl.parseQuery(baseQuery)
        params.put("id", target.id)
        confirm?.let { params.put("confirm", it) }
        target.resourceKey?.let {
            if (!params.containsKeyCaseInsensitive("resourcekey")) params.put("resourcekey", it)
        }
        return "$base?${params.toQueryString(::encodeQuery)}"
    }

    private fun encodeQuery(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    private fun execute(url: String, cookieJar: MutableList<String>): Response {
        val builder =
            try {
                Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "*/*")
            } catch (e: IllegalArgumentException) {
                throw DriveError.Failed(e.message ?: "invalid download URL")
            }
        if (cookieJar.isNotEmpty()) {
            builder.header("Cookie", cookieJar.joinToString("; "))
        }
        return try {
            val response = builder.build().let { client.newCall(it).execute() }
            mergeCookies(cookieJar, response.headers("Set-Cookie"))
            response
        } catch (e: IOException) {
            throw DriveError.Network(e.message ?: e.javaClass.simpleName)
        }
    }

    private fun mergeCookies(cookieJar: MutableList<String>, setCookieHeaders: List<String>) {
        for (header in setCookieHeaders) {
            val pair = header.substringBefore(';').trim()
            if (pair.isEmpty() || '=' !in pair) continue
            val name = pair.substringBefore('=')
            if (cookieJar.none { it.substringBefore('=') == name }) {
                cookieJar.add(pair)
            }
        }
    }

    private fun errorForStatus(status: Int): DriveError =
        when (status) {
            404 -> DriveError.NotFound
            403 -> DriveError.NotAccessible
            else -> DriveError.Failed("HTTP $status")
        }
}
