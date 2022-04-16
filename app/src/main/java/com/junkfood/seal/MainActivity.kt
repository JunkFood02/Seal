package com.junkfood.seal

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.junkfood.seal.databinding.ActivityMainBinding
import com.yausername.youtubedl_android.DownloadProgressCallback
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
        binding.downloadButton.setOnClickListener { getVideo(null) }
    }

    private fun getVideo(url: String?) {
        val youtubeDLDir = File(
            getExternalFilesDir(null)!!.absolutePath,
            "youtubedl-android"
        );
        val request = YoutubeDLRequest("https://youtu.be/t5c8D1xbXtw");
        request.addOption("-o", youtubeDLDir.absolutePath + "/%(title)s.%(ext)s")
        request.addOption("--proxy","http://127.0.0.1:7890")
        YoutubeDL.getInstance().execute(
            request
        ) { progress: Float, etaInSeconds: Long, _: String ->
            Log.d(
                TAG,
                "$progress% (ETA $etaInSeconds seconds)"
            )
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}