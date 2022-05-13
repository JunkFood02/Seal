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

    @Query("SELECT * FROM DownloadedVideoInfo")
    fun getAll(): Flow<List<DownloadedVideoInfo>>

    @Delete
    suspend fun delete(info: DownloadedVideoInfo)

    @Query("DELETE FROM DownloadedVideoInfo WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM DownloadedVideoInfo WHERE videoTitle = :title")
    suspend fun deleteByTitle(title: String)
}