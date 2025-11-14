package com.junkfood.seal.util

import android.content.ClipData
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
import java.io.File
import okhttp3.internal.closeQuietly

const val AUDIO_REGEX = "(mp3|aac|opus|m4a)$"
const val THUMBNAIL_REGEX = "\\.(jpg|png)$"
const val SUBTITLE_REGEX = "\\.(lrc|vtt|srt|ass|json3|srv.|ttml)$"
private const val PRIVATE_DIRECTORY_SUFFIX = ".Seal"

object FileUtil {
    fun openFileFromResult(downloadResult: Result<List<String>>) {
        val filePaths = downloadResult.getOrNull()
        if (filePaths.isNullOrEmpty()) return
        openFile(filePaths.first()) {
            ToastUtil.makeToastSuspend(context.getString(R.string.file_unavailable))
        }
    }

    inline fun openFile(path: String, onFailureCallback: (Throwable) -> Unit) =
        path
            .runCatching {
                createIntentForOpeningFile(this)?.run { context.startActivity(this) }
                    ?: throw Exception()
            }
            .onFailure { onFailureCallback(it) }

    private fun createIntentForFile(path: String?): Intent? {
        if (path == null) return null

        val uri =
            path
                .runCatching {
                    DocumentFile.fromSingleUri(context, Uri.parse(path)).run {
                        if (this?.exists() == true) {
                            this.uri
                        } else if (File(this@runCatching).exists()) {
                            FileProvider.getUriForFile(
                                context,
                                context.getFileProvider(),
                                File(this@runCatching),
                            )
                        } else null
                    }
                }
                .getOrNull() ?: return null

        return Intent().apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            data = uri
        }
    }

    fun createIntentForOpeningFile(path: String?): Intent? =
        createIntentForFile(path)?.let {
            it.apply {
                action = (Intent.ACTION_VIEW)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

    fun createIntentForSharingFile(path: String?): Intent? =
        createIntentForFile(path)?.apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, data)
            val mimeType = data?.let { context.contentResolver.getType(it) } ?: "media/*"
            setDataAndType(this.data, mimeType)
            clipData = ClipData(null, arrayOf(mimeType), ClipData.Item(data))
        }

    fun Context.getFileProvider() = "$packageName.provider"

    fun String.getFileSize(): Long =
        this.run {
            val length = File(this).length()
            if (length == 0L) DocumentFile.fromSingleUri(context, Uri.parse(this))?.length() ?: 0L
            else length
        }

    fun String.getFileName(): String =
        this.run {
            File(this).nameWithoutExtension.ifEmpty {
                DocumentFile.fromSingleUri(context, Uri.parse(this))?.name ?: "video"
            }
        }

    fun deleteFile(path: String) =
        path.runCatching {
            if (!File(path).delete()) DocumentFile.fromSingleUri(context, Uri.parse(this))?.delete()
        }

    @CheckResult
    fun scanFileToMediaLibraryPostDownload(title: String, downloadDir: String): List<String> =
        File(downloadDir)
            .walkTopDown()
            .filter { it.isFile && it.absolutePath.contains(title) }
            .map { it.absolutePath }
            .toMutableList()
            .apply {
                MediaScannerConnection.scanFile(context, this.toList().toTypedArray(), null, null)
                removeAll {
                    it.contains(Regex(THUMBNAIL_REGEX)) || it.contains(Regex(SUBTITLE_REGEX))
                }
            }

    fun scanDownloadDirectoryToMediaLibrary(downloadDir: String) =
        File(downloadDir)
            .walkTopDown()
            .filter { it.isFile }
            .map { it.absolutePath }
            .run {
                MediaScannerConnection.scanFile(context, this.toList().toTypedArray(), null, null)
            }

    @CheckResult
    fun moveFilesToSdcard(tempPath: File, sdcardUri: String): Result<List<String>> {
        val uriList = mutableListOf<String>()
        val destDir =
            Uri.parse(sdcardUri).run {
                DocumentsContract.buildDocumentUriUsingTree(
                    this,
                    DocumentsContract.getTreeDocumentId(this),
                )
            }
        val res =
            tempPath.runCatching {
                walkTopDown().forEach {
                    if (it.isDirectory) return@forEach
                    val mimeType =
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension) ?: "*/*"

                    val destUri =
                        DocumentsContract.createDocument(
                            context.contentResolver,
                            destDir,
                            mimeType,
                            it.name,
                        ) ?: return@forEach

                    val inputStream = it.inputStream()
                    val outputStream =
                        context.contentResolver.openOutputStream(destUri) ?: return@forEach
                    inputStream.copyTo(outputStream)
                    inputStream.closeQuietly()
                    outputStream.closeQuietly()
                    uriList.add(destUri.toString())
                }
                uriList
            }
        tempPath.deleteRecursively()
        return res
    }

    fun clearTempFiles(downloadDir: File): Int {
        var count = 0
        downloadDir.walkTopDown().forEach {
            if (it.isFile && !it.isHidden) {
                if (it.delete()) count++
            }
        }
        return count
    }

    fun Context.getConfigDirectory(): File = cacheDir

    fun Context.getConfigFile(suffix: String = "") = File(getConfigDirectory(), "config$suffix.txt")

    fun Context.getCookiesFile() = File(getConfigDirectory(), "cookies.txt")

    fun getExternalTempDir() =
        File(getExternalDownloadDirectory(), "tmp").apply {
            mkdirs()
            createEmptyFile(".nomedia")
        }

    fun Context.getSdcardTempDir(child: String?): File =
        getExternalTempDir().run { child?.let { resolve(it) } ?: this }

    fun Context.getArchiveFile(): File = filesDir.createEmptyFile("archive.txt").getOrThrow()

    fun Context.getInternalTempDir() = File(filesDir, "tmp")

    internal fun getExternalDownloadDirectory() =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Seal")
            .also { it.mkdir() }

    internal fun getExternalPrivateDownloadDirectory() =
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            PRIVATE_DIRECTORY_SUFFIX,
        )

    fun getPicturesSealFile(name: String): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ), "Seal"
        )
        if (!dir.exists()) dir.mkdirs()
        return File(dir, name)
    }

    fun File.createEmptyFile(fileName: String): Result<File> =
        this.runCatching {
                mkdirs()
                resolve(fileName).apply { this@apply.createNewFile() }
            }
            .onFailure { it.printStackTrace() }

    fun writeContentToFile(content: String, file: File): File = file.apply { writeText(content) }

    fun getRealPath(treeUri: Uri): String {
        val path: String = treeUri.path.toString()
        Log.d(TAG, path)
        if (!path.contains("primary:")) {
            ToastUtil.makeToast("This directory is not supported")
            return getExternalDownloadDirectory().absolutePath
        }
        val last: String = path.split("primary:").last()
        return Environment.getExternalStorageDirectory().absolutePath + "/$last"
    }

    private const val TAG = "FileUtil"
}
