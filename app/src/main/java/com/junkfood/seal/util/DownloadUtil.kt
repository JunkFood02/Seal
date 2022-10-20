package com.junkfood.seal.util

import android.util.Log
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
import com.junkfood.seal.util.PreferenceUtil.CUSTOM_PATH
import com.junkfood.seal.util.PreferenceUtil.MAX_FILE_SIZE
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
import org.json.JSONObject
import kotlin.math.roundToInt

object DownloadUtil {
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

    data class PlaylistInfo(
        val url: String = "",
        val size: Int = 0,
        val title: String = ""
    )

    fun getPlaylistInfo(playlistURL: String): PlaylistInfo {
        val downloadPlaylist: Boolean = PreferenceUtil.getValue(PreferenceUtil.PLAYLIST)
        var playlistCount = 1
        var playlistTitle = "Unknown"
        if (downloadPlaylist) {
            TextUtil.makeToastSuspend(context.getString(R.string.fetching_playlist_info))
            val request = YoutubeDLRequest(playlistURL)
            with(request) {
                addOption("--flat-playlist")
                addOption("-J")
                addOption("-R", "1")
                addOption("--socket-timeout", "5")
            }
            for (s in request.buildCommand())
                Log.d(TAG, s)
            val resp: YoutubeDLResponse = YoutubeDL.getInstance().execute(request, null)
            val jsonObj = JSONObject(resp.out)
            val tp: String = jsonObj.getString("_type")
            if (tp == "playlist") {
                playlistCount = jsonObj.getInt("playlist_count")
                playlistTitle = jsonObj.getString("title")
            }
        }
        return PlaylistInfo(playlistURL, playlistCount, playlistTitle)
    }

    fun fetchVideoInfo(url: String, playlistItem: Int = 0): VideoInfo {
        TextUtil.makeToastSuspend(context.getString(R.string.fetching_info))
        val videoInfo: VideoInfo = YoutubeDL.getInstance().getInfo(YoutubeDLRequest(url).apply {
            addOption("-R", "1")
            if (playlistItem != 0)
                addOption("--playlist-items", playlistItem)
            addOption("--socket-timeout", "5")
        })
        with(videoInfo) {
            if (title.isNullOrEmpty() or ext.isNullOrEmpty()) {
                throw Exception("Empty videoinfo")
            }
        }
        return videoInfo
    }

    fun downloadVideo(
        videoInfo: VideoInfo,
        playlistInfo: PlaylistInfo,
        playlistItem: Int = 0,
        progressCallback: ((Float, Long, String) -> Unit)?
    ): Result {

        val extractAudio: Boolean =
            PreferenceUtil.getValue(PreferenceUtil.EXTRACT_AUDIO) or (videoInfo.ext.matches(Regex("mp3|m4a|opus")))
        val createThumbnail: Boolean = PreferenceUtil.getValue(PreferenceUtil.THUMBNAIL)
        val downloadPlaylist: Boolean = PreferenceUtil.getValue(PreferenceUtil.PLAYLIST)
        val subdirectory: Boolean = PreferenceUtil.getValue(SUBDIRECTORY)
        val customPath: Boolean = PreferenceUtil.getValue(CUSTOM_PATH)
        val embedSubtitle: Boolean = PreferenceUtil.getValue(SUBTITLE)
        val concurrentFragments: Float = PreferenceUtil.getConcurrentFragments()
        val maxFileSize = PreferenceUtil.getString(MAX_FILE_SIZE, "")
        val sponsorBlock = PreferenceUtil.getValue(SPONSORBLOCK)
        val cookies = PreferenceUtil.getValue(COOKIES)
        val aria2c = PreferenceUtil.getValue(ARIA2C)
        val rateLimit = PreferenceUtil.getValue(RATE_LIMIT)
        val maxDownloadRate = PreferenceUtil.getMaxDownloadRate()
        val url = playlistInfo.url.ifEmpty {
            videoInfo.webpageUrl ?: return Result.failure()
        }
        val request = YoutubeDLRequest(url)
        val pathBuilder = StringBuilder()

        with(request) {
            addOption("--no-mtime")
            if (cookies) {
                FileUtil.writeContentToFile(
                    PreferenceUtil.getCookies(),
                    context.getCookiesFile()
                )
                addOption("--cookies", context.getCookiesFile().absolutePath)
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

            if (extractAudio) {
                pathBuilder.append(audioDownloadDir)

                addOption("-x")
                when (PreferenceUtil.getAudioFormat()) {
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
                FileUtil.writeContentToFile(
                    """--ppa "ffmpeg: -c:v png -vf crop=\"'if(gt(ih,iw),iw,ih)':'if(gt(iw,ih),ih,iw)'\""""",
                    context.getConfigFile()
                )
                addOption("--config", context.getConfigFile().absolutePath)
                if (playlistInfo.url.isNotEmpty()) {
                    addOption("--parse-metadata", "%(album,playlist,title)s:%(meta_album)s")
                    addOption("--parse-metadata", "%(track_number,playlist_index)d:%(meta_track)s")
                } else
                    addOption("--parse-metadata", "%(album,title)s:%(meta_album)s")

            } else {
                pathBuilder.append(videoDownloadDir)

                val sorter = StringBuilder()
                if (maxFileSize.isNumberInRange(1, 4096)) {
                    sorter.append("size:${maxFileSize}M,")
                }
                when (PreferenceUtil.getVideoFormat()) {
                    1 -> sorter.append("ext,")
                    2 -> sorter.append("ext:webm,")
                }
                when (PreferenceUtil.getVideoResolution()) {
                    1 -> sorter.append("res:2160")
                    2 -> sorter.append("res:1440")
                    3 -> sorter.append("res:1080")
                    4 -> sorter.append("res:720")
                    5 -> sorter.append("res:480")
                    6 -> sorter.append("res:360")
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
                    addOption("--sponsorblock-remove", PreferenceUtil.getSponsorBlockCategories())
                }
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
            addOption("-P", "temp:" + context.getTempDir())
            if (customPath)
                addOption(
                    "-o",
                    PreferenceUtil.getOutputPathTemplate() + "%(title).100s [%(id)s].%(ext)s"
                )
            else
                addOption("-o", "%(title).100s [%(id)s].%(ext)s")

            for (s in request.buildCommand())
                Log.d(TAG, s)
        }
        YoutubeDL.getInstance().execute(request, videoInfo.id, progressCallback)
        if (PreferenceUtil.getValue(PRIVATE_MODE)) {
            return Result.success(null)
        }
        val filePaths = FileUtil.scanFileToMediaLibrary(videoInfo.id, pathBuilder.toString())
        for (path in filePaths) {
            DatabaseUtil.insertInfo(
                DownloadedVideoInfo(
                    0,
                    videoInfo.title,
                    if (videoInfo.uploader == null) "null" else videoInfo.uploader,
                    videoInfo.webpageUrl ?: url,
                    TextUtil.urlHttpToHttps(videoInfo.thumbnail ?: ""),
                    path,
                    videoInfo.extractorKey
                )
            )
        }
        return Result.success(filePaths)


    }


}