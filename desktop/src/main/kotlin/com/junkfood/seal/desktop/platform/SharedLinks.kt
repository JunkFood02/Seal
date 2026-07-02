package com.junkfood.seal.desktop.platform

import java.net.URLDecoder

/**
 * Turns links handed to Seal by other apps into plain http(s) URLs.
 *
 * Links arrive as process arguments in one of these shapes:
 * - a plain URL (`Seal.exe https://…`) from Windows "Open with", the share/default-apps flow, or
 *   the command line
 * - a `seal://` / `seal:` protocol link (registered by [WindowsIntegration]), where the target
 *   URL may be plain (`seal://youtu.be/x`), carry its own scheme (`seal://https://youtu.be/x`),
 *   or be percent-encoded (`seal://https%3A%2F%2Fyoutu.be%2Fx`)
 */
object SharedLinks {

    /** First recognisable link among [args], normalised to an http(s) URL, or null. */
    fun fromArgs(args: Array<String>): String? = args.firstNotNullOfOrNull(::normalize)

    fun normalize(raw: String): String? {
        val arg = raw.trim().ifEmpty {
            return null
        }
        return when {
            arg.startsWith("http://", ignoreCase = true) ||
                arg.startsWith("https://", ignoreCase = true) -> arg

            arg.startsWith("seal:", ignoreCase = true) -> {
                val target = arg.substringAfter(':').trimStart('/')
                when {
                    target.isEmpty() -> null
                    target.startsWith("http://", ignoreCase = true) ||
                        target.startsWith("https://", ignoreCase = true) -> target
                    target.startsWith("http%3A", ignoreCase = true) ||
                        target.startsWith("https%3A", ignoreCase = true) ->
                        runCatching { URLDecoder.decode(target, Charsets.UTF_8) }.getOrNull()
                    else -> "https://$target"
                }
            }

            else -> null
        }
    }
}
