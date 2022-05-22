package com.junkfood.seal.util

import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.util.FileUtil.reformatFilename
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        toast(context.getString(R.string.fetching_info))
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
        val request = YoutubeDLRequest(url)
        val filename: String = reformatFilename(videoInfo.title)

        with(request) {
            addOption("-R", "3")
            addOption("-P", "${BaseApplication.downloadDir}/")
            if (url.contains("list")) {
                toast(context.getString(R.string.start_download_list))
                addOption("-o", "%(playlist)s/%(title)s.%(ext)s")
            } else {
                toast(context.getString(R.string.start_download).format(videoInfo.title))
                addOption("-o", "$filename.%(ext)s")
            }

            if (extractAudio or (videoInfo.ext.matches(Regex("mp3|m4a|opus")))) {
//                addOption("-f", "ba")
                addOption("-x")
//                addOption("--audio-format", "mp3")
//                addOption("--audio-quality", "0")
                addOption("--embed-metadata")
                addOption("--embed-thumbnail")
//                addOption("--compat-options", "embed-thumbnail-atomicparsley")
                addOption("--parse-metadata", "%(album,title)s:%(meta_album)s")
            }

            if (createThumbnail) {
                addOption("--write-thumbnail")
                addOption("--convert-thumbnails", "jpg")
            }

            YoutubeDL.getInstance().execute(request, progressCallback)
        }

        toast(context.getString(R.string.download_success_msg))

        if (!url.contains("list")) {
            val filePaths = FileUtil.scanFileToMediaLibrary(filename)
            if (filePaths != null)
                for (path in filePaths) {
                    DatabaseUtil.insertInfo(
                        DownloadedVideoInfo(
                            0,
                            videoInfo.title,
                            videoInfo.uploader ?: "null",
                            url,
                            TextUtil.urlHttpToHttps(videoInfo.thumbnail ?: ""), path
                        )
                    )
                }
            return Result.success(filePaths)
        }
        return Result.success(null)
    }

    suspend fun updateYtDlp(): String {
        withContext(Dispatchers.IO) {
            try {
                YoutubeDL.getInstance().updateYoutubeDL(context)
                toast(context.getString(R.string.yt_dlp_up_to_date))
            } catch (e: Exception) {
                toast(R.string.yt_dlp_update_fail)
            }
        }
        YoutubeDL.getInstance().version(context)?.let {
            BaseApplication.ytdlpVersion = it
            PreferenceUtil.updateString(PreferenceUtil.YT_DLP, it)
        }
        return BaseApplication.ytdlpVersion
    }


    private suspend fun toast(text: String) {
        withContext(Dispatchers.Main) {
            TextUtil.makeToast(text)
        }
    }

    private suspend fun toast(id: Int) {
        withContext(Dispatchers.Main) {
            TextUtil.makeToast(id)
        }
    }

}