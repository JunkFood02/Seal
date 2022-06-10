package com.junkfood.seal.util

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.core.content.FileProvider
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.BaseApplication.Companion.downloadDir
import java.io.File

/**
 * No `ScopedStorage` forever so feel free to curse me about this
 * And sorry for ugly codes for filename control
 */
object FileUtil {
    fun openFile(downloadResult: DownloadUtil.Result) {
        if (downloadResult.resultCode == DownloadUtil.ResultCode.EXCEPTION) return
        openFileInURI(downloadResult.filePath?.get(0) ?: return)
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
                if (path.contains(Regex("\\.mp3|\\.m4a|\\.opus"))) "audio/*" else "video/*"
            )
        })
    }

    fun openFileInURI(path: String) {
        MediaScannerConnection.scanFile(context, arrayOf(path), null) { _, uri ->
            context.startActivity(Intent().apply {
                action = (Intent.ACTION_VIEW)
                data = uri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    fun createIntentForOpenFile(downloadResult: DownloadUtil.Result): Intent? {
        if (downloadResult.resultCode == DownloadUtil.ResultCode.EXCEPTION) return null
        val path = downloadResult.filePath?.get(0) ?: return null
        return Intent().apply {
            action = (Intent.ACTION_VIEW)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            setDataAndType(
                FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    File(path)
                ),
                if (path.contains(Regex("\\.mp3|\\.m4a|\\.opus"))) "audio/*" else "video/*"
            )
        }
    }

    fun scanFileToMediaLibrary(title: String): ArrayList<String>? {
        val paths = ArrayList<String>()
        val files =
            File(downloadDir).listFiles { _, name ->
                with(name) {
                    contains("$title.") and !contains(Regex("\\.f\\d+?|(\\.jpg)"))
                }
            }
                ?: return null
        for (file in files) {
            val trimmedFile = File(file.absolutePath.replace("$title.", "."))
            if (file.renameTo(trimmedFile))
                paths.add(trimmedFile.absolutePath)
            else paths.add(file.absolutePath)
        }
        MediaScannerConnection.scanFile(
            context, paths.toTypedArray(),
            null, null
        )
        return paths
    }


    fun getRealPath(treeUri: Uri): String {
        val path: String = treeUri.path.toString()
        if (!path.contains("primary:")) {
            TextUtil.makeToast("Download on SD Card is not supported")
            return "SD Card Not Supported"
        }
        val last: String = path.split("primary:").last()
        return "/storage/emulated/0/$last"
    }

    private const val TAG = "FileUtil"
}