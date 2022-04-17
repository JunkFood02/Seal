package com.junkfood.seal.ui.home

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.downloadDir
import com.junkfood.seal.databinding.FragmentHomeBinding
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.File
import java.lang.Exception
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        homeViewModel.progress.observe(viewLifecycleOwner) {
            binding.downloadProgressBar.progress = it.toInt()
            binding.downloadProgressText.text = "$it%"
        }
        binding.inputTextUrl.editText?.setText(homeViewModel.url.value)

        homeViewModel.audioSwitch.observe(viewLifecycleOwner) {
            binding.audioSwitch.isChecked = it
        }
        homeViewModel.thumbnailSwitch.observe(viewLifecycleOwner) {
            binding.thumbnailSwitch.isChecked = it
        }
        binding.audioSwitch.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            homeViewModel.audioSwitchChange(b)
        }
        binding.thumbnailSwitch.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            homeViewModel.thumbnailSwitchChange(b)
        }
        homeViewModel.updateTime()
        binding.inputTextUrl.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                homeViewModel.url.value = p0.toString()
            }
        })
        binding.downloadButton.setOnClickListener {
            var url = binding.inputTextUrl.editText?.text.toString()
            if (url == "") {
                url = "https://youtu.be/t5c8D1xbXtw";
            }
            Toast.makeText(context, "Fetching video info.", Toast.LENGTH_SHORT).show()
            getVideo(url)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getVideo(url: String) {

        Thread {
            Looper.prepare()
            val request = YoutubeDLRequest(url)
            val videoInfo = YoutubeDL.getInstance().getInfo(url)
            var title = createFilename(videoInfo.title)
            var ext = videoInfo.ext
            Toast.makeText(context, "Start downloading '$title'", Toast.LENGTH_SHORT)
                .show()
            if (url.contains("list")) {
                request.addOption("-P", "$downloadDir/")
                request.addOption("-o", "%(playlist)s/%(playlist_index)s - %(title)s.%(ext)s")
//                request.addOption("-o", "$downloadDir/%{title}s.%{ext}s")
            } else {
                request.addOption("-P", "$downloadDir/")
//              request.addOption("-o", "$downloadDir/$title.$ext")
            }
            if (homeViewModel.audioSwitch.value == true) {
                request.addOption("-x")
                request.addOption("--audio-format", "mp3")
                request.addOption("--audio-quality", "0")
                ext = "mp3"
            }
            if (homeViewModel.thumbnailSwitch.value == true) {
                //request.addOption("--write-thumbnail")
                request.addOption("--add-metadata")
                request.addOption("--embed-thumbnail")
                request.addOption("--compat-options", "embed-thumbnail-atomicparsley")
                //request.addOption("--convert-thumbnails", "jpg")
            }
            request.addOption("--proxy", "http://127.0.0.1:7890")
            request.addOption("--force-overwrites")
            try {
                YoutubeDL.getInstance().execute(
                    request
                ) { progress: Float, _: Long, s: String ->
                    Log.d(TAG, s)
                    homeViewModel.updateProgress(progress)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Unknown error(s) occurred", Toast.LENGTH_SHORT).show()
            }
            homeViewModel.updateProgress(100f);
            Toast.makeText(context, "Download completed!", Toast.LENGTH_SHORT).show()
            if (!url.contains("list")) {
                Log.d(TAG, "$downloadDir/$title.$ext")
                startActivity(Intent().apply {
                    action = (Intent.ACTION_VIEW)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(
                        FileProvider.getUriForFile(
                            BaseApplication.context,
                            BaseApplication.context.packageName + ".provider",
                            File("$downloadDir/$title.$ext")
                        ), "video/*"
                    )
                })
            }
        }.start()
    }

    fun createFilename(title: String): String {
        val cleanFileName = title.replace("[\\\\><\"|*?'%:#/]".toRegex(), "_")
        var fileName = cleanFileName.trim { it <= '_' }.replace("_+".toRegex(), "_")
        if (fileName.length > 127) fileName = fileName.substring(0, 127)
        return fileName + Date().time
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}