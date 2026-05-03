# Seal - Deployment Guide

## Quick Start

### For Developers

1. **Clone & Setup**
   ```bash
   git clone https://github.com/JunkFood02/Seal.git
   cd Seal
   ./scripts/setup.sh
   ```

2. **Build Debug APK**
   ```bash
   ./gradlew assembleGenericDebug
   # APK: app/build/outputs/apk/generic/debug/Seal-debug.apk
   ```

3. **Run on Device**
   ```bash
   adb install app/build/outputs/apk/generic/debug/Seal-debug.apk
   ```

### For CI/CD

1. **Set up secrets** in GitHub repository settings
2. **Push to `main`** triggers CI (test + build)
3. **Tag with `v*`** triggers automatic release to GitHub Releases

---

## Build Process

### Local Build (Manual)

#### Pre-requisites
- Android Studio Flamingo (2022.2.1) or later
- Android NDK (r25b)
- CMake 3.22+
- JDK 21 (Temurin)

#### Steps

```bash
# 1. Clean
./gradlew clean

# 2. Format code (optional, but required for PR)
./gradlew ktlintFormat

# 3. Run tests
./gradlew testDebugUnitTest

# 4. Build release APK for specific ABI
./gradlew assembleGenericRelease -PABI_FILTERS=arm64-v8a

# 5. Build universal APK (all ABIs bundled)
./gradlew assembleGenericRelease -PnoSplits

# 6. Build AAB for Play Store
./gradlew bundleGenericRelease
```

#### Output Locations

| Build Type | Path |
|-----------|------|
| Debug APK | `app/build/outputs/apk/generic/debug/` |
| Release APK (split) | `app/build/outputs/apk/generic/release/Seal-{version}-{abi}.apk` |
| Release APK (universal) | `app/build/outputs/apk/generic/release/Seal-{version}-universal.apk` |
| AAB (bundle) | `app/build/outputs/bundle/genericRelease/` |

### Automated Build (CI)

GitHub Actions workflow runs on:
- Every push to `main`, `develop` branches
- Every PR targeting `main`
- Tags matching `v*` pattern (full release)

**Workflow**: `.github/workflows/ci.yml`

1. **Static analysis** (ktlint, detekt, lint)
2. **Unit tests**
3. **Build APKs** (matrix: arm64-v8a, armeabi-v7a, x86_64, x86)
4. **Build universal APK**
5. **Build AAB**
6. **Upload artifacts** (for 7 days)
7. **On tag → Create GitHub Release**

---

## Signing & Security

### Keystore Management

```bash
# 1. Generate keystore (do once)
keytool -genkeypair \
  -v \
  -keystore ~/.seal/keystore.jks \
  -alias github \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storetype JKS

# 2. Add to environment
echo "export KEYSTORE_PATH=~/.seal/keystore.jks" >> ~/.seal/seal.env
echo "export KEYSTORE_PASSWORD=your_password" >> ~/.seal/seal.env
source ~/.seal/seal.env

# 3. Add to CI (GitHub Secrets)
# Settings → Secrets and variables → Actions
# Add: KEYSTORE_PASSWORD, and upload keystore as secret file (or use artifact)
```

### Verifying Signature

```bash
# Verify APK is properly signed
apksigner verify --verbose Seal-2.1.0-arm64-v8a.apk

# Verify certificate matches expected
apksigner verify --print-certs Seal-*.apk
```

---

## Release Channels

### 1. GitHub Releases (Primary)

**Command**:
```bash
# Manual tagging & release
git tag v2.1.0
git push origin v2.1.0
# GitHub Actions auto-creates release with assets
```

**Artifacts uploaded**:
- `Seal-2.1.0-arm64-v8a.apk` (~45 MB)
- `Seal-2.1.0-armeabi-v7a.apk` (~38 MB)
- `Seal-2.1.0-x86_64.apk` (~46 MB)
- `Seal-2.1.0-universal.apk` (~92 MB)
- `Seal-2.1.0.aab` (~55 MB) (Play Store bundle)

**Fastlane alternative**:
```bash
bundle install
fastlane android deploy_github
```

### 2. Google Play Store (Staged Rollout)

| Track | Users | Purpose |
|-------|-------|---------|
| Internal testing | Up to 100 | Smoke test (1-2 days) |
| Alpha | Up to 1% | Early testing |
| Beta | Up to 10% | Wider testing |
| Production | 100% | Full release |

**Deployment to internal testing**:
```bash
fastlane android deploy_playstore   # Uploads to internal track
```

**Promotion**:
```bash
fastlane android promote_beta       # Internal → Beta
fastlane android promote_production # Beta → Production
```

### 3. F-Droid (Community Build Server)

F-Droid builds from source automatically if metadata updated.

Prerequisites:
- Source code available under FOSS license (GPLv3 ✓)
- Build instructions verified
- Dependencies in Maven Central

**Update F-Droid metadata**:
```bash
# In your fork of f-droid.org
# Add/update package metadata at:
#   fdroid/fdroiddata/data/com.junkfood.seal.json
```

---

## Versioning Scheme

### Semantic Versioning

```
MAJOR.MINOR.PATCH[-PRERELEASE]
```

Examples:
- `2.1.0` - Feature release (new player)
- `2.1.1` - Patch release (bug fix)
- `2.1.0-rc.1` - Release candidate
- `2.1.0-alpha.2` - Alpha build

### Version Code (integer)

Gradle `versionCode` must be monotonically increasing.

**Formula**:
```
versionCode = MAJOR × 10,000,000 + MINOR × 10,000 + PATCH × 100 + PRERELEASE_OFFSET
```

| Suffix | Offset |
|--------|--------|
| (none) | 0 |
| -alpha.N | 1 + N |
| -beta.N | 2 + N |
| -rc.N | 3 + N |
| -dev | 99 |

**Examples**:
```
2.1.3      → 20103100
2.1.3-rc1  → 20103104  (3 + 1 = 4)
2.1.3-rc2  → 20103105  (3 + 2 = 5)
```

**Automated** via `scripts/compute_version_code.sh` and Gradle.

---

## Post-Release Tasks

### 1. Changelog Update

Edit `CHANGELOG.md`:
```markdown
## [2.1.0] - 2026-05-02

### Added
- Media player with ExoPlayer integration
- QuickJS runtime for JavaScript challenges
- Picture-in-picture support

### Changed
- ...

### Fixed
- ...
```

### 2. Version Bump

Update in:
- `buildSrc/src/main/kotlin/Version.kt`
- (Repo is tagged, so Gradle can read from git tag)

### 3. GitHub Release Notes

Generated automatically from `CHANGELOG.md` section corresponding to version.
Can override via `fastlane github_release` manual upload.

### 4. Announcement Channels

- 📢 Telegram: @seal_app
- 🏪 Google Play Store listing (release notes)
- 🔖 GitHub Discussions (Release thread)

### 5. Monitoring

After release (first 24 hours):
- Monitor Crashlytics for new crashes
- Check Play Console ANR rate (< 0.1% target)
- Watch Git Issues for bug reports
- Telemetry (if opted-in): Success rate of downloads, playback

---

## Rollback Procedure

### Immediate Rollback (Hotfix)

If release causes widespread crash (≥ 5% crash-free users drop):

1. **Pause staged rollout** in Play Console
2. **Create hotfix branch** from `main`: `hotfix/rollback-v2.1.0`
3. **Revert changes** (or revert version bump):
   ```bash
   git revert <commit-with-breaking-change> --no-edit
   ```
4. **Bump patch version** → `2.1.1`
5. **Test** and repeat CI/CD
6. **Deploy** to internal testing → beta → production (10% → 100%)

### Database Downgrade

If Room migration fails on rollback:

```kotlin
// version 6 → version 5 (downgrade not supported by Room)
// Solution: bump versionCode, keep version=6 but provide fallback
@Database(version = 6, autoMigrations = [...])
abstract class AppDatabase : RoomDatabase() {
    // If user downgrades, app will MigrationRequiredException;
    // instruct user to reinstall (data loss, but acceptable for patch)
}
```

**User impact**: Downloaded video history preserved for 5 days, then cleared.

### QuickJS Rollback

QuickJS integration is via native library; cannot toggle at runtime. To revert to pre-QuickJS:

1. Build APK without QuickJS (remove JNI, runtime code)
2. Deploy as emergency hotfix (version 2.0.x)
3. Users on 2.1.x must manually downgrade (reinstall)

---

## Troubleshooting Build Issues

### CMake Error: "Could NOT find OpenGL"

```bash
# Install required packages
sudo apt-get install cmake ninja-build  # Ubuntu/Debian
brew install cmake ninja                 # macOS
```

### NDK not found

Set `ANDROID_NDK_HOME`:
```bash
export ANDROID_NDK_HOME=$HOME/Android/Sdk/ndk/25.2.9519653
```

Or install via SDK manager:
```bash
sdkmanager "ndk;25.2.9519653"
```

### ABI mismatch

Gradle may complain about `APP_ABI` not matching. Ensure `abiFilters` in `build.gradle`:

```gradle
android {
    defaultConfig {
        ndk {
            abiFilters 'arm64-v8a', 'armeabi-v7a', 'x86', 'x86_64'
        }
    }
}
```

### OutOfMemoryError during build

Increase Gradle heap size:
```bash
# Create/update gradle.properties
org.gradle.jvmargs=-Xmx8g -XX:MaxMetaspaceSize=1g
```

---

## Appendix

### A. Build Times (Reference)

| Machine | Task | Time |
|---------|------|------|
| M2 Max | Full build (all ABIs) | ~8 min |
| M2 Max | Single ABI APK | ~2 min |
| GitHub Actions (ubuntu-22.04) | Full build | ~12 min |
| CI (matrix parallel) | All 4 ABI APKs | ~6 min |

### B. Artifact Size Breakdown

**arm64-v8a APK** (~45 MB):

| Component | Size |
|-----------|------|
| Python 3.8 runtime | ~12 MB |
| yt-dlp + site-packages | ~8 MB |
| FFmpeg | ~15 MB |
| Aria2c | ~3 MB |
| QuickJS (lib + wrapper) | ~1.5 MB |
| App code & resources | ~3 MB |
| Kotlin stdlib, Compose, Media3 | ~2.5 MB |

### C. Useful Commands

```bash
# List all APKs
find app/build/outputs -name "*.apk" | sort

# Get APK size breakdown
apktool d Seal.apk -o decoded
du -sh decoded/* | sort -hr | head

# Print signing certificate info
apksigner verify --print-certs Seal.apk

# Zipalign (should be auto-done by Gradle)
zipalign -v -p 4 input.apk output.apk

# Extract native libraries
unzip -j Seal.apk "lib/*/*.so" -d native_libs/
```

---

**Document Version**: 1.0  
**Last Updated**: 2026-05-02  
**Maintained by**: JunkFood02 (Seal Development Team)
