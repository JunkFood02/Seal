package com.junkfood.seal.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.junkfood.seal.R
import com.yausername.ffmpeg.FFmpeg
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Locates and verifies the on-device FFmpeg engine used by yt-dlp for all post-processing
 * (audio extraction, merging, thumbnails, section downloads).
 *
 * The bundled youtubedl-android library passes `--ffmpeg-location
 * <nativeLibraryDir>/libffmpeg.so` to yt-dlp on every run. That binary is dynamically linked
 * against `libav*.so` libraries that [FFmpeg.init] extracts to
 * `<noBackupFilesDir>/youtubedl-android/packages/ffmpeg/usr/lib`, and those are only found
 * through `LD_LIBRARY_PATH`. Whenever extraction failed (e.g. low storage), has not finished
 * yet, or the binary cannot execute, yt-dlp reports the cryptic
 * "ffmpeg not found. Please install or provide the path using --ffmpeg-location" error.
 *
 * [ensureReady] re-runs the idempotent library initialization, verifies the required files
 * exist and executes a real self-test of the binary before a download starts, so failures
 * surface early with an actionable message instead of during post-processing.
 */
object FfmpegUtil {

    private const val TAG = "FfmpegUtil"

    private const val FFMPEG_BINARY_NAME = "libffmpeg.so"
    private const val FFMPEG_ZIP_NAME = "libffmpeg.zip.so"
    private const val CRITICAL_LIB_NAME = "libavcodec.so"
    private const val SELF_TEST_TIMEOUT_SECONDS = 8L

    enum class Failure {
        /** The app installation does not contain the FFmpeg executable. */
        BINARY_MISSING,

        /** FFmpeg's shared libraries have not been extracted (or extraction was incomplete). */
        LIBS_MISSING,

        /** [FFmpeg.init] threw while (re)extracting the bundled archive. */
        INIT_FAILED,

        /** The binary exists but cannot be executed on this device. */
        SELF_TEST_FAILED,
    }

    sealed class Status {
        data object Ready : Status()

        data class Unavailable(val failure: Failure, val detail: String? = null) : Status()
    }

    @Volatile private var ready = false

    /** Result of the last [ensureReady] invocation, for UI snapshots without running a test. */
    @Volatile private var lastStatus: Status? = null

    fun ffmpegBinary(context: Context): File =
        File(context.applicationInfo.nativeLibraryDir, FFMPEG_BINARY_NAME)

    fun ffmpegZip(context: Context): File =
        File(context.applicationInfo.nativeLibraryDir, FFMPEG_ZIP_NAME)

    /** Directory where [FFmpeg.init] extracts the FFmpeg shared libraries (matches the library's layout). */
    fun ffmpegPackageDir(context: Context): File =
        File(context.noBackupFilesDir, "youtubedl-android/packages/ffmpeg")

    fun ffmpegLibDir(context: Context): File = File(ffmpegPackageDir(context), "usr/lib")

    private fun pythonLibDir(context: Context): File =
        File(context.noBackupFilesDir, "youtubedl-android/packages/python/usr/lib")

    private fun aria2cLibDir(context: Context): File =
        File(context.noBackupFilesDir, "youtubedl-android/packages/aria2c/usr/lib")

    /**
     * Same value the youtubedl-android library exports as `LD_LIBRARY_PATH` for the yt-dlp
     * process, so a passing self-test is equivalent to what yt-dlp itself can execute.
     */
    fun buildLdLibraryPath(vararg dirs: File): String = dirs.joinToString(":") { it.absolutePath }

    fun ldLibraryPath(context: Context): String =
        buildLdLibraryPath(pythonLibDir(context), ffmpegLibDir(context), aria2cLibDir(context))

    /**
     * Structural check of the FFmpeg installation. Pure file-system logic, unit-testable
     * without an Android context. Returns the first failure found, or null when all required
     * files are present.
     */
    internal fun diagnose(binary: File, libDir: File): Failure? {
        if (!binary.exists() || !binary.isFile) return Failure.BINARY_MISSING
        if (!libDir.isDirectory) return Failure.LIBS_MISSING
        if (!File(libDir, CRITICAL_LIB_NAME).exists()) return Failure.LIBS_MISSING
        return null
    }

    /**
     * Runs `libffmpeg.so -version` with the environment yt-dlp receives. Returns the first
     * line of output on success, or a descriptive error message on failure.
     */
    internal fun selfTest(binary: File, ldLibraryPath: String): Result<String> =
        runCatching {
            val process =
                ProcessBuilder(binary.absolutePath, "-version")
                    .redirectErrorStream(true)
                    .apply {
                        environment()["LD_LIBRARY_PATH"] = ldLibraryPath
                        environment().remove("LD_PRELOAD")
                    }
                    .start()
            val finished =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    process.waitFor(SELF_TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                } else {
                    // Timed waitFor needs API 26; ffmpeg -version exits immediately.
                    process.waitFor()
                    true
                }
            if (!finished) {
                process.destroy()
                throw IllegalStateException("timed out after ${SELF_TEST_TIMEOUT_SECONDS}s")
            }
            val output = process.inputStream.bufferedReader().readText()
            if (process.exitValue() != 0) {
                throw IllegalStateException(
                    output.trim().ifEmpty { "exit code ${process.exitValue()}" }
                )
            }
            output.lineSequence().firstOrNull { it.isNotBlank() }?.trim()
                ?: throw IllegalStateException("no output")
        }

    /**
     * Ensures FFmpeg is initialized and executable. Idempotent; cached after the first
     * success. When anything is off, retries [FFmpeg.init] (which is safe to call repeatedly
     * and blocks while a concurrent initialization is still extracting) and re-verifies.
     */
    @JvmStatic
    @Synchronized
    fun ensureReady(context: Context): Status {
        if (ready) return Status.Ready

        var failure = diagnose(ffmpegBinary(context), ffmpegLibDir(context))
        var detail: String? = null

        if (failure != null) {
            // Retry initialization: extracts the archive on first run or after a failure,
            // and waits for an init already in progress on another thread.
            val initResult = runCatching { FFmpeg.init(context) }
            failure = diagnose(ffmpegBinary(context), ffmpegLibDir(context))
            if (failure != null && initResult.isFailure) {
                failure = Failure.INIT_FAILED
                detail =
                    initResult.exceptionOrNull()?.message
                        ?: initResult.exceptionOrNull()?.javaClass?.simpleName
            }
        }

        if (failure == null) {
            selfTest(ffmpegBinary(context), ldLibraryPath(context))
                .onSuccess {
                    ready = true
                    lastStatus = Status.Ready
                    return Status.Ready
                }
                .onFailure {
                    failure = Failure.SELF_TEST_FAILED
                    detail = it.message
                }
        }

        Log.e(TAG, "ffmpeg not ready: $failure detail=$detail")
        return Status.Unavailable(failure!!, detail).also { lastStatus = it }
    }

    /** Snapshot of the last known status without running initialization or a self-test. */
    fun currentStatus(): Status? = lastStatus

    /** Forces re-initialization and a fresh self-test (used by the troubleshooting page). */
    fun forceCheck(context: Context): Status {
        ready = false
        return ensureReady(context)
    }

    /** Human-readable description of [status] for logs and debugging. */
    fun describe(status: Status): String =
        when (status) {
            Status.Ready -> "ready"
            is Status.Unavailable -> status.detail?.let { "${status.failure}: $it" }
                ?: status.failure.name
        }

    /** Localized, user-facing description of [status]. Empty when [Status.Ready]. */
    fun userMessage(context: Context, status: Status): String =
        when (status) {
            Status.Ready -> ""
            is Status.Unavailable ->
                when (status.failure) {
                    Failure.BINARY_MISSING -> context.getString(R.string.ffmpeg_binary_missing)
                    Failure.LIBS_MISSING -> context.getString(R.string.ffmpeg_libs_missing)
                    Failure.INIT_FAILED ->
                        context.getString(R.string.ffmpeg_init_failed, status.detail ?: "")
                    Failure.SELF_TEST_FAILED ->
                        context.getString(R.string.ffmpeg_self_test_failed, status.detail ?: "")
                }
        }

    /** True when yt-dlp's own error text indicates the problem this class detects. */
    fun isMissingFfmpegError(message: String?): Boolean =
        message != null &&
            (message.contains("ffmpeg not found", ignoreCase = true) ||
                message.contains("--ffmpeg-location", ignoreCase = true))
}
