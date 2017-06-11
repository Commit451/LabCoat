# LabCoat for GitLab

[![build status](https://gitlab.com/Commit451/LabCoat/badges/master/build.svg)](https://gitlab.com/Commit451/LabCoat/commits/master) [![Discord](https://img.shields.io/discord/304078613114781696.svg)](https://discord.gg/SDxsaKE)

![Image](https://gitlab.com/Commit451/LabCoat/raw/master/art/screenshot-1.png)

[![Google Play](https://gitlab.com/Commit451/LabCoat/raw/master/art/google-play-badge.png)](https://play.google.com/store/apps/details?id=com.commit451.gitlab)

## Issues
Please see the [issues](https://gitlab.com/Commit451/LabCoat/issues) section to report any bugs or feature requests and to see the list of known issues.

## Building
You should be able to build the project from Android Studio without any further setup. The app uses Fabric for crash reporting, so if you wanted to do a release build, you would need to generate your own Crashlytics/Fabric key. All in all, your `gradle.properties` will look something like this:
```Gradle
LABCOAT_FABRIC_KEY = FABRIC_KEY_GOES_HERE_BUT_ONLY_REALLY_NEEDED_FOR_RELEASE_BUILDS
```

To build, run the following.

```bash
./gradlew assembleDebug
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
- RxJava (https://github.com/ReactiveX/RxJava)
- RxAndroid (https://github.com/ReactiveX/RxAndroid)
- Butter Knife (http://jakewharton.github.io/butterknife/)
- Timber (https://github.com/JakeWharton/timber)
- LoganSquare (https://github.com/bluelinelabs/LoganSquare)
- retrofit-logansquare (https://github.com/aurae/retrofit-logansquare)
- Joda Time Android (https://github.com/dlew/joda-time-android)
- Parceler (https://github.com/johncarl81/parceler)
- Bypasses (https://github.com/Commit451/bypasses)
- Easel (https://github.com/Commit451/Easel)
- ElasticDragDismissLayout (https://github.com/Commit451/ElasticDragDismissLayout)
- AdapterLayout (https://github.com/Commit451/AdapterLayout)
- Gimbal (https://github.com/Commit451/Gimbal)
- Teleprinter (https://github.com/Commit451/Teleprinter)
- BypassPicassoImageGetter (https://github.com/Commit451/BypassPicassoImageGetter)
- Jounce (https://github.com/Commit451/Jounce)
- EasyCallback (https://github.com/Commit451/EasyCallback)
- ForegroundViews (https://github.com/Commit451/ForegroundViews)
- Material-ish Progress (https://github.com/pnikosis/materialish-progress)
- PhysicsLayout (https://github.com/Jawnnypoo/PhysicsLayout)
- Material Letter Icon (https://github.com/IvBaranov/MaterialLetterIcon)
- RecyclerViewSquire (https://github.com/AlexKGwyn/RecyclerViewSquire)
- RobotoTextView (https://github.com/johnkil/Android-RobotoTextView)
- GitDiffTextView (https://github.com/alorma/GitDiffTextView)
- MaterialDateTimePicker (https://github.com/wdullaer/MaterialDateTimePicker)
- FlowLayout (https://github.com/blazsolar/FlowLayout)
- SimpleChromeCustomTabs (https://github.com/novoda/simple-chrome-custom-tabs)
- Material Dialogs (https://github.com/afollestad/material-dialogs)
- CircleImageView (https://github.com/hdodenhof/CircleImageView)
- EasyImage (https://github.com/jkwiecien/EasyImage)
- emoji-java (https://github.com/vdurmont/emoji-java)
- Crashlytics (https://www.crashlytics.com)
- highlight.js (https://highlightjs.org/)

## Contributing
Please fork this repository and contribute back! All Merge Requests should be made against the `develop` branch, as it is the active branch for development. Please make your best effort to break up commits as much as possible to improve the reviewing process.

If you are making substantial changes, please refer to Commit 451's style [guidelines](https://github.com/Commit451/guidelines) for Android

License
--------

    Copyright 2017 Commit 451

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.