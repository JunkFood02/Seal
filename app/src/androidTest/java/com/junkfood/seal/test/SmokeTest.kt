package com.junkfood.seal.test

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.junkfood.seal.player.PlayerViewModel
import com.junkfood.seal.player.PlaybackState
import com.junkfood.seal.js.JavaScriptRuntimeManager
import com.junkfood.seal.js.QuickJSRuntime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests for critical app functionality.
 * These are fast-running tests that verify core components work.
 *
 * Run with: ./gradlew connectedDebugAndroidTest
 * Or individual: ./gradlew connectedDebugAndroidTest --tests "com.junkfood.seal.test.SmokeTest*"
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    /**
     * Test QuickJS runtime initializes without crashing
     */
    @Test
    fun quickJsRuntimeInitialization() = runTest {
        // Note: QuickJS requires native libraries which must be present on device/emulator
        // This test verifies the Java wrapper loads correctly
        val available = QuickJSRuntime.isAvailable()
        if (available) {
            val runtime = QuickJSRuntime.getInstance(context)
            assertTrue(runtime.evaluate("2 + 2") { result ->
                assertEquals("4", result)
            })
        } else {
            // CI emulator should have QuickJS; skip if not present
            println("QuickJS not available - skipping runtime test")
        }
    }

    /**
     * Test JavaScriptRuntimeManager initializes and configures yt-dlp
     */
    @Test
    fun jsRuntimeManagerInitialization() = runTest {
        val manager = JavaScriptRuntimeManager
        // Should not crash; may fail if QuickJS not present
        val result = manager.initialize(context)
        assertTrue("JS Runtime should initialize", result || !QuickJSRuntime.isAvailable())
    }

    /**
     * Test PlayerViewModel can be instantiated and does not crash
     */
    @Test
    fun playerViewModelCreation() = runTest {
        val viewModel = PlayerViewModel(context)
        assertNotNull(viewModel.player)

        val initialState = viewModel.playbackState.first()
        assertTrue(initialState in listOf(PlaybackState.IDLE, PlaybackState.READY))

        viewModel.onCleared()
    }

    /**
     * Test database exists and can be opened
     */
    @Test
    fun databaseAvailability() {
        val db = com.junkfood.seal.database.AppDatabase.getInstance(context)
        assertNotNull(db)
        db.close()
    }

    /**
     * Test basic yt-dlp request creation (without execution)
     */
    @Test
    fun ytDlpRequestCreation() {
        val request = com.yausername.youtubedl_android.YoutubeDLRequest("https://example.com")
        assertNotNull(request)
        assertTrue(request.urls.isNotEmpty())
    }

    /**
     * Test file util operations (non-destructive)
     */
    @Test
    fun fileUtilBasicOperations() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val configFile = context.getConfigFile()
        assertTrue("Config file should be writable", configFile.canWrite())
    }

    /**
     * Test preference util loads defaults
     */
    @Test
    fun preferencesHaveDefaults() {
        val preferences = com.junkfood.seal.util.DownloadUtil.DownloadPreferences.createFromPreferences()
        assertNotNull(preferences)
        assertFalse(preferences.extractAudio) // default
    }
}
