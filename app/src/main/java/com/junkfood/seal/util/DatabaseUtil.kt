package com.junkfood.seal.util

import androidx.room.Room
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.database.AppDatabase
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.database.DownloadedVideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DatabaseUtil {
    val format = Json { prettyPrint = true }
    private const val DATABASE_NAME = "app_database"
    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java, DATABASE_NAME
    ).build()
    private val dao = db.videoInfoDao()
    suspend fun insertInfo(vararg infoList: DownloadedVideoInfo) {
        for (info in infoList) {
            dao.deleteByPath(info.videoPath)
            dao.insertAll(info)
        }
    }

    fun getMediaInfo() = dao.getAllMedia()

    fun getTemplateFlow() = dao.getTemplateFlow()

    suspend fun getTemplateList() = dao.getTemplateList()
    fun deleteInfoById(id: Int) {
        CoroutineScope(Job()).launch {
            dao.deleteById(id)
        }
    }

    suspend fun insertTemplate(commandTemplate: CommandTemplate) {
        dao.insertTemplate(commandTemplate)
    }

    suspend fun updateTemplate(commandTemplate: CommandTemplate) {
        dao.updateTemplate(commandTemplate)
    }

    suspend fun deleteTemplate(commandTemplate: CommandTemplate) {
        dao.deleteTemplate(commandTemplate)
    }

    suspend fun exportTemplatesToJson(): String {
        return format.encodeToString(getTemplateList())
    }

    suspend fun importTemplatesFromJson(json: String): Int {
        val list = getTemplateList()
        var cnt = 0
        format.decodeFromString<List<CommandTemplate>>(json)
            .forEach {
                if (!list.contains(it)) {
                    cnt++
                    dao.insertTemplate(it.copy(id = 0))
                }
            }
        return cnt
    }

    private const val TAG = "DatabaseUtil"
}