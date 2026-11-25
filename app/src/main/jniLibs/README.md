# Bundled Native JavaScript Runtimes

This directory holds optional prebuilt native JavaScript runtime executables for `yt-dlp` on Android.

## Why this is needed

Recent versions of `yt-dlp` require an external JavaScript runtime (Deno or QuickJS) to solve YouTube extraction challenges (EJS challenge solver). On Android 10+ (API 29+), SELinux W^X policies prevent executing binaries stored in the application's writable data directory (`/data/data/...`). Binaries can only be executed if they are placed in the application's native library directory (`nativeLibraryDir`), packaged via `jniLibs/<abi>/lib<name>.so`.

## Supported runtimes

1. **Deno** (`libdeno.so`):
   - Place Android ARM64 ELF executable `deno` renamed to `libdeno.so` in `app/src/main/jniLibs/arm64-v8a/libdeno.so`.
   - Seal will automatically detect `libdeno.so` in `nativeLibraryDir` and pass `--js-runtimes "deno:<path>/libdeno.so"` to `yt-dlp`.

2. **QuickJS** (`libquickjs.so` or `libqjs.so`):
   - Place QuickJS `qjs` executable renamed to `libquickjs.so` in `app/src/main/jniLibs/<abi>/libquickjs.so`.
   - Seal will automatically detect it and pass `--js-runtimes "quickjs:<path>/libquickjs.so"` to `yt-dlp`.
