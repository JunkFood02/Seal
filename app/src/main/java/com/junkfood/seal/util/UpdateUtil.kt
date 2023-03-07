package com.junkfood.seal.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.junkfood.seal.App
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.getInt
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

    private val requestForReleases =
        Request.Builder().url("https://api.github.com/repos/${OWNER}/${REPO}/releases")
            .build()

    private const val ytdlpNightlyBuildRelease =
        "https://api.github.com/repos/yt-dlp/yt-dlp-nightly-builds/releases/latest"

    private val jsonFormat = Json { ignoreUnknownKeys = true }

    suspend fun updateYtDlp(): YoutubeDL.UpdateStatus? =
        withContext(Dispatchers.IO) {
            YoutubeDL.getInstance().updateYoutubeDL(
                context,
                if (YT_DLP_NIGHTLY.getBoolean()) ytdlpNightlyBuildRelease else null
            ).apply {
                if (this == YoutubeDL.UpdateStatus.DONE)
                    YoutubeDL.getInstance().version(context)?.let {
                        PreferenceUtil.encodeString(YT_DLP, it)
                    }
            }
        }


    private suspend fun getLatestRelease(): LatestRelease {
        return suspendCoroutine { continuation ->
            client.newCall(requestForReleases).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body.string()
//                    val latestRelease = jsonFormat.decodeFromString<LatestRelease>(responseData)
                    val releaseList =
                        jsonFormat.decodeFromString<List<LatestRelease>>(responseData)
                    val latestRelease =
                        releaseList.filter { if (UPDATE_CHANNEL.getInt() == STABLE) it.name.toVersion() is Version.Stable else true }
                            .maxByOrNull { it.name.toVersion() }
                            ?: throw Exception("null response")
                    releaseList.sortedBy { it.name.toVersion() }.forEach {
                        Log.d(TAG, it.tagName.toString())
                    }
                    response.body.close()
                    continuation.resume(latestRelease)
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
            })
        }
    }

    suspend fun checkForUpdate(context: Context = App.context): LatestRelease? {
        val currentVersion = context.getCurrentVersion()
        val latestRelease = getLatestRelease()
        val latestVersion = latestRelease.name.toVersion()
        return if (currentVersion < latestVersion) latestRelease
        else null
    }

    private fun Context.getCurrentVersion(): Version =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName, PackageManager.PackageInfoFlags.of(0)
            ).versionName.toVersion()
        } else {
            packageManager.getPackageInfo(
                packageName, 0
            ).versionName.toVersion()
        }


    private fun Context.getLatestApk() =
        File(getExternalFilesDir("apk"), "latest.apk")

    private fun Context.getFileProvider() = "${packageName}.provider"

    fun installLatestApk(context: Context = App.context) = context.apply {
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
            ToastUtil.makeToast(R.string.app_update_failed)
        }
    }

    suspend fun deleteOutdatedApk(
        context: Context = App.context,
    ) = context.runCatching {
        val apkFile = getLatestApk()
        if (apkFile.exists()) {
            val apkVersion = context.packageManager.getPackageArchiveInfo(
                apkFile.absolutePath, 0
            )?.versionName.toVersion()
            if (apkVersion <= context.getCurrentVersion()) {
                apkFile.delete()
            }
        }
    }


    suspend fun downloadApk(
        context: Context = App.context,
        latestRelease: LatestRelease
    ): Flow<DownloadStatus> = withContext(Dispatchers.IO) {
        val apkVersion = context.packageManager.getPackageArchiveInfo(
            context.getLatestApk().absolutePath, 0
        )?.versionName.toVersion()

        Log.d(TAG, apkVersion.toString())

        if (apkVersion >= latestRelease.name.toVersion()) {
            return@withContext flow<DownloadStatus> { emit(DownloadStatus.Finished(context.getLatestApk())) }
        }

        val abiList = Build.SUPPORTED_ABIS
        val preferredArch = abiList.firstOrNull() ?: return@withContext emptyFlow()

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

    private val pattern = Pattern.compile("""v?(\d+)\.(\d+)\.(\d+)(-(\w+)\.(\d+))?""")
    private val EMPTY_VERSION = Version.Stable()

    fun String?.toVersion(): Version = this?.run {
        val matcher = pattern.matcher(this)
        if (matcher.find()) {
            val major = matcher.group(1)?.toInt() ?: 0
            val minor = matcher.group(2)?.toInt() ?: 0
            val patch = matcher.group(3)?.toInt() ?: 0
            val buildNumber = matcher.group(6)?.toInt() ?: 0
            when (matcher.group(5)) {
                "beta" -> Version.Beta(major, minor, patch, buildNumber)
                "rc" -> Version.ReleaseCandidate(major, minor, patch, buildNumber)
                else -> Version.Stable(major, minor, patch)
            }
        } else EMPTY_VERSION
    } ?: EMPTY_VERSION


    sealed class Version(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val build: Int = 0
    ) : Comparable<Version> {
        companion object {
            private const val BUILD = 1L
            private const val PATCH = 100L
            private const val MINOR = 10_000L
            private const val MAJOR = 1_000_000L
        }

        abstract fun toVersionName(): String
        abstract fun toNumber(): Long

        class Beta(versionMajor: Int, versionMinor: Int, versionPatch: Int, versionBuild: Int) :
            Version(versionMajor, versionMinor, versionPatch, versionBuild) {
            override fun toVersionName(): String =
                "${major}.${minor}.${patch}-beta.$build"

            override fun toNumber(): Long =
                major * MAJOR + minor * MINOR + patch * PATCH + build * BUILD

        }

        class Stable(versionMajor: Int = 0, versionMinor: Int = 0, versionPatch: Int = 0) :
            Version(versionMajor, versionMinor, versionPatch) {
            override fun toVersionName(): String =
                "${major}.${minor}.${patch}"

            override fun toNumber(): Long =
                major * MAJOR + minor * MINOR + patch * PATCH + build * BUILD + 50
            // Prioritize stable versions

        }

        class ReleaseCandidate(
            versionMajor: Int,
            versionMinor: Int,
            versionPatch: Int,
            versionBuild: Int
        ) :
            Version(versionMajor, versionMinor, versionPatch, versionBuild) {
            override fun toVersionName(): String =
                "${major}.${minor}.${patch}-rc.$build"

            override fun toNumber(): Long =
                major * MAJOR + minor * MINOR + patch * PATCH + build * BUILD + 25
        }

        override operator fun compareTo(other: Version): Int =
            this.toNumber().compareTo(other.toNumber())

    }
}