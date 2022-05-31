package com.junkfood.seal

import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.junkfood.seal.ui.page.HomeEntry
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setImmersiveStatusBar()
        setContent {
            HomeEntry()
        }
    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("zh-CN")
    AppCompatDelegate.setApplicationLocales(appLocale)
}

/*override fun attachBaseContext(newBase: Context?) {

    super.attachBaseContext(
        newBase?.applyNewLocale(
            Locale(
                PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("language", "en")!!
            )
        )
    )
}

private fun Context.applyNewLocale(locale: Locale): Context {
    val config = this.resources.configuration
    val sysLocale =
        config.locales.get(0)
    if (sysLocale.language != locale.language) {
        Locale.setDefault(locale)
        config.setLocale(locale)
        resources.configuration.updateFrom(config)
    }
    return this
}*/

private fun setImmersiveStatusBar() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v: View, windowInsets: WindowInsetsCompat ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(0, 0, 0, 0)
        WindowInsetsCompat.CONSUMED
    }
}

companion object {
    private const val TAG = "MainActivity"
}

}





