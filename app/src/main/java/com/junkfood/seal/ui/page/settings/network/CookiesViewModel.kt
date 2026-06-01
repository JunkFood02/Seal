package com.junkfood.seal.ui.page.settings.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.database.objects.CookieProfile
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.DownloadUtil.toCookiesFileContent
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
        val editingCookieProfile: CookieProfile =
            CookieProfile(id = NEW_PROFILE_ID, url = "", content = "")
    )

    data class CookieImportResult(val importedCount: Int)

    val cookiesFlow = DatabaseUtil.getCookiesFlow()

    private val mutableStateFlow = MutableStateFlow(ViewState())
    val stateFlow = mutableStateFlow.asStateFlow()
    private val state
        get() = stateFlow.value

    fun setEditingProfile(
        cookieProfile: CookieProfile =
            CookieProfile(id = NEW_PROFILE_ID, url = "https://", content = "")
    ) {
        mutableStateFlow.update { it.copy(editingCookieProfile = cookieProfile) }
    }

    fun deleteCookieProfile(cookieProfile: CookieProfile = state.editingCookieProfile) {
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

    fun updateUrl(url: String) {
        setEditingProfile(cookieProfile = state.editingCookieProfile.copy(url = url))
    }

    fun updateContent(content: String) =
        mutableStateFlow.update {
            it.copy(editingCookieProfile = it.editingCookieProfile.copy(content = content))
        }

    fun updateCookieProfile(profile: CookieProfile = state.editingCookieProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            if (profile.id == NEW_PROFILE_ID) {
                DatabaseUtil.insertCookieProfile(profile)
            } else {
                DatabaseUtil.updateCookieProfile(profile)
            }
        }
    }

    suspend fun importNetscapeCookieProfile(
        url: String,
        content: String,
    ): Result<CookieImportResult> =
        CookieParser.parseNetscapeCookies(content).mapCatching { cookies ->
            importNetscapeCookieProfile(url = url, cookies = cookies).getOrThrow()
        }

    suspend fun importNetscapeCookieProfile(
        url: String,
        cookies: List<Cookie>,
    ): Result<CookieImportResult> = runCatching {
        DatabaseUtil.insertCookieProfile(
            CookieProfile(id = NEW_PROFILE_ID, url = url, content = cookies.toCookiesFileContent())
        )
        CookieImportResult(importedCount = cookies.size)
    }
}
