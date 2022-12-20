package com.junkfood.seal.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoInfoDao {
    @Insert
    suspend fun insertAll(vararg info: DownloadedVideoInfo)

    @Query("select * from DownloadedVideoInfo")
    fun getAllMedia(): Flow<List<DownloadedVideoInfo>>

    @Query("select * from DownloadedVideoInfo where id=:id")
    suspend fun getInfoById(id: Int): DownloadedVideoInfo

    @Query("DELETE FROM DownloadedVideoInfo WHERE id = :id")
    suspend fun deleteInfoById(id: Int)

    @Query("DELETE FROM DownloadedVideoInfo WHERE videoPath = :path")
    suspend fun deleteInfoByPath(path: String)

    @Query("SELECT * FROM CommandTemplate")
    fun getTemplateFlow(): Flow<List<CommandTemplate>>

    @Query("SELECT * FROM CommandTemplate")
    suspend fun getTemplateList(): List<CommandTemplate>

    @Query("select * from CookieProfile")
    fun getCookieProfileFlow(): Flow<List<CookieProfile>>

    @Insert
    suspend fun insertTemplate(template: CommandTemplate)

    @Update
    suspend fun updateTemplate(template: CommandTemplate)

    @Delete
    suspend fun deleteTemplate(template: CommandTemplate)

    @Query("SELECT * FROM CommandTemplate where id = :id")
    suspend fun getTemplateById(id: Int): CommandTemplate

    @Query("select * from CookieProfile where id=:id")
    suspend fun getCookieById(id: Int): CookieProfile

    @Update
    suspend fun updateCookieProfile(cookieProfile: CookieProfile)

    @Delete
    suspend fun deleteCookieProfile(cookieProfile: CookieProfile)

    @Insert
    suspend fun insertCookieProfile(cookieProfile: CookieProfile)
}