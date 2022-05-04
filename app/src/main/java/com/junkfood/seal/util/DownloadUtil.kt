package com.junkfood.seal.util

import android.media.MediaScannerConnection
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.dot.HomeFragment
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DownloadUtil {
    class Result(val resultCode: ResultCode, val filePath: String?) {

        companion object {
            fun failure(): Result {
                return Result(ResultCode.EXCEPTION, null)
            }

            fun success(title: String, ext: String): Result {
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
    fun getVideo(url: String, handler: Handler) {
        Thread {
            Looper.prepare()
            if (WIP == 1) {
                Toast.makeText(
                    context,
                    context.getString(R.string.task_running),
                    Toast.LENGTH_SHORT
                ).show()
                return@Thread
            }
            var extractAudio: Boolean
            var createThumbnail: Boolean

            with(PreferenceManager.getDefaultSharedPreferences(context)) {
                extractAudio = getBoolean("audio", false)
                createThumbnail = getBoolean("thumbnail", false)
            }

            Toast.makeText(context, context.getString(R.string.fetching_info), Toast.LENGTH_SHORT)
                .show()
            WIP = 1
            val request = YoutubeDLRequest(url)
            lateinit var ext: String
            lateinit var title: String
            val videoInfo: VideoInfo
            try {
                videoInfo = YoutubeDL.getInstance().getInfo(url)
                title = createFilename(videoInfo.title)
                ext = videoInfo.ext
            } catch (e: Exception) {
                handler.post {
                    Toast.makeText(
                        context,
                        context.getString(R.string.fetch_info_error_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                BaseApplication.createLogFileOnDevice(e)
                Log.e(TAG, "getVideo: ", e)
                WIP = 0
                return@Thread
            }
            if (url.contains("list")) {
                handler.post {
                    Toast.makeText(
                        context,
                        context.getString(R.string.start_download_list),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                request.addOption("-P", "${BaseApplication.downloadDir}/")
                request.addOption("-o", "%(playlist)s/%(title)s.%(ext)s")
                request.buildCommand()
            } else {
                handler.post {
                    Toast.makeText(
                        context,
                        "%s'%s'".format(context.getString(R.string.start_download), title),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                request.addOption("-P", "${BaseApplication.downloadDir}/")
                request.addOption("-o", "$title.%(ext)s")
            }
            if (extractAudio) {
                request.addOption("-x")
                request.addOption("--audio-format", "mp3")
                request.addOption("--audio-quality", "0")
                ext = "mp3"
            }
            if (createThumbnail) {
                if (extractAudio) {
                    request.addOption("--embed-metadata")
                    request.addOption("--embed-thumbnail")
                    request.addOption("--compat-options", "embed-thumbnail-atomicparsley")
                    request.addOption("--parse-metadata", "$title:%(meta_album)s")
                    request.addOption("--add-metadata")
                } else {
                    request.addOption("--write-thumbnail")
                    request.addOption("--convert-thumbnails", "jpg")
                }
            }
            request.addOption("--force-overwrites")
            try {
                YoutubeDL.getInstance().execute(
                    request
                ) { progress: Float, _: Long, s: String ->
                    Log.d(TAG, s)
                    handler.handleMessage(Message().apply {
                        what = HomeFragment.UPDATE_PROGRESS
                        obj = progress
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    Toast.makeText(
                        context,
                        context.getString(R.string.download_error_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                BaseApplication.createLogFileOnDevice(e)
                WIP = 0
                return@Thread
            }
            handler.handleMessage(Message().apply {
                what = HomeFragment.UPDATE_PROGRESS
                obj = 100f
            })
            handler.post {
                Toast.makeText(
                    context,
                    context.getString(R.string.download_success_msg),
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (!url.contains("list")) {
                Log.d(TAG, "${BaseApplication.downloadDir}/$title.$ext")
                MediaScannerConnection.scanFile(
                    context, arrayOf("${BaseApplication.downloadDir}/$title.$ext"),
                    arrayOf(if (ext == "mp3") "audio/*" else "video/*"), null
                )
                handler.handleMessage(Message().apply {
                    what = HomeFragment.FINISH_DOWNLOADING
                    obj = "${BaseApplication.downloadDir}/$title.$ext"
                    arg1 = if (ext == "mp3") 1 else 0
                })
            }
            WIP = 0
        }.start()
    }

    private fun createFilename(title: String): String {
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
            makeToast(context.getString(R.string.task_running))
            return Result.failure()
        }
        WIP = 1

        val extractAudio: Boolean = PreferenceUtil.getValue("extract_audio")
        val createThumbnail: Boolean = PreferenceUtil.getValue("create_thumbnail")
        val request = YoutubeDLRequest(url)
        var ext: String
        val title: String
        val videoInfo: VideoInfo


        makeToast(context.getString(R.string.fetching_info))

        try {
            withContext(Dispatchers.IO) {
                videoInfo = YoutubeDL.getInstance().getInfo(url)
            }
            with(videoInfo) {
                if (this.title.isNullOrEmpty() or this.ext.isNullOrBlank()) throw Exception(
                    "Empty videoinfo"
                )
            }
        } catch (e: Exception) {
            BaseApplication.createLogFileOnDevice(e)
            makeToast(context.resources.getString(R.string.fetch_info_error_msg))
            WIP = 0
            return Result.failure()
        }


        title = createFilename(videoInfo.title)
        ext = videoInfo.ext
        with(request) {
            addOption("-P", "${BaseApplication.downloadDir}/")
            if (url.contains("list")) {
                makeToast(context.getString(R.string.start_download_list))
                addOption("-o", "%(playlist)s/%(title)s.%(ext)s")
            } else {
                makeToast("%s'%s'".format(context.getString(R.string.start_download), title))
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
                withContext(Dispatchers.IO) {
                    YoutubeDL.getInstance().execute(
                        request, progressCallback
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                makeToast(context.getString(R.string.download_error_msg))
                BaseApplication.createLogFileOnDevice(e)
                WIP = 0
                return Result.failure()
            }
        }

        makeToast(context.getString(R.string.download_success_msg))

        if (!url.contains("list")) {
            Log.d(TAG, "${BaseApplication.downloadDir}/$title.$ext")
            MediaScannerConnection.scanFile(
                context, arrayOf("${BaseApplication.downloadDir}/$title.$ext"),
                arrayOf(if (ext == "mp3") "audio/*" else "video/*"), null
            )
            WIP = 0
        }
        return Result.success(title, ext)

    }

    suspend fun updateYtDlp(): String {
        withContext(Dispatchers.IO) {
            try {
                YoutubeDL.getInstance().updateYoutubeDL(context)
                makeToast(context.getString(R.string.yt_dlp_up_to_date))
            } catch (e: Exception) {
                makeToast(context.getString(R.string.yt_dlp_update_fail))
            }
        }
        return YoutubeDL.getInstance().version(context) ?: BaseApplication.ytdlpVersion
    }

    private suspend fun makeToast(text: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }
}