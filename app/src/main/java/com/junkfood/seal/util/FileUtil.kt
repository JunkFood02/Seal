package com.junkfood.seal.util

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.core.content.FileProvider
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.BaseApplication.Companion.downloadDir
import java.io.File


object FileUtil {
    fun openFile(downloadResult: DownloadUtil.Result) {
        if (downloadResult.resultCode == DownloadUtil.ResultCode.EXCEPTION) return
        openFile(downloadResult.filePath?.get(0) ?: return)
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

    fun scanFileToMediaLibrary(title: String): ArrayList<String>? {
        val paths = ArrayList<String>()
        val files =
            File(downloadDir).listFiles { _, name ->
                name.contains(title) and !name.contains(Regex("\\.f\\d+?")) and !name.contains(
                    ".jpg"
                )
            }
                ?: return null
        for (file in files) {
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
            TextUtil.makeToast("Download on SD Card is not supported")
            return "SD Card Not Supported"
        }
        val last: String = path.split("primary:").last()
        return "/storage/emulated/0/$last"
    }

    private const val TAG = "FileUtil"
}