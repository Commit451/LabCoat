# LabCoat for GitLab
[![Google Play](https://gitlab.com/Commit451/LabCoat/raw/master/art/google-play-badge.png)](https://play.google.com/store/apps/details?id=com.commit451.gitlab)

[![build status](https://gitlab.com/ci/projects/7701/status.png?ref=master)](https://gitlab.com/ci/projects/7701?ref=master)

This is the source code for the unofficial GitLab Android app.

![Image](https://gitlab.com/Commit451/LabCoat/raw/master/art/screenshot-1.png)

Please see the [issues](https://gitlab.com/Commit451/LabCoat/issues) section to
report any bugs or feature requests and to see the list of known issues.

## Building
You should be able to build the project from Android Studio without any further setup. The app uses Fabric for Crashlytics, so if you wanted to do a release build, you would need to generate your own Crashlytics/Fabric key. All in all, your gradle.properties will look something like this:
```Gradle
LABCOAT_FABRIC_KEY = FABRIC_KEY_GOES_HERE_BUT_ONLY_REALLY_NEEDED_FOR_RELEASE_BUILDS
```
## Libraries
The following 3rd party libraries and resources are the reason this app works. Rapid development is easily attainable thanks to these fine folks and the work they do:

- AppCompat (https://developer.android.com/tools/support-library/features.html)
- Design (https://developer.android.com/tools/support-library/features.html)
- RecyclerView (https://developer.android.com/tools/support-library/features.html)
- CardView (https://developer.android.com/tools/support-library/features.html)
- Palette (https://developer.android.com/tools/support-library/features.html)
- Picasso (http://square.github.io/picasso/)
- Retrofit (http://square.github.io/retrofit/)
- OkHttp (http://square.github.io/okhttp/)
- Otto (http://square.github.io/otto/)
- Butter Knife (http://jakewharton.github.io/butterknife/)
- Timber (https://github.com/JakeWharton/timber)
- GSON (https://github.com/google/gson)
- Joda Time Android (https://github.com/dlew/joda-time-android)
- Parceler (https://github.com/johncarl81/parceler)
- Bypasses (https://github.com/Commit451/bypasses)
- Easel (https://github.com/Commit451/Easel)
- Material-ish Progress (https://github.com/pnikosis/materialish-progress)
- PhysicsLayout (https://github.com/Jawnnypoo/PhysicsLayout)
- Material Letter Icon (https://github.com/IvBaranov/MaterialLetterIcon)
- RobotoTextView (https://github.com/johnkil/Android-RobotoTextView)
- GitDiffTextView (https://github.com/alorma/GitDiffTextView)
- MaterialDateTimePicker (https://github.com/wdullaer/MaterialDateTimePicker)
- Crashlytics (https://www.crashlytics.com)
- highlight.js (https://highlightjs.org/)

## Contributing
Please fork this repository and contribute back! All Merge Requests should be made against the `develop` branch, as it is the active branch for development. Please make your best effort to break up commits as much as possible to improve the reviewing process.

License
--------

    Copyright 2016 Commit 451

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.