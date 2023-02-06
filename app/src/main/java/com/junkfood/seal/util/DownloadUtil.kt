package com.junkfood.seal.util

import android.os.Build
import android.util.Log
import androidx.annotation.CheckResult
import com.junkfood.seal.App
import com.junkfood.seal.App.Companion.audioDownloadDir
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.App.Companion.videoDownloadDir
import com.junkfood.seal.Downloader
import com.junkfood.seal.Downloader.onProcessEnded
import com.junkfood.seal.Downloader.onProcessStarted
import com.junkfood.seal.Downloader.onTaskEnded
import com.junkfood.seal.Downloader.onTaskError
import com.junkfood.seal.Downloader.onTaskStarted
import com.junkfood.seal.Downloader.toNotificationId
import com.junkfood.seal.R
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.util.FileUtil.getConfigFile
import com.junkfood.seal.util.FileUtil.getCookiesFile
import com.junkfood.seal.util.FileUtil.getSdcardTempDir
import com.junkfood.seal.util.FileUtil.getTempDir
import com.junkfood.seal.util.FileUtil.moveFilesToSdcard
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.getInt
import com.junkfood.seal.util.PreferenceUtil.getString
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
    private const val OUTPUT_TEMPLATE = "%(title).200B [%(id)s].%(ext)s"
    private const val OUTPUT_TEMPLATE_CLIPS =
        "%(title).200B [%(id)s][%(section_start)d-%(section_end)d].%(ext)s"
    private const val CROP_ARTWORK_COMMAND =
        """--ppa "ffmpeg: -c:v mjpeg -vf crop=\"'if(gt(ih,iw),iw,ih)':'if(gt(iw,ih),ih,iw)'\"""""


    @CheckResult
    fun getPlaylistOrVideoInfo(playlistURL: String): Result<YoutubeDLInfo> = YoutubeDL.runCatching {
        ToastUtil.makeToastSuspend(context.getString(R.string.fetching_playlist_info))
        val request = YoutubeDLRequest(playlistURL)
        with(request) {
            addOption("--compat-options", "no-youtube-unavailable-videos")
            addOption("--flat-playlist")
//                addOption("--playlist-items", "1:200")
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
    ): Result<VideoInfo> = YoutubeDLRequest(url).apply {
        preferences.run {
            if (extractAudio) {
                addOption("-x")
            }
            applyFormatSorter(preferences, toFormatSorter())
            if (cookies) {
                enableCookies()
            }
            if (downloadPlaylist) {
                addOption("--compat-options", "no-youtube-unavailable-videos")
            }
        }
        addOption("-R", "1")
        if (playlistItem != 0) addOption("--playlist-items", playlistItem)
        else addOption("--playlist-items", "1")
        addOption("--socket-timeout", "5")
    }.run { getVideoInfo(this) }


    data class DownloadPreferences(
        val extractAudio: Boolean = PreferenceUtil.getValue(EXTRACT_AUDIO),
        val createThumbnail: Boolean = PreferenceUtil.getValue(THUMBNAIL),
        val downloadPlaylist: Boolean = PreferenceUtil.getValue(PLAYLIST),
        val subdirectory: Boolean = PreferenceUtil.getValue(SUBDIRECTORY),
        val customPath: Boolean = PreferenceUtil.getValue(CUSTOM_PATH),
        val outputPathTemplate: String = PreferenceUtil.getOutputPathTemplate(),
        val downloadSubtitle: Boolean = PreferenceUtil.getValue(SUBTITLE),
        val embedSubtitle: Boolean = EMBED_SUBTITLE.getBoolean(),
        val subtitleLanguage: String = SUBTITLE_LANGUAGE.getString(),
        val autoSubtitle: Boolean = PreferenceUtil.getValue(AUTO_SUBTITLE),
        val concurrentFragments: Float = PreferenceUtil.getConcurrentFragments(),
        val maxFileSize: String = MAX_FILE_SIZE.getString(),
        val sponsorBlock: Boolean = PreferenceUtil.getValue(SPONSORBLOCK),
        val sponsorBlockCategory: String = PreferenceUtil.getSponsorBlockCategories(),
        val cookies: Boolean = PreferenceUtil.getValue(COOKIES),
        val aria2c: Boolean = PreferenceUtil.getValue(ARIA2C),
        val audioFormat: Int = AUDIO_FORMAT.getInt(),
        val audioQuality: Int = AUDIO_QUALITY.getInt(),
        val convertAudio: Boolean = AUDIO_CONVERT.getBoolean(),
        val formatSorting: Boolean = FORMAT_SORTING.getBoolean(),
        val sortingFields: String = SORTING_FIELDS.getString(),
        val audioConvertFormat: Int = PreferenceUtil.getAudioConvertFormat(),
        val videoFormat: Int = PreferenceUtil.getVideoFormat(),
        val formatId: String = "",
        val videoResolution: Int = PreferenceUtil.getVideoResolution(),
        val privateMode: Boolean = PreferenceUtil.getValue(PRIVATE_MODE),
        val rateLimit: Boolean = PreferenceUtil.getValue(RATE_LIMIT),
        val maxDownloadRate: String = PreferenceUtil.getMaxDownloadRate(),
        val privateDirectory: Boolean = PreferenceUtil.getValue(PRIVATE_DIRECTORY),
        val cropArtwork: Boolean = PreferenceUtil.getValue(CROP_ARTWORK),
        val sdcard: Boolean = PreferenceUtil.getValue(SDCARD_DOWNLOAD),
        val sdcardUri: String = SDCARD_URI.getString(),
        val videoClips: List<VideoClip> = emptyList(),
        val newTitle: String = ""
    )

    private fun YoutubeDLRequest.enableCookies(): YoutubeDLRequest = this.apply {
        PreferenceUtil.getCookies().run {
            if (isNotEmpty()) addOption(
                "--cookies", FileUtil.writeContentToFile(
                    this, context.getCookiesFile()
                ).absolutePath
            )
        }
    }

    private fun YoutubeDLRequest.enableAria2c(): YoutubeDLRequest =
        this.addOption("--downloader", "libaria2c.so")
            .addOption("--external-downloader-args", "aria2c:\"--summary-interval=1\"")

    private fun YoutubeDLRequest.addOptionsForVideoDownloads(
        downloadPreferences: DownloadPreferences,
    ): YoutubeDLRequest = this.apply {
        downloadPreferences.run {
            if (formatId.isNotEmpty()) addOption("-f", formatId)
            else applyFormatSorter(this, toFormatSorter())
            if (downloadSubtitle) {
                if (autoSubtitle) {
                    addOption("--write-auto-subs")
                    addOption("--extractor-args", "youtube:skip=translated_subs")
                }
                addOption("--sub-langs", subtitleLanguage)
                if (embedSubtitle) {
                    addOption("--remux-video", "mkv")
                    addOption("--embed-subs")
                } else {
                    addOption("--write-subs")
                }
            }
            if (videoClips.isEmpty())
                addOption("--embed-chapters")
        }
    }


    @CheckResult
    private fun DownloadPreferences.toAudioFormatSorter(): String = this.run {
        val format = when (audioFormat) {
            M4A -> "acodec:m4a"
            OPUS -> "acodec:opus"
            else -> ""
        }
        val quality = when (audioQuality) {
            HIGH -> "abr~192"
            MEDIUM -> "abr~128"
            LOW -> "abr~64"
            else -> ""
        }
        return@run connectWithDelimiter(format, quality, delimiter = ",")
    }

    @CheckResult
    private fun DownloadPreferences.toVideoFormatSorter(): String = this.run {
        val fileSize = if (maxFileSize.isNumberInRange(1, 4096)) {
            "size:${maxFileSize}M"
        } else ""
        val format = when (videoFormat) {
            1 -> "ext"
            2 -> "vcodec:vp9.2"
            3 -> "vcodec:av01"
            else -> ""
        }
        val res = when (videoResolution) {
            1 -> "res:2160"
            2 -> "res:1440"
            3 -> "res:1080"
            4 -> "res:720"
            5 -> "res:480"
            6 -> "res:360"
            7 -> "+res"
            else -> ""
        }
        return@run connectWithDelimiter(fileSize, format, res, delimiter = ",")
    }

    private fun YoutubeDLRequest.applyFormatSorter(
        preferences: DownloadPreferences,
        sorter: String
    ) = preferences.run {
        if (formatSorting && sortingFields.isNotEmpty())
            addOption("-S", sortingFields)
        else {
            if (sorter.isNotEmpty()) addOption("-S", sorter) else {
            }
        }
    }

    @CheckResult
    fun DownloadPreferences.toFormatSorter(): String =
        connectWithDelimiter(
            this.toVideoFormatSorter(),
            this.toAudioFormatSorter(),
            delimiter = ","
        )

    private fun YoutubeDLRequest.addOptionsForAudioDownloads(
        id: String, preferences: DownloadPreferences, playlistUrl: String
    ): YoutubeDLRequest = this.apply {
        with(preferences) {
            addOption("-x")
            if (formatId.isNotEmpty()) addOption("-f", formatId)
            else if (convertAudio) {
                when (audioConvertFormat) {
                    1 -> {
                        addOption("--audio-format", "mp3")
                    }

                    2 -> {
                        addOption("--audio-format", "m4a")
                    }
                }
            } else {
                applyFormatSorter(preferences, toAudioFormatSorter())
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
    ): List<String> = FileUtil.scanFileToMediaLibraryPostDownload(
        title = videoInfo.id, downloadDir = downloadPath
    ).apply {
        insertInfoIntoDownloadHistory(videoInfo, this)
    }


    private fun insertInfoIntoDownloadHistory(videoInfo: VideoInfo, filePaths: List<String>) =
        filePaths.forEach {
            DatabaseUtil.insertInfo(
                DownloadedVideoInfo(
                    id = 0,
                    videoTitle = videoInfo.title,
                    videoAuthor = videoInfo.uploader ?: videoInfo.channel.toString(),
                    videoUrl = videoInfo.webpageUrl ?: videoInfo.originalUrl.toString(),
                    thumbnailUrl = videoInfo.thumbnail.toHttpsUrl(),
                    videoPath = it,
                    extractor = videoInfo.extractorKey
                )
            )
        }

    @CheckResult
    fun downloadVideo(
        videoInfo: VideoInfo? = null,
        playlistUrl: String = "",
        playlistItem: Int = 0,
        taskId: String,
        downloadPreferences: DownloadPreferences,
        progressCallback: ((Float, Long, String) -> Unit)?
    ): Result<List<String>> {
        if (videoInfo == null) return Result.failure(Throwable(context.getString(R.string.fetch_info_error_msg)))

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
                } else {
                    addOption("--no-playlist")
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
                        preferences = downloadPreferences,
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
                if (subdirectory) {
                    pathBuilder.append("/${videoInfo.extractorKey}")
                }
                if (sdcard) {
                    addOption("-P", context.getSdcardTempDir(videoInfo.id).absolutePath)
                } else {
                    addOption("-P", pathBuilder.toString())
                }

                videoClips.forEach {
                    addOption("--download-sections", "*%d-%d".format(it.start, it.end))
                }
                if (newTitle.isNotEmpty()) {
                    addCommands(listOf("--replace-in-metadata", "title", ".+", newTitle))
                }
                if (Build.VERSION.SDK_INT > 23 && !sdcard) addOption(
                    "-P", "temp:" + context.getTempDir()
                )
                val outputFileName =
                    if (videoClips.isEmpty()) OUTPUT_TEMPLATE else OUTPUT_TEMPLATE_CLIPS

                if (customPath) addOption("-o", outputPathTemplate + outputFileName)
                else addOption("-o", outputFileName)

                for (s in request.buildCommand()) Log.d(TAG, s)
            }.runCatching {
                YoutubeDL.getInstance().execute(
                    request = this, processId = taskId, callback = progressCallback
                )
            }.onFailure { th ->
                return if (sponsorBlock && th.message?.contains("Unable to communicate with SponsorBlock API") == true) {
                    th.printStackTrace()
                    onFinishDownloading(
                        preferences = this,
                        videoInfo = videoInfo,
                        downloadPath = pathBuilder.toString(),
                        sdcardUri = sdcardUri
                    )
                } else Result.failure(th)
            }
            return onFinishDownloading(
                preferences = this,
                videoInfo = videoInfo,
                downloadPath = pathBuilder.toString(),
                sdcardUri = sdcardUri
            )
        }
    }

    private fun onFinishDownloading(
        preferences: DownloadPreferences,
        videoInfo: VideoInfo,
        downloadPath: String,
        sdcardUri: String
    ): Result<List<String>> = preferences.run {
        if (privateMode) {
            Result.success(emptyList())
        } else if (sdcard) {
            Result.success(moveFilesToSdcard(
                sdcardUri = sdcardUri, tempPath = context.getSdcardTempDir(videoInfo.id)
            ).apply {
                insertInfoIntoDownloadHistory(videoInfo, this)
            })
        } else {
            Result.success(
                scanVideoIntoDownloadHistory(
                    videoInfo = videoInfo,
                    downloadPath = downloadPath,
                )
            )
        }
    }

    suspend fun executeCommandInBackground(
        url: String, template: CommandTemplate = PreferenceUtil.getTemplate()
    ) {
        val taskId = Downloader.makeKey(url = url, templateName = template.name)
        val notificationId = taskId.toNotificationId()
        val urlList = url.split(Regex("[\n ]"))

        ToastUtil.makeToastSuspend(context.getString(R.string.start_execute))
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

        onProcessStarted()
        onTaskStarted(template, url)
        kotlin.runCatching {
            val response = YoutubeDL.getInstance().execute(
                request = request, processId = taskId
            ) { progress, _, text ->
                NotificationUtil.makeNotificationForCustomCommand(
                    notificationId = notificationId,
                    taskId = taskId,
                    progress = progress.toInt(),
                    templateName = template.name,
                    taskUrl = url,
                    text = text
                )
                Downloader.updateTaskOutput(
                    template = template, url = url, line = text, progress = progress
                )
            }
            onTaskEnded(template, url, response.out)
        }.onFailure {
            it.printStackTrace()
            if (it is YoutubeDL.CanceledException) return@onFailure
            it.message.run {
                if (isNullOrEmpty()) onTaskEnded(template, url)
                else onTaskError(this, template, url)
            }
        }
        onProcessEnded()
    }


}