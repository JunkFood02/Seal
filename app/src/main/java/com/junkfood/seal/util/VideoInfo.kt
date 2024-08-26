package com.junkfood.seal.util

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

sealed interface YoutubeDLInfo

@Serializable
data class VideoInfo(
    val id: String = "",
    val title: String = "",
    val formats: List<Format>? = emptyList(),
//    val thumbnails: List<Thumbnail> = emptyList(),
    val thumbnail: String? = null,
    val description: String? = null,
    val uploader: String? = null,
    @SerialName("uploader_id") val uploaderId: String? = null,
    val subtitles: Map<String, List<SubtitleFormat>> = emptyMap(),
    @SerialName("automatic_captions") val automaticCaptions: Map<String, List<SubtitleFormat>> = emptyMap(),
//    @SerialName("uploader_id") val uploaderId: String? = null,
//    @SerialName("uploader_url") val uploaderUrl: String? = null,
//    @SerialName("channel_id") val channelId: Int? = null,
//    @SerialName("channel_url") val channelUrl: String? = null,
    val duration: Double? = null,
    @SerialName("view_count") val viewCount: Long? = null,
    @SerialName("webpage_url") val webpageUrl: String? = null,
//    @SerialName("categories") val categories: List<String> = emptyList(),
    val tags: List<String>? = emptyList(),
    @SerialName("live_status") val liveStatus: String? = null,
//    @SerialName("release_timestamp") val releaseTimestamp: Int? = null,
    @SerialName("comment_count") val commentCount: Int? = null,
    val chapters: List<Chapter>? = null,
    @SerialName("like_count") val likeCount: Int? = null,
    val channel: String? = null,
//    @SerialName("channel_follower_count") val channelFollowerCount: Int? = null,
    @SerialName("upload_date") val uploadDate: String? = null,
    val availability: String? = null,
    @SerialName("original_url") val originalUrl: String? = null,
    @SerialName("webpage_url_basename") val webpageUrlBasename: String? = null,
    @SerialName("webpage_url_domain") val webpageUrlDomain: String? = null,
    val extractor: String? = null,
    @SerialName("extractor_key") val extractorKey: String = "",
    val playlist: String? = null,
    @SerialName("playlist_index") val playlistIndex: Int? = null,
    @SerialName("display_id") val displayId: String? = null,
    val fulltitle: String? = null,
    @SerialName("duration_string") val durationString: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val format: String? = null,
    @SerialName("format_id") val formatId: String? = null,
    val ext: String = "",
    val protocol: String? = null,
    @SerialName("format_note") val formatNote: String? = null,
    @SerialName("filesize_approx") val fileSizeApprox: Double? = null,
    @SerialName("filesize") val fileSize: Double? = null,
    val tbr: Double? = null,
    val width: Double? = null,
    val height: Double? = null,
    val resolution: String? = null,
    val fps: Double? = null,
    @SerialName("dynamic_range") val dynamicRange: String? = null,
    val vcodec: String? = null,
    val vbr: Double? = null,
    val acodec: String? = null,
    val abr: Double? = null,
    val asr: Int? = null,
    val epoch: Int? = null,
    @SerialName("requested_downloads") val requestedDownloads: List<RequestedDownload>? = null,
    @SerialName("requested_formats") val requestedFormats: List<Format>? = null,
    val filename: String? = null,
    @SerialName("_type") val type: String? = null,
) : YoutubeDLInfo

@Serializable
data class Format(
    @SerialName("format_id") val formatId: String? = null,
    @SerialName("format_note") val formatNote: String? = null,
    val ext: String? = null,
    @SerialName("acodec") val acodec: String? = null,
    @SerialName("vcodec") val vcodec: String? = null,
    val url: String? = null,
    val width: Double? = null,
    val height: Double? = null,
    val fps: Double? = null,
    @SerialName("audio_ext") val audioExt: String? = null,
    @SerialName("video_ext") val videoExt: String? = null,
    val format: String? = null,
    val resolution: String? = null,
    val vbr: Double? = null,
    val abr: Double? = null,
    val tbr: Double? = null,
    @SerialName("filesize") val fileSize: Double? = null,
    @SerialName("filesize_approx") val fileSizeApprox: Double? = null,
)

data class VideoClip(
    val start: Int = 0, val end: Int = 0
) {
    constructor(range: ClosedFloatingPointRange<Float>) : this(
        range.start.roundToInt(), range.endInclusive.roundToInt()
    )
}

@Serializable
data class Chapter(
    val title: String? = null,
    @SerialName("start_time") val startTime: Double? = null,
    @SerialName("end_time") val endTime: Double? = null
)


@Serializable
data class RequestedDownload(
    @SerialName("requested_formats") val requestedFormats: List<Format>? = emptyList(),
    @SerialName("format_id") val formatId: String? = null,
    @SerialName("format_note") val formatNote: String? = null,
    val ext: String? = null,
    @SerialName("acodec") val acodec: String? = null,
    @SerialName("vcodec") val vcodec: String? = null,
    val url: String? = null,
    val width: Double? = null,
    val height: Double? = null,
    val fps: Double? = null,
    @SerialName("audio_ext") val audioExt: String? = null,
    @SerialName("video_ext") val videoExt: String? = null,
    val format: String? = null,
    val resolution: String? = null,
    val vbr: Double? = null,
    val abr: Double? = null,
    val tbr: Double? = null,
    @SerialName("filesize") val fileSize: Double? = null,
    @SerialName("filesize_approx") val fileSizeApprox: Double? = null,
    val filename: String? = null,
) {
    fun toFormat(): Format = Format(
        formatId = formatId,
        formatNote = formatNote,
        ext = ext,
        acodec = acodec,
        vcodec = vcodec,
        url = url,
        width = width,
        height = height,
        fps = fps,
        audioExt = audioExt,
        videoExt = videoExt,
        format = format,
        resolution = resolution,
        vbr = vbr,
        abr = abr,
        tbr = tbr,
        fileSize = fileSize,
        fileSizeApprox = fileSizeApprox,
    )
}

@Serializable
data class PlaylistResult(
    val uploader: String? = null,
    val availability: String? = null,
    val channel: String? = null,
    val title: String? = null,
    val description: String? = null,
    @SerialName("_type") val type: String? = null,
    val entries: List<PlaylistEntry>? = emptyList(),
    @SerialName("webpage_url") val webpageUrl: String? = null,
    @SerialName("original_url") val originalUrl: String? = null,
    @SerialName("extractor_key") val extractorKey: String? = null,
) : YoutubeDLInfo

@Serializable
data class Thumbnail(
    val url: String,
    val height: Double = .0,
    val width: Double = .0,
)

@Serializable
data class PlaylistEntry(
    @SerialName("_type") val type: String? = null,
    val ieKey: String? = null,
    val id: String? = null,
    val url: String? = null,
    val title: String? = null,
    val duration: Double? = .0,
    val uploader: String? = null,
    val channel: String? = null,
    val thumbnails: List<Thumbnail>? = emptyList(),
)


@Serializable
data class SubtitleFormat(
    val ext: String, val url: String, val name: String? = null, val protocol: String? = null
)