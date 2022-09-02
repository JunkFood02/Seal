package com.junkfood.seal.util

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.junkfood.seal.BaseApplication.Companion.context
import java.io.File

/**
 * Sorry for ugly codes for filename control
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
        if (downloadResult.resultCode == DownloadUtil.ResultCode.EXCEPTION || downloadResult.filePath?.isEmpty() == true) return null
        val path = downloadResult.filePath?.first() ?: return null
        return Intent().apply {
            action = Intent.ACTION_VIEW
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

    fun scanFileToMediaLibrary(title: String, downloadDir: String): ArrayList<String>? {
        val files = ArrayList<File>()
        val paths = ArrayList<String>()

        /*Files.find(
            Paths.get(downloadDir),
            Integer.MAX_VALUE,
            { path: Path, attributes: BasicFileAttributes ->
                attributes.isRegularFile && path.toString().contains(title)
            }).forEach { files.add(it.toFile()) }*/

        File(downloadDir).walkTopDown()
            .forEach { if (it.isFile && it.path.contains(title)) files.add(it) }

        for (file in files) {
/*            val trimmedFile = File(file.absolutePath.replace("$title.", "."))
            if (file.renameTo(trimmedFile))
                paths.add(trimmedFile.absolutePath)
            else */
            paths.add(file.absolutePath)
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