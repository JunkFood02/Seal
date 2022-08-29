package com.junkfood.seal.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoInfoDao {
    @Insert
    suspend fun insertAll(vararg info: DownloadedVideoInfo)

    @Query("select * from DownloadedVideoInfo")
    fun getAllMedia(): Flow<List<DownloadedVideoInfo>>

    @Query("select * from DownloadedVideoInfo where id=:id")
    suspend fun getInfoById(id: Int): DownloadedVideoInfo

    @Delete
    suspend fun delete(info: DownloadedVideoInfo)

    @Query("DELETE FROM DownloadedVideoInfo WHERE id = :id")
    suspend fun deleteInfoById(id: Int)

    @Query("DELETE FROM DownloadedVideoInfo WHERE videoPath = :path")
    suspend fun deleteInfoByPath(path: String)

    @Query("SELECT * FROM CommandTemplate")
    fun getTemplateFlow(): Flow<List<CommandTemplate>>

    @Query("SELECT * FROM CommandTemplate")
    suspend fun getTemplateList(): List<CommandTemplate>

    @Insert
    suspend fun insertTemplate(template: CommandTemplate)

    @Update
    suspend fun updateTemplate(template: CommandTemplate)

    @Delete
    suspend fun deleteTemplate(template: CommandTemplate)

    @Query("SELECT * FROM CommandTemplate where id = :id")
    suspend fun getTemplateById(id: Int): CommandTemplate
}