package com.junkfood.seal.util

import android.util.Log
import com.junkfood.seal.database.DownloadedVideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import com.yausername.youtubedl_android.mapper.VideoInfo
import org.json.JSONObject
import kotlin.math.roundToInt

object DownloadUtilService {
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

    private const val TAG = "DownloadUtilService"

fun getPlaylistSize(playlistSize: String): Int {
        var playlistCount = 1
        val request = YoutubeDLRequest(playlistSize)
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
        }
        return playlistCount
    }

fun fetchVideoInfo(url: String, playlistItem: Int = 1): VideoInfo {
        val videoInfo: VideoInfo = YoutubeDL.getInstance().getInfo(YoutubeDLRequest(url).apply {
            addOption("-R", "1")
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

    suspend fun downloadVideo(
        videoInfo: VideoInfo,
        task: DownloadTask,
        progressCallback: ((Float, Long, String) -> Unit)?
    ): Result {

        val extractAudio: Boolean =  task.settings.extractAudio
        val createThumbnail: Boolean = task.settings.createThumbnail
        val downloadPlaylist: Boolean = task.settings.downloadPlaylist
        val concurrentFragments: Float = task.settings.concurrentFragments
        val url = videoInfo.webpageUrl ?: return Result.failure()
        val request = YoutubeDLRequest(url)
        val id = if (extractAudio) "${url.hashCode()}audio" else url.hashCode().toString()
        val pathBuilder = StringBuilder()

        with(request) {
            addOption("--no-mtime")

            if (extractAudio or (videoInfo.ext.matches(Regex("mp3|m4a|opus")))) {
                pathBuilder.append(task.settings.audioDownloadDir)

                addOption("-x")
                when (task.settings.audioFormat) {
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
                addOption("--parse-metadata", "%(album,title)s:%(meta_album)s")
            } else {
                pathBuilder.append(task.settings.videoDownloadDir)

                val sorter = StringBuilder()
                when (task.settings.videoQuality) {
                    1 -> sorter.append("res:1440")
                    2 -> sorter.append("res:1080")
                    3 -> sorter.append("res:720")
                    4 -> sorter.append("res:480")
                }
                when (task.settings.videoFormat) {
                    1 -> sorter.append(",ext:mp4")
                    2 -> sorter.append(",ext:webm")
                }
                if (sorter.isNotEmpty())
                    addOption("-S", sorter.toString())
            }
            if (concurrentFragments > 0f) {
                addOption("--concurrent-fragments", (concurrentFragments * 16).roundToInt())
            }
            if (createThumbnail) {
                addOption("--write-thumbnail")
                addOption("--convert-thumbnails", "jpg")
            }
            if (!downloadPlaylist)
                addOption("--no-playlist")
            if (task.settings.subdirectory)
                pathBuilder.append("/${videoInfo.extractorKey}/")
            addOption("-P", pathBuilder.toString())
            addOption("-o", "%(title).60s$id.%(ext)s")

            for (s in request.buildCommand())
                Log.d(TAG, s)
            YoutubeDL.getInstance().execute(request, progressCallback)
        }

        val filePaths = FileUtil.scanFileToMediaLibrary(id, pathBuilder.toString())
        if (filePaths != null)
            for (path in filePaths) {
                DatabaseUtil.insertInfo(
                    DownloadedVideoInfo(
                        0,
                        videoInfo.title,
                        if (videoInfo.uploader == null) "null" else videoInfo.uploader,
                        url,
                        TextUtil.urlHttpToHttps(videoInfo.thumbnail ?: ""),
                        path,
                        videoInfo.extractorKey
                    )
                )
            }
        return Result.success(filePaths)
    }

}