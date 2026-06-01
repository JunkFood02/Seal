package com.junkfood.seal.ui.page.settings.network

object CookieParser {
    private const val HTTP_ONLY_PREFIX = "#HttpOnly_"
    private const val FIELD_COUNT = 7

    fun parseNetscapeCookies(content: String): Result<List<Cookie>> = runCatching {
        val cookies = linkedMapOf<CookieKey, Cookie>()
        content.lineSequence().forEachIndexed { index, rawLine ->
            val line = rawLine.trimEnd('\r')
            if (line.isBlank()) return@forEachIndexed

            val cookieLine =
                when {
                    line.startsWith(HTTP_ONLY_PREFIX) -> line.removePrefix(HTTP_ONLY_PREFIX)
                    line.startsWith("#") -> return@forEachIndexed
                    else -> line
                }

            val fields = cookieLine.split('\t')
            if (fields.size != FIELD_COUNT) {
                throw IllegalArgumentException("Invalid cookie format at line ${index + 1}")
            }

            val domain = fields[0]
            val includeSubdomains = fields[1].toNetscapeBoolean(index)
            val path = fields[2]
            val secure = fields[3].toNetscapeBoolean(index)
            val expiry =
                fields[4].toLongOrNull()
                    ?: throw IllegalArgumentException("Invalid cookie expiry at line ${index + 1}")
            val name = fields[5]
            val value = fields[6]

            val cookie =
                Cookie(
                    domain = domain,
                    name = name,
                    value = value,
                    includeSubdomains = includeSubdomains,
                    path = path,
                    secure = secure,
                    expiry = expiry,
                )
            cookies[CookieKey(domain = domain, path = path, name = name)] = cookie
        }

        if (cookies.isEmpty()) throw IllegalArgumentException("No cookies found")
        cookies.values.toList()
    }

    fun groupCookiesByDomain(cookies: List<Cookie>): Map<String, List<Cookie>> =
        cookies.groupBy { it.domain.normalizedCookieDomain() }

    private fun String.toNetscapeBoolean(lineIndex: Int): Boolean =
        when (uppercase()) {
            "TRUE" -> true
            "FALSE" -> false
            else ->
                throw IllegalArgumentException("Invalid cookie boolean at line ${lineIndex + 1}")
        }

    private fun String.normalizedCookieDomain(): String {
        val normalized = trim().trimStart('.').lowercase()
        require(normalized.isNotEmpty()) { "Invalid cookie domain" }
        return normalized
    }

    private data class CookieKey(val domain: String, val path: String, val name: String)
}
