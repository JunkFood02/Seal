package com.junkfood.seal.util

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.CheckResult
import androidx.core.content.FileProvider
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.R
import okhttp3.internal.closeQuietly
import java.io.File

const val AUDIO_REGEX = "(mp3|aac|opus|m4a)$"
const val THUMBNAIL_REGEX = "\\.(jpg|png)$"

object FileUtil {
    fun openFile(downloadResult: Result<List<String>>) {
        val filePaths = downloadResult.getOrNull()
        if (filePaths.isNullOrEmpty()) return
        if (Build.VERSION.SDK_INT > 23)
            openFileInURI(filePaths.first())
        else context.startActivity(createIntentForFile(filePaths))
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

    fun createIntentForFile(filePaths: List<String>): Intent? {
        if (filePaths.isEmpty()) return null
        val path = filePaths.first()
        return Intent().apply {
            action = (Intent.ACTION_VIEW)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            data = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                File(path)
            )
        }
    }

    fun scanFileToMediaLibrary(title: String, downloadDir: String): List<String> {
        Log.d(TAG, "scanFileToMediaLibrary: $title")
        val files = mutableListOf<File>()
        val paths = mutableListOf<String>()

        File(downloadDir).walkTopDown()
            .forEach { if (it.isFile && it.path.contains(title)) files.add(it) }

        for (file in files) {
            paths.add(file.absolutePath)
        }

        MediaScannerConnection.scanFile(
            context, paths.toTypedArray(),
            null, null
        )
        paths.removeAll { it.contains(Regex(THUMBNAIL_REGEX)) }
        return paths
    }

    @CheckResult
    fun moveFilesToSdcard(
        tempPath: File = context.getSdcardTempDir(),
        sdcardUri: String
    ): List<String> {
        val uriList = mutableListOf<String>()
        val destDir = Uri.parse(sdcardUri).run {
            DocumentsContract.buildDocumentUriUsingTree(
                this,
                DocumentsContract.getTreeDocumentId(this)
            )
        }
        tempPath.walkTopDown().forEach {
            try {
                val mimeType =
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension) ?: "*/*"

                val destUri = DocumentsContract.createDocument(
                    context.contentResolver,
                    destDir,
                    mimeType,
                    it.name
                ) ?: return@forEach

                val inputStream = it.inputStream()
                val outputStream =
                    context.contentResolver.openOutputStream(destUri) ?: return@forEach
                inputStream.copyTo(outputStream)
                inputStream.closeQuietly()
                outputStream.closeQuietly()
                uriList.add(destUri.toString())
            } catch (th: Throwable) {
                th.printStackTrace()
            }
        }
        tempPath.deleteRecursively()
        return uriList
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

    fun Context.getSdcardTempDir() = File(filesDir, "sdcard_tmp")

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
            TextUtil.makeToast("This directory is not supported")
            return File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                "Seal"
            ).absolutePath
        }
        val last: String = path.split("primary:").last()
        return "/storage/emulated/0/$last"
    }

    private const val TAG = "FileUtil"
}