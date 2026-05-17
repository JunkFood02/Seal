package com.junkfood.seal.util

import androidx.room.Room
import com.junkfood.seal.App.Companion.applicationScope
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.database.AppDatabase
import com.junkfood.seal.database.backup.Backup
import com.junkfood.seal.database.backup.BackupUtil.BackupType
import com.junkfood.seal.database.backup.BackupUtil.decodeToBackup
import com.junkfood.seal.database.objects.CommandTemplate
import com.junkfood.seal.database.objects.CookieProfile
import com.junkfood.seal.database.objects.DownloadedVideoInfo
import com.junkfood.seal.database.objects.FolderInfo
import com.junkfood.seal.database.objects.OptionShortcut
import com.junkfood.seal.database.objects.Playlist
import com.junkfood.seal.database.objects.PlaylistVideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseUtil {
    private const val DATABASE_NAME = "app_database"
    private val db = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
    private val dao = db.videoInfoDao()

    fun insertInfo(vararg infoList: DownloadedVideoInfo) {
        applicationScope.launch(Dispatchers.IO) {
            infoList.forEach { dao.insertInfoDistinctByPath(it) }
        }
    }

    init {
        applicationScope.launch {
            getTemplateFlow().collect {
                if (it.isEmpty()) PreferenceUtil.initializeTemplateSample()
            }
        }
    }

    fun getDownloadHistoryFlow() = dao.getDownloadHistoryFlow()

    private suspend fun getDownloadHistory() = dao.getDownloadHistory()

    fun getTemplateFlow() = dao.getTemplateFlow()

    fun getCookiesFlow() = dao.getCookieProfileFlow()

    fun getShortcuts() = dao.getOptionShortcuts()

    suspend fun deleteShortcut(shortcut: OptionShortcut) = dao.deleteShortcut(shortcut)

    suspend fun insertShortcut(shortcut: OptionShortcut) = dao.insertShortcut(shortcut)

    suspend fun getCookieById(id: Int) = dao.getCookieById(id)

    suspend fun deleteCookieProfile(profile: CookieProfile) = dao.deleteCookieProfile(profile)

    suspend fun insertCookieProfile(profile: CookieProfile) = dao.insertCookieProfile(profile)

    suspend fun updateCookieProfile(profile: CookieProfile) = dao.updateCookieProfile(profile)

    suspend fun getTemplateList() = dao.getTemplateList()

    suspend fun getShortcutList() = dao.getShortcutList()

    suspend fun deleteInfoList(infoList: List<DownloadedVideoInfo>, deleteFile: Boolean = false) {
        dao.deleteInfoList(infoList)
        infoList.forEach { info -> if (deleteFile) FileUtil.deleteFile(info.videoPath) }
    }

    suspend fun getInfoById(id: Int): DownloadedVideoInfo = dao.getInfoById(id)

    suspend fun deleteInfoById(id: Int) = dao.deleteInfoById(id)

    suspend fun insertTemplate(commandTemplate: CommandTemplate) =
        dao.insertTemplate(commandTemplate)

    suspend fun updateTemplate(commandTemplate: CommandTemplate) {
        dao.updateTemplate(commandTemplate)
    }

    suspend fun importBackup(backup: Backup, types: Set<BackupType>): Int {
        var cnt = 0
        backup.run {
            if (types.contains(BackupType.DownloadHistory)) {
                val itemList = getDownloadHistory()

                if (!downloadHistory.isNullOrEmpty()) {
                    dao.insertAll(
                        downloadHistory
                            .filterNot { itemList.contains(it) }
                            .map { it.copy(id = 0) }
                            .also { cnt += it.size }
                    )
                }
            }
            if (types.contains(BackupType.CommandTemplate)) {
                if (templates != null) {
                    val templateList = getTemplateList()
                    dao.importTemplates(
                        templateList
                            .filterNot { templateList.contains(it) }
                            .map { it.copy(id = 0) }
                            .also { cnt += it.size }
                    )
                }
            }
            if (types.contains(BackupType.CommandShortcut)) {
                val shortcutList = getShortcutList()
                if (shortcuts != null) {
                    dao.insertAllShortcuts(
                        shortcuts
                            .filterNot { shortcutList.contains(it) }
                            .map { it.copy(id = 0) }
                            .also { cnt += it.size }
                    )
                }
            }
        }
        return cnt
    }

    suspend fun importTemplatesFromJson(json: String): Int {
        json
            .decodeToBackup()
            .onSuccess { backup ->
                return importBackup(
                    backup = backup,
                    types = setOf(BackupType.CommandTemplate, BackupType.CommandShortcut),
                )
            }
            .onFailure { it.printStackTrace() }
        return 0
    }

    suspend fun deleteTemplateById(id: Int) = dao.deleteTemplateById(id)

    suspend fun deleteTemplates(templates: List<CommandTemplate>) = dao.deleteTemplates(templates)

    // Folder operations
    fun getFoldersFlow() = dao.getFoldersFlow()

    suspend fun createFolder(name: String): Long = dao.insertFolder(FolderInfo(name = name))

    suspend fun updateFolder(folder: FolderInfo) = dao.updateFolder(folder)

    suspend fun deleteFolder(folder: FolderInfo) = dao.deleteFolder(folder)

    suspend fun moveVideoToFolder(videoId: Int, folderId: Int?) = dao.updateVideoFolder(videoId, folderId)

    fun getVideosByFolderFlow(folderId: Int) = dao.getVideosByFolderFlow(folderId)

    fun getUnfiledVideosFlow() = dao.getUnfiledVideosFlow()

    fun getFolderItemCount(folderId: Int) = dao.getFolderItemCount(folderId)

    // Playlist operations
    fun getPlaylistsFlow() = dao.getPlaylistsFlow()

    suspend fun createPlaylist(name: String): Long = dao.insertPlaylist(Playlist(name = name))

    suspend fun updatePlaylist(playlist: Playlist) = dao.updatePlaylist(playlist)

    suspend fun deletePlaylist(playlist: Playlist) = dao.deletePlaylist(playlist)

    suspend fun addVideoToPlaylist(playlistId: Int, videoId: Int) {
        if (dao.getPlaylistEntry(playlistId, videoId) == null) {
            dao.insertPlaylistVideo(PlaylistVideoInfo(playlistId = playlistId, videoId = videoId))
        }
    }

    suspend fun removeVideoFromPlaylist(playlistId: Int, videoId: Int) {
        dao.deletePlaylistVideo(PlaylistVideoInfo(playlistId = playlistId, videoId = videoId))
    }

    fun getPlaylistVideosFlow(playlistId: Int) = dao.getPlaylistVideosFlow(playlistId)

    fun getPlaylistItemCount(playlistId: Int) = dao.getPlaylistItemCount(playlistId)

    private const val TAG = "DatabaseUtil"
}
