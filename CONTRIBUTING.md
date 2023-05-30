# Contributing

Before reading, you may know what [yt-dlp](https://github.com/yt-dlp/yt-dlp) is and what it does. In short, it's a CLI (Command Line Interface) program written in python, which lets you download videos from [1000+ websites](https://github.com/yt-dlp/yt-dlp/blob/master/supportedsites.md).

For bug reports and feature requests, please search in issues first (including the closed ones). If there're no duplicates, feel free to [submit an issue](https://github.com/JunkFood02/Seal/issues/new) with an issue template.

**We'll probably ignore and close your issue if it's not using the existing templates or doesn't contain sufficient description.**

For questions or any other ideas to improve, you can join our official [Telegram group](https://t.me/seal_app_group) or [Matrix space](https://matrix.to/#/#seal-space:matrix.org).



## Bug Report

When submitting a bug report, please make sure your issue contains **enough** information for reproducing the problem, including the options or the custom command being used, the link to the video, and other fields in the issue template.



## Feature Request

Seal is and will remain a simple GUI for yt-dlp, providing most of the functionality of yt-dlp as is, without modifications. Thus, **we'll not take requests for features that yt-dlp does not support.**

The app has two download modes: 

- General mode: Save as audio, download playlist, and many other options that can be used individually or combined for normal download tasks. Once the download is complete, Seal will scan the files into the system media library, and store them in the download history.
- Custom command mode: For advanced usage of yt-dlp, a user can create and store multiple command templates in the app, then select and use one of them directly to execute the yt-dlp command like in a terminal. In this mode, all of the GUI options and features in the general mode will be disabled.

Since most of the functions can be implemented in custom command mode, the "feature request" would be treated as adding a shortcut to the general mode. However, not all feature requests will be accepted and implemented in the app. [Why not add an option for that?](https://neugierig.org/software/blog/2018/07/options.html)



## Pull Request

If you wish to contribute to the project by submitting code directly, please first leave a comment under the relevant issue or file a new issue, describe the changes you are about to make.

To avoid multiple pull requests resolving the same issue, let others know you are working on it by saying so in a comment, or ask the issue to be assigned to yourself.



## New contributors

Scan through our [existing issues](https://github.com/JunkFood02/Seal/issues) to find one that interests you. The [👋 good first issue](https://github.com/JunkFood02/Seal/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) is a good place to start exploring issues that are up-for-grab for newcomers. (Do not hesitate to ask for more details or clarifying questions on the issue!)



## Building From Source

Fork this project, import and compile it with the latest version of [Android Studio Canary](https://developer.android.com/studio/preview). 
