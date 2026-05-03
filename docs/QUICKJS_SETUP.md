# QuickJS Integration Guide for Seal

## Overview

This guide documents the complete process of integrating QuickJS JavaScript runtime into Seal for executing yt-dlp's JavaScript challenges (Cloudflare, reCAPTCHA, etc.) directly on Android devices.

## Table of Contents
1. [Prerequisites](#1-prerequisites)
2. [QuickJS Overview](#2-quickjs-overview)
3. [Cross-Compilation for Android](#3-cross-compilation-for-android)
4. [Native Library Integration](#4-native-library-integration)
5. [JNI Bridge Design](#5-jni-bridge-design)
6. [Kotlin Wrapper](#6-kotlin-wrapper)
7. [yt-dlp Configuration](#7-yt-dlp-configuration)
8. [Testing](#8-testing)
9. [Troubleshooting](#9-troubleshooting)
10. [Performance Tuning](#10-performance-tuning)

---

## 1. Prerequisites

### Required Tools

| Tool | Version | Purpose |
|------|---------|---------|
| Android NDK | r25b+ | Native compilation |
| CMake | 3.22+ | Build system |
| Clang/LLVM | 17+ | C++ compiler |
| Git | 2.0+ | Source management |
| Gradle | 8.0+ | Android build |
| Python (optional) | 3.8+ | Build scripts |

**Install NDK via Android Studio:**
```
SDK Manager → SDK Tools → NDK (Side by side) → Checkbox → Apply
```

Or command line:
```bash
sdkmanager "ndk;25.2.9519653"
```

---

## 2. QuickJS Overview

**QuickJS** is a lightweight, embeddable JavaScript engine developed by Fabrice Bellard.

### Key Characteristics

- **Size**: ~367 KB for static library (libquickjs.a)
- **Memory**: Configurable limits (default: no limit)
- **Feature Support**: ES2023 + modules, async generators, BigInt, Annex B
- **Performance**: Executes 78,000 ECMAScript tests in ~2 minutes on desktop
- **License**: MIT

### QuickJS vs Node.js/Deno

```
┌─────────────┬────────────┬──────────────┬─────────────┐
│   Runtime   │    Size    │ Android ABI  │ Startup     │
├─────────────┼────────────┼──────────────┼─────────────┤
│ QuickJS     │   ~6 MB    │ arm, arm64,  │     ~300µs  │
│  (static)   │ (lib + code)│    x86, x86_64│            │
├─────────────┼────────────┼──────────────┼─────────────┤
│ Node.js     │   ~71 MB   │ arm64 only   │  ~2-3 sec   │
├─────────────┼────────────┼──────────────┼─────────────┤
│ Deno        │   ~95 MB   │ arm64 only   │  ~1-2 sec   │
└─────────────┴────────────┴──────────────┴─────────────┘
```

### Why for yt-dlp?

YouTube uses increasingly complex JavaScript challenges. yt-dlp requires an interpreter to:
1. Solve Cloudflare anti-bot challenges
2. Decrypt signature functions
3. Execute length-limited obfuscated code snippets

QuickJS is the only viable option for Android due to size and architecture support.

---

## 3. Cross-Compilation for Android

### 3.1 Fetch QuickJS Source

```bash
# Clone QuickJS repository
git clone https://github.com/bellard/quickjs.git
cd quickjs

# Checkout stable release (recommended)
git checkout release-2025-09-13

# Directory structure:
quickjs/
├── CMakeLists.txt      # (we provide)
├── quickjs.c           # Main engine (67 KB)
├── quickjs.h
├── libunicode/         # Unicode tables
├── libregexp/          # ES2023 regex engine
└── [...]
```

### 3.2 Building Static Library

**Build script** (`scripts/build_quickjs.sh`):

```bash
#!/bin/bash
set -e

NDK_PATH=${ANDROID_NDK_HOME:-$HOME/Android/Sdk/ndk/25.2.9519653}
QUICKJS_VERSION="2025-09-13"
OUTPUT_DIR="app/src/main/cpp/quickjs"

echo "Building QuickJS $QUICKJS_VERSION for Android..."

# Clean previous build
rm -rf quickjs-build
mkdir -p quickjs-build
cd quickjs-build

# Configure with CMake
cmake \
  -DCMAKE_TOOLCHAIN_FILE=$NDK_PATH/build/cmake/android.toolchain.cmake \
  -DANDROID_ABI=arm64-v8a \
  -DANDROID_PLATFORM=android-24 \
  -DCMAKE_BUILD_TYPE=Release \
  ../quickjs

# Build static library
make quickjs -j$(nproc)

# Output: libquickjs.a
cp libquickjs.a $OUTPUT_DIR/arm64-v8a/libquickjs.a
```

**Cross-compile for all ABIs:**

```bash
for abi in armeabi-v7a arm64-v8a x86 x86_64; do
    cmake \
        -DCMAKE_TOOLCHAIN_FILE=$NDK_PATH/build/cmake/android.toolchain.cmake \
        -DANDROID_ABI=$abi \
        -DANDROID_PLATFORM=android-24 \
        -DCMAKE_BUILD_TYPE=Release \
        ../quickjs
    make quickjs -j$(nproc)
    cp libquickjs.a $OUTPUT_DIR/$abi/
    make clean
done
```

### 3.3 Building Shared Library (JNI)

We need `libquickjs_jni.so` which links against `libquickjs.a`.

**CMakeLists.txt** config:
```cmake
add_library(quickjs STATIC ${QUICKJS_SOURCES})
target_compile_options(quickjs PRIVATE -O3 -ffast-math)

add_library(quickjs_jni SHARED quickjs_jni.cpp quickjs_android_runtime.cpp)
target_link_libraries(quickjs_jni quickjs log android)
set_target_properties(quickjs PROPERTIES POSITION_INDEPENDENT_CODE ON)
```

Build via Gradle:
```bash
./gradlew assembleGenericRelease
```

---

## 4. Native Library Integration

### 4.1 Project Structure

```
app/src/main/
├── cpp/
│   ├── CMakeLists.txt                ← Build config
│   ├── quickjs_jni.h                  ← JNI declarations
│   ├── quickjs_android_runtime.cpp    ← C++ wrapper
│   └── quickjs/                       ← QuickJS source tree
│       ├── quickjs.c
│       ├── quickjs.h
│       ├── libregexp/
│       ├── libunicode/
│       └── [...]
├── java/com/junkfood/seal/js/
│   ├── QuickJSRuntime.kt              ← Kotlin singleton
│   └── JavaScriptRuntimeManager.kt    ← yt-dlp manager
└── AndroidManifest.xml                ← Service declarations
```

### 4.2 Gradle Configuration

**`app/build.gradle.kts`** additions:

```kotlin
android {
    defaultConfig {
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64"))
            stl = "c++_static"
        }

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                cppFlags += "-frtti"
                cppFlags += "-fexceptions"
                arguments.addAll(listOf(
                    "-DCMAKE_BUILD_TYPE=Release",
                    "-DANDROID_STL=c++_static"
                ))
                targets.add("quickjs_jni")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    packaging {
        jniLibs {
            // Keep QuickJS native libs
            keepDebugSymbols += "**/libquickjs*.so"
        }
    }
}

dependencies {
    // Media3 dependencies (added separately)
}
```

---

## 5. JNI Bridge Design

### 5.1 Design Principles

1. **Minimal JNI surface**: Only expose essential methods
2. **Pointer-based instance management**: Use opaque `jlong` handles
3. **Thread safety**: Synchronized access to runtime map
4. **Error propagation**: Native errors → Java exceptions or callbacks

### 5.2 JNI Interface

#### Header (`quickjs_jni.h`)

```c
#ifndef QUICKJS_JNI_H
#define QUICKJS_JNI_H

#include <jni.h>
#include <string>

class QuickJSBridge;

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Create new QuickJS runtime instance
 * @return native pointer (jlong), 0 on failure
 */
JNIEXPORT jlong JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeInit(JNIEnv*, jobject);

/**
 * Execute JavaScript code
 * @param ptr Runtime pointer (from nativeInit)
 * @param script JavaScript code
 * @param filename For error messages
 * @param callback Optional result callback (can be null)
 * @return JNI_TRUE on success, JNI_FALSE on failure
 */
JNIEXPORT jboolean JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeEvaluate(
    JNIEnv*, jobject, jlong, jstring, jstring, jobject);

/**
 * Set global variable in runtime
 */
JNIEXPORT void JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeSetVariable(
    JNIEnv*, jobject, jlong, jstring, jstring);

/**
 * Get global variable from runtime
 * @return variable value as string
 */
JNIEXPORT jstring JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeGetVariable(
    JNIEnv*, jobject, jlong, jstring);

/**
 * Release runtime and free memory
 */
JNIEXPORT void JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeRelease(
    JNIEnv*, jobject, jlong);

/**
 * Get last error message
 */
JNIEXPORT jstring JNICALL
Java_com_junkfood_seal_js_QuickJSRuntime_nativeGetLastError(
    JNIEnv*, jobject, jlong);

#ifdef __cplusplus
}
#endif

#endif // QUICKJS_JNI_H
```

### 5.3 Bridge Implementation

**Instance Management**:
```cpp
static std::unordered_map<jlong, QuickJSBridge*> s_bridgeMap;
static std::mutex s_bridgeMutex;
static jlong s_nextId = 0;

jlong createRuntime() {
    QuickJSBridge* bridge = new QuickJSBridge();
    if (!bridge->initialize()) {
        delete bridge;
        return 0;
    }
    std::lock_guard<std::mutex> lock(s_bridgeMutex);
    s_bridgeMap[++s_nextId] = bridge;
    return s_nextId;
}
```

**String Handling**:
- Java strings → UTF-8 with `GetStringUTFChars()`
- C++ strings → Java strings with `NewStringUTF()`
- Exception: If result is null, return empty Java string

**Callbacks**:
```java
// Java interface
interface QuickJSCallback {
    void onResult(String result);
}

// C++ calls back via JNI
jclass callbackClass = env->GetObjectClass(callback);
jmethodID onResult = env->GetMethodID(callbackClass, "onResult", "(Ljava/lang/String;)V");
jstring jResult = env->NewStringUTF(result.c_str());
env->CallVoidMethod(callback, onResult, jResult);
```

---

## 6. Kotlin Wrapper

### 6.1 QuickJSRuntime Class

```kotlin
class QuickJSRuntime private constructor(context: Context) : AutoCloseable {

    companion object {
        private var instance: QuickJSRuntime? = null

        fun getInstance(context: Context): QuickJSRuntime {
            return instance ?: synchronized(this) {
                instance ?: QuickJSRuntime(context.applicationContext).also { instance = it }
            }
        }

        fun isAvailable(): Boolean = try {
            System.loadLibrary("quickjs_jni")
            true
        } catch (e: UnsatisfiedLinkError) {
            false
        }
    }

    init {
        System.loadLibrary("quickjs_jni")
        nativePtr = nativeInit()
    }

    private external fun nativeInit(): Long
    private external fun nativeEvaluate(
        ptr: Long, script: String, filename: String,
        callback: ((String) -> Unit)?
    ): Boolean
    // ... other natives

    private var nativePtr: Long = 0

    fun evaluate(script: String, callback: ((String) -> Unit)? = null): Boolean {
        return nativeEvaluate(nativePtr, script, "<input>", callback)
    }

    override fun close() {
        if (nativePtr != 0L) {
            nativeRelease(nativePtr)
            nativePtr = 0
        }
    }
}
```

### 6.2 JavaScriptRuntimeManager (yt-dlp Integration)

```kotlin
object JavaScriptRuntimeManager {
    suspend fun initialize(context: Context): Boolean = withContext(Dispatchers.IO) {
        if (QuickJSRuntime.isAvailable()) {
            QuickJSRuntime.initialize(context)
            val quickjsPath = getQuickJSBinaryPath(context)
            // Configure yt-dlp global option
            YoutubeDL.getInstance()
                .addGlobalOption("--js-runtimes", "quickjs:$quickjsPath")
            true
        } else false
    }

    fun applyToRequest(request: YoutubeDLRequest) {
        request.addOption("--js-runtimes", "quickjs:/path/to/binary")
    }
}
```

### 6.3 Usage in DownloadUtil

```kotlin
val request = YoutubeDLRequest(url)
JavaScriptRuntimeManager.applyToRequest(request)
val response = YoutubeDL.getInstance().execute(request)
```

---

## 7. yt-dlp Configuration

### 7.1 Command-Line Format

```
yt-dlp --js-runtimes <type>:<path> <url>
```

**Examples**:
```
--js-runtimes quickjs:/data/data/com.junkfood.seal/files/quickjs
--js-runtimes quickjs:/data/data/com.junkfood.seal/files/quickjs:/optional/nodejs/path
```

### 7.2 Supported Runtime Types

| Type | Identifier | Path |
|------|-----------|------|
| QuickJS | `quickjs` | Path to QuickJS executable (or script wrapper) |
| Node.js | `nodejs` | Path to node binary |
| Deno | `deno` | Path to deno binary |

**Note**: We only ship QuickJS.

### 7.3 Multiple Runtimes

yt-dlp tries runtimes in order. We provide only QuickJS, but format allows fallback:

```kotlin
request.addOption("--js-runtimes",
    "quickjs:/data/.../quickjs:/system/bin/node"
)
```

### 7.4 QuickJS Binary Layout

```
/data/data/com.junkfood.seal/
├── files/
│   ├── quickjs/                    # QuickJS runtime directory
│   │   ├── quickjs                 # Shell script wrapper (executable)
│   │   └── libquickjs.so           # Native shared library (optional)
│   └── cache/
└── no_media/
```

**Shell wrapper** (`quickjs`):
```bash
#!/system/bin/sh
# QuickJS launcher for yt-dlp Android
LIB_DIR="/data/data/com.junkfood.seal/files/quickjs"
exec "$LIB_DIR/libquickjs.so" "$@"
```

---

## 8. Testing

### 8.1 Unit Tests (JVM)

Mock JNI bridge with fake implementation:

```kotlin
class QuickJSRuntimeTest {
    @Test fun `evaluate simple expression`() = runTest {
        val runtime = QuickJSRuntime.createForTesting()  // Returns mock
        val result = runtime.evaluate("1 + 2")
        assertEquals("3", result)
    }
}
```

### 8.2 Instrumentation Tests (Device)

```kotlin
@RunWith(AndroidJUnit4::class)
class QuickJSInstrumentationTest {
    @Test fun quickJsLoadsAndEvaluates() {
        val runtime = QuickJSRuntime.getInstance(appContext)
        assertTrue(runtime.evaluate("2 + 2") { result ->
            assertEquals("4", result)
        })
    }

    @Test fun ytDlpAcceptsJsRuntimeOption() {
        val request = YoutubeDLRequest("https://youtube.com")
        JavaScriptRuntimeManager.applyToRequest(request)
        // Verify --js-runtimes added to command
    }
}
```

### 8.3 Integration Test (Full Flow)

```kotlin
@Test fun downloadVideoWithJsChallenge() = runTest {
    // URL that requires JS solving (e.g., Cloudflare protected)
    val url = "https://example.com/protected-video"

    // 1. Fetch info (triggers JS solver)
    val result = DownloadUtil.fetchVideoInfoFromUrl(url)

    // 2. Assert success (no JS exception thrown)
    assertTrue(result.isSuccess)
}
```

### 8.4 Performance Test

```kotlin
@Test fun jsChallengeSolvingTime() {
    val start = System.currentTimeMillis()
    runtime.evaluate(sampleChallengeScript)
    val duration = System.currentTimeMillis() - start

    // Should complete in < 5 seconds (benchmark from cloudflare-challenge)
    assertTrue(duration < 5000, "JS challenge took ${duration}ms")
}
```

---

## 9. Troubleshooting

### 9.1 Common Errors

| Symptom | Cause | Fix |
|---------|-------|-----|
| `UnsatisfiedLinkError: libquickjs_jni.so` | Native lib not packaged | Check `externalNativeBuild` block in Gradle; clean & rebuild |
| JS evaluation returns empty | Runtime not initialized | Ensure `QuickJSRuntime.initialize()` called before use |
| `Cannot find '-lquickjs'` | Static lib missing | Verify CMake finds `libquickjs.a` in `app/src/main/cpp/quickjs/[abi]/` |
| Crash in `JS_Eval` | Invalid context | Ensure runtime pointer is valid (not released prematurely) |
| Out of memory | Large script or recursion | Increase `JS_SetMemoryLimit()` in bridge init |

### 9.2 Debug Logging

Enable verbose logs:

```kotlin
// In Application.onCreate()
if (BuildConfig.DEBUG) {
    QuickJSRuntime.setLogLevel(Log.VERBOSE)
}
```

C++ side:
```cpp
#define LOG_TAG "QuickJS"
#include <android/log.h>
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
LOGI("JS_Eval: evaluating %zu bytes", script.length());
```

### 9.3 Verifying Installation

```bash
# 1. Check native library exists in APK
$ unzip -l Seal-arm64-v8a.apk | grep quickjs
lib/arm64-v8a/libquickjs_jni.so

# 2. Check yt-dlp recognizes runtime
$ adb shell "logcat | grep -i 'js runtime'"
[youtube] Using JS runtime: quickjs:/data/.../quickjs

# 3. Test JS execution manually
$ adb shell
$ /data/data/com.junkfood.seal/files/quickjs/quickjs -e "1+1"
2
```

---

## 10. Performance Tuning

### 10.1 Memory Configuration

```cpp
// In QuickJSBridge::initialize()
JS_SetMemoryLimit(rt, 256 * 1024 * 1024);    // 256 MB cap
JS_SetGCThreshold(rt, 64 * 1024 * 1024);    // GC after 64 MB
```

**Trade-off**: Higher limits → fewer GC pauses, more memory usage.

### 10.2 Execution Timeout

```kotlin
// Wrap evaluation in withTimeoutOrNull on Kotlin side
val result = withTimeoutOrNull(30_000) {
    runtime.evaluate(script) { /* callback */ }
} ?: throw TimeoutCancellationException("JS execution timeout")
```

### 10.3 Caching

Cache compiled JS modules in memory:

```cpp
// QuickJSBridge caches JSValue for frequently-used scripts
std::unordered_map<std::string, JSValue> m_moduleCache;
```

### 10.4 Native Library Size Optimization

QuickJS can be stripped of unused features:
- Remove `qjsc` (compiler) from build
- Remove `qjs` shell binary
- Omit `libregexp`/`libunicode` if not needed (but ES2023 requires)

**Stripped library size**:
- Full QuickJS: ~6 MB (static)
- Minified (no regexp, unicode): ~2 MB
- Shared lib (`libquickjs.so`): ~1.5 MB

---

## Appendix

### A. QuickJS Version Timeline

| Version | Release Date | Notable Changes |
|---------|--------------|-----------------|
| 2025-09-13 | 2025-09-13 | Latest stable (recommended) |
| 2025-04-26 | 2025-04-26 | ES2023 full compliance |
| 2024-12-25 | 2024-12-25 | Security fixes |
| 2024-10-05 | 2024-10-05 | BigInt optimization |

### B. yt-dlp JS Challenge Types

| Challenge | Complexity | Avg. Solve Time |
|-----------|------------|-----------------|
| Simple arithmetic | Low | < 50ms |
| Obfuscated string decryption | Medium | 50-500ms |
| Cloudflare Turnstile | High | 2-8s |
| reCAPTCHA v2/v3 | Very High | Not supported (requires external) |

### C. References

- [QuickJS Repository](https://github.com/bellard/quickjs)
- [yt-dlp JavaScript Execution Docs](https://github.com/yt-dlp/yt-dlp/blob/master/CONTRIBUTING.md#javascript-execution)
- [Android NDK Guides](https://developer.android.com/ndk/guides)
- [JNI Specification](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/)

---

**Maintained by**: @JunkFood02  
**Last Updated**: 2026-05-02  
**QuickJS Version**: 2025-09-13
