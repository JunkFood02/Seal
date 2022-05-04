package com.junkfood.seal

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.BaseApplication.Companion.updateDownloadDir
import com.junkfood.seal.ui.home.DownloadViewModel
import com.junkfood.seal.ui.page.HomeEntry

class MainActivity : ComponentActivity() {
    private lateinit var downloadViewModel: DownloadViewModel

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: Init")
        super.onCreate(savedInstanceState)
        downloadViewModel = ViewModelProvider(this)[DownloadViewModel::class.java]
        setImmersiveStatusBar()
        val activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var permissionGranted = true
            for (b in result.values) {
                permissionGranted = permissionGranted && b
            }
            if (permissionGranted || Build.VERSION.SDK_INT > 29) {
                updateDownloadDir()
                downloadViewModel.startDownloadVideo()
            } else Toast.makeText(
                context,
                getString(R.string.permission_denied),
                Toast.LENGTH_SHORT
            )
                .show()
        }

        setContent {
            HomeEntry(activityResultLauncher, downloadViewModel)
        }
        Log.d(MainActivity.TAG, "onCreate: Init Finish")

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
        if (Build.VERSION.SDK_INT >= 30) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v: View, windowInsets: WindowInsetsCompat ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(0, insets.top, 0, if (insets.bottom > 50) insets.bottom else 0)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}





