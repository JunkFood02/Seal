package com.junkfood.seal.ui.page.settings.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.database.CookieProfile
import com.junkfood.seal.util.DatabaseUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CookiesViewModel : ViewModel() {
    companion object {
        const val NEW_PROFILE_ID = 0
    }

    data class ViewState(
        val showEditDialog: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val editingCookieProfile: CookieProfile = CookieProfile(
            id = NEW_PROFILE_ID,
            url = "",
            content = ""
        )
    )

    val cookiesFlow = DatabaseUtil.getCookiesFlow()

    private val mutableStateFlow = MutableStateFlow(ViewState())
    val stateFlow = mutableStateFlow.asStateFlow()

    fun showEditCookieDialog(
        cookieProfile: CookieProfile = CookieProfile(
            id = NEW_PROFILE_ID,
            url = "",
            content = ""
        )
    ) {
        mutableStateFlow.update {
            it.copy(
                editingCookieProfile = cookieProfile,
                showEditDialog = true
            )
        }
    }

    fun showDeleteCookieDialog(cookieProfile: CookieProfile) {
        mutableStateFlow.update {
            it.copy(
                editingCookieProfile = cookieProfile,
                showDeleteDialog = true
            )
        }
    }

    fun deleteCookieProfile(cookieProfile: CookieProfile = stateFlow.value.editingCookieProfile) {
        viewModelScope.launch(Dispatchers.IO) { DatabaseUtil.deleteCookieProfile(cookieProfile) }
    }

    fun generateNewCookies(content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableStateFlow.update {
                val newProfile = it.editingCookieProfile.copy(content = content)
                DatabaseUtil.updateCookieProfile(newProfile)
                it.copy(editingCookieProfile = newProfile)
            }
        }
    }

    fun updateUrl(url: String) =
        mutableStateFlow.update { it.copy(editingCookieProfile = it.editingCookieProfile.copy(url = url)) }

    fun updateContent(content: String) =
        mutableStateFlow.update {
            it.copy(
                editingCookieProfile = it.editingCookieProfile.copy(
                    content = content
                )
            )
        }

    fun updateCookieProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableStateFlow.update {
                if (it.editingCookieProfile.id == NEW_PROFILE_ID) {
                    DatabaseUtil.insertCookieProfile(it.editingCookieProfile)
                } else {
                    DatabaseUtil.updateCookieProfile(it.editingCookieProfile)
                }
                it.copy(showEditDialog = false)
            }
        }
    }

    fun hideDialog() {
        mutableStateFlow.update {
            it.copy(
                showEditDialog = false,
                showDeleteDialog = false
            )
        }
    }

}