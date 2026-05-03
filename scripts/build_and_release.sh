#!/bin/bash
#
# Seal - Automated Build & Release Script
# Usage: ./scripts/build_and_release.sh [version_name] [version_code]
#
# Example:
#   ./scripts/build_and_release.sh 2.1.0 20103100
#

set -euo pipefail

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
NDK_PATH="${ANDROID_NDK_HOME:-$ANDROID_HOME/ndk/25.2.9519653}"
KEYSTORE_PATH="${KEYSTORE_PATH:-$HOME/.seal/keystore.jks}"
KEY_ALIAS="${KEY_ALIAS:-github}"
KEYSTORE_PASSWORD="${KEYSTORE_PASSWORD:?Required}"
KEY_PASSWORD="${KEY_PASSWORD:-$KEYSTORE_PASSWORD}"

VERSION_NAME="${1:-$(git describe --tags --abbrev=0 2>/dev/null || echo '2.0.0')}"
VERSION_CODE="${2:-$(./scripts/compute_version_code.sh $VERSION_NAME)}"

echo "========================================"
echo "  Seal Build & Release Script"
echo "========================================"
echo "Version Name: $VERSION_NAME"
echo "Version Code: $VERSION_CODE"
echo ""

cd "$PROJECT_ROOT"

# 1. Verify environment
echo "[1/8] Verifying environment..."
if [ ! -f "$KEYSTORE_PATH" ]; then
    echo "ERROR: Keystore not found at $KEYSTORE_PATH"
    echo "Set KEYSTORE_PATH or copy keystore.jks to ~/.seal/"
    exit 1
fi

if [ ! -d "$NDK_PATH" ]; then
    echo "ERROR: NDK not found at $NDK_PATH"
    echo "Install NDK via Android Studio or set ANDROID_NDK_HOME"
    exit 1
fi

# 2. Clean previous builds
echo "[2/8] Cleaning build artifacts..."
./gradlew clean

# 3. Update version in Gradle
echo "[3/8] Updating version to $VERSION_NAME ($VERSION_CODE)..."
# This would typically be done automatically by Gradle based on git tag
# Or manually edit build.gradle.kts

# 4. Build per-ABI APKs
echo "[4/8] Building release APKs..."
./gradlew assembleGenericRelease \
    -PABI_FILTERS=arm64-v8a \
    -PKEYSTORE_PATH="$KEYSTORE_PATH" \
    -PKEYSTORE_PASSWORD="$KEYSTORE_PASSWORD" \
    -PKEY_ALIAS="$KEY_ALIAS" \
    -PKEY_PASSWORD="$KEY_PASSWORD"

./gradlew assembleGenericRelease \
    -PABI_FILTERS=armeabi-v7a \
    -PKEYSTORE_PATH="$KEYSTORE_PATH" \
    -PKEYSTORE_PASSWORD="$KEYSTORE_PASSWORD" \
    -PKEY_ALIAS="$KEY_ALIAS" \
    -PKEY_PASSWORD="$KEY_PASSWORD"

./gradlew assembleGenericRelease \
    -PABI_FILTERS=x86_64 \
    -PKEYSTORE_PATH="$KEYSTORE_PATH" \
    -PKEYSTORE_PASSWORD="$KEYSTORE_PASSWORD" \
    -PKEY_ALIAS="$KEY_ALIAS" \
    -PKEY_PASSWORD="$KEY_PASSWORD"

./gradlew assembleGenericRelease \
    -PABI_FILTERS=x86 \
    -PKEYSTORE_PATH="$KEYSTORE_PATH" \
    -PKEYSTORE_PASSWORD="$KEYSTORE_PASSWORD" \
    -PKEY_ALIAS="$KEY_ALIAS" \
    -PKEY_PASSWORD="$KEY_PASSWORD"

# 5. Build universal APK
echo "[5/8] Building universal APK..."
./gradlew assembleGenericRelease \
    -PnoSplits \
    -PKEYSTORE_PATH="$KEYSTORE_PATH" \
    -PKEYSTORE_PASSWORD="$KEYSTORE_PASSWORD" \
    -PKEY_ALIAS="$KEY_ALIAS" \
    -PKEY_PASSWORD="$KEY_PASSWORD"

# 6. Build AAB for Play Store
echo "[6/8] Building Android App Bundle (AAB)..."
./gradlew bundleGenericRelease \
    -PKEYSTORE_PATH="$KEYSTORE_PATH" \
    -PKEYSTORE_PASSWORD="$KEYSTORE_PASSWORD" \
    -PKEY_ALIAS="$KEY_ALIAS" \
    -PKEY_PASSWORD="$KEY_PASSWORD"

# 7. Verify signatures
echo "[7/8] Verifying signatures..."
APK_DIR="app/build/outputs/apk/generic/release"
for apk in "$APK_DIR"/*.apk; do
    if [ -f "$apk" ]; then
        echo "  Verifying $(basename "$apk")..."
        apksigner verify "$apk" || exit 1
    fi
done

# 8. Package artifacts
echo "[8/8] Packaging artifacts..."
RELEASE_DIR="$PROJECT_ROOT/releases/$VERSION_NAME"
mkdir -p "$RELEASE_DIR"

cp "$APK_DIR"/*-arm64-v8a.apk "$RELEASE_DIR/Seal-${VERSION_NAME}-arm64-v8a.apk" 2>/dev/null || true
cp "$APK_DIR"/*-armeabi-v7a.apk "$RELEASE_DIR/Seal-${VERSION_NAME}-armeabi-v7a.apk" 2>/dev/null || true
cp "$APK_DIR"/*-x86_64.apk "$RELEASE_DIR/Seal-${VERSION_NAME}-x86_64.apk" 2>/dev/null || true
cp "$APK_DIR"/*-x86.apk "$RELEASE_DIR/Seal-${VERSION_NAME}-x86.apk" 2>/dev/null || true
cp "$APK_DIR"/*-universal.apk "$RELEASE_DIR/Seal-${VERSION_NAME}-universal.apk" 2>/dev/null || true

AAB_DIR="app/build/outputs/bundle/genericRelease"
cp "$AAB_DIR"/*.aab "$RELEASE_DIR/" 2>/dev/null || true

# Generate checksums
cd "$RELEASE_DIR"
sha256sum *.apk *.aab > SHA256SUMS
cd "$PROJECT_ROOT"

echo ""
echo "✅ Build complete!"
echo "Artifacts: $RELEASE_DIR"
echo ""
echo "To deploy to GitHub Releases:"
echo "  fastlane android deploy_github"
echo ""
echo "Or run:"
echo "  gh release create v$VERSION_NAME $RELEASE_DIR/*.apk $RELEASE_DIR/*.aab \\"
echo "    --title \"Seal $VERSION_NAME\" \\"
echo "    --notes-file docs/release_notes_template.md"
