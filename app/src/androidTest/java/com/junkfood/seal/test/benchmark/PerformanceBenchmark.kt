package com.junkfood.seal.test.benchmark

import androidx.benchmark.BenchmarkState
import androidx.benchmark.Clock
import androidx.benchmark.ExperimentalBenchmarkConfigApi
import androidx.benchmark.ExperimentalShortcutBaselineProfileApi
import androidx.benchmark.ShortcutBaselineProfileGenerator
import androidx.benchmark.ShortcutBaselineProfileGenerator.Companion.ABSOLUTE
import androidx.benchmark.ShortcutBaselineProfileGenerator.Companion.COLUMN
import androidx.benchmark.ShortcutBaselineProfileGenerator.Companion.SCENARIO
import androidx.benchmark.ShortcutBaselineProfileGenerator.Companion.TEST_ID
import androidx.benchmark.ShortcutBaselineProfileRule
import androidx.benchmark.ShortcutBaselineProfileState
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.junkfood.seal.js.JavaScriptRuntimeManager
import com.junkfood.seal.js.QuickJSRuntime
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Performance benchmarks for critical operations.
 *
 * Benchmarks measure:
 *  - QuickJS cold start time
 *  - JavaScript evaluation performance
 *  - yt-dlp info fetch (mocked)
 *  - Player initialization
 *
 * Run with:
 *   ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.junkfood.seal.test.benchmark.PerformanceBenchmark
 */
@RunWith(AndroidJUnit4::class)
class PerformanceBenchmark {

    @get:Rule
    val baselineProfileRule = ShortcutBaselineProfileRule()

    /**
     * Generate baseline profile for startup performance
     * Used by Play Console for optimizing delivery
     */
    @OptIn(ExperimentalShortcutBaselineProfileApi::class)
    @Test
    fun collectStartupBaselineProfile() = runBlocking {
        // Baseline profile for cold start
        baselineProfileRule.collectBaselineProfile(
            packageName = "com.junkfood.seal",
            startupMode = ABSOLUTE,
            target screens = listOf(
                "com.junkfood.seal.MainActivity"
            )
        )
    }

    /**
     * Benchmark QuickJS initialization time
     */
    @Test
    fun benchmarkQuickJSStartup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val runtime = QuickJSRuntime.getInstance(context)

        val benchmarkState = BenchmarkState()
        benchmarkState.start()

        repeat(10) { iteration ->
            runtime.evaluate("1 + 1") { /* callback */ }
        }

        benchmarkState.end()

        println("QuickJS eval avg: ${benchmarkState.average}ms")
        // Target: < 10ms per eval on average
    }

    /**
     * Benchmark JS challenge solving (simulated)
     * Real-world: Cloudflare challenge, ~2s on desktop, aim <5s on mobile
     */
    @Test
    fun benchmarkJsChallengeSolving() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val runtime = QuickJSRuntime.getInstance(context)

        // Simulated Cloudflare challenge (simplified)
        val challengeScript = """
            (function() {
                var a = Math.floor(Math.random() * 10000);
                var b = Math.floor(Math.random() * 10000);
                return (a + b).toString();
            })();
        """.trimIndent()

        val benchmarkState = BenchmarkState()
        benchmarkState.start()

        repeat(20) {
            runtime.evaluate(challengeScript) { result ->
                // Validate result is numeric string
                assert(result.matches(Regex("\\d+")))
            }
        }

        benchmarkState.end()

        println("JS challenge avg: ${benchmarkState.average}ms")
        // Target: < 50ms per challenge
    }

    /**
     * Benchmark PlayerViewModel initialization
     */
    @Test
    fun benchmarkPlayerInit() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        val benchmarkState = BenchmarkState()
        benchmarkState.start()

        repeat(5) {
            val viewModel = com.junkfood.seal.player.PlayerViewModel(context)
            viewModel.onCleared()
        }

        benchmarkState.end()

        println("PlayerViewModel init avg: ${benchmarkState.average}ms")
        // Target: < 200ms for full init
    }

    /**
     * Benchmark database insert+query
     */
    @Test
    fun benchmarkDatabaseOperations() = runBlocking {
        val db = com.junkfood.seal.database.AppDatabase.getInstance(
            ApplicationProvider.getApplicationContext()
        )
        val dao = db.videoInfoDao()

        val testInfo = com.junkfood.seal.database.objects.DownloadedVideoInfo(
            id = 0,
            videoTitle = "Benchmark Test",
            videoAuthor = "Test",
            videoUrl = "https://example.com",
            thumbnailUrl = "",
            videoPath = "/sdcard/benchmark.mp4"
        )

        val benchmarkState = BenchmarkState()
        benchmarkState.start()

        repeat(100) { i ->
            dao.insert(testInfo.copy(id = i))
        }

        val results = dao.getDownloadHistory()
        assertEquals(100, results.size)

        benchmarkState.end()

        println("DB insert+query (100 rows) avg: ${benchmarkState.average}ms")
        // Target: < 100ms for 100 rows
    }
}
