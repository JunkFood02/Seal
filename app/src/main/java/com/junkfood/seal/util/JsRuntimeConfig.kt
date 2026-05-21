package com.junkfood.seal.util

import android.content.Context
import com.junkfood.seal.App
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.File

enum class EjsStatus {
    DISABLED,
    NOT_INSTALLED,
    AVAILABLE
}

object JsRuntimeConfig {

    const val EXTRACTOR_ARGS_WORKAROUND = "youtube:player_client=tv,ios"

    fun isEjsEnabled(): Boolean = PreferenceUtil.getValue(ENABLE_EJS_RUNTIME)

    fun checkStatus(context: Context = App.context): EjsStatus {
        if (!isEjsEnabled()) return EjsStatus.DISABLED
        return if (getQuickJsBinaryPath(context).exists()) EjsStatus.AVAILABLE
        else EjsStatus.NOT_INSTALLED
    }

    fun getQuickJsBinaryPath(context: Context = App.context): File {
        val nativeDir = File(context.applicationInfo.nativeLibraryDir)
        val qjsFile = File(nativeDir, "libqjs.so")
        if (qjsFile.exists() && qjsFile.canExecute()) {
            return qjsFile
        }
        val cachedQjs = File(context.cacheDir, "qjs")
        if (cachedQjs.exists() && cachedQjs.canExecute()) {
            return cachedQjs
        }
        if (qjsFile.exists()) {
            qjsFile.copyTo(cachedQjs, overwrite = true)
            cachedQjs.setExecutable(true)
            return cachedQjs
        }
        return qjsFile
    }

    fun applyJsRuntimeConfig(
        request: YoutubeDLRequest,
        context: Context = App.context,
    ) {
        if (isEjsEnabled()) {
            val qjsPath = getQuickJsBinaryPath(context)
            if (qjsPath.exists()) {
                request.addOption("--js-runtimes", "quickjs:${qjsPath.absolutePath}")
                request.addOption("--remote-components", "ejs:github")
                request.addOption("--extractor-args", EXTRACTOR_ARGS_WORKAROUND)
            } else {
                request.addOption("--extractor-args", EXTRACTOR_ARGS_WORKAROUND)
            }
        } else {
            request.addOption("--extractor-args", EXTRACTOR_ARGS_WORKAROUND)
        }
    }
}