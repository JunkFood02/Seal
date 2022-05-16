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

    @Query("SELECT * FROM DownloadedVideoInfo WHERE videoPath like :string")
    fun getAll(string: String): Flow<List<DownloadedVideoInfo>>

    @Query("SELECT * FROM DownloadedVideoInfo WHERE videoPath not like :string")
    fun getAllFilter(string: String): Flow<List<DownloadedVideoInfo>>

    @Delete
    suspend fun delete(info: DownloadedVideoInfo)

    @Query("DELETE FROM DownloadedVideoInfo WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM DownloadedVideoInfo WHERE videoTitle = :title")
    suspend fun deleteByTitle(title: String)

    @Query("DELETE FROM DownloadedVideoInfo WHERE videoPath = :path")
    suspend fun deleteByPath(path: String)
}