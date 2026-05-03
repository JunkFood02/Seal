package com.junkfood.seal.js

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * QuickJS JavaScript Runtime Bridge for Android
 *
 * Provides embedded JavaScript execution capability for yt-dlp to solve
 * web challenges (Cloudflare, reCAPTCHA, etc.) directly on device.
 *
 * Architecture:
 *  - Native QuickJS library compiled via NDK (CMake)
 *  - JNI bridge for Java-Kotlin interop
 *  - Thread-safe runtime management
 *  - Memory-limited isolated execution contexts
 *
 * Integration with yt-dlp:
 *  yt-dlp requirements --js-runtimes quickjs:/path/to/quickjs
 *
 * @property context Application context for native library loading
 */
class QuickJSRuntime private constructor(private val context: Context) : AutoCloseable {

    companion object {
        private const val TAG = "QuickJSRuntime"
        private const val QUICKJS_LIB_DIR = "quickjs"
        private const val QUICKJS_LIB_NAME = "libquickjs.so"

        @Volatile
        private var instance: QuickJSRuntime? = null

        /**
         * Get singleton instance
         */
        fun getInstance(context: Context): QuickJSRuntime {
            return instance ?: synchronized(this) {
                instance ?: QuickJSRuntime(context.applicationContext).also { instance = it }
            }
        }

        /**
         * Check if QuickJS native library is available
         */
        fun isAvailable(): Boolean {
            return try {
                System.loadLibrary("quickjs_jni")
                true
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "QuickJS native library not found", e)
                false
            }
        }

        /**
         * Initialize QuickJS native libraries
         * Extracts bundled native binaries to app storage
         */
        fun initialize(context: Context): Boolean {
            return try {
                // Load JNI bridge library
                System.loadLibrary("quickjs_jni")

                // Ensure QuickJS runtime binary is available
                val quickjsDir = File(context.applicationInfo.nativeLibraryDir, QUICKJS_LIB_DIR)
                if (!quickjsDir.exists()) {
                    quickjsDir.mkdirs()
                    // Copy QuickJS binary from assets if packaged
                    copyQuickJSBinary(context, quickjsDir)
                }

                Log.i(TAG, "QuickJS initialized successfully")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize QuickJS", e)
                false
            }
        }

        private fun copyQuickJSBinary(context: Context, destDir: File) {
            // In production, QuickJS binary would be packaged in assets/quickjs/
            // This is a stub - actual implementation would extract native binary
            Log.i(TAG, "QuickJS binary would be copied to ${destDir.absolutePath}")
        }
    }

    // Native pointer to C++ QuickJSBridge instance (0 = invalid)
    private var nativePtr: Long = 0

    /**
     * Runtime state
     */
    enum class State {
        UNINITIALIZED,
        READY,
        ERROR
    }

    private var state: State = State.UNINITIALIZED
    private var lastError: String? = null

    init {
        if (!initialize(context)) {
            state = State.ERROR
            lastError = "Failed to initialize QuickJS native bridge"
        } else {
            state = State.READY
        }
    }

    /**
     * Execute JavaScript code and get result
     *
     * @param script JavaScript code to execute
     * @param filename Optional filename for error reporting
     * @param callback Optional result callback
     * @return true if execution succeeded, false otherwise
     */
    fun evaluate(script: String, filename: String = "<input>", callback: ((String) -> Unit)? = null): Boolean {
        if (state != State.READY || nativePtr == 0L) {
            lastError = "Runtime not ready"
            return false
        }

        return try {
            val success = nativeEvaluate(nativePtr, script, filename, callback)
            if (!success) {
                lastError = getLastError() ?: "Unknown evaluation error"
            }
            success
        } catch (e: Exception) {
            lastError = "Exception during evaluation: ${e.message}"
            false
        }
    }

    /**
     * Set a global variable in the JavaScript runtime
     */
    fun setVariable(name: String, value: String) {
        if (state != State.READY || nativePtr == 0L) return
        nativeSetVariable(nativePtr, name, value)
    }

    /**
     * Get a global variable from the JavaScript runtime
     */
    fun getVariable(name: String): String? {
        if (state != State.READY || nativePtr == 0L) return null
        return nativeGetVariable(nativePtr, name)
    }

    /**
     * Get the last error message
     */
    fun getLastError(): String? = lastError

    /**
     * Release native resources
     */
    override fun close() {
        if (nativePtr != 0L) {
            nativeRelease(nativePtr)
            nativePtr = 0
        }
        state = State.UNINITIALIZED
    }

    // Native method declarations
    private external fun nativeInit(): Long
    private external fun nativeEvaluate(
        ptr: Long,
        script: String,
        filename: String,
        callback: ((String) -> Unit)?
    ): Boolean
    private external fun nativeSetVariable(ptr: Long, name: String, value: String)
    private external fun nativeGetVariable(ptr: Long, name: String): String
    private external fun nativeRelease(ptr: Long)
    private external fun nativeGetLastError(ptr: Long): String

    init {
        if (state == State.READY) {
            nativePtr = nativeInit()
            if (nativePtr == 0L) {
                state = State.ERROR
                lastError = "Failed to create native QuickJS instance"
            } else {
                Log.i(TAG, "QuickJS runtime created: ptr=0x${nativePtr.toString(16)}")
            }
        }
    }
}
