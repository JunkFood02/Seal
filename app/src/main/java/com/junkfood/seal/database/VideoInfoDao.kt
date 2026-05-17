package com.junkfood.seal.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.junkfood.seal.database.objects.CommandTemplate
import com.junkfood.seal.database.objects.CookieProfile
import com.junkfood.seal.database.objects.DownloadedVideoInfo
import com.junkfood.seal.database.objects.FolderInfo
import com.junkfood.seal.database.objects.OptionShortcut
import com.junkfood.seal.database.objects.Playlist
import com.junkfood.seal.database.objects.PlaylistVideoInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoInfoDao {

    @Insert suspend fun insert(info: DownloadedVideoInfo)

    @Insert suspend fun insertAll(infoList: List<DownloadedVideoInfo>)

    @Query("select * from DownloadedVideoInfo")
    fun getDownloadHistoryFlow(): Flow<List<DownloadedVideoInfo>>

    @Query("select * from DownloadedVideoInfo")
    suspend fun getDownloadHistory(): List<DownloadedVideoInfo>

    @Query("select * from DownloadedVideoInfo where id=:id")
    suspend fun getInfoById(id: Int): DownloadedVideoInfo

    @Query("DELETE FROM DownloadedVideoInfo WHERE id = :id") suspend fun deleteInfoById(id: Int)

    @Query("DELETE FROM DownloadedVideoInfo WHERE videoPath = :path")
    suspend fun deleteInfoByPath(path: String)

    @Query("select * from DownloadedVideoInfo where videoPath = :path")
    suspend fun getInfoByPath(path: String): DownloadedVideoInfo?

    @Transaction
    suspend fun insertInfoDistinctByPath(
        videoInfo: DownloadedVideoInfo,
        path: String = videoInfo.videoPath,
    ) {
        if (getInfoByPath(path) == null) insert(videoInfo)
    }

    @Delete suspend fun deleteInfo(vararg info: DownloadedVideoInfo)

    @Delete @Transaction suspend fun deleteInfoList(idList: List<DownloadedVideoInfo>)

    @Query("SELECT * FROM CommandTemplate") fun getTemplateFlow(): Flow<List<CommandTemplate>>

    @Query("SELECT * FROM CommandTemplate") suspend fun getTemplateList(): List<CommandTemplate>

    @Query("select * from CookieProfile") fun getCookieProfileFlow(): Flow<List<CookieProfile>>

    @Insert suspend fun insertTemplate(template: CommandTemplate): Long

    @Insert @Transaction suspend fun importTemplates(templateList: List<CommandTemplate>)

    @Update suspend fun updateTemplate(template: CommandTemplate)

    @Delete suspend fun deleteTemplate(template: CommandTemplate)

    @Query("SELECT * FROM CommandTemplate where id = :id")
    suspend fun getTemplateById(id: Int): CommandTemplate

    @Query("select * from CookieProfile where id=:id")
    suspend fun getCookieById(id: Int): CookieProfile?

    @Update suspend fun updateCookieProfile(cookieProfile: CookieProfile)

    @Delete suspend fun deleteCookieProfile(cookieProfile: CookieProfile)

    @Insert suspend fun insertCookieProfile(cookieProfile: CookieProfile)

    @Query("delete from CommandTemplate where id=:id") suspend fun deleteTemplateById(id: Int)

    @Delete suspend fun deleteTemplates(templates: List<CommandTemplate>)

    @Query("select * from OptionShortcut") fun getOptionShortcuts(): Flow<List<OptionShortcut>>

    @Query("select * from OptionShortcut") suspend fun getShortcutList(): List<OptionShortcut>

    @Delete suspend fun deleteShortcut(optionShortcut: OptionShortcut)

    @Insert suspend fun insertShortcut(optionShortcut: OptionShortcut): Long

    @Transaction @Insert suspend fun insertAllShortcuts(shortcuts: List<OptionShortcut>)

    // Folder operations
    @Query("SELECT * FROM FolderInfo ORDER BY createdAt DESC")
    fun getFoldersFlow(): Flow<List<FolderInfo>>

    @Query("SELECT * FROM FolderInfo ORDER BY createdAt DESC")
    suspend fun getFolders(): List<FolderInfo>

    @Insert suspend fun insertFolder(folder: FolderInfo): Long

    @Update suspend fun updateFolder(folder: FolderInfo)

    @Delete suspend fun deleteFolder(folder: FolderInfo)

    @Query("UPDATE DownloadedVideoInfo SET folderId = :folderId WHERE id = :videoId")
    suspend fun updateVideoFolder(videoId: Int, folderId: Int?)

    @Query("SELECT * FROM DownloadedVideoInfo WHERE folderId = :folderId")
    fun getVideosByFolderFlow(folderId: Int): Flow<List<DownloadedVideoInfo>>

    @Query("SELECT * FROM DownloadedVideoInfo WHERE folderId IS NULL")
    fun getUnfiledVideosFlow(): Flow<List<DownloadedVideoInfo>>

    // Playlist operations
    @Query("SELECT * FROM Playlist ORDER BY createdAt DESC")
    fun getPlaylistsFlow(): Flow<List<Playlist>>

    @Query("SELECT * FROM Playlist ORDER BY createdAt DESC")
    suspend fun getPlaylists(): List<Playlist>

    @Insert suspend fun insertPlaylist(playlist: Playlist): Long

    @Update suspend fun updatePlaylist(playlist: Playlist)

    @Delete suspend fun deletePlaylist(playlist: Playlist)

    @Insert suspend fun insertPlaylistVideo(entry: PlaylistVideoInfo)

    @Delete suspend fun deletePlaylistVideo(entry: PlaylistVideoInfo)

    @Query("SELECT dvi.* FROM DownloadedVideoInfo dvi INNER JOIN PlaylistVideoInfo pvi ON dvi.id = pvi.videoId WHERE pvi.playlistId = :playlistId ORDER BY pvi.addedAt DESC")
    fun getPlaylistVideosFlow(playlistId: Int): Flow<List<DownloadedVideoInfo>>

    @Query("SELECT * FROM PlaylistVideoInfo WHERE playlistId = :playlistId AND videoId = :videoId LIMIT 1")
    suspend fun getPlaylistEntry(playlistId: Int, videoId: Int): PlaylistVideoInfo?

    @Query("SELECT COUNT(*) FROM PlaylistVideoInfo WHERE playlistId = :playlistId")
    fun getPlaylistItemCount(playlistId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM DownloadedVideoInfo WHERE folderId = :folderId")
    fun getFolderItemCount(folderId: Int): Flow<Int>
}
