#ifndef QUICKJS_JNI_H
#define QUICKJS_JNI_H

#include <jni.h>
#include <memory>
#include <string>

// Forward declaration
struct JSRuntime;

/**
 * QuickJS JNI Bridge for Android
 * Provides Java interface to QuickJS JavaScript runtime
 * Used by yt-dlp to execute JavaScript challenges
 */
class QuickJSBridge {
public:
    QuickJSBridge();
    ~QuickJSBridge();

    // Initialize QuickJS runtime
    bool initialize();

    // Evaluate JavaScript code
    std::string evaluate(const std::string& script, const std::string& filename = "<input>");

    // Set variable in runtime
    void setVariable(const std::string& name, const std::string& value);

    // Get variable from runtime
    std::string getVariable(const std::string& name);

    // Check if runtime is initialized
    bool isInitialized() const { return m_initialized; }

    // Get last error message
    std::string getLastError() const { return m_lastError; }

    // Release runtime resources
    void release();

private:
    JSRuntime* m_runtime;          // QuickJS runtime
    bool m_initialized;            // Initialization flag
    std::string m_lastError;       // Last error message

    // Helper methods
    void clearError();
};

/**
 * Native implementation for QuickJS runtime operations
 * Called from Java via JNI
 */
extern "C" {

// Package: com.junkfood.seal.js
// Class: QuickJSRuntime

JNIEXPORT jlong JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeInit(JNIEnv* env, jobject thiz);

JNIEXPORT jboolean JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeEvaluate(
    JNIEnv* env,
    jobject thiz,
    jlong ptr,
    jstring jScript,
    jstring jFilename,
    jobject jResultCallback
);

JNIEXPORT void JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeSetVariable(
    JNIEnv* env,
    jobject thiz,
    jlong ptr,
    jstring jName,
    jstring jValue
);

JNIEXPORT jstring JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeGetVariable(
    JNIEnv* env,
    jobject thiz,
    jlong ptr,
    jstring jName
);

JNIEXPORT void JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeRelease(JNIEnv* env, jobject thiz, jlong ptr);

JNIEXPORT jstring JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeGetLastError(JNIEnv* env, jobject thiz, jlong ptr);

} // extern "C"

#endif // QUICKJS_JNI_H
