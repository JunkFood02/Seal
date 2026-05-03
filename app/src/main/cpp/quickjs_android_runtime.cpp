#include "quickjs_android.h"
#include <jni.h>
#include <android/log.h>
#include <sstream>
#include <mutex>

#define LOG_TAG "QuickJS"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// QuickJS headers - Placeholder implementation
// In production, these would come from the actual QuickJS source
namespace quickjs_ns {

struct JSRuntime;
struct JSContext;
struct JSValue;

// Minimal stubs for compilation (actual QuickJS source would provide these)
typedef struct JSRuntime JSRuntime;
typedef struct JSContext JSContext;
typedef struct JSValue JSValue;

// Placeholder: In real implementation, include QuickJS headers
extern "C" {
    JSRuntime* JS_NewRuntime(void);
    void JS_FreeRuntime(JSRuntime* rt);
    JSContext* JS_NewContext(JSRuntime* rt);
    void JS_FreeContext(JSContext* ctx);
    JSValue JS_Eval(JSContext* ctx, const char* buf, int buf_len,
                    const char* filename, int eval_flags);
    JSValue JS_GetException(JSContext* ctx);
    const char* JS_ToCString(JSContext* ctx, JSValue val);
    void JS_FreeCString(JSContext* ctx, const char* str);
    void JS_FreeValue(JSContext* ctx, JSValue val);
    JSValue JS_GetGlobalObject(JSContext* ctx);
    JSValue JS_NewString(JSContext* ctx, const char* str);
    void JS_SetPropertyStr(JSContext* ctx, JSValue obj, const char* prop, JSValue val);
    void JS_SetMemoryLimit(JSRuntime* rt, size_t limit);
    void JS_SetGCThreshold(JSRuntime* rt, size_t threshold);
}

// Test stub implementation (for architecture demonstration)
class QuickJSRuntimeImpl {
public:
    JSRuntime* rt = nullptr;
    JSContext* ctx = nullptr;
    std::string lastError;

    QuickJSRuntimeImpl() : rt(nullptr), ctx(nullptr) {}

    ~QuickJSRuntimeImpl() {
        if (ctx) JS_FreeContext(ctx);
        if (rt) JS_FreeRuntime(rt);
    }

    bool init() {
        rt = JS_NewRuntime();
        if (!rt) {
            lastError = "JS_NewRuntime failed";
            return false;
        }
        JS_SetMemoryLimit(rt, 256 * 1024 * 1024);
        JS_SetGCThreshold(rt, 64 * 1024 * 1024);

        ctx = JS_NewContext(rt);
        if (!ctx) {
            lastError = "JS_NewContext failed";
            return false;
        }
        return true;
    }

    std::string eval(const std::string& code, const std::string& filename) {
        if (!ctx) {
            lastError = "Runtime not initialized";
            return "";
        }

        JSValue ret = JS_Eval(ctx, code.c_str(), code.length(), filename.c_str(), 0);

        if (JS_IsException(ret)) {
            JSValue exc = JS_GetException(ctx);
            const char* excStr = JS_ToCString(ctx, exc);
            lastError = "JS Exception: ";
            lastError += excStr ? excStr : "unknown";
            JS_FreeCString(ctx, excStr);
            JS_FreeValue(ctx, exc);
            JS_FreeValue(ctx, ret);
            return "";
        }

        const char* result = JS_ToCString(ctx, ret);
        std::string out = result ? result : "";
        JS_FreeCString(ctx, result);
        JS_FreeValue(ctx, ret);
        return out;
    }

    void setGlobal(const std::string& name, const std::string& value) {
        if (!ctx) return;
        JSValue global = JS_GetGlobalObject(ctx);
        JSValue jsVal = JS_NewString(ctx, value.c_str());
        JS_SetPropertyStr(ctx, global, name.c_str(), jsVal);
        JS_FreeValue(ctx, global);
    }

    std::string getGlobal(const std::string& name) {
        if (!ctx) return "";
        JSValue global = JS_GetGlobalObject(ctx);
        JSValue val = JS_GetPropertyStr(ctx, global, name.c_str());
        const char* str = JS_ToCString(ctx, val);
        std::string result = str ? str : "";
        JS_FreeCString(ctx, str);
        JS_FreeValue(ctx, val);
        JS_FreeValue(ctx, global);
        return result;
    }
};

} // namespace quickjs_ns

// Static member definitions
std::unordered_map<jlong, QuickJSAndroidRuntime*> QuickJSRuntimeBridge::s_runtimes;
std::mutex QuickJSRuntimeBridge::s_runtimeMutex;
jlong QuickJSRuntimeBridge::s_nextId = 0;

/**
 * Native implementation for QuickJS JNI bridge
 *
 * This provides C++ implementation of QuickJS operations that are
 * called from Kotlin/Java via JNI. The bridge manages runtime instances
 * and coordinates with the yt-dlp library for JavaScript challenge solving.
 *
 * Architecture:
 *   Java/Kotlin → JNI → C++ QuickJS Bridge → QuickJS Library
 */

// Initialize a new QuickJS runtime
extern "C" JNIEXPORT jlong JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeInit(JNIEnv* env, jobject thiz) {
    LOGI("QuickJSRuntime: Initializing native runtime");

    QuickJSAndroidRuntime* runtime = new QuickJSAndroidRuntime();
    if (!runtime->init()) {
        LOGE("QuickJSRuntime: Failed to initialize: %s", runtime->getError().c_str());
        delete runtime;
        return 0;
    }

    std::lock_guard<std::mutex> lock(s_runtimeMutex);
    jlong id = ++s_nextId;
    s_runtimes[id] = runtime;

    LOGI("QuickJSRuntime: Initialized with ID %lld", id);
    return id;
}

// Evaluate JavaScript code
extern "C" JNIEXPORT jboolean JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeEvaluate(
    JNIEnv* env,
    jobject thiz,
    jlong ptr,
    jstring jScript,
    jstring jFilename,
    jobject jResultCallback
) {
    QuickJSAndroidRuntime* runtime = QuickJSRuntimeBridge::getRuntime(ptr);
    if (!runtime) {
        LOGE("QuickJSRuntime: Invalid runtime pointer %lld", ptr);
        return JNI_FALSE;
    }

    const char* script = env->GetStringUTFChars(jScript, nullptr);
    const char* filename = env->GetStringUTFChars(jFilename, nullptr);

    LOGI("QuickJSRuntime: Evaluating script (len=%zu)", strlen(script));

    std::string result = runtime->eval(script, filename ? filename : "<input>");

    env->ReleaseStringUTFChars(jScript, script);
    if (filename) env->ReleaseStringUTFChars(jFilename, filename);

    if (runtime->hasError()) {
        LOGE("QuickJSRuntime: Evaluation error: %s", runtime->getError().c_str());
        return JNI_FALSE;
    }

    LOGI("QuickJSRuntime: Evaluation result: %s", result.c_str());

    // Invoke callback if provided
    if (jResultCallback != nullptr) {
        jclass callbackClass = env->GetObjectClass(jResultCallback);
        jmethodID onResultMethod = env->GetMethodID(
            callbackClass,
            "onResult",
            "(Ljava/lang/String;)V"
        );

        if (onResultMethod != nullptr) {
            jstring jResult = env->NewStringUTF(result.c_str());
            env->CallVoidMethod(jResultCallback, onResultMethod, jResult);
            env->DeleteLocalRef(jResult);
        }
        env->DeleteLocalRef(callbackClass);
    }

    return JNI_TRUE;
}

// Set global variable in runtime
extern "C" JNIEXPORT void JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeSetVariable(
    JNIEnv* env,
    jobject thiz,
    jlong ptr,
    jstring jName,
    jstring jValue
) {
    QuickJSAndroidRuntime* runtime = QuickJSRuntimeBridge::getRuntime(ptr);
    if (!runtime) return;

    const char* name = env->GetStringUTFChars(jName, nullptr);
    const char* value = env->GetStringUTFChars(jValue, nullptr);

    runtime->setGlobal(name, value);

    env->ReleaseStringUTFChars(jName, name);
    env->ReleaseStringUTFChars(jValue, value);
}

// Get global variable from runtime
extern "C" JNIEXPORT jstring JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeGetVariable(
    JNIEnv* env,
    jobject thiz,
    jlong ptr,
    jstring jName
) {
    QuickJSAndroidRuntime* runtime = QuickJSRuntimeBridge::getRuntime(ptr);
    if (!runtime) return nullptr;

    const char* name = env->GetStringUTFChars(jName, nullptr);
    std::string value = runtime->getGlobal(name);
    env->ReleaseStringUTFChars(jName, name);

    return env->NewStringUTF(value.c_str());
}

// Release runtime resources
extern "C" JNIEXPORT void JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeRelease(JNIEnv* env, jobject thiz, jlong ptr) {
    LOGI("QuickJSRuntime: Releasing runtime %lld", ptr);

    std::lock_guard<std::mutex> lock(s_runtimeMutex);
    auto it = s_runtimes.find(ptr);
    if (it != s_runtimes.end()) {
        delete it->second;
        s_runtimes.erase(it);
    }
}

// Get last error message
extern "C" JNIEXPORT jstring JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeGetLastError(
    JNIEnv* env,
    jobject thiz,
    jlong ptr
) {
    QuickJSAndroidRuntime* runtime = QuickJSRuntimeBridge::getRuntime(ptr);
    if (!runtime) {
        return env->NewStringUTF("Invalid runtime pointer");
    }
    return env->NewStringUTF(runtime->getError().c_str());
}

// Helper implementation
QuickJSAndroidRuntime* QuickJSRuntimeBridge::getRuntime(jlong id) {
    std::lock_guard<std::mutex> lock(s_runtimeMutex);
    auto it = s_runtimes.find(id);
    return (it != s_runtimes.end()) ? it->second : nullptr;
}
