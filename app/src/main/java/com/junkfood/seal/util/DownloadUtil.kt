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
import com.junkfood.seal.ui.home.HomeFragment
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo

object DownloadUtil {
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

}