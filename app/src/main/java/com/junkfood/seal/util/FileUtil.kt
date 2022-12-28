package com.junkfood.seal.util

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.R
import java.io.File

object FileUtil {
    fun openFile(downloadResult: DownloadUtil.Result) {
        if (downloadResult.resultCode == DownloadUtil.ResultCode.EXCEPTION) return
        if (Build.VERSION.SDK_INT > 23)
            openFileInURI(downloadResult.filePath?.firstOrNull() ?: "")
        else context.startActivity(createIntentForOpenFile(downloadResult))
    }

    fun openFileInURI(path: String) {
        MediaScannerConnection.scanFile(context, arrayOf(path), null) { _, uri ->
            if (uri == null) {
                TextUtil.makeToastSuspend(context.getString(R.string.file_unavailable))
            } else {
                context.startActivity(Intent().apply {
                    action = (Intent.ACTION_VIEW)
                    data = uri
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }
    }

    fun createIntentForOpenFile(downloadResult: DownloadUtil.Result): Intent? {
        if (downloadResult.resultCode == DownloadUtil.ResultCode.EXCEPTION || downloadResult.filePath?.isEmpty() == true) return null
        val path = downloadResult.filePath?.first() ?: return null
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

    fun scanFileToMediaLibrary(title: String, downloadDir: String): ArrayList<String> {
        Log.d(TAG, "scanFileToMediaLibrary: $title")
        val files = ArrayList<File>()
        val paths = ArrayList<String>()

        File(downloadDir).walkTopDown()
            .forEach { if (it.isFile && it.path.contains(title)) files.add(it) }

        for (file in files) {
            paths.add(file.absolutePath)
        }

        MediaScannerConnection.scanFile(
            context, paths.toTypedArray(),
            null, null
        )
        paths.removeAll { it.contains(Regex(".png$")) }
        return paths
    }

    fun clearTempFiles(downloadDir: File): Int {
        var count = 0
        downloadDir.walkTopDown().forEach {
            if (it.isFile) {
                if (it.delete())
                    count++
            }
        }
        return count
    }

    fun Context.getConfigDirectory(): File = cacheDir

    fun Context.getConfigFile(suffix: String = "") =
        File(getConfigDirectory(), "config$suffix.txt")

    fun Context.getCookiesFile(suffix: String = "") =
        File(getConfigDirectory(), "cookies$suffix.txt")

    fun Context.getTempDir() = File(filesDir, "tmp")

    fun File.createEmptyFile(fileName: String) {
        kotlin.runCatching {
            this.mkdir()
            this.resolve(fileName).createNewFile()
        }.onFailure { it.printStackTrace() }
    }

    fun writeContentToFile(content: String, file: File): File {
        file.writeText(content)
        return file
    }

    fun getRealPath(treeUri: Uri): String {
        val path: String = treeUri.path.toString()
        Log.d(TAG, path)
        if (!path.contains("primary:")) {
            //Get the SD ID that is after two "/" and before an ":"
            val sdCardId = path.substring(path.indexOf("/", 1) + 1, path.indexOf(":"))
            Log.d(TAG, "SD Card ID: $sdCardId")
            //Get the path after the SD ID
            val pathAfterSdId =
                path.substring(path.indexOf(sdCardId) + sdCardId.length + 1, path.length)

            val pathExists: Boolean = File("/storage/$sdCardId/$pathAfterSdId").exists()
            Log.d(TAG, "Path exists: $pathExists")

            return "/storage/$sdCardId/$pathAfterSdId"
        }

        val last: String = path.split("primary:").last()
        return "/storage/emulated/0/$last"
    }

    private const val TAG = "FileUtil"

    const val downloadDirKey = "downloadDir"
}