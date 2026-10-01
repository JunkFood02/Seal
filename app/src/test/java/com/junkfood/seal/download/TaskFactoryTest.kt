package com.junkfood.seal.download

import com.junkfood.seal.database.objects.CommandTemplate
import com.junkfood.seal.download.Task.DownloadState
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.PlaylistEntry
import com.junkfood.seal.util.PlaylistResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskFactoryTest {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @Test
    fun testCreateWithCustomCommand() {
        val template =
            CommandTemplate(id = 1, name = "Extract Audio Only", template = "-x --audio-format mp3")
        val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val preferences = DownloadUtil.DownloadPreferences.EMPTY

        val taskWithState =
            TaskFactory.createWithCustomCommand(
                url = url,
                template = template,
                preferences = preferences,
            )

        val task = taskWithState.task
        val state = taskWithState.state

        assertEquals(url, task.url)
        assertTrue(task.type is Task.TypeInfo.CustomCommand)
        assertEquals(template, (task.type as Task.TypeInfo.CustomCommand).template)
        assertEquals(DownloadState.Idle, state.downloadState)
        assertEquals(template.name, state.viewState.title)
        assertEquals(url, state.viewState.url)
    }

    @Test
    fun testCreateWithPlaylistResult() {
        val playlistUrl = "https://www.youtube.com/playlist?list=PLtest"
        val entry1 =
            PlaylistEntry(
                id = "vid1",
                title = "Video 1",
                duration = 120.0,
                url = "https://youtube.com/watch?v=vid1",
            )
        val entry2 =
            PlaylistEntry(
                id = "vid2",
                title = "Video 2",
                duration = 240.0,
                url = "https://youtube.com/watch?v=vid2",
            )
        val playlistResult = PlaylistResult(title = "My Playlist", entries = listOf(entry1, entry2))
        val preferences = DownloadUtil.DownloadPreferences.EMPTY

        val tasks =
            TaskFactory.createWithPlaylistResult(
                playlistUrl = playlistUrl,
                indexList = listOf(1, 2),
                playlistResult = playlistResult,
                preferences = preferences,
            )

        assertEquals(2, tasks.size)
        assertEquals("Video 1", tasks[0].state.viewState.title)
        assertEquals(120, tasks[0].state.viewState.duration)
        assertEquals(Task.TypeInfo.Playlist(1), tasks[0].task.type)

        assertEquals("Video 2", tasks[1].state.viewState.title)
        assertEquals(240, tasks[1].state.viewState.duration)
        assertEquals(Task.TypeInfo.Playlist(2), tasks[1].task.type)
    }

    @Test
    fun testTaskSerialization() {
        val template =
            CommandTemplate(id = 42, name = "Best Quality", template = "-f bestvideo+bestaudio")
        val url = "https://example.com/video"
        val preferences = DownloadUtil.DownloadPreferences.EMPTY

        val originalTask =
            Task(url = url, type = Task.TypeInfo.CustomCommand(template), preferences = preferences)

        val originalState =
            Task.State(
                downloadState = DownloadState.Idle,
                videoInfo = null,
                viewState = Task.ViewState(url = url, title = "Best Quality"),
            )

        val encodedTask = json.encodeToString(originalTask)
        val decodedTask = json.decodeFromString<Task>(encodedTask)

        assertEquals(originalTask.id, decodedTask.id)
        assertEquals(originalTask.url, decodedTask.url)
        assertEquals(originalTask.type, decodedTask.type)

        val encodedState = json.encodeToString(originalState)
        val decodedState = json.decodeFromString<Task.State>(encodedState)

        assertEquals(originalState.downloadState, decodedState.downloadState)
        assertEquals(originalState.viewState.title, decodedState.viewState.title)
    }
}
