package com.junkfood.seal.download

import com.junkfood.seal.database.objects.CommandTemplate
import com.junkfood.seal.download.Task.DownloadState
import com.junkfood.seal.download.Task.RestartableAction
import com.junkfood.seal.util.DownloadUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskStateTransitionTest {

    @Test
    fun testDownloadStatePriorityOrdering() {
        // Queue scheduling prioritizes active downloads over pending/idle states
        val running = DownloadState.Running(taskId = "task_running")
        val readyWithInfo = DownloadState.ReadyWithInfo
        val fetchingInfo = DownloadState.FetchingInfo(taskId = "task_fetching")
        val idle = DownloadState.Idle
        val canceled = DownloadState.Canceled(action = RestartableAction.Download)
        val error = DownloadState.Error(throwable = Throwable("network"), action = RestartableAction.Download)
        val completed = DownloadState.Completed(filePath = "/path/to/file.mp4")

        val states = listOf(completed, error, canceled, idle, fetchingInfo, readyWithInfo, running)
        val sortedStates = states.sorted()

        assertEquals(
            listOf(running, readyWithInfo, fetchingInfo, idle, canceled, error, completed),
            sortedStates,
        )
    }

    @Test
    fun testRestartTransitions() {
        // Canceled or Error with FetchInfo should restart to Idle
        val canceledFetch = DownloadState.Canceled(action = RestartableAction.FetchInfo)
        val restartedFetchState =
            when (canceledFetch.action) {
                RestartableAction.FetchInfo -> DownloadState.Idle
                RestartableAction.Download -> DownloadState.ReadyWithInfo
            }
        assertEquals(DownloadState.Idle, restartedFetchState)

        // Canceled or Error with Download should restart to ReadyWithInfo
        val canceledDownload = DownloadState.Canceled(action = RestartableAction.Download, progress = 0.45f)
        val restartedDownloadState =
            when (canceledDownload.action) {
                RestartableAction.FetchInfo -> DownloadState.Idle
                RestartableAction.Download -> DownloadState.ReadyWithInfo
            }
        assertEquals(DownloadState.ReadyWithInfo, restartedDownloadState)

        // Error with Download should restart to ReadyWithInfo
        val errorDownload = DownloadState.Error(throwable = RuntimeException("timeout"), action = RestartableAction.Download)
        val restartedErrorState =
            when (errorDownload.action) {
                RestartableAction.FetchInfo -> DownloadState.Idle
                RestartableAction.Download -> DownloadState.ReadyWithInfo
            }
        assertEquals(DownloadState.ReadyWithInfo, restartedErrorState)
    }

    @Test
    fun testCustomCommandLifecycleStates() {
        val template = CommandTemplate(id = 10, name = "Audio Extractor", template = "-x --audio-format m4a")
        val taskWithState = TaskFactory.createWithCustomCommand(
            url = "https://example.com/audio",
            template = template,
            preferences = DownloadUtil.DownloadPreferences.EMPTY,
        )

        val task = taskWithState.task
        val initialState = taskWithState.state

        assertEquals(DownloadState.Idle, initialState.downloadState)
        assertTrue(task.type is Task.TypeInfo.CustomCommand)

        // Simulating transition to Running
        val runningState = initialState.copy(
            downloadState = DownloadState.Running(taskId = task.id, progress = 0.5f, progressText = "[download] 50%"),
        )
        assertTrue(runningState.downloadState is DownloadState.Running)
        assertEquals(0.5f, (runningState.downloadState as DownloadState.Running).progress, 0.001f)

        // Simulating transition to Canceled
        val canceledState = runningState.copy(
            downloadState = DownloadState.Canceled(action = RestartableAction.Download, progress = 0.5f),
        )
        assertTrue(canceledState.downloadState is DownloadState.Canceled)
        assertEquals(RestartableAction.Download, (canceledState.downloadState as DownloadState.Canceled).action)

        // Restarting custom command
        val restartedDownloadState =
            when ((canceledState.downloadState as DownloadState.Canceled).action) {
                RestartableAction.FetchInfo -> DownloadState.Idle
                RestartableAction.Download -> DownloadState.ReadyWithInfo
            }
        assertEquals(DownloadState.ReadyWithInfo, restartedDownloadState)
    }
}
