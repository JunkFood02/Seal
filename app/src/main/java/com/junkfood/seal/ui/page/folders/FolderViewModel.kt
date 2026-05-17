package com.junkfood.seal.ui.page.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.database.objects.FolderInfo
import com.junkfood.seal.util.DatabaseUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FolderViewModel : ViewModel() {
    val foldersFlow = DatabaseUtil.getFoldersFlow()

    suspend fun createFolder(name: String) {
        DatabaseUtil.createFolder(name)
    }

    suspend fun renameFolder(folder: FolderInfo, newName: String) {
        DatabaseUtil.updateFolder(folder.copy(name = newName))
    }

    fun deleteFolder(folder: FolderInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseUtil.deleteFolder(folder)
        }
    }
}
