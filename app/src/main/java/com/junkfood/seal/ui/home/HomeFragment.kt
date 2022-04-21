package com.junkfood.seal.ui.home

import android.Manifest
import android.content.Intent
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.R
import com.junkfood.seal.databinding.FragmentHomeBinding
import com.junkfood.seal.util.DownloadUtil
import java.io.File


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var activityResultLauncher: ActivityResultLauncher<Array<String>>

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                UPDATE_PROGRESS -> {
                    homeViewModel.updateProgress(msg.obj as Float)
                }
                FINISH_DOWNLOADING -> {
                    if (PreferenceManager.getDefaultSharedPreferences(requireContext())
                            .getBoolean("open", false)
                    )
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
            if (permissionGranted || Build.VERSION.SDK_INT > 29) {
                updateDownloadDir()
                var url = binding.inputTextUrl.editText?.text.toString()
                if (url == "") {
                    url = "https://youtu.be/t5c8D1xbXtw";
                }
                Toast.makeText(context, getString(R.string.fetching_info), Toast.LENGTH_SHORT).show()
                with(PreferenceManager.getDefaultSharedPreferences(requireContext())) {
                    DownloadUtil.getVideo(
                        url, getBoolean("audio", true),
                        getBoolean("thumbnail", true), handler
                    )
                }
            } else Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_SHORT)
                    .show()
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

        }
        with(binding) {
            inputTextUrl.editText?.setText(homeViewModel.url.value)

            inputTextUrl.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    homeViewModel.url.value = p0.toString()
                }
            })

            downloadButton.setOnClickListener {
                activityResultLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
//            downloadDirText.text =
//                "%s%s".format(resources.getString(R.string.download_directory), downloadDir)
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun updateDownloadDir() {
        BaseApplication.updateDownloadDir()
//        binding.downloadDirText.text =
//            "%s%s".format(resources.getString(R.string.download_directory), downloadDir)
    }

    companion object {
        const val UPDATE_PROGRESS = 1
        const val FINISH_DOWNLOADING = 2
        private const val TAG = "HomeFragment"
    }
}