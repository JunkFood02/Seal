<div align="center">

<img width="" src="fastlane/metadata/android/en-US/images/icon.png"  width=160 height=160  align="center">

# Seal


### Загрузчик видео и аудио файлов на Android



<p align="center">
<a href="https://github.com/JunkFood02/Seal/blob/main/README.md">English</a>
&nbsp;&nbsp;| &nbsp;&nbsp;
Русский
</p>


[![F-Droid](https://img.shields.io/f-droid/v/com.junkfood.seal?color=b4eb12&label=F-Droid&logo=fdroid&logoColor=1f78d2)](https://f-droid.org/ru/packages/com.junkfood.seal)
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

## 📱 Снимки экрана

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

<br>

## 📖 Возможности
- Загрузка видео и аудио файлов с видео платформ, поддерживаемых [yt-dlp](https://github.com/yt-dlp/yt-dlp) (ранее youtube-dl).

- Добавляйте метаданные и превью в загружаемые аудио файлы с помощью [mutagen](https://github.com/quodlibet/mutagen).

- Загрузка всех видео из плейлиста в один клик

- Использует встроенный [aria2c](https://github.com/aria2/aria2) как внешний загрузчик для всех ваших загрузок

- Можно встроить субтитры в загружаемое видео

- Контроль над загрузками в приложении и кастомные шаблоны команд

- Просмотр и управление загрузками в приложении

- Простой в использовании

- Стилизованный под [Material Design 3](https://m3.material.io/), с динамической цветовой схемой

- Интерфейс и его поведение написано на Kotlin. Один активити, без фрагментов, только composable destinations.




## ⬇️ Установка

Для большинства устройств рекомендовано устанавливать версию apk **arm64-v8a**

- Скачать последнюю стабильную версию со [страницы с релизами](https://github.com/JunkFood02/Seal/releases/latest)
  - Установить [пре-релиз](https://github.com/JunkFood02/Seal/releases/) чтобы помочь протестировать нам новые функции и изменения

- Стабильные релизы также доступны на [F-Droid](https://f-droid.org/packages/com.junkfood.seal/)

<!-- [<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Скачайте его с F-Droid"
     height="70">](https://f-droid.org/packages/com.junkfood.seal/) -->

## 💬 Связаться

Присоединяйтесь к нашему [Telegram каналу](https://t.me/seal_app) или [Matrix Space](https://matrix.to/#/#seal-space:matrix.org) для рассуждений, анонсов и релизов!

## 💖 Спонсоры

<p><!-- sponsors --><a href="https://github.com/4kaimar"><img src="https://github.com/4kaimar.png" width="60px" alt="" /></a><a href="https://github.com/gordongw"><img src="https://github.com/gordongw.png" width="60px" alt="Gordon" /></a><a href="https://github.com/MayBeNotSimon"><img src="https://github.com/MayBeNotSimon.png" width="60px" alt="" /></a><!-- sponsors --></p>


Seal всегда будет бесплатным проектом с открытым исходным кодом для каждого. Если вам это нравится, пожалуйста рассмотрите возможность [поддержать меня](https://github.com/sponsors/JunkFood02)!

## 🤝 Помочь с переводом

Помощь приветствуется!

Вы можете принять участие в переводе Seal на [Hosted Weblate](https://hosted.weblate.org/projects/seal/).

[![Translate status](https://hosted.weblate.org/widgets/seal/-/multi-auto.svg)](https://hosted.weblate.org/engage/seal/)

>[!Note]
>
> Чтобы отсылать нам баги, запросы на добавление новых функций или любые другие идеи, которые помогут проекту, сперва прочитайте [CONTRIBUTING.md](https://github.com/JunkFood02/Seal/blob/main/CONTRIBUTING.md) для важной информации и инструкций.

## ⭐️ График роста кол-ва звёздочек

[![Star History Chart](https://api.star-history.com/svg?repos=JunkFood02/Seal&type=Timeline)](https://star-history.com/#JunkFood02/Seal&Timeline)

## 🧱 Особая благодарность

Seal - это простой интерфейс для [yt-dlp](https://github.com/yt-dlp/yt-dlp), созданный на базе [youtubedl-android](https://github.com/yausername/youtubedl-android)

Некоторые элементы дизайна и кода были заимствованны у [Read You](https://github.com/Ashinch/ReadYou) и [Music You](https://github.com/Kyant0/MusicYou)

[dvd](https://github.com/yausername/dvd)

[Material color utilities](https://github.com/material-foundation/material-color-utilities)

[Monet](https://github.com/Kyant0/Monet)

## 📃 Лицензия

[![GitHub](https://img.shields.io/github/license/JunkFood02/Seal?style=for-the-badge)](https://github.com/JunkFood02/Seal/blob/main/LICENSE)

>[!Warning]
>
>За исключением исходного кода, лицензированного по лицензии GPLv3,
>всем остальным сторонам запрещено использовать название Seal в качестве загрузчика приложений,
>то же самое распространяется на производные Seal.
>Деривативы разрешены, но они не ограничиваются в производных и неофициальных сборках.

<div align="right">
<table><td>
<a href="#start-of-content">👆 Пролистать наверх</a>
</td></table>
</div>
