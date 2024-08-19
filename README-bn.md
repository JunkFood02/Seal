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
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-hi.md">हिंदी</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
<a href="https://github.com/JunkFood02/Seal/blob/main/README-bn.md">বাংলা</a>

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


## 📱  স্ক্রিনশট

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

- [yt-dlp](https://github.com/yt-dlp/yt-dlp) (পূর্বের youtube-dl) সাপোর্টেড ভিডিও প্ল্যাটফর্ম থেকে ভিডিও এবং অডিও ফাইল ডাউনলোড করুন।

- [mutagen](https://github.com/quodlibet/mutagen) সাপোর্টেড এক্সট্র্যাক্টটেড অডিও ফাইলগুলিতে মেটাডেটা এবং ভিডিও থাম্বনেল এমবেডেড ।

- এক ক্লিকে প্লেলিস্টের সব ভিডিও ডাউনলোড করুন।

- সব ডাউনলোডের জন্য ডাউনলোডার হিসাবে এমবেডেড [aria2c](https://github.com/aria2/aria2) .

- ডাউনলোড করা ভিডিওগুলিতে সাবটাইটেল এম্বেড ।

- টেমপ্লেট সহ কাস্টম yt-dlp কমান্ড রান করা।

- ইন-অ্যাপ ডাউনলোড এবং কাস্টম কমান্ড টেমপ্লেট এর ব্যাবহার।

- ব্যবহার করা সহজ এবং ইউজার ফ্রেন্ডলি।

- [Material Design 3](https://m3.material.io/) স্টাইলের UI, ডাইনামিক থিম সহ।

- MAD: UI এবং logic pure Kotlin এ লিখা. একক কার্যকলাপ, কোন টুকরা নেই, শুধুমাত্র কমপোজ যোগ্য গন্তব্য।



## ⬇️ ডাউনলোড

বেশিরভাগ ডিভাইসের জন্য, apks-এর **arm64-v8a** ইনস্টল করুন।

- [GitHub releases](https://github.com/JunkFood02/Seal/releases/latest) থেকে লেটেস্ট রিলিজ ডাউনলোড করুন।
  - আমাদের সাহায্য করতে নতুন ফিচার ও পরিবর্তন টেস্ট করতে জন্য [pre-release](https://github.com/JunkFood02/Seal/releases/) ভার্সন ইনস্টল করুন।

- [F-Droid](https://f-droid.org/packages/com.junkfood.seal/) এও লেটেস্ট রিলিজ পাওয়া যাবে

<!-- [<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="70">](https://f-droid.org/packages/com.junkfood.seal/) -->

## 💬 যোগাযোগ

আলোচনা, ঘোষণা এবং প্রকাশের জন্য আমাদের  [টেলিগ্রাম চ্যানেল](https://t.me/seal_app) বা [ম্যাট্রিক্স স্পেস](https://matrix.to/#/#seal-space:matrix.org) এ যোগ দিন!

## 💖 স্পনসর

<p><!-- sponsors --><a href="https://github.com/4kaimar"><img src="https://github.com/4kaimar.png" width="60px" alt="" /></a><a href="https://github.com/gordongw"><img src="https://github.com/gordongw.png" width="60px" alt="Gordon" /></a><a href="https://github.com/zuble"><img src="https://github.com/zuble.png" width="60px" alt="zuble" /></a><a href="https://github.com/Dannyordaniel"><img src="https://github.com/Dannyordaniel.png" width="60px" alt="Daniel " /></a><a href="https://github.com/NRN83"><img src="https://github.com/NRN83.png" width="60px" alt="" /></a><!-- sponsors --></p>


সীল সর্বদা বিনামূল্যে এবং সবার জন্য উন্মুক্ত থাকবে । আপনারা যদি এটা পছন্দ করেন, আমাকে [স্পনসর করুন!](https://github.com/sponsors/JunkFood02)!

## 🤝 অবদান

 সবার অবদানকে স্বাগত জানাই!
 আপনি [Hosted Weblate](https://hosted.weblate.org/projects/seal/) সিল অনুবাদ করতে সাহায্য করতে পারেন।
	
[![Translate status](https://hosted.weblate.org/widgets/seal/-/strings/multi-auto.svg)](https://hosted.weblate.org/engage/seal/)
	
>[!দ্রষ্টব্য]
>
>বাগ রিপোর্ট জমা দেওয়ার জন্য, ফিচার এর অনুরোধ, প্রশ্ন, বা উন্নতির জন্য অন্য কোন পরামর্শ থাকলে অনুগ্রহ করে প্রথমে নির্দেশাবলী এবং নির্দেশিকাগুলির জন্য  [CONTRIBUTING.md](https://github.com/JunkFood02/Seal/blob/main/CONTRIBUTING.md) পড়ুন।

## ⭐️ স্টার চার্ট

[![Star History Chart](https://api.star-history.com/svg?repos=JunkFood02/Seal&type=Timeline)](https://star-history.com/#JunkFood02/Seal&Timeline)


## 🧱 ক্রেডিট

[youtubedl-android](https://github.com/yausername/youtubedl-android) -এর উপর ভিত্তি করে সিল  [yt-dlp](https://github.com/yt-dlp/yt-dlp) -এর একটি সাধারণ GUI, 

কিছু UI ডিজাইন এবং কোড [Read You](https://github.com/Ashinch/ReadYou) এবং [Music You](https://github.com/Kyant0/MusicYou) থেকে ধার করা হয়েছে।

[dvd](https://github.com/yausername/dvd)

[Material color utilities](https://github.com/material-foundation/material-color-utilities)

[Monet](https://github.com/Kyant0/Monet)

## 📃  লাইসেন্স

[![GitHub](https://img.shields.io/github/license/JunkFood02/Seal?style=for-the-badge)](https://github.com/JunkFood02/Seal/blob/main/LICENSE)

>[!সতর্কতা]
>
>GPLv3 লাইসেন্সের অধীনে লাইসেন্সকৃত সোর্স কোড ব্যতীত, অন্যান্য সমস্ত পক্ষকে ডাউনলোডার অ্যাপ 
>হিসাবে সিলের নাম ব্যবহার করা নিষিদ্ধ এবং সিলের ডেরিভেটিভের ক্ষেত্রেও এটি সত্য। ডেরিভেটিভস 
>অন্তর্ভুক্ত কিন্তু ফোর্ক এবং অফিসিয়াল বিল্ডের মধ্যে সীমাবদ্ধ নয়।

<div align="right">
<table><td>
<a href="#start-of-content">👆 উপরে স্ক্রোল করুন</a>
</td></table>
</div>