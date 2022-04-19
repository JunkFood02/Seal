package com.junkfood.seal.ui.home

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.downloadDir
import com.junkfood.seal.databinding.FragmentHomeBinding
import com.junkfood.seal.ui.home.HomeFragment.Companion.UPDATE_PROGRESS
import com.junkfood.seal.util.DownloadUtil
import java.io.File
import java.util.*


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var activityResultLauncher: ActivityResultLauncher<Array<String>>

    val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                SHOW_ERROR_MESSAGE -> Toast.makeText(context, msg.obj as String, Toast.LENGTH_SHORT)
                    .show()
                UPDATE_PROGRESS -> {
                    homeViewModel.updateProgress(msg.obj as Float)
                }
                FINISH_DOWNLOADING -> {
                    startActivity(Intent().apply {
                        action = (Intent.ACTION_VIEW)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setDataAndType(
                            FileProvider.getUriForFile(
                                BaseApplication.context,
                                BaseApplication.context.packageName + ".provider",
                                File(msg.obj as String)
                            ), if (msg.arg1 == 1) "audio/*" else "video/*"
                        )
                    })
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var permissionGranted = true
            for (b in result.values) {
                permissionGranted = permissionGranted && b
            }
            if (permissionGranted) {
                updateDownloadDir()
                var url = binding.inputTextUrl.editText?.text.toString()
                if (url == "") {
                    url = "https://youtu.be/t5c8D1xbXtw";
                }
                Toast.makeText(context, "Fetching video info.", Toast.LENGTH_SHORT).show()
                DownloadUtil.getVideo(
                    url, homeViewModel.audioSwitch.value!!,
                    homeViewModel.thumbnailSwitch.value!!, handler
                )
            } else {
                Toast.makeText(context, "Failed to request permission", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.textHome
        with(homeViewModel) {
            text.observe(viewLifecycleOwner) {
                textView.text = it
            }
            progress.observe(viewLifecycleOwner) {
                binding.downloadProgressBar.progress = it.toInt()
                binding.downloadProgressText.text = "$it%"
            }
            audioSwitch.observe(viewLifecycleOwner) {
                binding.audioSwitch.isChecked = it
            }
            thumbnailSwitch.observe(viewLifecycleOwner) {
                binding.thumbnailSwitch.isChecked = it
            }
            proxySwitch.observe(viewLifecycleOwner) {
                binding.proxySwitch.isChecked = it
            }
        }
        with(binding) {
            inputTextUrl.editText?.setText(homeViewModel.url.value)
            inputProxy.editText?.setText(homeViewModel.proxy.value)
            audioSwitch.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
                homeViewModel.audioSwitchChange(b)
            }
            thumbnailSwitch.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
                homeViewModel.thumbnailSwitchChange(b)
            }
            proxySwitch.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
                homeViewModel.proxySwitchChange(b)
            }
            inputTextUrl.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    homeViewModel.url.value = p0.toString()
                }
            })
            inputProxy.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    homeViewModel.proxy.value = p0.toString()
                }
            })
            downloadButton.setOnClickListener {
                activityResultLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
            downloadDirText.text = "Download Directory:$downloadDir"
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun updateDownloadDir() {
        BaseApplication.updateDownloadDir()
        binding.downloadDirText.text = downloadDir
    }

    companion object {
        const val UPDATE_PROGRESS = 1
        const val FINISH_DOWNLOADING = 2
        const val SHOW_ERROR_MESSAGE = -1
        private const val TAG = "HomeFragment"
    }
}