package com.junkfood.seal.download

import com.junkfood.seal.database.objects.CommandTemplate
import com.junkfood.seal.download.Task.DownloadState
import com.junkfood.seal.download.Task.RestartableAction
import com.junkfood.seal.util.DownloadUtil
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskBackupRestoreTest {

    private val json = Json {
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
        encodeDefaults = true
    }

    @Test
    fun testTaskMapSerializationAndDeserialization() {
        val task1 = Task(
            url = "https://example.com/video1",
            type = Task.TypeInfo.URL,
            preferences = DownloadUtil.DownloadPreferences.EMPTY,
        )
        val state1 = Task.State(
            downloadState = DownloadState.Running(taskId = task1.id, progress = 0.72f),
            videoInfo = null,
            viewState = Task.ViewState(url = task1.url, title = "Video 1"),
        )

        val template = CommandTemplate(id = 2, name = "Extract Audio", template = "-x")
        val task2 = Task(
            url = "https://example.com/video2",
            type = Task.TypeInfo.CustomCommand(template),
            preferences = DownloadUtil.DownloadPreferences.EMPTY,
        )
        val state2 = Task.State(
            downloadState = DownloadState.Canceled(action = RestartableAction.Download, progress = 0.3f),
            videoInfo = null,
            viewState = Task.ViewState(url = task2.url, title = "Custom Command"),
        )

        val task3 = Task(
            url = "https://example.com/playlist",
            type = Task.TypeInfo.Playlist(index = 3),
            preferences = DownloadUtil.DownloadPreferences.EMPTY,
        )
        val state3 = Task.State(
            downloadState = DownloadState.Idle,
            videoInfo = null,
            viewState = Task.ViewState(url = task3.url, title = "Playlist item 3"),
        )

        val originalMap: Map<Task, Task.State> = mapOf(
            task1 to state1,
            task2 to state2,
            task3 to state3,
        )

        val encoded = json.encodeToString<Map<Task, Task.State>>(originalMap)
        val decoded = json.decodeFromString<Map<Task, Task.State>>(encoded)

        assertEquals(originalMap.size, decoded.size)

        val decodedTask1 = decoded.keys.find { it.id == task1.id }
        assertTrue(decodedTask1 != null)
        val decodedState1 = decoded[decodedTask1]
        assertTrue(decodedState1?.downloadState is DownloadState.Running)
        assertEquals(0.72f, (decodedState1?.downloadState as DownloadState.Running).progress, 0.001f)

        val decodedTask2 = decoded.keys.find { it.id == task2.id }
        assertTrue(decodedTask2 != null)
        assertTrue(decodedTask2?.type is Task.TypeInfo.CustomCommand)
        val decodedState2 = decoded[decodedTask2]
        assertTrue(decodedState2?.downloadState is DownloadState.Canceled)
        assertEquals(RestartableAction.Download, (decodedState2?.downloadState as DownloadState.Canceled).action)

        val decodedTask3 = decoded.keys.find { it.id == task3.id }
        assertTrue(decodedTask3 != null)
        assertEquals(Task.TypeInfo.Playlist(3), decodedTask3?.type)
        val decodedState3 = decoded[decodedTask3]
        assertEquals(DownloadState.Idle, decodedState3?.downloadState)
    }

    @Test
    fun testEnqueueFromBackupRecoveryLogic() {
        val prefs = DownloadUtil.DownloadPreferences.EMPTY
        val taskIdle = Task(url = "https://example.com/idle", preferences = prefs)
        val stateIdle = Task.State(DownloadState.Idle, null, Task.ViewState(title = "Idle Task"))

        val taskFetching = Task(url = "https://example.com/fetching", preferences = prefs)
        val stateFetching = Task.State(DownloadState.FetchingInfo(taskId = taskFetching.id), null, Task.ViewState(title = "Fetching Task"))

        val taskReady = Task(url = "https://example.com/ready", preferences = prefs)
        val stateReady = Task.State(DownloadState.ReadyWithInfo, null, Task.ViewState(title = "Ready Task"))

        val taskRunning = Task(url = "https://example.com/running", preferences = prefs)
        val stateRunning = Task.State(DownloadState.Running(taskId = taskRunning.id, progress = 0.5f), null, Task.ViewState(title = "Running Task"))

        val taskCompleted = Task(url = "https://example.com/completed", preferences = prefs)
        val stateCompleted = Task.State(DownloadState.Completed(filePath = "/path"), null, Task.ViewState(title = "Completed Task"))

        val taskError = Task(url = "https://example.com/error", preferences = prefs)
        val stateError = Task.State(DownloadState.Error(throwable = Throwable("network"), action = RestartableAction.Download), null, Task.ViewState(title = "Error Task"))

        val backupMap = mapOf(
            taskIdle to stateIdle,
            taskFetching to stateFetching,
            taskReady to stateReady,
            taskRunning to stateRunning,
            taskCompleted to stateCompleted,
            taskError to stateError,
        )

        // Simulating the recovery logic in DownloaderV2Impl.enqueueFromBackup()
        val recoveredTasks =
            backupMap
                .filter { it.value.downloadState !is DownloadState.Completed }
                .mapValues { (_, state) ->
                    val preState = state.downloadState
                    val downloadState =
                        when (preState) {
                            is DownloadState.FetchingInfo,
                            DownloadState.Idle -> {
                                DownloadState.Canceled(action = RestartableAction.FetchInfo)
                            }
                            is DownloadState.Running -> {
                                DownloadState.Canceled(action = RestartableAction.Download, progress = preState.progress)
                            }
                            DownloadState.ReadyWithInfo -> {
                                DownloadState.Canceled(action = RestartableAction.Download, progress = null)
                            }
                            else -> preState
                        }
                    state.copy(downloadState = downloadState)
                }

        // 1. Completed tasks are filtered out
        assertFalse(recoveredTasks.containsKey(taskCompleted))
        assertEquals(5, recoveredTasks.size)

        // 2. Idle tasks become Canceled(FetchInfo)
        assertEquals(DownloadState.Canceled(action = RestartableAction.FetchInfo), recoveredTasks[taskIdle]?.downloadState)

        // 3. FetchingInfo tasks become Canceled(FetchInfo)
        assertEquals(DownloadState.Canceled(action = RestartableAction.FetchInfo), recoveredTasks[taskFetching]?.downloadState)

        // 4. ReadyWithInfo tasks become Canceled(Download, null)
        assertEquals(DownloadState.Canceled(action = RestartableAction.Download, progress = null), recoveredTasks[taskReady]?.downloadState)

        // 5. Running tasks become Canceled(Download, progress)
        val recoveredRunning = recoveredTasks[taskRunning]?.downloadState as DownloadState.Canceled
        assertEquals(RestartableAction.Download, recoveredRunning.action)
        assertEquals(0.5f, recoveredRunning.progress ?: 0f, 0.001f)

        // 6. Error state is preserved
        assertTrue(recoveredTasks[taskError]?.downloadState is DownloadState.Error)
    }
}
