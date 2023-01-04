package com.junkfood.seal.util

import android.os.Build
import android.util.Log
import androidx.annotation.CheckResult
import com.junkfood.seal.App
import com.junkfood.seal.App.Companion.audioDownloadDir
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.App.Companion.videoDownloadDir
import com.junkfood.seal.Downloader.toNotificationId
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
import com.junkfood.seal.util.TextUtil.toHttpsUrl
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

object DownloadUtil {

    private val jsonFormat = Json {
        ignoreUnknownKeys = true
    }

    private const val TAG = "DownloadUtil"
    private const val OUTPUT_TEMPLATE = "%(title).70s [%(id)s].%(ext)s"
    private const val CROP_ARTWORK_COMMAND =
        """--ppa "ffmpeg: -c:v mjpeg -vf crop=in_h""""


    @CheckResult
    fun getPlaylistOrVideoInfo(playlistURL: String): Result<YoutubeDLInfo> =
        YoutubeDL.runCatching {
            TextUtil.makeToastSuspend(context.getString(R.string.fetching_playlist_info))
            val request = YoutubeDLRequest(playlistURL)
            with(request) {
                addOption("--compat-options", "no-youtube-unavailable-videos")
                addOption("--flat-playlist")
                addOption("-J")
                addOption("-R", "1")
                addOption("--socket-timeout", "5")
                if (PreferenceUtil.getValue(COOKIES)) {
                    enableCookies()
                }
            }
            execute(request, playlistURL).out.run {
                val playlistInfo = jsonFormat.decodeFromString<PlaylistResult>(this)
                Log.d(TAG, "getPlaylistInfo: " + Json.encodeToString(playlistInfo))
                if (playlistInfo.type != "playlist") {
                    jsonFormat.decodeFromString<VideoInfo>(this)
                } else playlistInfo
            }
        }


    @CheckResult
    private fun getVideoInfo(request: YoutubeDLRequest): Result<VideoInfo> =
        request.addOption("--dump-json").runCatching {
            val response: YoutubeDLResponse = YoutubeDL.getInstance().execute(request, null, null)
            jsonFormat.decodeFromString(response.out)
        }


    @CheckResult
    fun fetchVideoInfoFromUrl(
        url: String, playlistItem: Int = 0, preferences: DownloadPreferences = DownloadPreferences()
    ): Result<VideoInfo> =
        YoutubeDLRequest(url).apply {
            preferences.run {
                if (extractAudio) {
                    addOption("-x")
                } else {
                    addOption("-S", toVideoFormatSorter())
                }
                if (cookies) {
                    enableCookies()
                }
                if (downloadPlaylist) {
                    addOption("--compat-options", "no-youtube-unavailable-videos")
                }
            }
            addOption("-R", "1")
            if (playlistItem != 0) addOption("--playlist-items", playlistItem)
            else addOption("--no-playlist")
            addOption("--socket-timeout", "5")
        }.run { getVideoInfo(this) }


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
        val formatId: String = "",
        val videoResolution: Int = PreferenceUtil.getVideoResolution(),
        val privateMode: Boolean = PreferenceUtil.getValue(PRIVATE_MODE),
        val rateLimit: Boolean = PreferenceUtil.getValue(RATE_LIMIT),
        val maxDownloadRate: String = PreferenceUtil.getMaxDownloadRate(),
        val privateDirectory: Boolean = PreferenceUtil.getValue(PRIVATE_DIRECTORY),
        val cropArtwork: Boolean = PreferenceUtil.getValue(CROP_ARTWORK),
        val customCommandTemplate: String = ""
    )

    private fun YoutubeDLRequest.enableCookies(): YoutubeDLRequest = this.apply {
        PreferenceUtil.getCookies().run {
            if (isNotEmpty())
                addOption(
                    "--cookies", FileUtil.writeContentToFile(
                        this, context.getCookiesFile()
                    ).absolutePath
                )
        }
    }

    private fun YoutubeDLRequest.enableAria2c(): YoutubeDLRequest =
        this.addOption("--downloader", "libaria2c.so")
            .addOption("--external-downloader-args", "aria2c:\"--summary-interval=500\"")

    private fun YoutubeDLRequest.addOptionsForVideoDownloads(
        downloadPreferences: DownloadPreferences,
    ): YoutubeDLRequest {
        return this.apply {
            downloadPreferences.run {
                if (formatId.isNotEmpty())
                    addOption("-f", formatId)
                else
                    addOption("-S", toVideoFormatSorter())
                if (embedSubtitle) {
                    addOption("--remux-video", "mkv")
                    addOption("--embed-subs")
                    addOption("--sub-lang", "all,-live_chat")
                }
            }
        }
    }

    @CheckResult
    private fun DownloadPreferences.toVideoFormatSorter(): String =
        StringBuilder().run {
            if (maxFileSize.isNumberInRange(1, 4096)) {
                append("size:${maxFileSize}M,")
            }
            when (videoFormat) {
                1 -> append("ext,")
                2 -> append("vcodec:vp9.2,")
                3 -> append("vcodec:av01,")
            }
            when (videoResolution) {
                1 -> append("res:2160")
                2 -> append("res:1440")
                3 -> append("res:1080")
                4 -> append("res:720")
                5 -> append("res:480")
                6 -> append("res:360")
                7 -> append("+size,+br,+res,+fps")
                else -> append("res")
            }
        }.toString()

    private fun YoutubeDLRequest.addOptionsForAudioDownloads(
        id: String, downloadPreferences: DownloadPreferences, playlistUrl: String
    ): YoutubeDLRequest = this.apply {
        with(downloadPreferences) {
            addOption("-x")
            if (formatId.isNotEmpty())
                addOption("-f", formatId)
            else
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
            addOption("--convert-thumbnails", "jpg")

            if (cropArtwork) {
                val configFile = context.getConfigFile(id)
                FileUtil.writeContentToFile(CROP_ARTWORK_COMMAND, configFile)
                addOption("--config", configFile.absolutePath)
            }

            addOption("--parse-metadata", "%(release_year,upload_date)s:%(meta_date)s")

            if (playlistUrl.isNotEmpty()) {
                addOption("--parse-metadata", "%(album,playlist,title)s:%(meta_album)s")
                addOption(
                    "--parse-metadata", "%(track_number,playlist_index)d:%(meta_track)s"
                )
            } else {
                addOption("--parse-metadata", "%(album,title)s:%(meta_album)s")
            }
        }

    }

    @CheckResult
    private fun scanVideoIntoDownloadHistory(
        videoInfo: VideoInfo,
        downloadPath: String,
    ): List<String> {
        val filePaths = FileUtil.scanFileToMediaLibrary(
            title = videoInfo.id, downloadDir = downloadPath
        )
        for (path in filePaths) {
            DatabaseUtil.insertInfo(
                DownloadedVideoInfo(
                    id = 0,
                    videoTitle = videoInfo.title,
                    videoAuthor = videoInfo.uploader ?: videoInfo.channel.toString(),
                    videoUrl = videoInfo.webpageUrl ?: videoInfo.originalUrl.toString(),
                    thumbnailUrl = videoInfo.thumbnail.toHttpsUrl(),
                    videoPath = path,
                    extractor = videoInfo.extractorKey
                )
            )
        }
        return filePaths
    }

    @CheckResult
    fun downloadVideo(
        videoInfo: VideoInfo? = null,
        playlistUrl: String = "",
        playlistItem: Int = 0,
        downloadPreferences: DownloadPreferences,
        progressCallback: ((Float, Long, String) -> Unit)?
    ): Result<List<String>> {
        if (videoInfo == null) return Result.failure(Throwable(context.getString(R.string.fetch_info_error_msg)))

        val filePaths = mutableListOf<String>()
        with(downloadPreferences) {
            val url = playlistUrl.ifEmpty {
                videoInfo.webpageUrl
                    ?: return Result.failure(Throwable(context.getString(R.string.fetch_info_error_msg)))
            }
            val request = YoutubeDLRequest(url)
            val pathBuilder = StringBuilder()

            request.apply {
                addOption("--no-mtime")
//                addOption("-v")
                if (cookies) {
                    enableCookies()
                }

                if (rateLimit && maxDownloadRate.isNumberInRange(1, 1000000)) {
                    addOption("-r", "${maxDownloadRate}K")
                }

                if (playlistItem != 0 && downloadPlaylist) {
                    addOption("--playlist-items", playlistItem)
                    addOption("--compat-options", "no-youtube-unavailable-videos")
                }

                if (aria2c) {
                    enableAria2c()
                } else if (concurrentFragments > 0f) {
                    addOption("--concurrent-fragments", (concurrentFragments * 16).roundToInt())
                }

                if (extractAudio || (videoInfo.vcodec == "none")) {
                    if (privateDirectory) pathBuilder.append(App.getPrivateDownloadDirectory())
                    else pathBuilder.append(audioDownloadDir)
                    addOptionsForAudioDownloads(
                        id = videoInfo.id,
                        downloadPreferences = downloadPreferences,
                        playlistUrl = playlistUrl
                    )
                } else {
                    if (privateDirectory) pathBuilder.append(App.getPrivateDownloadDirectory())
                    else pathBuilder.append(videoDownloadDir)
                    addOptionsForVideoDownloads(downloadPreferences)
                }
                if (sponsorBlock) {
                    addOption("--sponsorblock-remove", sponsorBlockCategory)
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
                if (Build.VERSION.SDK_INT > 23) addOption("-P", "temp:" + context.getTempDir())
                if (customPath) addOption("-o", outputPathTemplate + OUTPUT_TEMPLATE)
                else addOption("-o", OUTPUT_TEMPLATE)

                for (s in request.buildCommand()) Log.d(TAG, s)
            }.runCatching {
                YoutubeDL.getInstance().execute(
                    request = this,
                    processId = videoInfo.id,
                    callback = progressCallback
                )
            }.onFailure { th ->
                if (sponsorBlock && th.message?.contains("Unable to communicate with SponsorBlock API") == true) {
                    th.printStackTrace()
                    filePaths.addAll(
                        scanVideoIntoDownloadHistory(
                            videoInfo = videoInfo,
                            downloadPath = pathBuilder.toString(),
                        )
                    )
                    return Result.success(filePaths)
                }
                return Result.failure(th)
            }.onSuccess {
                if (privateMode) {
                    return Result.success(emptyList())
                }
                filePaths.addAll(
                    scanVideoIntoDownloadHistory(
                        videoInfo = videoInfo,
                        downloadPath = pathBuilder.toString(),
                    )
                )
            }
        }
        return Result.success(filePaths)

    }

    suspend fun executeCommandInBackground(url: String) {
        val notificationId = url.toNotificationId()
        val urlList = url.split(Regex("[\n ]"))
        val template = PreferenceUtil.getTemplate()
        TextUtil.makeToastSuspend(context.getString(R.string.start_execute))
        val request = YoutubeDLRequest(urlList).apply {
            addOption(
                "-P",
                if (PreferenceUtil.getValue(PRIVATE_DIRECTORY)) App.getPrivateDownloadDirectory() else videoDownloadDir
            )
            if (PreferenceUtil.getValue(ARIA2C)) {
                enableAria2c()
            }
            addOption(
                "--config-locations", FileUtil.writeContentToFile(
                    template.template, context.getConfigFile()
                ).absolutePath
            )
//            addOption("-v")
            if (PreferenceUtil.getValue(COOKIES)) {
                enableCookies()
            }
        }

        App.startService()
        kotlin.runCatching {
            var last = System.nanoTime()
            YoutubeDL.getInstance().execute(request, url) { progress, _, text ->
                val now = System.nanoTime()
                if (now - last > 500L) {
                    last = now
                    NotificationUtil.makeNotificationForCustomCommand(
                        notificationId = notificationId,
                        taskId = url,
                        progress = progress.toInt(),
                        templateName = template.name,
                        taskUrl = url,
                        text = text
                    )
                }
            }
            NotificationUtil.finishNotification(
                notificationId = notificationId,
                title = template.name + "_" + url,
                text = context.getString(R.string.status_completed),
            )
        }.onFailure {
            it.printStackTrace()
            if (it is YoutubeDL.CanceledException) return
            val msg = it.message
            if (msg.isNullOrEmpty())
                NotificationUtil.finishNotification(
                    notificationId = notificationId,
                    title = template.name + "_" + url,
                    text = context.getString(R.string.status_completed),
                )
            else {
                NotificationUtil.makeErrorReportNotificationForCustomCommand(
                    notificationId = notificationId,
                    error = msg
                )
            }
        }

        App.stopService()

    }


}