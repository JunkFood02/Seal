# Seal - Technical Architecture & Deployment Guide

## Table of Contents
1. [System Architecture](#1-system-architecture)
2. [JavaScript Runtime (QuickJS)](#2-javascript-runtime-quickjs-integration)
3. [Media Playback Subsystem](#3-media-playback-subsystem)
4. [Build & Deployment Pipeline](#4-build--deployment-pipeline)
5. [Environment Configuration](#5-environment-configuration)
6. [CI/CD Pipeline](#6-cicd-pipeline)
7. [Testing](#7-testing)
8. [Rollback Procedures](#8-rollback-procedures)

---

## 1. System Architecture

### High-Level Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        Android App                         │
│  ┌───────────────────────────────────────────────────────┐ │
│  │                 Jetpack Compose UI                   │ │
│  │  ┌─────────┐  ┌──────────┐  ┌─────────────────────┐ │ │
│  │  │Download │  │ Downloads│  │   Media Player      │ │ │
│  │  │Page     │  │History   │  │   (ExoPlayer)       │ │ │
│  │  └─────────┘  └──────────┘  └─────────────────────┘ │ │
│  └───────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────┐ │
│  │              Koin Dependency Injection                │ │
│  │  ┌─────────────┐  ┌──────────────┐  ┌─────────────┐ │ │
│  │  │DownloaderV2 │  │PlayerViewModel│ │JS Runtime   │ │ │
│  │  └─────────────┘  └──────────────┘  └─────────────┘ │ │
│  └───────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────┐ │
│  │              youtubedl-android Library                 │ │
│  │         (Python + yt-dlp + FFmpeg + Aria2c)            │ │
│  └───────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────┐ │
│  │               Native Layer (C++/JNI)                   │ │
│  │  ┌─────────────────────────────────────────────────┐  │ │
│  │  │          QuickJS Runtime (libquickjs.so)        │  │ │
│  │  │    JNI Bridge (libquickjs_jni.so)               │  │ │
│  │  └─────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────┐ │
│  │              Foreground Services                       │ │
│  │  ┌──────────────┐          ┌───────────────────────┐ │ │
│  │  │DownloadService│          │    PlayerService     │ │ │
│  │  │(Background)  │          │  (Media Playback)    │ │ │
│  │  └──────────────┘          └───────────────────────┘ │ │
│  └───────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────┐ │
│  │              Room Database                            │ │
│  │  ┌─────────────────────────────────────────────────┐  │ │
│  │  │ DownloadedVideoInfo │ CommandTemplate │ Cookie  │  │ │
│  │  └─────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Architecture Principles

1. **Single Activity Architecture**
   - MainActivity hosts all screens via Jetpack Navigation Compose
   - Separate activities only for overlay/dialog purposes (QuickDownloadActivity, PlayerActivity, CrashReportActivity)

2. **Compose-First UI**
   - All new screens built with Jetpack Compose
   - Material Design 3 theming with dynamic colors
   - Monolithic composables organized by destination

3. **Separation of Concerns**
   - DownloaderV2: Task orchestration and state management
   - DownloadUtil: Low-level yt-dlp wrapper
   - PlayerViewModel: Playback state isolation
   - JavaScriptRuntimeManager: QuickJS lifecycle

4. **Service-Based Background Processing**
   - DownloadService: Foreground service for downloads (notifications required)
   - PlayerService: MediaSessionService for media playback (lock screen controls)

---

## 2. JavaScript Runtime (QuickJS Integration)

### 2.1 Why QuickJS?

| Factor | Node.js | Deno | QuickJS |
|--------|---------|------|---------|
| APK Size Impact | +71 MB | +95 MB | +6 MB (static lib) |
| Architecture Support | arm64 only | arm64 only | arm, arm64, x86, x86_64 |
| Startup Time | Slow | Moderate | 300µs |
| ES2023 Support | Full | Full | Full |

**Selection**: QuickJS provides minimal footprint (<1MB static library) with full ES2023 support, making it ideal for embedded use.

### 2.2 Integration Architecture

```
┌─────────────────────────────────────────────────────────────┐
│              Kotlin Application Layer                       │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  JavaScriptRuntimeManager.kt                          │ │
│  │  - initialize() → nativeInit()                        │ │
│  │  - executeScript() → nativeEvaluate()                 │ │
│  │  - configureYtDlpForJsRuntime()                       │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            ↓ JNI
┌─────────────────────────────────────────────────────────────┐
│               JNI Bridge (quickjs_jni.cpp)                  │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  Java_com_junkfood_seal_js_QuickJSRuntime_*           │ │
│  │  - Pointer management                                 │ │
│  │  - String marshaling                                  │ │
│  │  - Callback invocation                                │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│            C++ Bridge Layer (quickjs_android_runtime.cpp)   │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  QuickJSBridge class                                  │ │
│  │  - JS_NewRuntime() / JS_FreeRuntime()                 │ │
│  │  - JS_Eval() with exception handling                  │ │
│  │  - Global variable management                         │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│             QuickJS Core Library (C source)                 │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  quickjs.c, libregexp/*.c, libunicode/*.c            │ │
│  │  - ECMAScript 2023 interpreter                        │ │
│  │  - Garbage collection (ref-counting + cycle)          │ │
│  │  - Built-in modules (regex, unicode, etc.)            │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 2.3 Implementation Files

#### Native Layer (C++)

- `app/src/main/cpp/quickjs_jni.h` - JNI interface declarations
- `app/src/main/cpp/quickjs_android_runtime.cpp` - C++ wrapper
- `app/src/main/cpp/CMakeLists.txt` - Build configuration
- `app/src/main/cpp/quickjs/` - QuickJS source (v2025-09-13)

#### Java/Kotlin Layer

- `app/src/main/java/com/junkfood/seal/js/QuickJSRuntime.kt` - Singleton runtime manager
- `app/src/main/java/com/junkfood/seal/js/JavaScriptRuntimeManager.kt` - yt-dlp integration

### 2.4 Initialization Flow

```kotlin
// App.kt → onCreate()
JavaScriptRuntimeManager.initialize(context).let { success ->
    if (success) {
        Log.i(TAG, "QuickJS runtime ready for yt-dlp")
    }
}
```

### 2.5 yt-dlp Integration

```kotlin
// When creating YouTubeDLRequest
val request = YoutubeDLRequest(url)
JavaScriptRuntimeManager.applyToRequest(request)
// Result: request includes --js-runtimes quickjs:/data/data/.../quickjs

// yt-dlp usage (external):
// yt-dlp --js-runtimes quickjs:/path/to/quickjs <url>
```

### 2.6 Error Handling

```cpp
// C++ side
if (!JS_Eval(ctx, script, len, filename, 0)) {
    JSValue exc = JS_GetException(ctx);
    const char* msg = JS_ToCString(ctx, exc);
    // Propagate to Java via callback
}

// Kotlin side
QuickJSRuntime.evaluate(script) { result ->
    if (result.isSuccess) { /* use result */ }
    else { Log.e(TAG, "JS error: ${runtime.getLastError()}") }
}
```

---

## 3. Media Playback Subsystem

### 3.1 Core Components

| Component | Purpose | Lifecycle |
|-----------|---------|-----------|
| **PlayerService** | Foreground MediaSessionService | App lifetime (while playing) |
| **PlayerViewModel** | Reactive playback state | Activity/Fragment scope |
| **ExoPlayer** | Media rendering engine | Managed by PlayerService |
| **MediaSession** | External control integration | Connected via MediaController |

### 3.2 Playback Architecture

```
┌──────────────────────────────────────────────────────────┐
│                      Composable UI                       │
│  ┌────────────────────────────────────────────────────┐ │
│  │       MediaPlayerScreen (Composable)               │ │
│  │  - VideoPlayerSurface (PlayerSurface)              │ │
│  │  - PlaybackControls (custom composables)           │ │
│  │  - Observes PlayerViewModel state flows            │ │
│  └────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────┐
│                PlayerViewModel (Kotlin)                  │
│  ┌────────────────────────────────────────────────────┐ │
│  │  - Expose playback state as StateFlow              │ │
│  │  - Expose position, duration, buffering            │ │
│  │  - Command methods: play(), pause(), seek()        │ │
│  └────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────┐
│               PlayerService (Android Service)            │
│  ┌────────────────────────────────────────────────────┐ │
│  │  - Foreground service with notification            │ │
│  │  - MediaSession for external controls              │ │
│  │  - Manages ExoPlayer lifecycle                     │ │
│  │  - Handles ACTION_PLAY, PAUSE, NEXT, PREV          │ │
│  └────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────┐
│                    ExoPlayer Instance                    │
│  ┌────────────────────────────────────────────────────┐ │
│  │  - Render video (SurfaceView/SurfaceHolder)        │ │
│  │  - Audio session (AudioAttributes)                 │ │
│  │  - Playlist management                             │ │
│  │  - DRM, Subtitles, Adaptive streaming              │ │
│  └────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

### 3.3 Supported Media Formats

| Container | Video Codecs | Audio Codecs | Notes |
|-----------|--------------|--------------|-------|
| MP4 | H.264, H.265, VP9, AV1 | AAC, MP3, Opus | ✅ Native support |
| MKV | All | All | ✅ FFmpeg-based |
| WebM | VP9, AV1 | Opus, Vorbis | ✅ |
| TS, M3U8 | H.264, H.265 | AAC | ✅ Adaptive streaming |
| Audio-only (MP3/M4A/FLAC) | - | All standard | ✅ Background playback |

### 3.4 Features

- **Background Playback**: Foreground service with persistent notification
- **MediaSession Integration**: Android Auto, Wear OS, lock screen controls
- **Adaptive Streaming**: HLS/DASH support via ExoPlayer
- **Subtitle Rendering**: External (SRT/VTT/ASS) and embedded
- **Audio Focus**: Automatic ducking and interruption handling
- **Picture-in-Picture**: Supported on Android 8.0+

### 3.5 UI Control Mapping

| Button | Action | ExoPlayer Command |
|--------|--------|-------------------|
| Play/Pause | togglePlayPause() | `player.playWhenReady = !player.playWhenReady` |
| Seek | onSeekTo(position) | `player.seekTo(position)` |
| Next | playNext() | `player.seekToNext()` |
| Previous | playPrevious() | `player.seekToPrevious()` |
| Volume | onVolumeChanged(vol) | `player.volume = vol` |
| Speed | onSpeedChanged(speed) | `player.setPlaybackParameters(PlaybackParameters(speed))` |

### 3.6 Seamless Download → Play Transition

```kotlin
// When download completes (DownloadUtil.kt -> DownloaderV2.kt)
onSuccess { filePaths ->
    // 1. Insert into database for history
    insertInfoIntoDownloadHistory(videoInfo, filePaths)

    // 2. Create intent to launch player
    val intent = Intent(context, PlayerActivity::class.java).apply {
        putExtra("media_uri", filePaths.first())
        putExtra("media_title", videoInfo.title)
        putExtra("media_artist", videoInfo.uploader)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}
```

---

## 4. Build & Deployment Pipeline

### 4.1 Build Chain Overview

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Developer  │────▶│  Git Push   │────▶│ GitHub Actions│
│   Laptop     │     │  to remote  │     │   CI/CD      │
└──────────────┘     └──────────────┘     └──────────────┘
                                                     │
                   ┌─────────────────────────────────┘
                   ▼
         ┌──────────────────────┐
         │ Static Analysis     │
         │  - ktlint           │
         │  - detekt           │
         │  - lint             │
         └──────────────────────┘
                   │
                   ▼
         ┌──────────────────────┐
         │ Unit Tests          │
         │  - JUnit4           │
         │  - Robolectric      │
         └──────────────────────┘
                   │
                   ▼
         ┌──────────────────────┐
         │ Instrumentation     │
         │  - Espresso         │
         │  - UI Automator     │
         └──────────────────────┘
                   │
         ┌─────────┴─────────┐
         ▼                   ▼
    ┌─────────┐         ┌─────────┐
    │ APK (per│         │  AAB    │
    │  ABI)   │         │(Bundle) │
    └─────────┘         └─────────┘
         │                   │
         └─────────┬─────────┘
                   ▼
         ┌──────────────────────┐
         │  Artifact Upload    │
         │  → GitHub Releases  │
         └──────────────────────┘
```

### 4.2 Build Variants

| Variant | Purpose | Minify | Signing |
|---------|---------|--------|---------|
| **genericDebug** | Development & testing | No | Debug key |
| **genericRelease** | Direct APK distribution | Yes (R8) | Release key |
| **githubPreview** | Pre-release testing | Yes | Release key |
| **fdroid** | F-Droid repository | Yes | F-Droid key |
| **genericRelease (AAB)** | Google Play Store | Yes | Play signing |

### 4.3 ABI Splitting Strategy

```gradle
splits {
    abi {
        enable true
        reset()
        include "arm64-v8a", "armeabi-v7a", "x86", "x86_64"
        universalApk true  // Also build universal APK
    }
}
```

**APK sizes** (approx.):
- arm64-v8a: ~45 MB (yt-dlp + ffmpeg + aria2c + QuickJS)
- armeabi-v7a: ~38 MB
- x86_64: ~46 MB
- universal: ~92 MB (all ABIs combined)

### 4.4 ProGuard/R8 Rules

```proguard
# Keep yt-dlp reflection usage
-keep class com.yausername.youtubedl_android.** { *; }
-keep class org.python.** { *; }
-keep class _** { *; }

# Keep QuickJS JNI bridge
-keep class com.junkfood.seal.js.** { *; }
-keepclassmembers class * {
    native <methods>;
}

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontwarn kotlinx.serialization.**
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
```

---

## 5. Environment Configuration

### 5.1 Required Environment Variables

| Variable | Purpose | Example |
|----------|---------|---------|
| `KEYSTORE_PATH` | Path to Android keystore | `/home/user/keystore.jks` |
| `KEYSTORE_PASSWORD` | Keystore password | `super_secret` |
| `KEY_ALIAS` | Key alias in keystore | `github` |
| `KEY_PASSWORD` | Key password (often same as keystore) | `super_secret` |
| `GITHUB_TOKEN` | GitHub API token for releases | `ghp_xxxxxxxx` |
| `PLAY_STORE_JSON_KEY` | Service account JSON for Play Console | `base64-encoded` |

### 5.2 Keystore Setup

**Generate keystore (once):**
```bash
keytool -genkey -v \
  -keystore keystore.jks \
  -alias github \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**Store in secure location:**
```bash
# Recommended: ~/.seal/keystore.jks (or CI secrets)
mkdir -p ~/.seal
cp keystore.jks ~/.seal/
chmod 600 ~/.seal/keystore.jks
```

### 5.3 GitHub Secrets (CI/CD)

In repository Settings → Secrets and variables → Actions:

| Secret Name | Value |
|--------------|-------|
| `KEYSTORE_PATH` | `/home/runner/keystore.jks` |
| `KEYSTORE_PASSWORD` | `${{ secrets.KEYSTORE_PASSWORD }}` |
| `KEY_ALIAS` | `github` |
| `KEY_PASSWORD` | `${{ secrets.KEYSTORE_PASSWORD }}` (if same) |
| `GITHUB_TOKEN` | `${{ secrets.GITHUB_TOKEN }}` (auto-provided but need `repo` scope) |

### 5.4 F-Droid Build Configuration

F-Droid builds use separate keystore. No additional configuration needed if flavor is set to `fdroid`.

---

## 6. CI/CD Pipeline

### 6.1 GitHub Actions Workflow

The CI pipeline (`/.github/workflows/ci.yml`) executes:

1. **Static Analysis** (on all PRs/pushes)
   ```
   ./gradlew ktlintFormatCheck   # Code formatting
   ./gradlew detekt             # Static analysis
   ./gradlew lint               # Android lint
   ```

2. **Unit Tests**
   ```
   ./gradlew testDebugUnitTest
   ```

3. **Build Matrix** (on `main` branch or tags)
   ```
   - Platform: ubuntu-latest
   - JDK: 21 (Temurin)
   - Build for: arm64-v8a, armeabi-v7a, x86_64, x86, universal
   - Output: APKs uploaded as artifacts
   ```

4. **Smoke Tests** (on PRs only)
   - Android Emulator (API 35)
   - Instrumentation tests

### 6.2 Fastlane Lanes

| Lane | Purpose | Trigger |
|------|---------|---------|
| `build_release` | Build all release APKs | Manual / CI |
| `test` | Run all tests | Pre-commit/CI |
| `lint` | Format + static analysis | Pre-commit/CI |
| `deploy_github` | Upload to GitHub Releases | Git tag (`v*`) |
| `deploy_playstore` | Upload to Play Store (internal) | Manual |
| `promote_beta` | Promote to beta track | Manual |
| `promote_production` | Promote to production | Manual |
| `release_notes` | Generate changelog | Pre-deploy |

### 6.3 Automated Deployment Triggers

| Event | Action | Artifacts |
|-------|--------|-----------|
| Push to `main` | Build APKs, upload as workflow artifacts | All ABIs |
| Tag `v*` | Create GitHub Release with APKs + AAB | Universal + Split APKs, AAB |
| PR from fork | Run tests & lint only | Test results |
| Manual `deploy_playstore` lane | Upload to Google Play Console (internal) | AAB |

### 6.4 Quality Gates

- ✅ All lint warnings must be fixed
- ✅ All unit tests must pass (100% pass rate)
- ✅ APK size check (<60MB per ABI)
- ✅ Build must include QuickJS native library (`libquickjs_jni.so`)
- ✅ Version code must increment (CI fails if not)
- ✅ APK signature verification (CI validates with public key)

---

## 7. Testing

### 7.1 Unit Testing

**Location**: `app/src/test/java/`

**Coverage Areas**:
- `DownloadUtilTest` - URL parsing, format selection
- `PlayerViewModelTest` - Playback state logic
- `JavaScriptRuntimeTest` - QuickJS evaluation (with mock)
- `DatabaseTest` - Room DAO operations

**Command**:
```bash
./gradlew testDebugUnitTest
./gradlew testDebugUnitTest --tests "com.junkfood.seal.player.PlayerViewModelTest"
```

### 7.2 Instrumentation Testing

**Location**: `app/src/androidTest/java/`

**Test Cases**:
- Download flow: Input URL → Fetch info → Select format → Download
- Player: Launch from download → Play/Pause → Seek → Exit
- QuickJS: Execute simple script via JNI
- Database: Insert/retrieve/delete video info

**Command**:
```bash
./gradlew connectedDebugAndroidTest
```

### 7.3 Smoke Tests (CI)

Minimal test suite runs on PRs (faster):
```kotlin
@RunWith(AndroidJUnit4::class)
class SmokeTest {
    @Test fun appLaunches() { /* ... */ }
    @Test fun quickJsInitializes() { /* ... */ }
    @Test fun playerServiceStarts() { /* ... */ }
}
```

### 7.4 Performance Benchmarks

**Cold Start Time**: < 2 seconds
**Memory Usage**: < 150 MB (idle), < 300 MB (playing 4K video)
**APK Size**: < 50 MB per ABI split

Benchmark via `androidx.benchmark:benchmark-macro` (future).

---

## 8. Rollback Procedures

### 8.1 Instant Rollback (Hotfix)

If a release causes crashes:

1. **Identify** affected version via Crashlytics/firebase
2. **Rollback** on Google Play Console:
   - Open Play Console → Release > Production
   - Click "Review and roll out to production"
   - Select previous version → Promote to 100%
3. **Communicate** via Telegram channel / GitHub Discussions
4. **Fix** in `hotfix/` branch and prepare emergency release

**Rollback window**: < 1 hour for 100% rollout via staged rollout de-escalation.

### 8.2 Hotfix Release Process

```bash
# 1. Create hotfix branch from main
git checkout -b hotfix/crash-fix-v2.1.1 main

# 2. Make fix, test
./gradlew test && ./gradlew lint

# 3. Version bump (patch)
# In build.gradle.kts: versionName = "2.1.1", versionCode = 200000151

# 4. Commit and tag
git commit -am "Fix crash on JS evaluation"
git tag v2.1.1
git push origin main --tags

# 5. Fastlane deploys automatically via GitHub Actions
```

### 8.3 Database Migration Fallback

Room database migrations are auto-generated. If migration fails:

```kotlin
// Fallback: Clear database (logged as last resort)
// Users receive warning and data loss notice
@Database(
    entities = [...],
    version = 7,
    autoMigrations = [...],
    exportSchema = true
)
// If migration fails, app shows MigrationRequiredDialog
```

### 8.4 QuickJS Update Rollback

QuickJS is a native library; version changes require full APK update. To rollback:

- On GitHub Releases: Download previous APK (pre-QuickJS or previous QuickJS version)
- yt-dlp update functionality allows quick patch release

### 8.5 Monitoring & Alerts

**Firebase Crashlytics** integrated for crash reporting.

**Custom metrics** (optional future):
- Download success rate (> 98% target)
- Playback success rate (> 99%)
- JS challenge solver latency (< 5s target)
- APK crash-free sessions (> 99.5%)

### 8.6 Communication Strategy

- **In-app**: Crash dialog with copy-to-clipboard error (already implemented)
- **Telegram**: @seal_app announcements channel
- **GitHub**: Create issue with `type: bug` label
- **Changelog**: Written using Keep a Changelog format (CHANGELOG.md)

---

## Appendix

### A. Directory Structure

```
Sindra-Music/                         # Project root
├── app/
│   ├── src/main/
│   │   ├── cpp/                      # Native C++ code
│   │   │   ├── CMakeLists.txt
│   │   │   ├── quickjs_jni.h
│   │   │   ├── quickjs_android_runtime.cpp
│   │   │   └── quickjs/              # QuickJS source (large)
│   │   ├── java/com/junkfood/seal/
│   │   │   ├── js/                   # JS runtime classes
│   │   │   ├── player/               # Media player
│   │   │   │   ├── PlayerService.kt
│   │   │   │   ├── PlayerViewModel.kt
│   │   │   │   ├── PlayerActivity.kt
│   │   │   │   └── ui/
│   │   │   │       └── MediaPlayerScreen.kt
│   │   │   └── ... (existing classes)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── fastlane/
│   ├── Fastfile
│   └── Appfile
├── .github/workflows/
│   ├── ci.yml                        # CI pipeline
│   └── deploy.yml                    # CD pipeline (future)
├── gradle/
│   └── libs.versions.toml            # Version catalog
├── scripts/
│   ├── build_quickjs.sh              # Build QuickJS for Android
│   ├── release.sh                    # Manual release script
│   └── sign_apk.sh                   # Signing utility
└── docs/
    ├── ARCHITECTURE.md               # This document
    ├── QUICKJS_SETUP.md              # QuickJS integration details
    └── DEPLOYMENT.md                 # Deployment instructions
```

### B. QuickJS Build Instructions

QuickJS needs to be cross-compiled for Android. The build script:

```bash
#!/bin/bash
# scripts/build_quickjs.sh

NDK_PATH=${ANDROID_NDK_HOME:-$HOME/Library/Android/sdk/ndk/25.2.9519653}
QUICKJS_VERSION=2025-09-13
OUTPUT_DIR=app/src/main/cpp/quickjs

# Clone QuickJS
git clone https://github.com/bellard/quickjs.git
cd quickjs
git checkout release-$QUICKJS_VERSION

# Build for each ABI
for abi in arm64-v8a armeabi-v7a x86 x86_64; do
    $NDK_PATH/toolchains/llvm/prebuilt/linux-x86_64/bin/${abi}21-linux-android-clang \
        -shared -fPIC \
        -I. \
        quickjs.c libunicode/*.c libregexp/*.c \
        -o libquickjs_${abi}.so
done
```

### C. Versioning Scheme

Following [Semantic Versioning](https://semver.org/):

```
<major>.<minor>.<patch>-<build>

Example: 2.1.3-150
- major: Large feature release
- minor: Feature + yt-dlp version bump
- patch: Bug fixes, JS runtime updates
- build: Internal build number (matches versionCode in Gradle)
```

`versionCode` formula (used by Play Store):
```
versionCode = (major * 10000000) + (minor * 10000) + (patch * 100) + build
Example: 2.1.3-150 → 20103150
```

### D. References

- [yt-dlp JavaScript Execution](https://github.com/yt-dlp/yt-dlp/blob/master/CONTRIBUTING.md#javascript-execution)
- [Media3 Developer Guide](https://developer.android.com/media/media3)
- [ExoPlayer Extension Points](https://exoplayer.dev/)
- [QuickJS Official Docs](https://bellard.org/quickjs/)
- [Jetpack Compose Best Practices](https://developer.android.com/jetpack/compose/designs)

---

**Last Updated**: 2026-05-02  
**Maintainer**: JunkFood02 / Seal contributors  
**License**: GPLv3 (See LICENSE file)
