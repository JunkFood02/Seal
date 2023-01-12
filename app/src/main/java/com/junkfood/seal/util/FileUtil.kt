package com.junkfood.seal.util

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.CheckResult
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.R
import okhttp3.internal.closeQuietly
import java.io.File

const val AUDIO_REGEX = "(mp3|aac|opus|m4a)$"
const val THUMBNAIL_REGEX = "\\.(jpg|png)$"
const val SUBTITLE_REGEX = "\\.(lrc|vtt|srt|ass|json3|srv.|ttml)$"

object FileUtil {
    fun openFileFromResult(downloadResult: Result<List<String>>) {
        val filePaths = downloadResult.getOrNull()
        if (filePaths.isNullOrEmpty()) return
        openFile(filePaths.first())
    }

    fun openFile(path: String) =
        path.runCatching {
            createIntentForFile(this)?.run { context.startActivity(this) } ?: throw Exception()
        }.onFailure {
            TextUtil.makeToastSuspend(context.getString(R.string.file_unavailable))
        }


    fun createIntentForFile(path: String): Intent? {

        val uri = path.runCatching {
            DocumentFile.fromSingleUri(context, Uri.parse(path)).run {
                if (this?.exists() == true) {
                    this.uri
                } else if (File(this@runCatching).exists()) {
                    FileProvider.getUriForFile(
                        context,
                        context.packageName + ".provider",
                        File(this@runCatching)
                    )
                } else null
            }
        }.getOrNull() ?: return null

        return Intent().apply {
            action = (Intent.ACTION_VIEW)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            data = uri
        }
    }

    fun String.getFileSize(): Long = this.run {
        val length = File(this).length()
        if (length == 0L)
            DocumentFile.fromSingleUri(context, Uri.parse(this))?.length() ?: 0L
        else length
    }

    fun deleteFile(path: String) =
        path.runCatching {
            if (!File(path).delete())
                DocumentFile.fromSingleUri(context, Uri.parse(this))?.delete()
        }

    fun scanFileToMediaLibraryPostDownload(title: String, downloadDir: String): List<String> {
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
        paths.removeAll { it.contains(Regex(THUMBNAIL_REGEX)) || it.contains(Regex(SUBTITLE_REGEX)) }
        return paths
    }

    fun scanDownloadDirectoryToMediaLibrary(downloadDir: String) =
        File(downloadDir).walkTopDown().filter { it.isFile }.map { it.absolutePath }.run {
            MediaScannerConnection.scanFile(
                context, this.toList().toTypedArray(),
                null, null
            )
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
            if (it.isDirectory) return@forEach
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