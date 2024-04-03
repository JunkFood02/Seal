package com.junkfood.seal.util

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.OPEN_READONLY
import android.media.MediaCodecList
import android.os.Build
import android.util.Log
import android.webkit.CookieManager
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
import com.junkfood.seal.database.objects.CommandTemplate
import com.junkfood.seal.database.objects.DownloadedVideoInfo
import com.junkfood.seal.ui.page.settings.network.Cookie
import com.junkfood.seal.util.FileUtil.getArchiveFile
import com.junkfood.seal.util.FileUtil.getConfigFile
import com.junkfood.seal.util.FileUtil.getCookiesFile
import com.junkfood.seal.util.FileUtil.getExternalTempDir
import com.junkfood.seal.util.FileUtil.getFileName
import com.junkfood.seal.util.FileUtil.getSdcardTempDir
import com.junkfood.seal.util.FileUtil.moveFilesToSdcard
import com.junkfood.seal.util.PreferenceUtil.COOKIE_HEADER
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.getInt
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.Locale


object DownloadUtil {

    object CookieScheme {
        const val NAME = "name"
        const val VALUE = "value"
        const val SECURE = "is_secure"
        const val EXPIRY = "expires_utc"
        const val HOST = "host_key"
        const val PATH = "path"
    }

    private val jsonFormat = Json {
        ignoreUnknownKeys = true
    }

    private const val TAG = "DownloadUtil"


    const val BASENAME = "%(title).200B"

    const val EXTENSION = ".%(ext)s"

    private const val ID = "[%(id)s]"

    private const val CLIP_TIMESTAMP = "%(section_start)d-%(section_end)d"

    const val OUTPUT_TEMPLATE_DEFAULT = BASENAME + EXTENSION

    const val OUTPUT_TEMPLATE_ID = "$BASENAME $ID$EXTENSION"

    private const val OUTPUT_TEMPLATE_CLIPS = "$BASENAME [$CLIP_TIMESTAMP]$EXTENSION"

    private const val OUTPUT_TEMPLATE_CHAPTERS =
        "chapter:$BASENAME/%(section_number)d - %(section_title).200B$EXTENSION"

    private const val OUTPUT_TEMPLATE_SPLIT = "$BASENAME/$OUTPUT_TEMPLATE_DEFAULT"

    private const val PLAYLIST_TITLE_SUBDIRECTORY_PREFIX = "%(playlist)s/"

    private const val CROP_ARTWORK_COMMAND =
        """--ppa "ffmpeg: -c:v mjpeg -vf crop=\"'if(gt(ih,iw),iw,ih)':'if(gt(iw,ih),ih,iw)'\"""""


    @CheckResult
    fun getPlaylistOrVideoInfo(
        playlistURL: String, downloadPreferences: DownloadPreferences = DownloadPreferences()
    ): Result<YoutubeDLInfo> = YoutubeDL.runCatching {
        ToastUtil.makeToastSuspend(context.getString(R.string.fetching_playlist_info))
        val request = YoutubeDLRequest(playlistURL)
        with(request) {
//            addOption("--compat-options", "no-youtube-unavailable-videos")
            addOption("--flat-playlist")
            addOption("--dump-single-json")
            addOption("-o", BASENAME)
            addOption("-R", "1")
            addOption("--socket-timeout", "5")
            downloadPreferences.run {
                if (extractAudio) {
                    addOption("-x")
                }
                applyFormatSorter(this, toFormatSorter())
                if (proxy) {
                    enableProxy(proxyUrl)
                }
                if (forceIpv4) {
                    addOption("-4")
                }
                if (cookies) {
                    enableCookies(userAgentString)
                }
                if (restrictFilenames) {
                    addOption("--restrict-filenames")
                }
            }
        }
        execute(request, playlistURL).out.run {
            val playlistInfo = jsonFormat.decodeFromString<PlaylistResult>(this)
            if (playlistInfo.type != "playlist") {
                jsonFormat.decodeFromString<VideoInfo>(this)
            } else playlistInfo
        }
    }


    @CheckResult
    private fun getVideoInfo(request: YoutubeDLRequest): Result<VideoInfo> = request.runCatching {
        val response: YoutubeDLResponse = YoutubeDL.getInstance().execute(request, null, null)
        jsonFormat.decodeFromString(response.out)
    }


    @CheckResult
    fun fetchVideoInfoFromUrl(
        url: String, playlistItem: Int = 0, preferences: DownloadPreferences = DownloadPreferences()
    ): Result<VideoInfo> = YoutubeDLRequest(url).apply {
        preferences.run {
            addOption("-o", BASENAME)
            if (restrictFilenames) {
                addOption("--restrict-filenames")
            }
            if (extractAudio) {
                addOption("-x")
            }
            applyFormatSorter(preferences, toFormatSorter())
            if (cookies) {
                enableCookies(userAgentString)
            }
            if (proxy) {
                enableProxy(proxyUrl)
            }
            if (forceIpv4) {
                addOption("-4")
            }
            /*            if (debug) {
                            addOption("-v")
                        }*/
            if (autoSubtitle) {
                addOption("--write-auto-subs")
                if (!autoTranslatedSubtitles) {
                    addOption("--extractor-args", "youtube:skip=translated_subs")
                }
            }
        }
        addOption("--dump-json")
        addOption("-R", "1")
        addOption("--no-playlist")
        if (playlistItem != 0) addOption("--playlist-items", playlistItem)
        else addOption("--playlist-items", "1")
        addOption("--socket-timeout", "5")
    }.run { getVideoInfo(this) }

    data class DownloadPreferences(
        val extractAudio: Boolean = PreferenceUtil.getValue(EXTRACT_AUDIO),
        val createThumbnail: Boolean = PreferenceUtil.getValue(THUMBNAIL),
        val downloadPlaylist: Boolean = PreferenceUtil.getValue(PLAYLIST),
        val subdirectoryExtractor: Boolean = PreferenceUtil.getValue(SUBDIRECTORY_EXTRACTOR),
        val subdirectoryPlaylistTitle: Boolean = SUBDIRECTORY_PLAYLIST_TITLE.getBoolean(),
        val commandDirectory: String = COMMAND_DIRECTORY.getString(),
        val downloadSubtitle: Boolean = PreferenceUtil.getValue(SUBTITLE),
        val embedSubtitle: Boolean = EMBED_SUBTITLE.getBoolean(),
        val keepSubtitle: Boolean = KEEP_SUBTITLE_FILES.getBoolean(),
        val subtitleLanguage: String = SUBTITLE_LANGUAGE.getString(),
        val autoSubtitle: Boolean = PreferenceUtil.getValue(AUTO_SUBTITLE),
        val autoTranslatedSubtitles: Boolean = AUTO_TRANSLATED_SUBTITLES.getBoolean(),
        val convertSubtitle: Int = CONVERT_SUBTITLE.getInt(),
        val concurrentFragments: Int = CONCURRENT.getInt(),
        val sponsorBlock: Boolean = PreferenceUtil.getValue(SPONSORBLOCK),
        val sponsorBlockCategory: String = PreferenceUtil.getSponsorBlockCategories(),
        val cookies: Boolean = COOKIES.getBoolean(),
        val aria2c: Boolean = PreferenceUtil.getValue(ARIA2C),
        val audioFormat: Int = AUDIO_FORMAT.getInt(),
        val audioQuality: Int = AUDIO_QUALITY.getInt(),
        val convertAudio: Boolean = AUDIO_CONVERT.getBoolean(),
        val formatSorting: Boolean = FORMAT_SORTING.getBoolean(),
        val sortingFields: String = SORTING_FIELDS.getString(),
        val audioConvertFormat: Int = PreferenceUtil.getAudioConvertFormat(),
        val videoFormat: Int = PreferenceUtil.getVideoFormat(),
        val formatIdString: String = "",
        val videoResolution: Int = PreferenceUtil.getVideoResolution(),
        val privateMode: Boolean = PreferenceUtil.getValue(PRIVATE_MODE),
        val rateLimit: Boolean = PreferenceUtil.getValue(RATE_LIMIT),
        val maxDownloadRate: String = PreferenceUtil.getMaxDownloadRate(),
        val privateDirectory: Boolean = PreferenceUtil.getValue(PRIVATE_DIRECTORY),
        val cropArtwork: Boolean = PreferenceUtil.getValue(CROP_ARTWORK),
        val sdcard: Boolean = PreferenceUtil.getValue(SDCARD_DOWNLOAD),
        val sdcardUri: String = SDCARD_URI.getString(),
        val embedThumbnail: Boolean = EMBED_THUMBNAIL.getBoolean(),
        val videoClips: List<VideoClip> = emptyList(),
        val splitByChapter: Boolean = false,
        val debug: Boolean = DEBUG.getBoolean(),
        val proxy: Boolean = PROXY.getBoolean(),
        val proxyUrl: String = PROXY_URL.getString(),
        val newTitle: String = "",
        val userAgentString: String = USER_AGENT_STRING.run {
            if (USER_AGENT.getBoolean()) getString() else ""
        },
        val outputTemplate: String = OUTPUT_TEMPLATE.getString(),
        val useDownloadArchive: Boolean = DOWNLOAD_ARCHIVE.getBoolean(),
        val embedMetadata: Boolean = EMBED_METADATA.getBoolean(),
        val restrictFilenames: Boolean = RESTRICT_FILENAMES.getBoolean(),
        val supportAv1HardwareDecoding: Boolean = checkIfAv1HardwareAccelerated(),
        val forceIpv4: Boolean = FORCE_IPV4.getBoolean(),
        val mergeAudioStream: Boolean = false
    )

    private fun YoutubeDLRequest.enableCookies(userAgentString: String): YoutubeDLRequest =
        this.addOption("--cookies", context.getCookiesFile().absolutePath).apply {
            if (userAgentString.isNotEmpty()) {
                addOption("--add-header", "User-Agent:$userAgentString")
            }
        }

    private fun YoutubeDLRequest.enableProxy(proxyUrl: String): YoutubeDLRequest =
        this.addOption("--proxy", proxyUrl)

    private fun YoutubeDLRequest.useDownloadArchive(): YoutubeDLRequest =
        this.addOption("--download-archive", context.getArchiveFile().absolutePath)


    @CheckResult
    fun getCookiesContentFromDatabase(): Result<String> = runCatching {
        CookieManager.getInstance().run {
            if (!hasCookies()) throw Exception("There is no cookies in the database!")
            flush()
        }
        SQLiteDatabase.openDatabase(
            "/data/data/com.junkfood.seal/app_webview/Default/Cookies", null, OPEN_READONLY
        ).run {
            val projection = arrayOf(
                CookieScheme.HOST,
                CookieScheme.EXPIRY,
                CookieScheme.PATH,
                CookieScheme.NAME,
                CookieScheme.VALUE,
                CookieScheme.SECURE
            )
            val cookieList = mutableListOf<Cookie>()
            query(
                "cookies", projection, null, null, null, null, null
            ).run {
                while (moveToNext()) {
                    val expiry = getLong(getColumnIndexOrThrow(CookieScheme.EXPIRY))
                    val name = getString(getColumnIndexOrThrow(CookieScheme.NAME))
                    val value = getString(getColumnIndexOrThrow(CookieScheme.VALUE))
                    val path = getString(getColumnIndexOrThrow(CookieScheme.PATH))
                    val secure = getLong(getColumnIndexOrThrow(CookieScheme.SECURE)) == 1L
                    val hostKey = getString(getColumnIndexOrThrow(CookieScheme.HOST))

                    val host = if (hostKey[0] != '.') ".$hostKey" else hostKey
                    cookieList.add(
                        Cookie(
                            domain = host,
                            name = name,
                            value = value,
                            path = path,
                            secure = secure,
                            expiry = expiry
                        )
                    )
                }
                close()
            }
            close()
            Log.d(TAG, "Loaded ${cookieList.size} cookies from database!")
            cookieList.fold(StringBuilder(COOKIE_HEADER)) { acc, cookie ->
                acc.append(cookie.toNetscapeCookieString()).append("\n")
            }.toString()
        }
    }


    private fun YoutubeDLRequest.enableAria2c(): YoutubeDLRequest =
        this.addOption("--downloader", "libaria2c.so")
            .addOption("--external-downloader-args", "aria2c:\"--summary-interval=1\"")

    private fun YoutubeDLRequest.addOptionsForVideoDownloads(
        downloadPreferences: DownloadPreferences,
    ): YoutubeDLRequest = this.apply {
        downloadPreferences.run {
            addOption("--add-metadata")
            addOption("--no-embed-info-json")
            if (formatIdString.isNotEmpty()) {
                addOption("-f", formatIdString)
                if (mergeAudioStream) {
                    addOption("--audio-multistreams")
                }
            } else {
                applyFormatSorter(this, toFormatSorter())
            }
            if (downloadSubtitle) {
                if (autoSubtitle) {
                    addOption("--write-auto-subs")
                    if (!autoTranslatedSubtitles) {
                        addOption("--extractor-args", "youtube:skip=translated_subs")
                    }
                }
                subtitleLanguage.takeIf { it.isNotEmpty() }?.let { addOption("--sub-langs", it) }
                if (embedSubtitle) {
                    addOption("--remux-video", "mkv")
                    addOption("--embed-subs")
                    if (keepSubtitle) {
                        addOption("--write-subs")
                    }
                } else {
                    addOption("--write-subs")
                }
                when (convertSubtitle) {
                    CONVERT_ASS -> addOption("--convert-subs", "ass")
                    CONVERT_SRT -> addOption("--convert-subs", "srt")
                    CONVERT_VTT -> addOption("--convert-subs", "vtt")
                    CONVERT_LRC -> addOption("--convert-subs", "lrc")
                    else -> {}
                }
            }
            if (embedThumbnail) {
                addOption("--embed-thumbnail")
            }
            if (videoClips.isEmpty()) addOption("--embed-chapters")
        }
    }


    @CheckResult
    private fun DownloadPreferences.toAudioFormatSorter(): String = this.run {
        val format = when (audioFormat) {
            M4A -> "acodec:aac"
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
        val format = when (videoFormat) {
            FORMAT_COMPATIBILITY -> "proto,vcodec:h264,ext"
            FORMAT_QUALITY -> if (supportAv1HardwareDecoding) {
                "vcodec:av01"
            } else {
                "vcodec:vp9.2"
            }

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
        return@run connectWithDelimiter(format, res, delimiter = ",")
    }

    private fun YoutubeDLRequest.applyFormatSorter(
        preferences: DownloadPreferences, sorter: String
    ) = preferences.run {
        if (formatSorting && sortingFields.isNotEmpty()) addOption("-S", sortingFields)
        else if (sorter.isNotEmpty()) addOption("-S", sorter) else {
        }
    }

    @CheckResult
    fun DownloadPreferences.toFormatSorter(): String = connectWithDelimiter(
        this.toVideoFormatSorter(), this.toAudioFormatSorter(), delimiter = ","
    )

    private fun YoutubeDLRequest.addOptionsForAudioDownloads(
        id: String, preferences: DownloadPreferences, playlistUrl: String
    ): YoutubeDLRequest = this.apply {
        with(preferences) {
            addOption("-x")
            if (downloadSubtitle) {
                addOption("--write-subs")

                if (autoSubtitle) {
                    addOption("--write-auto-subs")
                    if (!autoTranslatedSubtitles) {
                        addOption("--extractor-args", "youtube:skip=translated_subs")
                    }
                }
                subtitleLanguage.takeIf { it.isNotEmpty() }?.let { addOption("--sub-langs", it) }
                when (convertSubtitle) {
                    CONVERT_ASS -> addOption("--convert-subs", "ass")
                    CONVERT_SRT -> addOption("--convert-subs", "srt")
                    CONVERT_VTT -> addOption("--convert-subs", "vtt")
                    CONVERT_LRC -> addOption("--convert-subs", "lrc")
                    else -> {}
                }
            }
            if (formatIdString.isNotEmpty()) {
                addOption("-f", formatIdString)
                if (mergeAudioStream) {
                    addOption("--audio-multistreams")
                }
            } else if (convertAudio) {
                when (audioConvertFormat) {
                    CONVERT_MP3 -> {
                        addOption("--audio-format", "mp3")
                    }

                    CONVERT_M4A -> {
                        addOption("--audio-format", "m4a")
                    }
                }
            } else {
                applyFormatSorter(preferences, toAudioFormatSorter())
            }

            if (embedMetadata) {
                addOption("--embed-metadata")
                addOption("--embed-thumbnail")
                addOption("--convert-thumbnails", "jpg")

                if (cropArtwork) {
                    val configFile = context.getConfigFile(id)
                    FileUtil.writeContentToFile(CROP_ARTWORK_COMMAND, configFile)
                    addOption("--config", configFile.absolutePath)
                }
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

    private fun insertInfoIntoDownloadHistory(
        videoInfo: VideoInfo, filePaths: List<String>
    ): List<String> = filePaths.onEach {
        DatabaseUtil.insertInfo(videoInfo.toDownloadedVideoInfo(videoPath = it))
    }

    private fun VideoInfo.toDownloadedVideoInfo(
        id: Int = 0, videoPath: String
    ): DownloadedVideoInfo = this.run {
        DownloadedVideoInfo(
            id = id,
            videoTitle = title,
            videoAuthor = uploader ?: channel ?: uploaderId.toString(),
            videoUrl = webpageUrl ?: originalUrl.toString(),
            thumbnailUrl = thumbnail.toHttpsUrl(),
            videoPath = videoPath,
            extractor = extractorKey
        )
    }

    private fun insertSplitChapterIntoHistory(videoInfo: VideoInfo, filePaths: List<String>) =
        filePaths.onEach {
            DatabaseUtil.insertInfo(
                videoInfo.toDownloadedVideoInfo(videoPath = it).copy(videoTitle = it.getFileName())
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
                videoInfo.originalUrl ?: videoInfo.webpageUrl ?: return Result.failure(
                    Throwable(
                        context.getString(R.string.fetch_info_error_msg)
                    )
                )
            }
            val request = YoutubeDLRequest(url)
            val pathBuilder = StringBuilder()
            val outputBuilder = StringBuilder()

            request.apply {
                addOption("--no-mtime")
//                addOption("-v")
                if (cookies) {
                    enableCookies(userAgentString)
                }
                if (restrictFilenames) {
                    addOption("--restrict-filenames")
                }
                if (proxy) {
                    enableProxy(proxyUrl)
                }
                if (forceIpv4) {
                    addOption("-4")
                }
                if (debug) {
                    addOption("-v")
                }
                if (useDownloadArchive) {
                    val archiveFile = context.getArchiveFile()
                    val archiveFileContent = archiveFile.readText()
                    if (archiveFileContent.contains("${videoInfo.extractor} ${videoInfo.id}")) {
                        return Result.failure(YoutubeDLException(context.getString(R.string.download_archive_error)))
                    } else {
                        useDownloadArchive()
                    }
                }

                if (rateLimit && maxDownloadRate.isNumberInRange(1, 1000000)) {
                    addOption("-r", "${maxDownloadRate}K")
                }

                if (playlistItem != 0 && downloadPlaylist) {
                    addOption("--playlist-items", playlistItem)
                    if (subdirectoryPlaylistTitle && !videoInfo.playlist.isNullOrEmpty()) {
                        outputBuilder.append(PLAYLIST_TITLE_SUBDIRECTORY_PREFIX)
                    }
//                    addOption("--compat-options", "no-youtube-unavailable-videos")
                } else {
                    addOption("--no-playlist")
                }

                if (aria2c) {
                    enableAria2c()
                } else if (concurrentFragments > 1) {
                    addOption("--concurrent-fragments", concurrentFragments)
                }

                if (extractAudio || (videoInfo.vcodec == "none")) {
                    if (privateDirectory) pathBuilder.append(App.privateDownloadDir)
                    else pathBuilder.append(audioDownloadDir)
                    addOptionsForAudioDownloads(
                        id = videoInfo.id,
                        preferences = downloadPreferences,
                        playlistUrl = playlistUrl
                    )
                } else {
                    if (privateDirectory) pathBuilder.append(App.privateDownloadDir)
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
                if (subdirectoryExtractor) {
                    pathBuilder.append("/${videoInfo.extractorKey}")
                }

                if (sdcard) {
                    addOption("-P", context.getSdcardTempDir(videoInfo.id).absolutePath)
                } else {
                    addOption("-P", pathBuilder.toString())
                }


                videoClips.forEach {
                    addOption(
                        "--download-sections", "*%d-%d".format(locale = Locale.US, it.start, it.end)
                    )
                }
                if (newTitle.isNotEmpty()) {
                    addCommands(listOf("--replace-in-metadata", "title", ".+", newTitle))
                }
                if (Build.VERSION.SDK_INT > 23 && !sdcard) addOption(
                    "-P", "temp:" + getExternalTempDir()
                )

                if (splitByChapter) {
                    addOption("-o", OUTPUT_TEMPLATE_CHAPTERS)
                    addOption("--split-chapters")
                }

                val output =
                    if (splitByChapter) {
                        OUTPUT_TEMPLATE_SPLIT
                    } else if (videoClips.isEmpty()) {
                        outputTemplate
                    } else {
                        OUTPUT_TEMPLATE_CLIPS
                    }

                addOption("-o", outputBuilder.append(output).toString())

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
        val fileName = preferences.newTitle.ifEmpty {
            videoInfo.filename ?: videoInfo.requestedDownloads?.firstOrNull()?.filename
            ?: videoInfo.title
        }

        Log.d(TAG, "onFinishDownloading: $fileName")
        if (sdcard) {
            moveFilesToSdcard(
                sdcardUri = sdcardUri, tempPath = context.getSdcardTempDir(videoInfo.id)
            ).onSuccess {
                if (privateMode) {
                    return Result.success(emptyList())
                } else if (splitByChapter) {
                    insertSplitChapterIntoHistory(videoInfo, it)
                } else {
                    insertInfoIntoDownloadHistory(videoInfo, it)
                }
            }
        } else {
            FileUtil.scanFileToMediaLibraryPostDownload(
                title = fileName, downloadDir = downloadPath
            ).run {
                if (privateMode) Result.success(emptyList())
                else Result.success(
                    if (splitByChapter) {
                        insertSplitChapterIntoHistory(videoInfo, this)
                    } else {
                        insertInfoIntoDownloadHistory(videoInfo, this)
                    }
                )
            }
        }
    }

    suspend fun executeCommandInBackground(
        url: String,
        template: CommandTemplate = PreferenceUtil.getTemplate(),
        downloadPreferences: DownloadPreferences = DownloadPreferences(),
    ) {
        downloadPreferences.run {
            val taskId = Downloader.makeKey(url = url, templateName = template.name)
            val notificationId = taskId.toNotificationId()
            val urlList = url.split(Regex("[\n ]")).filter { it.isNotBlank() }

            ToastUtil.makeToastSuspend(context.getString(R.string.start_execute))
            val request = YoutubeDLRequest(urlList).apply {
                commandDirectory.takeIf { it.isNotEmpty() }?.let {
                    addOption("-P", it)
                }
                addOption("--newline")
                if (aria2c) {
                    enableAria2c()
                }
                if (useDownloadArchive) {
                    useDownloadArchive()
                }
                if (restrictFilenames) {
                    addOption("--restrict-filenames")
                }
                addOption(
                    "--config-locations", FileUtil.writeContentToFile(
                        template.template, context.getConfigFile()
                    ).absolutePath
                )
                if (cookies) {
                    enableCookies(userAgentString)
                }
            }

            onProcessStarted()
            withContext(Dispatchers.Main) {
                onTaskStarted(template, url)
            }
            runCatching {
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
                onTaskEnded(template, url, response.out + "\n" + response.err)
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

    private fun checkIfAv1HardwareAccelerated(): Boolean {
        if (PreferenceUtil.containsKey(AV1_HARDWARE_ACCELERATED)) {
            return AV1_HARDWARE_ACCELERATED.getBoolean()
        } else {
            val res = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                false
            } else {
                MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.any { info ->
                    info.supportedTypes.any {
                        it.equals(
                            "video/av01",
                            ignoreCase = true
                        )
                    } && info.isHardwareAccelerated
                }
            }
            AV1_HARDWARE_ACCELERATED.updateBoolean(res)
            return res
        }
    }
}