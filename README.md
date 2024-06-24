<div align="center">

<img width="" src="fastlane/metadata/android/en-US/images/icon.png"  width=160 height=160  align="center">

# Seal

### Video/Audio Downloader for Android


English
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-zh_Hans.md">简体中文</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-zh_Hant.md">繁體中文</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-ar.md">العربية</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-pt.md">Portuguese</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-ua.md">Українська</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-th.md">ภาษาไทย</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-fa.md">فارسی</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-it.md">Italiano</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-az.md">Azərbaycanca</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-ru.md">Русский</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-sr.md">Српски</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-ja.md">日本語</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-id.md">Indonesia</a>


[![F-Droid](https://img.shields.io/f-droid/v/com.junkfood.seal?color=b4eb12&label=F-Droid&logo=fdroid&logoColor=1f78d2)](https://f-droid.org/en/packages/com.junkfood.seal)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/JunkFood02/Seal?color=black&label=Stable&logo=github)](https://github.com/JunkFood02/Seal/releases/latest/)
[![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/JunkFood02/Seal?include_prereleases&label=Preview&logo=Github)](https://github.com/JunkFood02/Seal/releases/)
[![Keep a Changelog](https://img.shields.io/badge/Changelog-lightgray?style=flat&color=gray&logo=keep-a-changelog)](https://github.com/JunkFood02/Seal/blob/main/CHANGELOG.md)
[![GitHub all releases](https://img.shields.io/github/downloads/JunkFood02/Seal/total?label=Downloads&logo=github)](https://github.com/JunkFood02/Seal/releases/)
[![GitHub Repo stars](https://img.shields.io/github/stars/JunkFood02/Seal?style=flat&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPHN2ZyBoZWlnaHQ9IjI0IiB2aWV3Qm94PSIwIC05NjAgOTYwIDk2MCIgd2lkdGg9IjI0IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgogIDxwYXRoIGQ9Im0zNTQtMjQ3IDEyNi03NiAxMjYgNzctMzMtMTQ0IDExMS05Ni0xNDYtMTMtNTgtMTM2LTU4IDEzNS0xNDYgMTMgMTExIDk3LTMzIDE0M1pNMjMzLTgwbDY1LTI4MUw4MC01NTBsMjg4LTI1IDExMi0yNjUgMTEyIDI2NSAyODggMjUtMjE4IDE4OSA2NSAyODEtMjQ3LTE0OUwyMzMtODBabTI0Ny0zNTBaIiBzdHlsZT0iZmlsbDogcmdiKDI0NSwgMjI3LCA2Nik7Ii8%2BCjwvc3ZnPg%3D%3D&color=%23f8e444)](https://github.com/JunkFood02/Seal/stargazers)
[![Supported-Sites](https://img.shields.io/badge/Sites-9cf?style=flat&logo=data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPHN2ZyBoZWlnaHQ9IjI0cHgiIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0cHgiIGZpbGw9IiNGRkZGRkYiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgPHBhdGggZD0iTTAgMGgyNHYyNEgwVjB6IiBmaWxsPSJub25lIi8+CiAgPHBhdGggZD0iTTExLjk5IDJDNi40NyAyIDIgNi40OCAyIDEyczQuNDcgMTAgOS45OSAxMEMxNy41MiAyMiAyMiAxNy41MiAyMiAxMlMxNy41MiAyIDExLjk5IDJ6bTYuOTMgNmgtMi45NWMtLjMyLTEuMjUtLjc4LTIuNDUtMS4zOC0zLjU2IDEuODQuNjMgMy4zNyAxLjkxIDQuMzMgMy41NnpNMTIgNC4wNGMuODMgMS4yIDEuNDggMi41MyAxLjkxIDMuOTZoLTMuODJjLjQzLTEuNDMgMS4wOC0yLjc2IDEuOTEtMy45NnpNNC4yNiAxNEM0LjEgMTMuMzYgNCAxMi42OSA0IDEycy4xLTEuMzYuMjYtMmgzLjM4Yy0uMDguNjYtLjE0IDEuMzItLjE0IDJzLjA2IDEuMzQuMTQgMkg0LjI2em0uODIgMmgyLjk1Yy4zMiAxLjI1Ljc4IDIuNDUgMS4zOCAzLjU2LTEuODQtLjYzLTMuMzctMS45LTQuMzMtMy41NnptMi45NS04SDUuMDhjLjk2LTEuNjYgMi40OS0yLjkzIDQuMzMtMy41NkM4LjgxIDUuNTUgOC4zNSA2Ljc1IDguMDMgOHpNMTIgMTkuOTZjLS44My0xLjItMS40OC0yLjUzLTEuOTEtMy45NmgzLjgyYy0uNDMgMS40My0xLjA4IDIuNzYtMS45MSAzLjk2ek0xNC4zNCAxNEg5LjY2Yy0uMDktLjY2LS4xNi0xLjMyLS4xNi0ycy4wNy0xLjM1LjE2LTJoNC42OGMuMDkuNjUuMTYgMS4zMi4xNiAycy0uMDcgMS4zNC0uMTYgMnptLjI1IDUuNTZjLjYtMS4xMSAxLjA2LTIuMzEgMS4zOC0zLjU2aDIuOTVjLS45NiAxLjY1LTIuNDkgMi45My00LjMzIDMuNTZ6TTE2LjM2IDE0Yy4wOC0uNjYuMTQtMS4zMi4xNC0ycy0uMDYtMS4zNC0uMTQtMmgzLjM4Yy4xNi42NC4yNiAxLjMxLjI2IDJzLS4xIDEuMzYtLjI2IDJoLTMuMzh6IiBzdHlsZT0iZmlsbDogcmdiKDE2MiwgMTk4LCAyMzQpOyIvPgo8L3N2Zz4=&label=Supported)](https://github.com/yt-dlp/yt-dlp/blob/master/supportedsites.md)
[![Telegram Channel](https://img.shields.io/badge/Telegram-Seal-blue?style=flat&logo=telegram)](https://t.me/seal_app)
[![Matrix](https://img.shields.io/matrix/seal-space%3Amatrix.org?server_fqdn=matrix.org&style=flat&logo=element&label=Matrix&color=%230DBD8B)
](https://matrix.to/#/#seal-space:matrix.org)


</div>


## 📱 Screenshots

<div align="center">
<div>
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" width="30%" />
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg" width="30%" />
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.jpg" width="30%" />
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg" width="30%" />
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.jpg" width="30%" />
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.jpg" width="30%" />
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/7.jpg" width="30%" />
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/8.jpg" width="30%" />
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/9.jpg" width="30%" />
</div>
</div>

<br>

## 📖 Features

- Download videos and audio files from video platforms supported by [yt-dlp](https://github.com/yt-dlp/yt-dlp) (formerly youtube-dl).

- Embed metadata and video thumbnail into extracted audio files supported by [mutagen](https://github.com/quodlibet/mutagen).

- Download all videos in the playlist with one click.

- Use embedded [aria2c](https://github.com/aria2/aria2) as external downloader for all your downloads.

- Embed subtitles into the downloaded videos.

- Execute custom yt-dlp commands with templates.

- Manage in-app downloads and custom command templates.

- Easy to use and user-friendly.

- [Material Design 3](https://m3.material.io/) style UI, with dynamic color theme.

- MAD: UI and logic written with pure Kotlin. Single activity, no fragments, only composable destinations.



## ⬇️ Download

For most devices, it is recommended to install the **arm64-v8a** version of the apks

- Download the latest stable version from [GitHub releases](https://github.com/JunkFood02/Seal/releases/latest)
  - Install the [pre-release](https://github.com/JunkFood02/Seal/releases/) versions to help us test out new features & changes

- Stable releases are also available on [F-Droid](https://f-droid.org/packages/com.junkfood.seal/)

<!-- [<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="70">](https://f-droid.org/packages/com.junkfood.seal/) -->

## 💬 Contact

Join our [Telegram Channel](https://t.me/seal_app) or [Matrix Space](https://matrix.to/#/#seal-space:matrix.org) for discussion, announcements, and releases!

## 💖 Sponsors

<p><!-- sponsors --><a href="https://github.com/4kaimar"><img src="https://github.com/4kaimar.png" width="60px" alt="" /></a><a href="https://github.com/gordongw"><img src="https://github.com/gordongw.png" width="60px" alt="Gordon" /></a><a href="https://github.com/zuble"><img src="https://github.com/zuble.png" width="60px" alt="zuble" /></a><a href="https://github.com/Dannyordaniel"><img src="https://github.com/Dannyordaniel.png" width="60px" alt="Daniel " /></a><a href="https://github.com/bycassius"><img src="https://github.com/bycassius.png" width="60px" alt="CASSIUS" /></a><a href="https://github.com/AlexVasiluta"><img src="https://github.com/AlexVasiluta.png" width="60px" alt="Alex" /></a><a href="https://github.com/familyguy23"><img src="https://github.com/familyguy23.png" width="60px" alt="" /></a><!-- sponsors --></p>


Seal will be always free and open source for everyone. If you like it, please consider [sponsoring me](https://github.com/sponsors/JunkFood02)!

## 🤝 Contributing

Contributions are welcome!

You can help translate Seal on [Hosted Weblate](https://hosted.weblate.org/projects/seal/).
	
[![Translate status](https://hosted.weblate.org/widgets/seal/-/strings/multi-auto.svg)](https://hosted.weblate.org/engage/seal/)
	
>[!Note]
>
>For submitting bug reports, feature requests, questions, or any other ideas to improve, please read [CONTRIBUTING.md](https://github.com/JunkFood02/Seal/blob/main/CONTRIBUTING.md) for instructions and guidelines first.

## ⭐️ Star History

[![Star History Chart](https://api.star-history.com/svg?repos=JunkFood02/Seal&type=Timeline)](https://star-history.com/#JunkFood02/Seal&Timeline)


## 🧱 Credits

Seal is a simple GUI of [yt-dlp](https://github.com/yt-dlp/yt-dlp), based on [youtubedl-android](https://github.com/yausername/youtubedl-android)

Some of the UI designs and codes are borrowed from [Read You](https://github.com/Ashinch/ReadYou) and [Music You](https://github.com/Kyant0/MusicYou)

[dvd](https://github.com/yausername/dvd)

[Material color utilities](https://github.com/material-foundation/material-color-utilities)

[Monet](https://github.com/Kyant0/Monet)

## 📃 License

[![GitHub](https://img.shields.io/github/license/JunkFood02/Seal?style=for-the-badge)](https://github.com/JunkFood02/Seal/blob/main/LICENSE)

>[!Warning]
>
>Except for the source code licensed under the GPLv3 license,
>all other parties are prohibited from using Seal's name as a downloader app,
>and the same is true for Seal's derivatives.
>Derivatives include but are not limited to forks and unofficial builds.

<div align="right">
<table><td>
<a href="#start-of-content">👆 Scroll to top</a>
</td></table>
</div>
