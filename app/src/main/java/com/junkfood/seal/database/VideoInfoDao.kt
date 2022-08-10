package com.junkfood.seal.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoInfoDao {
    @Insert
    suspend fun insertAll(vararg info: DownloadedVideoInfo)

    @Query("SELECT * FROM DownloadedVideoInfo WHERE videoPath not like '%.mp3' and videoPath not like '%.m4a' and videoPath not like '%.opus'")
    fun getAllVideos(): Flow<List<DownloadedVideoInfo>>

    @Query("SELECT * FROM DownloadedVideoInfo WHERE videoPath like '%.mp3' or videoPath like '%.m4a' or videoPath like '%.opus'")
    fun getAllAudios(): Flow<List<DownloadedVideoInfo>>

    @Delete
    suspend fun delete(info: DownloadedVideoInfo)

    @Query("DELETE FROM DownloadedVideoInfo WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM DownloadedVideoInfo WHERE videoTitle = :title")
    suspend fun deleteByTitle(title: String)

    @Query("DELETE FROM DownloadedVideoInfo WHERE videoPath = :path")
    suspend fun deleteByPath(path: String)

    @Query("SELECT * FROM CommandTemplate")
    fun getTemplates(): Flow<List<CommandTemplate>>

    @Insert
    suspend fun insertTemplate(template: CommandTemplate)

    @Update
    suspend fun updateTemplate(template: CommandTemplate)

    @Delete
    suspend fun deleteTemplate(template: CommandTemplate)

    @Query("SELECT * FROM CommandTemplate where id = :id")
    suspend fun getTemplateById(id: Int): CommandTemplate
}