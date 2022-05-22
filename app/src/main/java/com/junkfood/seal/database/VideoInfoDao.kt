package com.junkfood.seal.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
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
}