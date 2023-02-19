# Changelog

All notable changes (starting from v1.7.3) to stable releases will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v1.9.0][1.9.0] - unreleased

### Added

- Add share file operation in `VideoDetailDrawer`
- Add pre-release channel for auto-updating
- Add an option to update to Nightly builds of yt-dlp
- Add a switch for auto updating yt-dlp
- Make video/audio clip in `FormatSelectionPage`
- Edit title of the video in `FormatSelectionPage` before downloading
- Implement a new method to extract cookies from the database of WebView
- Add `Badge` on the icon to indicate the count of running process
- Add legacy app icon

### Changed

- Change the operation of open link to long pressing the link button in `VideoDetailDrawer`
- Change the thread number range of multi-threaded download to 1-24
- Change status bar icon to filled icon

### Fixed

- Fix a bug causing high-quality audio not downloaded with YT Premium cookies & YT Music URLs
- UI bug in `ShortcutChip` with long template
- Empty subtitle language breaks downloads
- Languages not visible in system settings

### Known issues

- Cookies may not work as expected in some devices, please try to re-generate cookies after this occurs. File an issue on GitHub with your device info when experience errors.

## [v1.8.2][1.8.2] - 2023-02-10

### Fixed

- Trimmed ASCII characters filename
- Unexpected error when downloading multiple video to SD card with quick download
- Error when cropping vertical thumbnails as artwork
- ID conflicts when importing custom templates

### Changed

- Add `horizontalScroll` to `LogPage`
- Revert the URL intent filters

## [v1.8.1][1.8.1] - 2023-02-01

### Fixed

- App crashes when downloading in private mode
- Unexpected ImeActions in TextFields
- Disable SD card download when the directory is not set
- Localized strings for file size texts

## [v1.8.0][1.8.0] - 2023-01-29

### Added

- Download to SD card
- Quick download in parallel
- Task dashboard & log page for custom commands
- Custom shortcuts for command templates
- Subtitle preferences
- Apply `--embed-chapters` for video downloads by default
- New color schemes for UI theming

### Changed

- New transition animation between destinations
- Change `minSdkVersion` to 21 (Android 5.0)
- Accessibility improvements to components
- Revert playlist items limit in v1.7.3
- Scan the download directory to the system media library after running commands
- Change the LongClick operations of `FormatItem` to share the stream URLs

## [v1.7.3][1.7.3] - 2023-01-10

### Fixed

- `Webview` captures Cookies from wrong domains
- Notifications of custom commands remain unfinished status
- App crashes when fails to parse video info for format selection
- App crashes when parsing channel info for playlist download

### Added

- Tips about streams merging in `FormatSelectionPage`

### Changed

- Playlist results are limited to 200 videos

[1.7.3]: https://github.com/JunkFood02/Seal/releases/tag/v1.7.3

[1.8.0]: https://github.com/JunkFood02/Seal/releases/tag/v1.8.0

[1.8.1]: https://github.com/JunkFood02/Seal/releases/tag/v1.8.1

[1.8.2]: https://github.com/JunkFood02/Seal/releases/tag/v1.8.2