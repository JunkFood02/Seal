package com.junkfood.seal

import android.util.Log
import androidx.lifecycle.ViewModel
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.DARK_THEME
import com.junkfood.seal.util.PreferenceUtil.DYNAMIC_COLORS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor() : ViewModel() {
    data class ViewState(
        val darkTheme: Boolean = false,
        val dynamicColor: Boolean = false
    )

    private val TAG = "AppearanceViewModel"
    val viewState = MutableStateFlow(ViewState())
    fun darkThemeSwitch() {
        viewState.update { it.copy(darkTheme = !it.darkTheme) }
        PreferenceUtil.updateValue(DARK_THEME, viewState.value.darkTheme)
    }

    fun dynamicColorSwitch() {
        viewState.update { it.copy(dynamicColor = !it.dynamicColor) }
        PreferenceUtil.updateValue(DYNAMIC_COLORS, viewState.value.dynamicColor)
        Log.d(TAG, viewState.value.dynamicColor.toString())
    }
}