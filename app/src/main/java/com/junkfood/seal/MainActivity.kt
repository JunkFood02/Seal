package com.junkfood.seal

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import com.junkfood.seal.ui.page.HomeEntry
import com.junkfood.seal.util.PreferenceUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLanguage(PreferenceUtil.getLanguageConfiguration())
        WindowCompat.setDecorFitsSystemWindows(window, false)
        BaseApplication.context = this.baseContext
        setContent {
            HomeEntry()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        fun setLanguage(locale: String) {
            if (locale.isEmpty()) return
            BaseApplication.applicationScope.launch(Dispatchers.Main) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))
            }
        }
    }

}





