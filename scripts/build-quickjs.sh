#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
JNILIBS_DIR="$ROOT_DIR/app/src/main/jniLibs"

# Locate Android NDK
NDK_PATH="${ANDROID_NDK_HOME:-${ANDROID_NDK_ROOT:-}}"
if [ -z "$NDK_PATH" ] && [ -n "${ANDROID_HOME:-}" ] && [ -d "${ANDROID_HOME:-}/ndk" ]; then
    NDK_PATH=$(find "$ANDROID_HOME/ndk" -maxdepth 1 -mindepth 1 2>/dev/null | sort -V | tail -n 1 || true)
fi

if [ -z "$NDK_PATH" ] || [ ! -d "$NDK_PATH" ]; then
    echo "Android NDK not found. Skipping native QuickJS compilation."
    exit 0
fi

echo "Using Android NDK at: $NDK_PATH"
TOOLCHAIN_FILE="$NDK_PATH/build/cmake/android.toolchain.cmake"

BUILD_DIR=$(mktemp -d)
trap 'rm -rf "$BUILD_DIR"' EXIT

echo "Cloning QuickJS-NG..."
git clone --depth 1 https://github.com/quickjs-ng/quickjs.git "$BUILD_DIR/quickjs"

ABIS=("arm64-v8a" "armeabi-v7a" "x86" "x86_64")

for ABI in "${ABIS[@]}"; do
    TARGET_DIR="$JNILIBS_DIR/$ABI"
    mkdir -p "$TARGET_DIR"

    # If libdeno.so already exists in this ABI, don't overwrite
    if [ -f "$TARGET_DIR/libdeno.so" ]; then
        echo "Skipping $ABI: libdeno.so already present"
        continue
    fi

    echo "Building QuickJS for $ABI..."
    ABI_BUILD_DIR="$BUILD_DIR/build-$ABI"
    cmake -B "$ABI_BUILD_DIR" -S "$BUILD_DIR/quickjs" \
        -DCMAKE_TOOLCHAIN_FILE="$TOOLCHAIN_FILE" \
        -DANDROID_ABI="$ABI" \
        -DANDROID_PLATFORM=24 \
        -DCMAKE_BUILD_TYPE=Release

    cmake --build "$ABI_BUILD_DIR" --target qjs_exe -j"$(nproc 2>/dev/null || echo 2)"

    cp "$ABI_BUILD_DIR/qjs" "$TARGET_DIR/libquickjs.so"
    chmod +x "$TARGET_DIR/libquickjs.so"
    echo "Successfully built $TARGET_DIR/libquickjs.so ($(du -h "$TARGET_DIR/libquickjs.so" | cut -f1))"
done

echo "JavaScript runtimes setup completed successfully."
