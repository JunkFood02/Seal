package com.junkfood.seal.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.junkfood.seal.BaseApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
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

    private val client = OkHttpClient()
    private val requestForLatestRelease =
        Request.Builder().url("https://api.github.com/repos/${OWNER}/${REPO}/releases").build()


    private suspend fun getLatestRelease(): LatestRelease {
        return suspendCoroutine { continuation ->
            client.newCall(requestForLatestRelease).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body.string()
                    val latestRelease = Json.decodeFromString<LatestRelease>(responseData)
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
        kotlin.runCatching {
            val currentVersion = context.getCurrentVersion()
            val latestRelease = getLatestRelease()
            val latestVersion = Version(latestRelease.name ?: "")
            if (currentVersion < latestVersion)
                latestRelease
            else null
        }.onSuccess { latestRelease ->
            return latestRelease
        }.onFailure { throwable ->
            throwable.printStackTrace()
        }
        return null
    }

    private fun Context.getCurrentVersion() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            ).versionName.toVersion()
        } else {
            packageManager.getPackageInfo(
                packageName,
                0
            ).versionName.toVersion()
        }

    private fun String.toVersion() = Version(this)

    suspend fun downloadApk(latestRelease: LatestRelease) {
        val is64BitsArchSupported = Build.SUPPORTED_ABIS.contains(ARM64)
        val targetUrl = latestRelease.assets?.find {
            it.name?.contains(if (is64BitsArchSupported) ARM64 else ARM32) ?: false
        }?.browserDownloadUrl ?: return
        val request = Request.Builder().url(targetUrl).build()
        val response = client.newCall(request).execute()
        val responseBody = response.body
        response.close()

    }


    fun ResponseBody.downloadFileWithProgress(saveFile: File): Flow<Download> =
        flow {
            emit(Download.Progress(0))

            // flag to delete file if download errors or is cancelled
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

                            emit(Download.Progress(percent = ((progressBytes * 100) / totalBytes).toInt()))
                        }

                        when {
                            progressBytes < totalBytes ->
                                throw Exception("missing bytes")
                            progressBytes > totalBytes ->
                                throw Exception("too many bytes")
                            else ->
                                deleteFile = false
                        }
                    }
                }

                emit(Download.Finished(saveFile))
            } finally {
                // check if download was successful

                if (deleteFile) {
                    saveFile.delete()
                }
            }
        }.flowOn(Dispatchers.IO).distinctUntilChanged()

    data class LatestRelease(
        @SerialName("html_url")
        val htmlUrl: String? = null,
        @SerialName("tag_name")
        val tagName: String? = null,
        val name: String? = null,
        val draft: Boolean? = null,
        @SerialName("prerelease")
        val preRelease: Boolean? = null,
        @SerialName("created_at")
        val createdAt: String? = null,
        @SerialName("published_at")
        val publishedAt: String? = null,
        val assets: List<AssetsItem>? = null,
        val body: String? = null,
    )

    data class AssetsItem(
        val name: String? = null,
        @SerialName("content_type")
        val contentType: String? = null,
        val size: Int? = null,
        @SerialName("download_count")
        val downloadCount: Int? = null,
        @SerialName("created_at")
        val createdAt: String? = null,
        @SerialName("updated_at")
        val updatedAt: String? = null,
        @SerialName("browser_download_url")
        val browserDownloadUrl: String? = null,
    )

    sealed class Download {
        object NotYet : Download()
        data class Progress(val percent: Int) : Download()
        data class Finished(val file: File) : Download()
    }

    class Version(
        versionName: String,
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
                build = matcher.group(4)?.toInt() ?: 0
            }
        }

        operator fun compareTo(other: Version) = toNumber().compareTo(other.toNumber())
    }
}