package com.junkfood.seal.util

import android.os.Build
import android.util.Log
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.audioDownloadDir
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.BaseApplication.Companion.videoDownloadDir
import com.junkfood.seal.R
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.util.FileUtil.getConfigFile
import com.junkfood.seal.util.FileUtil.getCookiesFile
import com.junkfood.seal.util.FileUtil.getTempDir
import com.junkfood.seal.util.PreferenceUtil.ARIA2C
import com.junkfood.seal.util.PreferenceUtil.COOKIES
import com.junkfood.seal.util.PreferenceUtil.CROP_ARTWORK
import com.junkfood.seal.util.PreferenceUtil.CUSTOM_PATH
import com.junkfood.seal.util.PreferenceUtil.MAX_FILE_SIZE
import com.junkfood.seal.util.PreferenceUtil.PRIVATE_DIRECTORY
import com.junkfood.seal.util.PreferenceUtil.PRIVATE_MODE
import com.junkfood.seal.util.PreferenceUtil.RATE_LIMIT
import com.junkfood.seal.util.PreferenceUtil.SPONSORBLOCK
import com.junkfood.seal.util.PreferenceUtil.SUBDIRECTORY
import com.junkfood.seal.util.PreferenceUtil.SUBTITLE
import com.junkfood.seal.util.TextUtil.isNumberInRange
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

object DownloadUtil {

    private val jsonFormat = Json {
        ignoreUnknownKeys = true
    }

    class Result(val resultCode: ResultCode, val filePath: List<String>?) {
        companion object {
            fun failure(): Result {
                return Result(ResultCode.EXCEPTION, null)
            }

            fun success(filePaths: List<String>?): Result {
                return Result(ResultCode.SUCCESS, filePaths)
            }
        }
    }


    enum class ResultCode {
        SUCCESS, EXCEPTION
    }

    private const val TAG = "DownloadUtil"
    private const val OUTPUT_TEMPLATE = "%(title).100s [%(id)s].%(ext)s"
    private const val AUDIO_REGEX = "(mp3)|(aac)|(opus)|(m4a)"
    private const val CROP_ARTWORK_COMMAND =
        """--ppa "ffmpeg: -c:v png -vf crop=\"'if(gt(ih,iw),iw,ih)':'if(gt(iw,ih),ih,iw)'\"""""


    data class PlaylistInfo(
        val url: String = "",
        val size: Int = 0,
        val title: String = ""
    )

    @Serializable
    data class PlaylistResult(
        val uploader: String? = null,
        val availability: String? = null,
        @SerialName("playlist_count") val playlistCount: Int = 0,
        val channel: String? = null,
        val title: String? = null,
        val description: String? = null,
        @SerialName("_type") val type: String? = null,
        val entries: ArrayList<Entries> = arrayListOf(),
        @SerialName("webpage_url") val webpageUrl: String? = null,
        @SerialName("extractor_key") val extractorKey: String? = null,
    ) {}

    @Serializable
    data class Thumbnails(
        val url: String,
        val height: Int,
        val width: Int,
    )

    @Serializable
    data class Entries(
        @SerialName("_type") val type: String? = null,
        val ieKey: String? = null,
        val id: String? = null,
        val url: String? = null,
        val title: String? = null,
        val duration: Float? = 0f,
        val uploader: String? = null,
        val thumbnails: ArrayList<Thumbnails> = arrayListOf(),
    )


    fun getPlaylistInfo(playlistURL: String): PlaylistResult {
        TextUtil.makeToastSuspend(context.getString(R.string.fetching_playlist_info))
        val request = YoutubeDLRequest(playlistURL)
        with(request) {
            addOption("--flat-playlist")
            addOption("-J")
            addOption("-R", "1")
            addOption("--socket-timeout", "5")
        }
        for (s in request.buildCommand()) Log.d(TAG, s)
        val resp: YoutubeDLResponse = YoutubeDL.getInstance().execute(request, null)
        val res = jsonFormat.decodeFromString<PlaylistResult>(resp.out)
        Log.d(TAG, "getPlaylistInfo: " + Json.encodeToString(res))
        if (res.type != "playlist") {
            return PlaylistResult(playlistCount = 1)
        }
        return res
    }

    fun fetchVideoInfo(url: String, playlistItem: Int = 0): VideoInfo {
//        TextUtil.makeToastSuspend(context.getString(R.string.fetching_info))
        val videoInfo: VideoInfo = YoutubeDL.getInstance().getInfo(YoutubeDLRequest(url).apply {
            addOption("-R", "1")
            if (playlistItem != 0)
                addOption("--playlist-items", playlistItem)
            addOption("--socket-timeout", "5")
        })
        with(videoInfo) {
            if (title.isNullOrEmpty() or ext.isNullOrEmpty()) {
                throw Exception(context.getString(R.string.fetch_info_error_msg))
            }
        }
        return videoInfo
    }


    data class DownloadPreferences(
        val extractAudio: Boolean = PreferenceUtil.getValue(PreferenceUtil.EXTRACT_AUDIO),
        val createThumbnail: Boolean = PreferenceUtil.getValue(PreferenceUtil.THUMBNAIL),
        val downloadPlaylist: Boolean = PreferenceUtil.getValue(PreferenceUtil.PLAYLIST),
        val subdirectory: Boolean = PreferenceUtil.getValue(SUBDIRECTORY),
        val customPath: Boolean = PreferenceUtil.getValue(CUSTOM_PATH),
        val outputPathTemplate: String = PreferenceUtil.getOutputPathTemplate(),
        val embedSubtitle: Boolean = PreferenceUtil.getValue(SUBTITLE),
        val concurrentFragments: Float = PreferenceUtil.getConcurrentFragments(),
        val maxFileSize: String = PreferenceUtil.getString(MAX_FILE_SIZE, ""),
        val sponsorBlock: Boolean = PreferenceUtil.getValue(SPONSORBLOCK),
        val sponsorBlockCategory: String = PreferenceUtil.getSponsorBlockCategories(),
        val cookies: Boolean = PreferenceUtil.getValue(COOKIES),
        val cookiesContent: String = PreferenceUtil.getCookies(),
        val aria2c: Boolean = PreferenceUtil.getValue(ARIA2C),
        val audioFormat: Int = PreferenceUtil.getAudioFormat(),
        val videoFormat: Int = PreferenceUtil.getVideoFormat(),
        val videoResolution: Int = PreferenceUtil.getVideoResolution(),
        val privateMode: Boolean = PreferenceUtil.getValue(PRIVATE_MODE),
        val rateLimit: Boolean = PreferenceUtil.getValue(RATE_LIMIT),
        val maxDownloadRate: String = PreferenceUtil.getMaxDownloadRate(),
        val privateDirectory: Boolean = PreferenceUtil.getValue(PRIVATE_DIRECTORY),
        val cropArtwork: Boolean = PreferenceUtil.getValue(CROP_ARTWORK),
        val customCommandTemplate: String = ""
    )

    private fun YoutubeDLRequest.addOptionsForVideoDownloads(
        downloadPreferences: DownloadPreferences,
    ): YoutubeDLRequest {
        return this.apply {
            with(downloadPreferences) {
                val sorter = StringBuilder()
                if (maxFileSize.isNumberInRange(1, 4096)) {
                    sorter.append("size:${maxFileSize}M,")
                }
                when (videoFormat) {
                    1 -> sorter.append("ext,")
                    2 -> sorter.append("vcodec:vp9.2,")
                    3 -> sorter.append("vcodec:av01,")
                }
                when (videoResolution) {
                    1 -> sorter.append("res:2160")
                    2 -> sorter.append("res:1440")
                    3 -> sorter.append("res:1080")
                    4 -> sorter.append("res:720")
                    5 -> sorter.append("res:480")
                    6 -> sorter.append("res:360")
                    7 -> sorter.append("+size,+br,+res,+fps")
                    else -> sorter.append("res")
                }
                if (sorter.isNotEmpty())
                    addOption("-S", sorter.toString())

                if (embedSubtitle) {
                    addOption("--remux-video", "mkv")
                    addOption("--embed-subs")
                    addOption("--sub-lang", "all,-live_chat")
                }
                if (sponsorBlock) {
                    addOption(
                        "--sponsorblock-remove",
                        sponsorBlockCategory
                    )
                }
            }
        }
    }

    private fun YoutubeDLRequest.addOptionsForAudioDownloads(
        id: String,
        downloadPreferences: DownloadPreferences,
        playlistUrl: String
    ): YoutubeDLRequest {
        return this.apply {
            with(downloadPreferences) {
                addOption("-x")
                when (audioFormat) {
                    1 -> {
                        addOption("--audio-format", "mp3")
                        addOption("--audio-quality", "0")
                    }

                    2 -> {
                        addOption("--audio-format", "m4a")
                        addOption("--audio-quality", "0")
                    }
                }
                addOption("--embed-metadata")
                addOption("--embed-thumbnail")
                addOption("--convert-thumbnails", "png")

                if (cropArtwork) {
                    val configFile = context.getConfigFile(id)
                    FileUtil.writeContentToFile(CROP_ARTWORK_COMMAND, configFile)
                    addOption("--config", configFile.absolutePath)
                }

                if (playlistUrl.isNotEmpty()) {
                    addOption("--parse-metadata", "%(album,playlist,title)s:%(meta_album)s")
                    addOption(
                        "--parse-metadata",
                        "%(track_number,playlist_index)d:%(meta_track)s"
                    )
                } else {
                    addOption("--parse-metadata", "%(album,title)s:%(meta_album)s")
                }
            }
        }
    }

    fun downloadVideo(
        videoInfo: VideoInfo? = null,
        playlistUrl: String = "",
        playlistItem: Int = 0,
        downloadPreferences: DownloadPreferences = DownloadPreferences(),
        progressCallback: ((Float, Long, String) -> Unit)?
    ): Result {
        if (videoInfo == null)
            return Result.failure()

        with(downloadPreferences) {
            // TODO: Move custom template configurations here
//            if (customCommandTemplate.isEmpty()) { }
            val url = playlistUrl.ifEmpty {
                videoInfo.webpageUrl ?: return Result.failure()
            }
            val request = YoutubeDLRequest(url)
            val pathBuilder = StringBuilder()

            with(request) {
                addOption("--no-mtime")
                if (cookies) {
                    val cookiesFile = context.getCookiesFile(videoInfo.id)
                    FileUtil.writeContentToFile(cookiesContent, cookiesFile)
                    addOption("--cookies", cookiesFile.absolutePath)
                }

                if (rateLimit && maxDownloadRate.isNumberInRange(1, 1000000)) {
                    addOption("-r", "${maxDownloadRate}K")
                }

                if (playlistItem != 0 && downloadPlaylist)
                    addOption("--playlist-items", playlistItem)

                if (aria2c) {
                    addOption("--downloader", "libaria2c.so")
                    addOption("--external-downloader-args", "aria2c:\"--summary-interval=1\"")
                } else if (concurrentFragments > 0f) {
                    addOption("--concurrent-fragments", (concurrentFragments * 16).roundToInt())
                }

                if (extractAudio or (videoInfo.ext.matches(Regex(AUDIO_REGEX)))) {
                    if (privateDirectory)
                        pathBuilder.append(BaseApplication.getPrivateDownloadDirectory())
                    else
                        pathBuilder.append(audioDownloadDir)
                    addOptionsForAudioDownloads(
                        id = videoInfo.id,
                        downloadPreferences = downloadPreferences,
                        playlistUrl = playlistUrl
                    )
                } else {
                    if (privateDirectory)
                        pathBuilder.append(BaseApplication.getPrivateDownloadDirectory())
                    else
                        pathBuilder.append(videoDownloadDir)
                    addOptionsForVideoDownloads(downloadPreferences)
                }

                if (createThumbnail) {
                    addOption("--write-thumbnail")
                    addOption("--convert-thumbnails", "png")
                }
                if (!downloadPlaylist) {
                    addOption("--no-playlist")
                }
                if (subdirectory) {
                    pathBuilder.append("/${videoInfo.extractorKey}")
                }

                addOption("-P", pathBuilder.toString())
                if (Build.VERSION.SDK_INT > 23)
                    addOption("-P", "temp:" + context.getTempDir())
                if (customPath)
                    addOption("-o", outputPathTemplate + OUTPUT_TEMPLATE)
                else
                    addOption("-o", OUTPUT_TEMPLATE)

                for (s in request.buildCommand())
                    Log.d(TAG, s)
            }
            YoutubeDL.getInstance().execute(request, videoInfo.id, progressCallback)
            if (privateMode) {
                return Result.success(null)
            }
            val filePaths = FileUtil.scanFileToMediaLibrary(
                title = videoInfo.id,
                downloadDir = pathBuilder.toString()
            )
            for (path in filePaths) {
                DatabaseUtil.insertInfo(
                    DownloadedVideoInfo(
                        id = 0,
                        videoTitle = videoInfo.title,
                        videoAuthor = videoInfo.uploader ?: "null",
                        videoUrl = videoInfo.webpageUrl ?: url,
                        thumbnailUrl = TextUtil.urlHttpToHttps(videoInfo.thumbnail ?: ""),
                        videoPath = path,
                        extractor = videoInfo.extractorKey
                    )
                )
            }
            return Result.success(filePaths)
        }
    }


}