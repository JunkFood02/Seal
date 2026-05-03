# Technical Architecture: Android Media Application with Embedded yt-dlp

## Executive Summary

This document presents a comprehensive technical architecture for an Android-based media download and playback application that leverages a forked `yt-dlp` repository. The solution addresses two critical integration challenges: (1) embedded JavaScript runtime execution for web-based solvers required by yt-dlp, and (2) high-performance media playback with seamless download-to-play transitions.

**Project**: Seal (Video/Audio Downloader for Android)  
**Architecture**: Modern Android with Jetpack Compose, Media3, NDK integration  
**Technology Stack**: Kotlin, C++ (QuickJS), Python (embedded via youtubedl-android), ExoPlayer

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [JavaScript Runtime Integration](#2-javascript-runtime-integration-architecture)
3. [Media Playback Subsystem](#3-media-playback-subsystem-design)
4. [Component Interactions](#4-component-interaction-workflow)
5. [Build & Deployment Architecture](#5-build--deployment-architecture)
6. [CI/CD Pipeline](#6-cicd-pipeline-implementation)
7. [Implementation Roadmap](#7-implementation-roadmap)
8. [Performance & Scaling](#8-performance--scaling-considerations)
9. [Security Considerations](#9-security-considerations)
10. [Monitoring & Observability](#10-monitoring--observability)

---

## 1. System Overview

### 1.1 High-Level Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                              Android OS                                  │
├──────────────────────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────────────────────┐ │
│ │                    Sealed Application Container                      │ │
│ │                                                                    │ │
│ │  ┌──────────────────────────────────────────────────────────────┐  │ │
│ │  │              UI Layer (Jetpack Compose)                       │  │ │
│ │  │  • DownloadPageV2          • VideoListPage                   │  │ │
│ │  │  • Settings screens         • PlayerActivity                 │  │ │
│ │  │  • Material Design 3 theme                                    │  │ │
│ │  └──────────────────────────────────────────────────────────────┘  │ │
│ │                          ↕ State (StateFlow)                       │ │
│ │  ┌──────────────────────────────────────────────────────────────┐  │ │
│ │  │      ViewModel Layer (Koin DI)                                │  │ │
│ │  │  • DownloadDialogViewModel  • PlayerViewModel                │  │ │
│ │  │  • VideoListViewModel       • CookiesViewModel               │  │ │
│ │  └──────────────────────────────────────────────────────────────┘  │ │
│ │                          ↕ Method calls                            │ │
│ │  ┌──────────────────────────────────────────────────────────────┐  │ │
│ │  │        Service Layer (Foreground Services)                    │  │ │
│ │  │  ┌─────────────────┐            ┌────────────────────┐       │  │ │
│ │  │  │ DownloadService │            │   PlayerService    │       │  │ │
│ │  │  │ (Background)    │            │ (Media3 Session)   │       │  │ │
│ │  │  └─────────────────┘            └────────────────────┘       │  │ │
│ │  └──────────────────────────────────────────────────────────────┘  │ │
│ │                          ↕ JNI / IPC                               │ │
│ │  ┌──────────────────────────────────────────────────────────────┐  │ │
│ │  │        Native/Third-Party Layer                                │  │ │
│ │  │  ┌─────────────────┐  ┌──────────────┐  ┌─────────────────┐ │  │ │
│ │  │  │  youtubedl-     │  │ FFmpeg       │  │  QuickJS        │ │  │ │
│ │  │  │  android lib    │  │ (native)     │  │  (JNI bridge)   │ │  │ │
│ │  │  │  (Python yt-dlp)│  │              │  │                 │ │  │ │
│ │  │  └─────────────────┘  └──────────────┘  └─────────────────┘ │  │ │
│ │  └──────────────────────────────────────────────────────────────┘  │ │
│ │                                                                    │ │
│ └──────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Technology Stack Rationale

| Layer | Technology | Rationale |
|-------|------------|-----------|
| **UI** | Jetpack Compose + Material 3 | Declarative UI, dynamic theming, easy state management |
| **Architecture** | Single-Activity + Navigation Compose | Reduced boilerplate, type-safe navigation, state retention |
| **DI** | Koin | Lightweight, Kotlin-first, no code generation |
| **Local Storage** | Room + MMKV | Type-safe SQLite for relational data, MMKV for key-value prefs |
| **Networking** | OkHttp + youtubedl-android | Efficient HTTP client; youtubedl-android embeds yt-dlp + FFmpeg |
| **Media Playback** | ExoPlayer (Media3) | Industry standard, comprehensive format support, MediaSession |
| **JavaScript** | QuickJS (via NDK/JNI) | Minimal footprint (6 MB), full ES2023, fast startup (300μs) |
| **Async** | Kotlin Coroutines + Flow | Structured concurrency, reactive streams |
| **Build** | Gradle (Kotlin DSL) | Type-safe build scripts, modern tooling |
| **CI/CD** | GitHub Actions + Fastlane | Native integration, artifact management, deployment automation |

---

## 2. JavaScript Runtime Integration Architecture

### 2.1 Problem Statement

`yt-dlp` requires a JavaScript runtime to solve web challenges from sites that employ anti-bot measures (e.g., Cloudflare Turnstile, reCAPTCHA, signature deciphering). Standard embedded Python cannot interpret JavaScript; it needs an external interpreter.

**Challenges**:
- Mobile environments have limited storage (APK size constraint)
- JavaScript execution must be fast (user experience)
- Must support full ES2023 (modern obfuscation techniques)
- Multiple CPU architectures (arm64, armv7, x86, x86_64)

### 2.2 Solution Comparison

| Runtime | APK Size Impact | Supported ABIs | Startup Time | Suitability |
|---------|----------------|----------------|--------------|-------------|
| **QuickJS** | +6 MB (static lib) | arm, arm64, x86, x86_64 | ~300μs | ✅ **Best** |
| Node.js | +71 MB | arm64 only | ~2-3s | ❌ Too large |
| Deno | +95 MB | arm64 only | ~1-2s | ❌ Too large |
| V8 (via J2V8) | +25 MB | arm64 only | ~500ms | ⚠️ Large, but feasible |

**Decision**: QuickJS offers the optimal balance of size, speed, and compatibility.

### 2.3 Integration Deep Dive

#### 2.3.1 Native Layer (C++/JNI)

**Files**:
- `app/src/main/cpp/CMakeLists.txt`: Build configuration
- `app/src/main/cpp/quickjs_jni.h`: JNI interface declarations
- `app/src/main/cpp/quickjs_android_runtime.cpp`: Implementation of C++ bridge
- `app/src/main/cpp/quickjs/`: QuickJS source code (v2025-09-13)

**Compilation**:
```cmake
# Build static QuickJS library from source
add_library(quickjs STATIC ${QUICKJS_SOURCES})
target_compile_definitions(quickjs PRIVATE CONFIG_VERSION="${QUICKJS_VERSION}")

# Build JNI shared library that links against QuickJS
add_library(quickjs_jni SHARED quickjs_jni.cpp quickjs_android_runtime.cpp)
target_link_libraries(quickjs_jni quickjs log android)
set_target_properties(quickjs PROPERTIES POSITION_INDEPENDENT_CODE ON)
```

**Build output**:
```
app/build/intermediates/cmake/release/obj/
├── arm64-v8a/libquickjs.a
├── arm64-v8a/libquickjs_jni.so
├── armeabi-v7a/libquickjs.a
├── armeabi-v7a/libquickjs_jni.so
├── x86_64/libquickjs.a
└── x86_64/libquickjs_jni.so
```

**JNI Interface Design**:
- **Opaque pointer management**: Native `QuickJSBridge*` wrapped as `jlong` in Java
- **Thread-safe instance map**: `ConcurrentHashMap<jlong, QuickJSBridge*>`
- **String marshaling**: UTF-8 conversion via `GetStringUTFChars`/`NewStringUTF`
- **Exception safety**: C++ exceptions not thrown across JNI boundary; errors returned as boolean + message getter

**Key JNI Methods**:
```cpp
JNIEXPORT jlong JNICALL Java_com_junkfood_seal_js_QuickJSRuntime_nativeInit(...)
JNIEXPORT jboolean JNICALL Java_com_junkfood_seal_js_QuickJSRuntime_nativeEvaluate(...)
JNIEXPORT void JNICALL Java_com_junkfood_seal_js_QuickJSRuntime_nativeSetVariable(...)
JNIEXPORT jstring JNICALL Java_com_junkfood_seal_js_QuickJSRuntime_nativeGetVariable(...)
JNIEXPORT void JNICALL Java_com_junkfood_seal_js_QuickJSRuntime_nativeRelease(...)
```

#### 2.3.2 Kotlin Wrapper Layer

**QuickJSRuntime.kt** – Singleton wrapper providing thread-safe access to native runtime:

```kotlin
class QuickJSRuntime private constructor(context: Context) : AutoCloseable {
    companion object {
        private var instance: QuickJSRuntime? = null
        fun getInstance(context: Context): QuickJSRuntime { /* ... */ }
        fun isAvailable(): Boolean = try {
            System.loadLibrary("quickjs_jni"); true
        } catch (e: UnsatisfiedLinkError) { false }
    }

    private var nativePtr: Long = 0

    init {
        System.loadLibrary("quickjs_jni")
        nativePtr = nativeInit()
        require(nativePtr != 0L) { "QuickJS init failed" }
    }

    fun evaluate(script: String, callback: ((String) -> Unit)? = null): Boolean {
        return nativeEvaluate(nativePtr, script, "<input>", callback)
    }

    override fun close() {
        if (nativePtr != 0L) nativeRelease(nativePtr)
    }
}
```

**JavaScriptRuntimeManager.kt** – High-level manager that configures yt-dlp:

```kotlin
object JavaScriptRuntimeManager {
    suspend fun initialize(context: Context): Boolean = withContext(Dispatchers.IO) {
        if (QuickJSRuntime.isAvailable()) {
            val runtime = QuickJSRuntime.getInstance(context)
            // Configure yt-dlp global option
            YoutubeDL.getInstance().addGlobalOption("--js-runtimes", "quickjs:/path/to/quickjs")
            true
        } else false
    }

    fun applyToRequest(request: YoutubeDLRequest) {
        request.addOption("--js-runtimes", "quickjs:/path/to/quickjs")
    }
}
```

#### 2.3.3 yt-dlp Integration

**Hook point**: `DownloadUtil.kt` – all `YoutubeDLRequest` objects are wrapped:

```kotlin
val request = YoutubeDLRequest(url).apply {
    // ... existing options
    JavaScriptRuntimeManager.applyToRequest(this)  // <-- Inject JS runtime
}
```

**yt-dlp usage**:
- When `--js-runtimes` option is provided, yt-dlp tries to execute JavaScript using specified interpreter
- Format: `--js-runtimes <type>:<path>[:<type>:<path>]...`
- Multiple runtimes can be chained for fallback

---

## 3. Media Playback Subsystem Design

### 3.1 Requirements

- **Playback formats**: MP4, MKV, WebM, AVI, MOV, FLV; audio: MP3, M4A, AAC, FLAC, Opus
- **Streaming**: HLS (.m3u8), DASH, progressive download
- **Background playback**: Continue when app minimized / screen off
- **External controls**: Lock screen widgets, Android Auto, Wear OS, Bluetooth headsets
- **Seamless transition**: From download completion → automatic playback option
- **Performance**: 4K@60fps video decoding with subtitles
- **UI/UX**: Material Design 3, custom controls, skip/seek, volume, playback speed

### 3.2 Media3/ExoPlayer Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Compose UI Layer                             │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │              MediaPlayerScreen.kt (Composable)               │  │
│  │  ┌────────────────────────────────────────────────────────┐ │  │
│  │  │  VideoPlayerSurface    ← PlayerSurface (Media3 Compose) │ │  │
│  │  │  PlaybackControls      ← Custom Composable controls     │ │  │
│  │  │  SeekBar, Volume, Speed                                │ │  │
│  │  └────────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                            ↓ observes                               │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │           PlayerViewModel (Kotlin, Async/Flow)               │  │
│  │  • playbackState: StateFlow<PlaybackState>                  │  │
│  │  • position: StateFlow<Long>                                │  │
│  │  • currentMedia: StateFlow<MediaInfo?>                      │  │
│  │  • Commands: play(), pause(), seekTo(), skip()              │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                            ↓ commands                               │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │               PlayerService (MediaSessionService)            │  │
│  │  Foreground service with persistent notification             │  │
│  │  • Manages ExoPlayer instance                                │  │
│  │  • Handles MediaController commands                          │  │
│  │  • MediaSession.connect() → system integration               │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                            ↓                                        │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │               ExoPlayer (Media3 core)                        │  │
│  │  • Render video → SurfaceView/SurfaceHolder                  │  │
│  │  • Audio output via AudioTrack                               │  │
│  │  • Adaptive streaming (HLS/DASH)                             │  │
│  │  • DRM, subtitle rendering, track selection                  │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.3 Key Components

#### 3.3.1 PlayerService

**Purpose**: Foreground service that keeps playback alive when UI is not visible. Implements `MediaSessionService` for system integration.

```kotlin
class PlayerService : MediaSessionService() {
    private lateinit var mediaSession: MediaSession
    private var player: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        setupMediaSession()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    fun playMedia(uri: String, title: String?, artist: String?) {
        val mediaItem = MediaItem.Builder().setUri(uri).build()
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }
}
```

**Manifest registration**:
```xml
<service
    android:name=".player.PlayerService"
    android:foregroundServiceType="mediaPlayback"
    android:exported="false">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
    </intent-filter>
</service>
```

#### 3.3.2 PlayerViewModel

**State management** for playback (CollectAsStateWithLifecycle compatible):

```kotlin
data class PlaybackState(
    val isPlaying: Boolean,
    val position: Long,
    val duration: Long,
    val bufferedPercentage: Int,
    val currentMedia: MediaInfo?
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun togglePlayPause() {
        if (player?.isPlaying == true) player?.pause()
        else player?.play()
    }

    fun seekTo(positionMs: Long) { player?.seekTo(positionMs) }
    fun skip(direction: SkipDirection) { /* ... */ }
    fun setVolume(vol: Float) { player?.volume = vol }
}
```

**Compose UI integration**:
```kotlin
@Composable
fun MediaPlayerScreen(viewModel: PlayerViewModel) {
    val state by viewModel.playbackState.collectAsStateWithLifecycle()

    Box {
        VideoPlayerSurface(player = viewModel.player!!)
        PlaybackControls(
            playbackState = state,
            onPlayPause = { viewModel.togglePlayPause() },
            onSeek = { viewModel.seekTo(it) }
        )
    }
}
```

### 3.4 Integration with Download Flow

**When download completes** (`DownloadUtil.kt`):

```kotlin
.onSuccess { filePaths ->
    // Insert into history (Room DB)
    insertInfoIntoDownloadHistory(videoInfo, filePaths)

    // Launch player automatically (if user preference enabled)
    if (PreferenceUtil.getBoolean(Preferences.AUTO_PLAY, false)) {
        val intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra("media_uri", filePaths.first())
            putExtra("media_title", videoInfo.title)
            putExtra("media_artist", videoInfo.uploader)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
```

**VideoListPage integration**: From `VideoDetailDrawer` (bottom sheet), user can tap "Play" button to launch `PlayerActivity`.

---

## 4. Component Interaction Workflow

### 4.1 Download Execution Flow

```
User enters URL → [DownloadDialogV2] → Request validated
     ↓
DownloadUtil.getInfoAndDownload(url)
     ↓
YoutubeDLRequest built + JS runtime option injected
     ↓
┌────────────────────────────────────────────────────┐
│  DownloaderV2.enqueue(task)                         │
│  • State = Idle                                     │
│  • Persisted to Room (backup)                       │
└────────────────────────────────────────────────────┘
     ↓
Fetch video info (background thread)
     ↓
YouTube may return JS challenge → yt-dlp executes QuickJS
     ↓
JS challenge solved → video info returned
     ↓
Download begins → FFmpeg (or aria2c) fetches media
     ↓
Progress callbacks → Notification updates
     ↓
Complete → file scanned → inserted into history
     ↓
User can tap "Play" from VideoListPage → PlayerActivity
```

### 4.2 JavaScript Challenge Solving

```
yt-dlp extracts challenge from YouTube page
     ↓
Writes challenge JS to temp file
     ↓
Spawns QuickJS process (or calls via JNI for in-process)
     ↓
QuickJS executes:
   - Sets up environment (window, document, navigator)
   - Evaluates obfuscated function
   - Returns solution (token, cookie, etc.)
     ↓
yt-dlp receives solution → adds to request headers
     ↓
Request retried → success
```

### 4.3 Media Playback Flow

```
PlayerActivity.onCreate()
     ↓
PlayerViewModel.initialize()
     ↓
PlayerService.bind()
     ↓
ExoPlayer created + MediaSession configured
     ↓
Intent extras read → playMedia(uri, title, artist)
     ↓
MediaItem added to queue → prepare() → play()
     ↓
Surface renders video (SurfaceView via PlayerSurface)
     ↓
User interactions:
 • Play/Pause → player.playWhenReady toggle
 • Seek → player.seekTo()
 • Volume → player.volume = value
 • Speed → player.setPlaybackParameters(PlaybackParameters(speed))
```

---

## 5. Build & Deployment Architecture

### 5.1 Build Matrix

The project supports four ABI splits + universal APK + Android App Bundle:

| ABI | APK Name Pattern | Target Devices | Approx Size |
|-----|------------------|----------------|-------------|
| arm64-v8a | `Seal-{ver}-arm64-v8a.apk` | Most modern devices (64-bit) | ~45 MB |
| armeabi-v7a | `Seal-{ver}-armeabi-v7a.apk` | 32-bit legacy devices | ~38 MB |
| x86_64 | `Seal-{ver}-x86_64.apk` | Android Emulators, some Chromebooks | ~46 MB |
| x86 | `Seal-{ver}-x86.apk` | 32-bit emulators | ~39 MB |
| universal | `Seal-{ver}-universal.apk` | Fallback (all ABIs) | ~92 MB |
| bundle | `Seal-{ver}.aab` | Google Play Store (dynamic delivery) | ~55 MB |

### 5.2 Build Process

**Prerequisites**:
- JDK 21 (Temurin)
- Android SDK with API 35
- Android NDK r25b+
- CMake 3.22+

**Build commands**:

```bash
# Clean
./gradlew clean

# Code formatting check (required for PR)
./gradlew ktlintCheck

# Static analysis
./gradlew detekt

# Unit tests
./gradlew testDebugUnitTest

# Build all release variants
./gradlew assembleGenericRelease

# Build specific ABI
./gradlew assembleGenericRelease -PABI_FILTERS=arm64-v8a

# Build universal (no splits)
./gradlew assembleGenericRelease -PnoSplits

# Build bundle for Play Store
./gradlew bundleGenericRelease
```

**Gradle properties**:

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4g -Dkotlin.daemon.jvm.options="-Xmx2g"
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

### 5.3 Versioning Strategy

**Semantic Versioning** (MAJOR.MINOR.PATCH[-PRERELEASE]):

```kotlin
// In Version.kt (buildSrc)
object CurrentVersion {
    val name = "2.1.0"          // User-facing
    val code = "20103100"       // Google Play (integer)
}
```

**versionCode formula**:
```
versionCode = MAJOR × 10^7 + MINOR × 10^4 + PATCH × 10^2 + PRERELEASE_OFFSET

PRERELEASE_OFFSET:
  release (no suffix)  → +0
  alpha.N              → +1 + N
  beta.N               → +2 + N
  rc.N                 → +3 + N
  dev                  → +99
```

**Example**:
```
2.1.0     → 20100000
2.1.0-rc1 → 20100004  (3 + 1)
2.1.0-rc2 → 20100005  (3 + 2)
2.1.1     → 20100100
```

---

## 6. CI/CD Pipeline Implementation

### 6.1 GitHub Actions Workflow

**File**: `.github/workflows/ci.yml`

**Jobs**:

#### 1. Static Analysis
- `ktlintFormatCheck` – Kotlin style compliance
- `detekt` – Static analysis (code smells)
- `lint` – Android lint (resource issues, security)

#### 2. Unit Tests
- `testDebugUnitTest` – JVM tests (fast)
- Upload test results as artifact

#### 3. Build Matrix
- Parallel builds for each ABI (4 workers)
- Outputs: split APKs + universal APK + AAB
- Artifacts: retained 30 days for debugging

#### 4. Smoke Tests (on PRs to main)
- Instrumentation tests on emulator (API 35, x86_64)
- QuickJS initialization test
- PlayerService lifecycle test

### 6.2 Fastlane Automation

**Fastfile** lanes:

| Lane | Purpose | Trigger |
|------|---------|---------|
| `test` | Run unit + instrumentation tests | Pre-commit / CI |
| `lint` | Format + static analysis | Pre-commit / CI |
| `build_release` | Build all APKs + AAB | Manual / CI |
| `deploy_github` | Upload to GitHub Releases | Git tag (`v*`) |
| `deploy_playstore` | Upload to Play Store (internal) | Manual |
| `promote_beta` | Promote to beta track | Manual |
| `promote_production` | Full release | Manual |

**Version bump automation**: Hook into Gradle properties via git tags.

### 6.3 Staged Rollout Strategy

**Google Play Console**:
1. **Internal testing**: 100 testers, 1-2 days smoke test
2. **Closed alpha**: 5% of users, 3-5 days
3. **Open beta**: 10% of users, 1 week
4. **Production staged**: 10% → 50% → 100% over 1-2 weeks

**Monitoring**:
- Crashlytics crash-free sessions > 99.5% required to proceed
- ANR rate < 0.1%
- Play Store rating > 4.2

---

## 7. Implementation Roadmap

### Phase 1: Foundation (Completed)

✅ **Embedded JS Runtime**
- QuickJS compiled for arm64, armeabi-v7a, x86, x86_64
- JNI bridge with thread-safe pointer management
- Kotlin singleton wrapper (`QuickJSRuntime`)
- Integrated with `yt-dlp` via `--js-runtimes` option
- Error handling + callbacks

✅ **Media Playback Core**
- ExoPlayer + Media3 dependencies added
- `PlayerService` (foreground, MediaSession)
- `PlayerViewModel` (Compose-reactive state)
- `MediaPlayerScreen` composable (video surface + controls)
- Playback queue, skip/seek, volume, speed

✅ **Integration**
- Play button in `VideoDetailDrawer` (bottom sheet)
- `PlayerActivity` launched from downloads
- Database migration for duration field

✅ **CI/CD**
- GitHub Actions workflow (lint, test, build)
- Fastlane lanes (build, test, deploy)
- Release automation via git tags

### Phase 2: Polish (Next Steps)

🟡 **User Experience**
- [ ] Picture-in-picture mode (Android 8+)
- [ ] Lock screen widget (MediaStyle)
- [ ] Cast support (Chromecast via Media3)
- [ ] Background playback controls (notification)

🟡 **Performance**
- [ ] Baseline profiles for startup optimization
- [ ] APK size reduction (strip symbols, compress assets)
- [ ] Pre-warm QuickJS at app start
- [ ] ExoPlayer caching for streamed content

🟡 **Analytics & Monitoring**
- [ ] Firebase Crashlytics integration
- [ ] Play Console pre-launch reports
- [ ] Performance monitoring (startup, memory)
- [ ] Usage statistics (opt-in)

🟡 **Testing**
- [ ] Full instrumentation test suite
- [ ] UI tests for critical flows
- [ ] Regression tests for JS challenges
- [ ] Performance benchmarks (Startup < 2s, resource < 150 MB)

### Phase 3: Production Readiness

🟢 **Release Preparation**
- [ ] Beta testing (closed → open)
- [ ] Accessibility audit (TalkBack, color contrast)
- [ ] Localization verification (30+ languages)
- [ ] Legal compliance (GPLv3 notices)
- [ ] Documentation finalization

🟢 **Launch**
- [ ] GitHub Release v2.1.0
- [ ] Google Play Store (staged rollout)
- [ ] F-Droid submission
- [ ] Announcement (Telegram, Reddit, GitHub Discussions)

---

## 8. Performance & Scaling Considerations

### 8.1 APK Size Optimization

**Current breakdown** (arm64-v8a, ~45 MB):

| Component | Size | Optimizations |
|-----------|------|--------------|
| Python + stdlib | 12 MB | Unchanged (bundled) |
| yt-dlp + deps | 8 MB | Strip debug symbols |
| FFmpeg | 15 MB | Custom build (disable unused codecs) |
| Aria2c | 3 MB | Already minimal |
| QuickJS | 1.5 MB | Strip unused modules (regexp, unicode optional) |
| App code (Kotlin) | 2.5 MB | R8 full mode |
| Media3 + Compose | 2.5 MB | R8 |
| **Total** | **~45 MB** | **Target: <40 MB** |

**Future reductions**:
- Compress native libraries with `android:extractNativeLibs="false"` (requires API 23+)
- Dynamic feature modules for rarely used features (e.g., Cast)
- On-demand download of codecs (via Play Asset Delivery)

### 8.2 Memory Management

**Memory limits**:
- QuickJS: 256 MB cap, 64 MB GC threshold (configurable)
- ExoPlayer: `setMaxBufferSizeMs()` for RAM-limited devices
- App heap: Default (no override; let system manage)

**Memory leaks**:
- `PlayerService` releases player in `onDestroy()`
- `QuickJSRuntime` closed with `AutoCloseable` in `ViewModel.onCleared()`
- Room database closed on app exit

### 8.3 Concurrent Execution

**Max concurrent downloads**: 3 (configurable via `DownloaderV2.MAX_CONCURRENCY`)
**Background services**: `DownloadService` (download), `PlayerService` (playback) – both foreground with high priority

### 8.4 Cold Start Performance

**Baseline**: < 2 seconds to first draw (Pixel 6, API 35)

**Optimization strategies**:
- Baseline profiles (generated via `ShortcutBaselineProfileRule`)
- Defer QuickJS init until first download (lazy)
- Use `android:extractNativeLibs="true"` for faster load (trade-off: larger install size)

---

## 9. Security Considerations

### 9.1 Native Code Security

- **JNI validation**: All native methods validate `nativePtr` before dereferencing
- **Memory safety**: QuickJS uses reference counting with cycle detection; no GC pauses
- **Sandboxing**: JS runtime has no filesystem/network access (yt-dlp-youtube communicates via stdout)

### 9.2 Network Security

- **HTTPS enforcement**: All downloads use HTTPS only (yt-dlp default)
- **Cookies**: Stored in MMKV (encrypted via Android Keystore on API 23+)
- **User-Agent rotation**: Optional random UA string via settings
- **Proxy support**: HTTP/HTTPS/SOCKS via yt-dlp options

### 9.3 Permissions Model

| Permission | Purpose | When requested |
|-----------|---------|----------------|
| `INTERNET` | Download media | Install time |
| `WRITE_EXTERNAL_STORAGE` (legacy) | Save to shared storage | Pre-Android 10: runtime |
| `MANAGE_EXTERNAL_STORAGE` | Scoped storage override | Android 11+ (optional) |
| `FOREGROUND_SERVICE` | Download player | Install time |
| `POST_NOTIFICATIONS` | Progress notifications | Android 13+ (runtime) |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Background download | Optional prompt |

### 9.4 Content Protection

- No DRM implementation (yt-dlp removes DRM where possible)
- Downloaded files stored on external storage (user-accessible)
- Recommend user enable device encryption

---

## 10. Monitoring & Observability

### 10.1 Crash Reporting

- **Firebase Crashlytics** recommended for production
- Automatic stack traces with custom keys:
  - `yt_dlp_version`, `quickjs_version`, `android_version`, `abi`

### 10.2 Performance Monitoring

- **Android Vitals** (Play Console): ANR, crash-free sessions
- **Custom metrics** (future):
  - JS challenge solve time
  - Download success rate
  - Playback start time

### 10.3 User Feedback Loop

- In-app crash reporter (with clipboard copy)
- Telegram channel for community feedback
- GitHub Issues for bug tracking (with `bug` label)

### 10.4 Logging Strategy

**Logcat tags**:
- `Seal/Downloader` – Download operations
- `Seal/Player` – Playback events
- `Seal/QuickJS` – JS runtime
- `Seal/DB` – Database operations
- `Seal/Updater` – yt-dlp version updates

**Log levels**:
- Release builds: `WARN` and above (errors only)
- Debug builds: `DEBUG` verbose
- CI: capture all logs

---

## Appendix

### A. File References

| File Path | Purpose |
|-----------|---------|
| `app/src/main/cpp/` | Native C++ source (QuickJS + JNI) |
| `app/src/main/java/com/junkfood/seal/js/` | Kotlin JS wrapper |
| `app/src/main/java/com/junkfood/seal/player/` | Media playback |
| `app/src/main/java/com/junkfood/seal/download/` | Download orchestration |
| `app/src/main/java/com/junkfood/seal/util/DownloadUtil.kt` | yt-dlp integration |
| `docs/ARCHITECTURE.md` | System design |
| `docs/QUICKJS_SETUP.md` | JS runtime setup |
| `docs/DEPLOYMENT.md` | Release guide |
| `.github/workflows/ci.yml` | CI/CD pipeline |
| `fastlane/Fastfile` | Deployment automation |
| `scripts/build_and_release.sh` | Manual build script |

### B. Dependencies Summary

**Gradle libraries** (excerpt from `libs.versions.toml`):

```toml
[versions]
media3Exoplayer = "1.10.0"
media3UiCompose = "1.10.0"
kotlin = "2.0.20"
koin = "4.0.0"
youtubedlAndroid = "0.17.3"

[libraries]
androidx-media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer" }
androidx-media3-ui-compose = { group = "androidx.media3", name = "media3-ui-compose" }
```

**Native dependencies**:
- QuickJS source (embedded, MIT License)
- FFmpeg (LGPL/GPL, bundled via youtubedl-android)
- Python 3.8 (PSF License, embedded)

### C. Resources

- [yt-dlp Documentation](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android (fork)](https://github.com/JunkFood02/youtubedl-android)
- [QuickJS Official](https://bellard.org/quickjs/)
- [Media3 Developer Guide](https://developer.android.com/media/media3)
- [ExoPlayer Documentation](https://exoplayer.dev/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

---

**Document Version**: 1.0  
**Last Updated**: 2026-05-02  
**Author**: Technical Architecture Team  
**Classification**: Internal Documentation
