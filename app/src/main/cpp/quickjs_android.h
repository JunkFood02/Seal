#ifndef QUICKJS_ANDROID_H
#define QUICKJS_ANDROID_H

#include <memory>
#include <string>
#include <unordered_map>
#include <mutex>

// QuickJS runtime wrapper for Android
class QuickJSAndroidRuntime {
public:
    // Constructor/Destructor
    QuickJSAndroidRuntime();
    ~QuickJSAndroidRuntime();

    // Initialize the runtime with optional memory limits
    bool initialize(size_t memoryLimitMB = 256);

    // Execute JavaScript code
    std::string evaluate(const std::string& code, const std::string& filename = "<input>");

    // Variable management
    void setGlobal(const std::string& name, const std::string& value);
    std::string getGlobal(const std::string& name);

    // Module support
    bool loadModule(const std::string& moduleName, const std::string& code);

    // Cleanup
    void release();

    // Error handling
    bool hasError() const { return !m_lastError.empty(); }
    std::string getError() const { return m_lastError; }

private:
    class Impl;
    std::unique_ptr<Impl> m_impl;
};

/**
 * QuickJS Android Bridge for yt-dlp
 *
 * This class provides a Java Native Interface (JNI) bridge between
 * Android/Java and the QuickJS JavaScript engine. It manages the
 * lifecycle of QuickJS runtimes and provides thread-safe operations.
 *
 * Usage:
 *   long ptr = QuickJSRuntime.nativeInit();
 *   QuickJSRuntime.nativeEvaluate(ptr, "1+1", "test.js", callback);
 *   QuickJSRuntime.nativeRelease(ptr);
 */
class QuickJSRuntimeBridge {
public:
    // Create new runtime instance
    static jlong createRuntime(JNIEnv* env);

    // Evaluate script in runtime
    static jboolean evaluate(
        JNIEnv* env,
        jlong runtimePtr,
        jstring jScript,
        jstring jFilename,
        jobject jCallback
    );

    // Set global variable
    static void setVariable(JNIEnv* env, jlong runtimePtr, jstring jName, jstring jValue);

    // Get global variable
    static jstring getVariable(JNIEnv* env, jlong runtimePtr, jstring jName);

    // Release runtime
    static void releaseRuntime(JNIEnv* env, jlong runtimePtr);

    // Get last error
    static jstring getLastError(JNIEnv* env, jlong runtimePtr);

private:
    static std::unordered_map<jlong, QuickJSAndroidRuntime*> s_runtimes;
    static std::mutex s_runtimeMutex;
    static jlong s_nextId;

    // Helper to find runtime
    static QuickJSAndroidRuntime* getRuntime(jlong id);
};

#endif // QUICKJS_ANDROID_H
