package com.junkfood.seal.util

import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Build
import androidx.core.content.FileProvider
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object FileUtil {
    fun openFile(downloadResult: DownloadUtil.Result) {
        context.startActivity(Intent().apply {
            action = (Intent.ACTION_VIEW)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            setDataAndType(
                FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    File(downloadResult.filePath.toString())
                ),
                if (downloadResult.resultCode == DownloadUtil.ResultCode.FINISH_AUDIO) "audio/*" else "video/*"
            )
        })
    }

    fun openFile(path: String) {
        context.startActivity(Intent().apply {
            action = (Intent.ACTION_VIEW)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            setDataAndType(
                FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    File(path)
                ),
                if (path.contains(".mp3")) "audio/*" else "video/*"
            )
        })
    }

    fun scanFileToMediaLibrary(title: String, ext: String) {
        MediaScannerConnection.scanFile(
            context, arrayOf("${BaseApplication.downloadDir}/$title.$ext"),
            arrayOf(if (ext == "mp3") "audio/*" else "video/*"), null
        )
    }

    suspend fun createLogFileOnDevice(th: Throwable) {
        withContext(Dispatchers.IO) {
            with(context.getExternalFilesDir(null)) {
                if (this?.canWrite() == true) {
                    val timeNow: String =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                        } else {
                            SimpleDateFormat(
                                "yyyyMMdd_HHmmss",
                                Locale.ENGLISH
                            ).format(Date())
                        }
                    with(File(this, "log$timeNow.txt")) {
                        if (!exists()) createNewFile()
                        val logWriter = FileWriter(this, true)
                        val out = BufferedWriter(logWriter)
                        out.append(th.stackTraceToString())
                        out.close()
                    }
                }
            }

        }
    }

    fun reformatFilename(title: String): String {
        val cleanFileName = title.replace("[\\\\><\"|*?'%:#/]".toRegex(), "_")
        var fileName = cleanFileName.trim { it <= ' ' }.replace(" +".toRegex(), " ")
        if (fileName.length > 127) fileName = fileName.substring(0, 127)
        return fileName
    }
}