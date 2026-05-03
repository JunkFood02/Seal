package com.junkfood.seal.js

import android.content.Context
import android.util.Log
import com.junkfood.seal.App
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * JavaScript Runtime Manager for yt-dlp
 *
 * Manages JavaScript runtime instances for executing yt-dlp's
 * JavaScript challenges (e.g., Cloudflare, puzzle challenges).
 *
 * yt-dlp integration:
 *   YoutubeDLRequest().addOption("--js-runtimes", "quickjs:/path/to/quickjs")
 *
 * Implementation based on yt-dlp's external JS runtime specification:
 * https://github.com/yt-dlp/yt-dlp/blob/master/CONTRIBUTING.md#javascript-execution
 */
object JavaScriptRuntimeManager {

    private const val TAG = "JsRuntimeManager"
    private const val QUICKJS_BIN_NAME = "quickjs"
    private const val RUNTIME_CONFIG_KEY = "js_runtime_path"

    /**
     * QuickJS runtime configuration
     */
    data class RuntimeConfig(
        val type: RuntimeType,
        val path: String,
        val version: String = "2025-09-13"
    )

    enum class RuntimeType {
        QUICKJS,
        NODEJS,
        DENO
    }

    private var runtimeConfig: RuntimeConfig? = null
    private var isQuickJSAvailable: Boolean = false

    /**
     * Initialize JavaScript runtime support
     * Call during app startup (App.onCreate)
     */
    suspend fun initialize(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if QuickJS is available
            isQuickJSAvailable = QuickJSRuntime.isAvailable()

            if (isQuickJSAvailable) {
                // Initialize QuickJS
                QuickJSRuntime.initialize(context)
                val quickjsPath = getQuickJSBinaryPath(context)
                runtimeConfig = RuntimeConfig(
                    type = RuntimeType.QUICKJS,
                    path = quickjsPath
                )
                Log.i(TAG, "QuickJS runtime initialized at $quickjsPath")

                // Configure yt-dlp to use QuickJS
                configureYtDlpForJsRuntime()
            } else {
                Log.w(TAG, "QuickJS native library not available")
            }

            isQuickJSAvailable
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize JavaScript runtime", e)
            false
        }
    }

    /**
     * Get path to QuickJS binary
     */
    private fun getQuickJSBinaryPath(context: Context): String {
        val nativeLibDir = context.applicationInfo.nativeLibraryDir
        val quickjsDir = File(nativeLibDir, "quickjs")

        // Ensure directory exists
        if (!quickjsDir.exists()) {
            quickjsDir.mkdirs()
        }

        // The actual QuickJS binary (libquickjs.so is the JNI bridge,
        // but yt-dlp needs a standalone executable or script)
        // For Android, we use a shell script wrapper
        return File(quickjsDir, QUICKJS_BIN_NAME).absolutePath
    }

    /**
     * Configure youtubedl-android to use our JS runtime
     *
     * yt-dlp format: --js-runtimes <runtime_type>:<path>
     * Examples:
     *   --js-runtimes quickjs:/data/data/.../quickjs
     *   --js-runtimes node:/data/data/.../nodejs
     */
    private fun configureYtDlpForJsRuntime() {
        runtimeConfig?.let { config ->
            val jsRuntimeOption = "--js-runtimes"
            val jsRuntimeValue = when (config.type) {
                RuntimeType.QUICKJS -> "quickjs:${config.path}"
                RuntimeType.NODEJS -> "nodejs:${config.path}"
                RuntimeType.DENO -> "deno:${config.path}"
            }

            // Set as global configuration for all yt-dlp requests
            YoutubeDL.getInstance().addGlobalOption(jsRuntimeOption, jsRuntimeValue)
            Log.d(TAG, "Configured yt-dlp with JS runtime: $jsRuntimeValue")
        }
    }

    /**
     * Apply JS runtime configuration to a request
     * Useful for per-request control (custom commands)
     */
    fun applyToRequest(request: YoutubeDLRequest) {
        runtimeConfig?.let { config ->
            val jsRuntimeValue = when (config.type) {
                RuntimeType.QUICKJS -> "quickjs:${config.path}"
                RuntimeType.NODEJS -> "nodejs:${config.path}"
                RuntimeType.DENO -> "deno:${config.path}"
            }
            request.addOption("--js-runtimes", jsRuntimeValue)
        }
    }

    /**
     * Check if JavaScript runtime is available
     */
    fun isAvailable(): Boolean = isQuickJSAvailable

    /**
     * Execute JavaScript directly (for custom operations)
     */
    suspend fun executeScript(script: String, callback: ((String) -> Unit)? = null): String? {
        return withContext(Dispatchers.IO) {
            QuickJSRuntime.getInstance(App.context).use { runtime ->
                if (runtime.evaluate(script, "custom.js", callback)) {
                    // Result delivered via callback
                    "success"
                } else {
                    Log.e(TAG, "Script execution failed: ${runtime.getLastError()}")
                    null
                }
            }
        }
    }

    /**
     * Get current runtime configuration
     */
    fun getRuntimeConfig(): RuntimeConfig? = runtimeConfig

    /**
     * Cleanup resources
     */
    fun shutdown() {
        QuickJSRuntime.getInstance(App.context).close()
        isQuickJSAvailable = false
        runtimeConfig = null
    }
}
