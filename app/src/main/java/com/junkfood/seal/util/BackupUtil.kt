package com.junkfood.seal.util

import com.junkfood.seal.database.Backup
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.database.OptionShortcut
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BackupUtil {
    val format = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportTemplatesToJson() =
        exportTemplatesToJson(
            templates = DatabaseUtil.getTemplateList(),
            shortcuts = DatabaseUtil.getShortcutList()
        )

    fun exportTemplatesToJson(
        templates: List<CommandTemplate>,
        shortcuts: List<OptionShortcut>
    ): String {
        return format.encodeToString(
            Backup(
                templates = templates, shortcuts = shortcuts
            )
        )
    }

    fun List<DownloadedVideoInfo>.toJson(): String {
        return format.encodeToString(Backup(downloadHistory = this))
    }

    fun List<DownloadedVideoInfo>.toUrlList(): String {
        return this.map { it.videoUrl }.joinToString(separator = "\n") { it }
    }


}