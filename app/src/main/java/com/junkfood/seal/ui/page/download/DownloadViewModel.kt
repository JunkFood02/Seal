package com.junkfood.seal.ui.page.download

import android.os.*
import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.service.Constants.What.*
import com.junkfood.seal.util.*
import com.junkfood.seal.util.FileUtil.openFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
@OptIn(ExperimentalMaterialApi::class)
class DownloadViewModel @Inject constructor() : ViewModel() {

    private val mutableStateFlow = MutableStateFlow(DownloadViewState(
        debugMode = PreferenceUtil.getValue(
            PreferenceUtil.DEBUG)))
    val stateFlow = mutableStateFlow.asStateFlow()

    data class DownloadViewState(
        val url: String = "",
        val urlTask: String = "",
        val isDownloadError: Boolean = false,
        val isInCustomCommandMode: Boolean = false,
        val isProcessing: Boolean = false,
        val debugMode: Boolean = false,
        val errorMessage: String = "",
        val drawerState: ModalBottomSheetState = ModalBottomSheetState(
            ModalBottomSheetValue.Hidden,
            isSkipHalfExpanded = true
        ),
        val isDownloadingPlaylist: Boolean = false,
        val state: DownloadState = DownloadState()
    )

    data class PlaylistSelectionViewState (
        val showPlaylistSelectionDialog: Boolean = false,
        val playlistSize: Int = 0,
    )
    private val playlistSelectionMutable = MutableStateFlow(PlaylistSelectionViewState())
    val playlistSelectionState = playlistSelectionMutable.asStateFlow()

    private val ytdlpVersionM = MutableStateFlow("")
    val ytdlpVersion = ytdlpVersionM.asStateFlow()

    fun updateytDlp(force: Boolean = false) {
        val s = PreferenceUtil.getString(PreferenceUtil.YT_DLP)
        if (s.isNullOrEmpty() || force) {
            val b = Bundle()
            b.putString(WHAT_YTDLP_VERSION.name, "")
            val m = Message.obtain(null, WHAT_YTDLP_VERSION.ordinal)
            m.replyTo = mMessenger
            m.data = b
            BaseApplication.sendMessage(m)
        }
        else
            ytdlpVersionM.update { s }
    }

    //Receive messages from the server
    private val downloadHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT_APPEND_TASK_ASK.ordinal -> {
                    Log.i(TAG, "find device")
                    if (!messageHasError(msg) && msg.arg1 > 1) {
                        showPlaylistDialog(msg.arg1)
                    }
                }
                WHAT_YTDLP_VERSION.ordinal -> {
                    Log.i(TAG, "ver ok")
                    val b = msg.peekData()
                    if (!messageHasError(msg) && b != null) {
                        val s = b.getString(WHAT_YTDLP_VERSION.name)!!
                        PreferenceUtil.updateString(PreferenceUtil.YT_DLP, s)
                        if (msg.arg1>0)
                            viewModelScope.launch { TextUtil.makeToastSuspend(context.getString(msg.arg1) + " (" + s +")") }
                        ytdlpVersionM.update { s }
                    }
                }
                WHAT_APPEND_TASK.ordinal -> {
                    if (!messageHasError(msg)) {
                        val task = messageHasParcel<DownloadTask>(msg)
                        if (task != null) {
                            viewModelScope.launch {
                                TextUtil.makeToastSuspend(
                                    context.getString(R.string.task_added).format(task.url)
                                )
                            }
                        }
                    }
                }
                WHAT_TASK_HALT.ordinal -> {
                    if (!messageHasError(msg)) {
                        val task = messageHasParcel<DownloadTask>(msg)
                        if (task != null && task.url == stateFlow.value.url) {
                            val b = msg.peekData()!!
                            val stopOk = b.getBoolean(WHAT_TASK_HALT.name + "0")
                            if (stopOk) {
                                viewModelScope.launch {
                                    TextUtil.makeToastSuspend(
                                        context.getString(R.string.task_cancelled)
                                            .format(task.url)
                                    )
                                }
                            }
                        }
                    }
                }
                WHAT_TASK_PROGRESS.ordinal -> {
                    if (!messageHasError(msg)) {
                        val downState = messageHasParcel<ServiceState>(msg)
                        if (downState != null) {
                            val b = msg.peekData()!!
                            val isNewTask = b.getBoolean(WHAT_TASK_PROGRESS.name + "0")
                            switchDownloadMode(downState.task!= null && downState.task.settings.isCustom())
                            viewModelScope.launch {

                                mutableStateFlow.update {
                                    if (it.state.videoTitle != downState.state.videoTitle)
                                        TextUtil.makeToastSuspend(context.getString(R.string.download_start_msg).format(
                                            downState.state.videoTitle
                                        ))
                                    if (downState.task == null) {
                                        if (isNewTask)
                                            TextUtil.makeToastSuspend(context.getString(R.string.download_success_msg))
                                        it.copy(
                                            state = downState.state,
                                            isProcessing = false,
                                            isDownloadingPlaylist = false,
                                            debugMode = PreferenceUtil.getValue(
                                                PreferenceUtil.DEBUG)
                                        )
                                    }
                                    else if (isNewTask) {
                                        it.copy(
                                            state = downState.state,
                                            urlTask = downState.task.url,
                                            isDownloadError = false,
                                            isDownloadingPlaylist = downState.state.downloadItemCount-downState.state.currentIndex > 0,
                                            isProcessing = true,
                                            debugMode = PreferenceUtil.getValue(
                                                PreferenceUtil.DEBUG)
                                        )
                                    }
                                    else {
                                        val proc = if (!it.isProcessing && it.state.progressText != downState.state.progressText) true else it.isProcessing
                                        it.copy(
                                            state = downState.state,
                                            urlTask = downState.task.url,
                                            isProcessing = proc,
                                            isDownloadingPlaylist = downState.state.downloadItemCount-downState.state.currentIndex > 0,
                                            debugMode = PreferenceUtil.getValue(
                                                PreferenceUtil.DEBUG)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private val mMessenger = Messenger(downloadHandler)

    private inline fun <reified R: Parcelable> messageHasParcel(msg: Message): R? {
        try {
            val b = msg.peekData()
            b.classLoader = R::class.java.classLoader
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) b.getParcelable(values()[msg.what].name) else b.getParcelable(values()[msg.what].name, R::class.java)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun messageHasError(msg: Message): Boolean {
        if (msg.arg1 == -WHAT_ERROR.ordinal) {
            viewModelScope.launch {
                TextUtil.makeToastSuspend(context.getString(msg.arg2))
                if (PreferenceUtil.getValue(PreferenceUtil.DEBUG)) {
                    val b = msg.peekData()
                    var errorMsg: String? = null
                    if (b != null) {
                        errorMsg = b.getString(WHAT_ERROR.name)
                    }

                    showErrorReport(errorMsg ?: context.getString(R.string.unknown_error))
                }
            }
            return true
        }
        else
            return false
    }


    fun updateUrl(url: String) = mutableStateFlow.update { it.copy(url = url) }

    fun hideDrawer(scope: CoroutineScope) {
        scope.launch {
            stateFlow.value.drawerState.hide()
        }
    }

    fun showDrawer(scope: CoroutineScope) {
        scope.launch {
            stateFlow.value.drawerState.show()
        }
    }


    private fun sendDownloadMessage(startItem: Int = 0, endItem: Int = 0, what: Int = WHAT_APPEND_TASK.ordinal): DownloadTask {
        val task = DownloadTask(if (what == WHAT_APPEND_TASK.ordinal) stateFlow.value.url else stateFlow.value.urlTask, startItem=startItem, endItem = endItem)
        val m = Message.obtain(null, what)
        m.replyTo = mMessenger
        val b = Bundle()
        b.putParcelable(values()[what].name, task)
        m.data = b
        BaseApplication.sendMessage(m)
        return task
    }

    fun startDownloadVideo(indexRange: IntRange = 0..0) {
        if (stateFlow.value.url.isBlank()) {
            viewModelScope.launch { TextUtil.makeToastSuspend(context.getString(R.string.url_empty)) }
            return
        }
        val task = sendDownloadMessage(indexRange.first, indexRange.last)
        if (task.settings.downloadPlaylist && indexRange.first == 0 && indexRange.last == 0)
            viewModelScope.launch { TextUtil.makeToastSuspend(context.getString(R.string.fetching_playlist_info)) }
    }

    private fun switchDownloadMode(enabled: Boolean) {
        with(mutableStateFlow) {
            if (enabled) {
                if (!stateFlow.value.isInCustomCommandMode || stateFlow.value.state.showVideoCard) {
                    update {
                        val st = it.state.copy(showVideoCard = false)
                        it.copy(
                            state = st,
                            isInCustomCommandMode = true,
                        )
                    }
                }
            } else if (stateFlow.value.isInCustomCommandMode)
                update { it.copy(isInCustomCommandMode = false) }
        }
    }

    private suspend fun showErrorReport(s: String) {
        TextUtil.makeToastSuspend(context.getString(R.string.error_copied))
        mutableStateFlow.update {
            it.copy(
                isDownloadError = true,
                errorMessage = s,
                isProcessing = false,
            )
        }
    }


    fun openVideoFile() {
        if (stateFlow.value.state.fileNames != null)
            openFile(DownloadUtilService.Result.success(stateFlow.value.state.fileNames))
    }

    private fun showPlaylistDialog(playlistSize: Int) {
        playlistSelectionMutable.update {
            it.copy(
                showPlaylistSelectionDialog = true, playlistSize = playlistSize
            )
        }
    }

    fun hidePlaylistDialog() {
        playlistSelectionMutable.update { it.copy(showPlaylistSelectionDialog = false) }
    }

    fun stopDownloadPlaylistOnNextItem() {
        sendDownloadMessage(what=WHAT_TASK_HALT.ordinal)
    }

    companion object {
        private const val TAG = "DownloadViewModel"
    }

    init {
        val m = Message.obtain(null, WHAT_TASK_PROGRESS.ordinal)
        updateytDlp()
        m.replyTo = mMessenger
        BaseApplication.sendMessage(m)
    }
}