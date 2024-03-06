package com.junkfood.seal.util

import androidx.room.Room
import com.junkfood.seal.App.Companion.applicationScope
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.database.AppDatabase
import com.junkfood.seal.database.Backup
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.database.CookieProfile
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.database.OptionShortcut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object DatabaseUtil {
    private const val DATABASE_NAME = "app_database"
    private val db = Room.databaseBuilder(
        context, AppDatabase::class.java, DATABASE_NAME
    ).build()
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
    suspend fun deleteInfoListByIdList(idList: List<Int>, deleteFile: Boolean = false) =
        dao.deleteInfoListByIdList(idList, deleteFile)

    suspend fun getInfoById(id: Int): DownloadedVideoInfo = dao.getInfoById(id)
    suspend fun deleteInfoById(id: Int) = dao.deleteInfoById(id)

    suspend fun insertTemplate(commandTemplate: CommandTemplate) =
        dao.insertTemplate(commandTemplate)

    suspend fun updateTemplate(commandTemplate: CommandTemplate) {
        dao.updateTemplate(commandTemplate)
    }

    suspend fun importTemplatesFromJson(json: String): Int {
        val templateList = getTemplateList()
        val shortcutList = getShortcutList()
        var cnt = 0
        try {
            BackupUtil.format.decodeFromString<Backup>(json).run {
                if (templates != null) {
                    dao.importTemplates(
                        templateList.filterNot {
                            templateList.contains(it)
                        }.map { it.copy(id = 0) }.also { cnt += it.size }
                    )
                }
                if (shortcuts != null) {
                    dao.insertAllShortcuts(
                        shortcuts.filterNot {
                            shortcutList.contains(it)
                        }.map { it.copy(id = 0) }.apply { cnt += size }
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cnt
    }

    suspend fun importDownloadHistoryFromJson(json: String): Int {
        val itemList = getDownloadHistory()
        var cnt = 0
        BackupUtil.format.runCatching {
            decodeFromString<Backup>(json).run {
                if (!downloadHistory.isNullOrEmpty()) {
                    dao.insertAll(
                        downloadHistory
                            .filterNot { itemList.contains(it) }
                            .map { it.copy(id = 0) }
                            .also { cnt += it.size }
                    )
                }
            }
        }
        return cnt
    }

    suspend fun deleteTemplateById(id: Int) = dao.deleteTemplateById(id)

    suspend fun deleteTemplates(templates: List<CommandTemplate>) = dao.deleteTemplates(templates)


    private const val TAG = "DatabaseUtil"
}