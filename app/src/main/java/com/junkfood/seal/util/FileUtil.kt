package com.junkfood.seal.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.junkfood.seal.BaseApplication
import java.io.File

object FileUtil {
    fun openFile(activity: Context, downloadResult: DownloadUtil.Result) {
            activity.startActivity(Intent().apply {
                action = (Intent.ACTION_VIEW)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                setDataAndType(
                    FileProvider.getUriForFile(
                        BaseApplication.context,
                        BaseApplication.context.packageName + ".provider",
                        File(downloadResult.filePath.toString())
                    ),
                    if (downloadResult.resultCode == DownloadUtil.ResultCode.FINISH_AUDIO) "audio/*" else "video/*"
                )
            })
    }
}