package com.junkfood.seal.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.util.PreferenceUtil.YT_DLP
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.IOException
import java.io.File
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object UpdateUtil {

    private const val OWNER = "JunkFood02"
    private const val REPO = "Seal"
    private const val ARM64 = "arm64-v8a"
    private const val ARM32 = "armeabi-v7a"
    private const val X86 = "x86"
    private const val X64 = "x86_64"
    private const val TAG = "UpdateUtil"

    private val client = OkHttpClient()
    private val requestForLatestRelease =
        Request.Builder().url("https://api.github.com/repos/${OWNER}/${REPO}/releases/latest")
            .build()
    private val jsonFormat = Json { ignoreUnknownKeys = true }

    suspend fun updateYtDlp(): String {
        withContext(Dispatchers.IO) {
            YoutubeDL.getInstance().updateYoutubeDL(context)
            YoutubeDL.getInstance().version(context)?.let {
                PreferenceUtil.updateString(YT_DLP, it)
            }
        }
        return PreferenceUtil.getString(YT_DLP) ?: context.getString(R.string.ytdlp_update)
    }

    private suspend fun getLatestRelease(): LatestRelease {
        return suspendCoroutine { continuation ->
            client.newCall(requestForLatestRelease).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body.string()
                    val latestRelease = jsonFormat.decodeFromString<LatestRelease>(responseData)
                    response.body.close()
                    continuation.resume(latestRelease)
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
            })
        }
    }

    suspend fun checkForUpdate(context: Context = BaseApplication.context): LatestRelease? {
        val currentVersion = context.getCurrentVersion()
        val latestRelease = getLatestRelease()
        val latestVersion = Version(latestRelease.name ?: "")
        return if (currentVersion < latestVersion) latestRelease
        else null
    }

    private fun Context.getCurrentVersion() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName, PackageManager.PackageInfoFlags.of(0)
            ).versionName.toVersion()
        } else {
            packageManager.getPackageInfo(
                packageName, 0
            ).versionName.toVersion()
        }

    private fun String.toVersion() = Version(this)

    private fun Context.getLatestApk() =
        File(getExternalFilesDir("apk"), "latest.apk")

    private fun Context.getFileProvider() = "${packageName}.provider"

    fun installLatestApk(context: Context = BaseApplication.context) = context.apply {
        kotlin.runCatching {
            val contentUri = FileProvider.getUriForFile(this, getFileProvider(), getLatestApk())
            val intent = Intent(Intent.ACTION_VIEW).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, "application/vnd.android.package-archive")
            }
            startActivity(intent)
        }.onFailure { throwable: Throwable ->
            throwable.printStackTrace()
            TextUtil.makeToast(R.string.app_update_failed)
        }
    }

    suspend fun downloadApk(
        context: Context = BaseApplication.context,
        latestRelease: LatestRelease
    ): Flow<DownloadStatus> = withContext(Dispatchers.IO) {
        val apkVersion = context.packageManager.getPackageArchiveInfo(
            context.getLatestApk().absolutePath, 0
        )?.versionName?.toVersion() ?: Version()

        Log.d(TAG, apkVersion.toString())

        if (apkVersion >= Version(latestRelease.tagName.toString())
        ) {
            return@withContext flow<DownloadStatus> { emit(DownloadStatus.Finished(context.getLatestApk())) }
        }

        val isArmArchSupported = Build.SUPPORTED_ABIS.contains(ARM32)
        val is64BitsArchSupported = with(Build.SUPPORTED_ABIS) {
            if (isArmArchSupported) contains(ARM64)
            else contains(X64)
        }
        val preferredArch = if (is64BitsArchSupported) {
            if (isArmArchSupported) ARM64 else X64
        } else {
            if (isArmArchSupported) ARM32 else X86
        }

        val targetUrl = latestRelease.assets?.find {
            return@find it.name?.contains(preferredArch) ?: false
        }?.browserDownloadUrl ?: return@withContext emptyFlow()
        val request = Request.Builder().url(targetUrl).build()
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body
            return@withContext responseBody.downloadFileWithProgress(context.getLatestApk())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emptyFlow()
    }


    private fun ResponseBody.downloadFileWithProgress(saveFile: File): Flow<DownloadStatus> = flow {
        emit(DownloadStatus.Progress(0))

        var deleteFile = true

        try {
            byteStream().use { inputStream ->
                saveFile.outputStream().use { outputStream ->
                    val totalBytes = contentLength()
                    val data = ByteArray(8_192)
                    var progressBytes = 0L

                    while (true) {
                        val bytes = inputStream.read(data)

                        if (bytes == -1) {
                            break
                        }

                        outputStream.channel
                        outputStream.write(data, 0, bytes)
                        progressBytes += bytes
                        emit(DownloadStatus.Progress(percent = ((progressBytes * 100) / totalBytes).toInt()))
                    }

                    when {
                        progressBytes < totalBytes -> throw Exception("missing bytes")
                        progressBytes > totalBytes -> throw Exception("too many bytes")
                        else -> deleteFile = false
                    }
                }
            }

            emit(DownloadStatus.Finished(saveFile))
        } finally {
            if (deleteFile) {
                saveFile.delete()
            }
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    @Serializable
    data class LatestRelease(
        @SerialName("html_url") val htmlUrl: String? = null,
        @SerialName("tag_name") val tagName: String? = null,
        val name: String? = null,
        val draft: Boolean? = null,
        @SerialName("prerelease") val preRelease: Boolean? = null,
        @SerialName("created_at") val createdAt: String? = null,
        @SerialName("published_at") val publishedAt: String? = null,
        val assets: List<AssetsItem>? = null,
        val body: String? = null,
    )

    @Serializable
    data class AssetsItem(
        val name: String? = null,
        @SerialName("content_type") val contentType: String? = null,
        val size: Int? = null,
        @SerialName("download_count") val downloadCount: Int? = null,
        @SerialName("created_at") val createdAt: String? = null,
        @SerialName("updated_at") val updatedAt: String? = null,
        @SerialName("browser_download_url") val browserDownloadUrl: String? = null,
    )

    sealed class DownloadStatus {
        object NotYet : DownloadStatus()
        data class Progress(val percent: Int) : DownloadStatus()
        data class Finished(val file: File) : DownloadStatus()
    }

    class Version(
        versionName: String = "v0.0.0",
    ) {
        var major: Int = 0
            private set
        var minor: Int = 0
            private set
        var patch: Int = 0
            private set
        var build: Int = 0
            private set

        private fun toNumber(): Long {
            return major * MAJOR + minor * MINOR + patch * PATCH + build * BUILD
        }

        companion object {
            private val pattern = Pattern.compile("""v?(\d+)\.(\d+)\.(\d+)(-.*?(\d+))*""")

            private const val BUILD = 1L
            private const val PATCH = 100L
            private const val MINOR = 10_000L
            private const val MAJOR = 1_000_000L
        }

        init {
            val matcher = pattern.matcher(versionName)
            if (matcher.find()) {
                major = matcher.group(1)?.toInt() ?: 0
                minor = matcher.group(2)?.toInt() ?: 0
                patch = matcher.group(3)?.toInt() ?: 0
                build = matcher.group(5)?.toInt() ?: 99
                // Prioritize stable versions
            }
        }

        operator fun compareTo(other: Version) = toNumber().compareTo(other.toNumber())
    }
}