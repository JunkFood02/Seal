package com.junkfood.seal.util

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.MainActivity
import java.io.File

object FileUtil {
    fun openFile(activity: Activity, downloadResult: DownloadUtil.Result) {
            activity.startActivity(Intent().apply {
                action = (Intent.ACTION_VIEW)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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