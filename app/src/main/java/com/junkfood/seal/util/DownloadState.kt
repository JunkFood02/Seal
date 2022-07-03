package com.junkfood.seal.util

import android.os.Parcelable
import com.junkfood.seal.BaseApplication
import kotlinx.parcelize.Parcelize

@Parcelize
data class DownloadState (
    val progress: Float = 0f,
    val videoTitle: String = "",
    val showVideoCard: Boolean = false,
    val videoThumbnailUrl: String = "",
    val videoAuthor: String = "",
    val progressText: String = "",
    val downloadItemCount: Int = 0,
    val currentIndex: Int = 0,
    val fileNames: List<String>? = null,
): Parcelable

@Parcelize
data class TaskSettings(
    val downloadPlaylist: Boolean =  false,
    val extractAudio: Boolean = false,
    val createThumbnail: Boolean = false,
    val concurrentFragments: Float = 0f,
    val videoDownloadDir: String = "",
    val audioDownloadDir: String = "",
    val subdirectory: Boolean = false,
    val videoQuality: Int = 1,
    val videoFormat: Int = 1,
    val audioFormat: Int = 1,
    val command: String? = null,
): Parcelable {
    fun isCustom(): Boolean {
        return command != null && command.isNotEmpty()
    }
    companion object {
        fun fromSettings():  TaskSettings{
            return TaskSettings(
                downloadPlaylist = PreferenceUtil.getValue(PreferenceUtil.PLAYLIST),
                extractAudio = PreferenceUtil.getValue(PreferenceUtil.EXTRACT_AUDIO),
                createThumbnail = PreferenceUtil.getValue(PreferenceUtil.THUMBNAIL),
                concurrentFragments = PreferenceUtil.getConcurrentFragments(),
                videoDownloadDir = BaseApplication.videoDownloadDir,
                audioDownloadDir = BaseApplication.audioDownloadDir,
                subdirectory = PreferenceUtil.getValue(PreferenceUtil.SUBDIRECTORY),
                videoQuality = PreferenceUtil.getVideoQuality(),
                audioFormat = PreferenceUtil.getAudioFormat(),
                videoFormat = PreferenceUtil.getVideoFormat(),
                command = if (PreferenceUtil.getValue(PreferenceUtil.CUSTOM_COMMAND)) PreferenceUtil.getTemplate() else null,
            )
        }
    }
}

@Parcelize
data class DownloadTask(
    val url: String,
    val startItem: Int = 0,
    val endItem: Int = 0,
    val settings: TaskSettings = TaskSettings.fromSettings(),
): Parcelable

@Parcelize
data class ServiceState (
    val task: DownloadTask? = null,
    val state: DownloadState = DownloadState()
): Parcelable