package com.junkfood.seal.util

import androidx.room.Room
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.database.AppDatabase
import com.junkfood.seal.database.DownloadedVideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

object DatabaseUtil {
    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java, "app_database"
    ).build()
    private val dao = db.videoInfoDao()

    suspend fun insertInfo(vararg info: DownloadedVideoInfo) = dao.insertAll(*info)

    fun getInfo(): Flow<List<DownloadedVideoInfo>> = dao.getAll()

    fun deleteInfoById(id: Int) {
        CoroutineScope(Job()).launch {
            dao.deleteById(id)
        }
    }
}