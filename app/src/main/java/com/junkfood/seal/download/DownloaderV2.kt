package com.junkfood.seal.download

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object DownloaderV2 {
    private val scope = CoroutineScope(SupervisorJob())

    private val mRunningTaskFlow = MutableStateFlow(0)
    val runningTaskFlow = mRunningTaskFlow.asStateFlow()
}
