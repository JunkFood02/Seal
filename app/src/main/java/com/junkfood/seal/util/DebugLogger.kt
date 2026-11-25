package com.junkfood.seal.util

import android.util.Log
import com.junkfood.seal.App
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DebugLogger {
    private const val LOG_FILE_NAME = "seal_debug.log"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val writeMutex = Mutex()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private const val MAX_LOG_SIZE_BYTES = 10 * 1024 * 1024L // 10 MB

    fun getLogFile(): File {
        return File(FileUtil.getExternalTempDir(), LOG_FILE_NAME)
    }

    fun isEnabled(): Boolean {
        return PreferenceUtil.containsKey(DEBUG_LOG_TO_FILE) && DEBUG_LOG_TO_FILE.getBoolean()
    }

    fun log(tag: String, message: String, throwable: Throwable? = null) {
        Log.d(tag, message, throwable)
        if (!isEnabled()) return

        val timestamp = synchronized(dateFormat) { dateFormat.format(Date()) }
        val threadName = Thread.currentThread().name
        val logEntry = buildString {
            append("[$timestamp] [$threadName] [$tag] $message\n")
            if (throwable != null) {
                append(throwable.stackTraceToString())
                append("\n")
            }
        }

        scope.launch {
            writeMutex.withLock {
                runCatching {
                    val file = getLogFile()
                    file.parentFile?.mkdirs()
                    if (file.exists() && file.length() > MAX_LOG_SIZE_BYTES) {
                        val backupFile = File(file.parentFile, "$LOG_FILE_NAME.1")
                        if (backupFile.exists()) backupFile.delete()
                        file.renameTo(backupFile)
                    }
                    file.appendText(logEntry)
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    fun getLogFileSizeString(): String {
        val file = getLogFile()
        if (!file.exists()) return "0 KB"
        val bytes = file.length()
        return when {
            bytes >= 1024 * 1024 -> String.format(Locale.US, "%.2f MB", bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes.toDouble() / 1024)
            else -> "$bytes B"
        }
    }

    suspend fun clearLog(): Boolean {
        return writeMutex.withLock {
            runCatching {
                val file = getLogFile()
                val backupFile = File(file.parentFile, "$LOG_FILE_NAME.1")
                if (backupFile.exists()) backupFile.delete()
                if (file.exists()) file.delete() else true
            }.getOrDefault(false)
        }
    }
}
