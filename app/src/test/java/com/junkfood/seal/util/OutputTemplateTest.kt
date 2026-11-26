package com.junkfood.seal.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the yt-dlp output templates in [DownloadUtil].
 *
 * Every field a website controls has to carry a byte limit, otherwise the rendered name can exceed
 * the 255-byte limit that ext4/F2FS impose on a single path component, and the download fails with
 * `[Errno 36] File name too long`.
 *
 * See https://github.com/JunkFood02/Seal/issues/1886
 */
class OutputTemplateTest {

    /**
     * The id yt-dlp derived for the URL in issue #1886. The generic extractor falls back to the URL
     * basename plus its query string, so an id is not necessarily short.
     */
    private val genericExtractorId =
        "The_Rita_Panahi_Show_27_August.mp3?ttag=omny_clip_id:003e5d16-7f8c-440b-9e1f-b1d9007e8c01," +
            "omny_clip_title:The Rita Panahi Show | 27 August,omny_program_name:The Rita Panahi " +
            "Show,omny_program_slug:the-rita-panahi-show,omny_network_name:Sky News," +
            "omny_organization_id:2fb3740d-3436-44af-8cc0-a91900716aa5,omny_playback_source:embed," +
            "omny_playlist_id:68b93c66-ba21-4e67-a3af-ae27017458aa,omny_playlist_title:The Rita " +
            "Panahi Show,omny_playlist_slug:podcast,omny_program_tag:Block-Alcohol," +
            "omny_program_tag:Block-News,omny_program_tag:Block-AdultContent,omny_program_tag:" +
            "Australian Voices,omny_program_tag:Curious Thinkers,omny_program_tag:Men 25-44," +
            "omny_program_tag:Men 45+,omny_program_tag:News & Daily Briefing,omny_program_tag:" +
            "Women 25-44,omny_program_tag:Women 45+,omny_program_tag:50+&t=1724753335&" +
            "starship-rollup=v0_444444444134&starship-episode-id=757b7e3b-51ba-4031-81dc-" +
            "af6bd1387a11&in_playlist=68b93c66-ba21-4e67-a3af-ae27017458aa&utm_source=Embed&" +
            "embeddedUrl=https://www.skynews.com"

    @Test
    fun longGenericExtractorIdFitsWithinNameLimit() {
        val name =
            DownloadUtil.OUTPUT_TEMPLATE_ID.render(
                "title" to "The_Rita_Panahi_Show_27_August",
                "id" to genericExtractorId,
                "ext" to "mp3",
            )
        assertTrue(
            "rendered name is ${name.utf8Size()} bytes, which overflows the $NAME_LIMIT-byte limit",
            name.utf8Size() <= NAME_LIMIT,
        )
    }

    /** Worst case: a title that saturates its own limit next to an unbounded id. */
    @Test
    fun saturatedTitleAndLongIdFitWithinNameLimit() {
        val name =
            DownloadUtil.OUTPUT_TEMPLATE_ID.render(
                "title" to "a".repeat(500),
                "id" to genericExtractorId,
                "ext" to "webm",
            )
        assertTrue(
            "rendered name is ${name.utf8Size()} bytes, which overflows the $NAME_LIMIT-byte limit",
            name.utf8Size() <= NAME_LIMIT,
        )
    }

    /**
     * The limit must not disturb the ids real extractors emit, which are far shorter. YouTube ids
     * are 11 characters; the longest in common use are still well under the cap.
     */
    @Test
    fun ordinaryIdsAreLeftIntact() {
        val name =
            DownloadUtil.OUTPUT_TEMPLATE_ID.render(
                "title" to "Never Gonna Give You Up",
                "id" to "dQw4w9WgXcQ",
                "ext" to "mp4",
            )
        assertEquals("Never Gonna Give You Up [dQw4w9WgXcQ].mp4", name)
    }

    private companion object {
        /** Maximum length of a single path component on ext4 and F2FS. */
        const val NAME_LIMIT = 255

        val FIELD = Regex("""%\((\w+)\)(?:\.(\d+)B)?s?""")

        fun String.utf8Size(): Int = toByteArray(Charsets.UTF_8).size

        /**
         * Expands a yt-dlp output template the way yt-dlp does, honouring the `.<n>B` suffix that
         * truncates a field to n bytes of UTF-8.
         */
        fun String.render(vararg fields: Pair<String, String>): String {
            val values = fields.toMap()
            return FIELD.replace(this) { match ->
                val value = values.getValue(match.groupValues[1])
                when (val limit = match.groupValues[2].toIntOrNull()) {
                    null -> value
                    else -> String(value.toByteArray(Charsets.UTF_8).take(limit).toByteArray())
                }
            }
        }
    }
}
