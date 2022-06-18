package com.junkfood.seal.util

import android.util.Log
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.BaseApplication.Companion.downloadDir
import com.junkfood.seal.R
import com.junkfood.seal.database.DownloadedVideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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

    suspend fun fetchVideoInfo(url: String): VideoInfo {
        TextUtil.makeToastSuspend(context.getString(R.string.fetching_info))
        val videoInfo: VideoInfo = YoutubeDL.getInstance().getInfo(YoutubeDLRequest(url).apply {
            addOption("-R", "1")
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
        url: String,
        videoInfo: VideoInfo,
        progressCallback: ((Float, Long, String) -> Unit)?
    ): Result {

        val extractAudio: Boolean = PreferenceUtil.getValue(PreferenceUtil.EXTRACT_AUDIO)
        val createThumbnail: Boolean = PreferenceUtil.getValue(PreferenceUtil.THUMBNAIL)
        val downloadPlaylist: Boolean = PreferenceUtil.getValue(PreferenceUtil.PLAYLIST)
        val concurrentFragments: Float = PreferenceUtil.getConcurrentFragments()
        val request = YoutubeDLRequest(url)
        val id = if (extractAudio) "${url.hashCode()}audio" else url.hashCode().toString()

        with(request) {
            addOption("--no-mtime")
            addOption("-P", "$downloadDir/")
            addOption("-o", "%(title).60s$id.%(ext)s")
            if (downloadPlaylist)
                addOption("--yes-playlist")
            else
                addOption("--no-playlist")

            if (extractAudio or (videoInfo.ext.matches(Regex("mp3|m4a|opus")))) {
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
                addOption("--parse-metadata", "%(album,title)s:%(meta_album)s")
            } else {
                val sorter = StringBuilder()
                when (PreferenceUtil.getVideoQuality()) {
                    1 -> sorter.append("res:1440")
                    2 -> sorter.append("res:1080")
                    3 -> sorter.append("res:720")
                    4 -> sorter.append("res:480")
                }
                when (PreferenceUtil.getVideoFormat()) {
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
            for (s in request.buildCommand())
                Log.d(TAG, s)
            YoutubeDL.getInstance().execute(request, progressCallback)
        }

        val filePaths = FileUtil.scanFileToMediaLibrary(id)
        if (filePaths != null)
            for (path in filePaths) {
                DatabaseUtil.insertInfo(
                    DownloadedVideoInfo(
                        0,
                        if (filePaths.size > 1) File(path).nameWithoutExtension else videoInfo.title,
                        videoInfo.uploader ?: "null",
                        url,
                        TextUtil.urlHttpToHttps(videoInfo.thumbnail ?: ""),
                        path,
                        videoInfo.extractorKey
                    )
                )
            }
        return Result.success(filePaths)
    }

    suspend fun updateYtDlp(): String {
        withContext(Dispatchers.IO) {
            try {
                YoutubeDL.getInstance().updateYoutubeDL(context)
                TextUtil.makeToastSuspend(context.getString(R.string.yt_dlp_up_to_date))
            } catch (e: Exception) {
                TextUtil.makeToastSuspend(context.getString(R.string.yt_dlp_update_fail))
            }
        }
        YoutubeDL.getInstance().version(context)?.let {
            BaseApplication.ytdlpVersion = it
            PreferenceUtil.updateString(PreferenceUtil.YT_DLP, it)
        }
        return BaseApplication.ytdlpVersion
    }


}