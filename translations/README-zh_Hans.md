<div align="center">
<img width="" src="fastlane/metadata/android/en-US/images/icon.png"  width=160 height=160  align="center">

# Seal

### 一个简单的 Android 视频/音频下载器，使用 [Jetpack Compose](https://developer.android.com/jetpack/compose) 开发

<p align="center">
  <a href="https://github.com/JunkFood02/Seal/blob/main/README.md">English</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-zh_Hans.md">简体中文</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-zh_Hant.md">繁體中文</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-ar.md">العربية</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-pt.md">Portuguese</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-ua.md">Українська</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-th.md">ภาษาไทย</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-fa.md">فارسی</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-it.md">Italiano</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-az.md">Azərbaycanca</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-ru.md">Русский</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-sr.md">Српски</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-ja.md">日本語</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-id.md">Indonesia</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-hi.md">हिंदी</a>&nbsp;|&nbsp;
  <a href="https://github.com/JunkFood02/Seal/blob/main/translations/README-bn.md">বাংলা</a>
</p>

[![F-Droid](https://img.shields.io/f-droid/v/com.junkfood.seal?color=b4eb12&label=F-Droid&logo=fdroid&logoColor=1f78d2)](https://f-droid.org/en/packages/com.junkfood.seal)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/JunkFood02/Seal?color=black&label=Stable&logo=github)](https://github.com/JunkFood02/Seal/releases/latest/)
[![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/JunkFood02/Seal?include_prereleases&label=Preview&logo=Github)](https://github.com/JunkFood02/Seal/releases/)
[![Keep a Changelog](https://img.shields.io/badge/Changelog-lightgray?style=flat&color=gray&logo=keep-a-changelog)](https://github.com/JunkFood02/Seal/blob/main/CHANGELOG.md)
[![GitHub all releases](https://img.shields.io/github/downloads/JunkFood02/Seal/total?label=Downloads&logo=github)](https://github.com/JunkFood02/Seal/releases/)
[![GitHub Repo stars](https://img.shields.io/github/stars/JunkFood02/Seal?color=informational&label=Stars)](https://github.com/JunkFood02/Seal/stargazers)
[![Supported Sites](https://img.shields.io/badge/Supported-Sites-9cf.svg?style=flat)](https://github.com/yt-dlp/yt-dlp/blob/master/supportedsites.md)
[![Telegram Channel](https://img.shields.io/badge/Telegram-Seal-blue?style=flat&logo=telegram)](https://t.me/seal_app)
[![Matrix Space](https://img.shields.io/badge/Matrix-Seal-Black?style=flat&color=black&logo=matrix)](https://matrix.to/#/#seal-space:matrix.org)
</div>

## 📱 屏幕截图

<div>
<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" width="30%" />
<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg" width="30%" />
<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/3.jpg" width="30%" />
<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg" width="30%" />
<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/5.jpg" width="30%" />
<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/6.jpg" width="30%" />
<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/7.jpg" width="30%" />
<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/8.jpg" width="30%" />
<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/9.jpg" width="30%" />
</div>


## 📖 功能特色

- 从 [yt-dlp](https://github.com/yt-dlp/yt-dlp) 所支持的数千个视频平台下载视频与音频

- 提取媒体元数据与专辑封面，调用 [mutagen](https://github.com/quodlibet/mutagen) 嵌入到提取的音频文件中

- 播放列表下载支持

- 使用 [aria2c](https://github.com/aria2/aria2) 进行下载

- 内嵌字幕于视频文件中

- 执行自定义的 yt-dlp 命令模板

- 管理应用内下载与自定义命令模板

- 使用简单、用户友好

- 遵循 [Material Design 3](https://m3.material.io/) 设计规范，实现了 [动态色彩](https://m3.material.io/foundations/customization) 主题的应用界面
  
- MAD：完全使用 Kotlin 构造界面与编写逻辑，单 Activity + Compose Navigation 应用结构

## ⬇️ 下载

对于大多数设备，推荐安装 **arm64-v8a** 版本的 apk 文件。

- 从 [GitHub Releases](https://github.com/JunkFood02/Seal/releases/latest) 下载最新的稳定版本
  - 也可安装 [预发布版本](https://github.com/JunkFood02/Seal/releases/) 帮助我们测试新功能和改进

- 稳定版也上架了 [F-Droid](https://f-droid.org/packages/com.junkfood.seal/)

<!-- [<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="70">](https://f-droid.org/packages/com.junkfood.seal/) -->

## 💬 联系我们

欢迎加入我们的 [Telegram 频道](https://t.me/seal_app) 或 [Matrix 空间](https://matrix.to/#/#seal-space:matrix.org) 进行讨论、获取最新动态与版本发布信息！

## 💖 贡献者

<p><!-- sponsors --><a href="https://github.com/Cook-I-T"><img src="https:&#x2F;&#x2F;github.com&#x2F;Cook-I-T.png" width="60px" alt="User avatar: Cook I.T!" /></a><a href="https://github.com/reallyrealcolby"><img src="https:&#x2F;&#x2F;github.com&#x2F;reallyrealcolby.png" width="60px" alt="User avatar: " /></a><a href="https://github.com/abelladianne458-gif"><img src="https:&#x2F;&#x2F;github.com&#x2F;abelladianne458-gif.png" width="60px" alt="User avatar: " /></a><a href="https://github.com/agusterodin"><img src="https:&#x2F;&#x2F;github.com&#x2F;agusterodin.png" width="60px" alt="User avatar: Jeff Rosen" /></a><!-- sponsors --></p>

Seal 将始终保持免费开源。如果你喜欢这个项目，欢迎 [赞助我](https://github.com/sponsors/JunkFood02)！

## 🤝 参与贡献

欢迎各种形式的贡献！

你可以通过 [Hosted Weblate](https://hosted.weblate.org/projects/seal/) 参与 Seal 的翻译工作。

[![Translate status](https://hosted.weblate.org/widgets/seal/-/strings/multi-auto.svg)](https://hosted.weblate.org/engage/seal/)

> [!note]
> 在提交错误报告、功能请求或其他改进建议前，请先在 [Issues](https://github.com/JunkFood02/Seal/issues) 和 [Discussions](https://github.com/JunkFood02/Seal/discussions) 中搜索（包括已关闭的内容），避免重复。如未找到类似内容，欢迎 [发起讨论](https://github.com/JunkFood02/Seal/discussions) 或 [提交 Issue](https://github.com/JunkFood02/Seal/issues/new)。详细规范请参阅 [CONTRIBUTING.md](https://github.com/JunkFood02/Seal/blob/main/CONTRIBUTING.md)。

## ⭐️ Star（星标） 历史

[![Star History Chart](https://api.star-history.com/svg?repos=JunkFood02/Seal&type=Timeline)](https://star-history.com/#JunkFood02/Seal&Timeline)

## 🧱 致谢

Seal 是 [yt-dlp](https://github.com/yt-dlp/yt-dlp) 的简单 GUI 封装，基于 [youtubedl-android](https://github.com/yausername/youtubedl-android) 实现。

部分 UI 设计与代码参考了 [Read You](https://github.com/Ashinch/ReadYou) 和 [Music You](https://github.com/Kyant0/MusicYou)。

- [dvd](https://github.com/yausername/dvd)
- [Material color utilities](https://github.com/material-foundation/material-color-utilities)
- [Monet](https://github.com/Kyant0/Monet)

## 📃 许可证

[GNU GPL v3.0（通用公共许可证）](https://github.com/JunkFood02/Seal/blob/main/LICENSE)

> [!Warning]
> 除采用 GPLv3 许可证的源代码外，禁止任何其他方将 Seal 的名称用作下载器应用，Seal 的衍生项目（包括但不限于复刻及非官方构建）亦同。

<div align="right">
<table><td>
<a href="#start-of-content">👆 回到顶部</a>
</td></table>
</div>
