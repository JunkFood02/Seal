package com.junkfood.seal.util

import android.util.Log
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DownloadUtil {
    class Result(val resultCode: ResultCode, val filePath: String?) {
        companion object {
            fun failure(): Result {
                WIP = 0
                return Result(ResultCode.EXCEPTION, null)
            }

            fun success(title: String, ext: String): Result {
                WIP = 0
                with(if (ext == "mp3") ResultCode.FINISH_AUDIO else ResultCode.FINISH_VIDEO) {
                    return Result(this, "${BaseApplication.downloadDir}/$title.$ext")
                }
            }
        }
    }

    enum class ResultCode {
        FINISH_VIDEO, FINISH_AUDIO, EXCEPTION
    }

    private const val TAG = "DownloadUtil"
    private var WIP = 0

    private fun reformatFilename(title: String): String {
        val cleanFileName = title.replace("[\\\\><\"|*?'%:#/]".toRegex(), "_")
        var fileName = cleanFileName.trim { it <= ' ' }.replace(" +".toRegex(), " ")
        if (fileName.length > 127) fileName = fileName.substring(0, 127)
        return fileName //+ Date().time
    }


    suspend fun downloadVideo(
        url: String,
        progressCallback: ((Float, Long, String) -> Unit)?
    ): Result {
        if (WIP == 1) {
            toast(context.getString(R.string.task_running))
            return Result.failure()
        }
        WIP = 1

        val extractAudio: Boolean = PreferenceUtil.getValue("extract_audio")
        val createThumbnail: Boolean = PreferenceUtil.getValue("create_thumbnail")
        val request = YoutubeDLRequest(url)
        var ext: String
        val title: String
        val videoInfo: VideoInfo


        toast(context.getString(R.string.fetching_info))

        try {
            videoInfo = YoutubeDL.getInstance().getInfo(url)
            with(videoInfo) {
                if (this.title.isNullOrEmpty() or this.ext.isNullOrBlank()) throw Exception(
                    "Empty videoinfo"
                )
            }
        } catch (e: Exception) {
            FileUtil.createLogFileOnDevice(e)
            toast(context.resources.getString(R.string.fetch_info_error_msg))
            return Result.failure()
        }

        title = reformatFilename(videoInfo.title)
        ext = videoInfo.ext

        with(request) {
            addOption("-P", "${BaseApplication.downloadDir}/")
            if (url.contains("list")) {
                toast(context.getString(R.string.start_download_list))
                addOption("-o", "%(playlist)s/%(title)s.%(ext)s")
            } else {
                toast("%s'%s'".format(context.getString(R.string.start_download), title))
                addOption("-o", "$title.%(ext)s")
            }
            if (extractAudio) {
                addOption("-x")
                addOption("--audio-format", "mp3")
                addOption("--audio-quality", "0")
                ext = "mp3"
            }
            if (createThumbnail) {
                if (extractAudio) {
                    addOption("--embed-metadata")
                    addOption("--embed-thumbnail")
                    addOption("--compat-options", "embed-thumbnail-atomicparsley")
                    addOption("--parse-metadata", "$title:%(meta_album)s")
                    addOption("--add-metadata")
                } else {
                    addOption("--write-thumbnail")
                    addOption("--convert-thumbnails", "jpg")
                }
            }
            addOption("--force-overwrites")

            try {
                YoutubeDL.getInstance().execute(request, progressCallback)
            } catch (e: Exception) {
                e.printStackTrace()
                toast(context.getString(R.string.download_error_msg))
                FileUtil.createLogFileOnDevice(e)
                return Result.failure()
            }
        }

        toast(context.getString(R.string.download_success_msg))

        if (!url.contains("list")) {
            Log.d(TAG, "${BaseApplication.downloadDir}/$title.$ext")
            FileUtil.scanFileToMediaLibrary(title, ext)
        }
        return Result.success(title, ext)

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
        YoutubeDL.getInstance().version(context)?.let { BaseApplication.ytdlpVersion = it }
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