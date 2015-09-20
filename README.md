# GitLab for Android
[![Google Play](http://developer.android.com/images/brand/en_generic_rgb_wo_45.png)](https://play.google.com/store/apps/details?id=com.commit451.gitlab)

[![Build Status](https://travis-ci.org/Commit451/GitLabAndroid.svg?branch=master)](https://travis-ci.org/Commit451/GitLabAndroid)

This is the source code for the unofficial GitLab Android app.

![Image](https://raw.githubusercontent.com/Commit451/GitLabAndroid/master/ic_launcher-web.png)

Please see the [issues](https://github.com/Commit451/GitLabAndroid/issues) section to
report any bugs or feature requests and to see the list of known issues.

## Building
The app uses Fabric for Crashlytics, so you will need to generate your own Crashlytics/Fabric key. All in all, your gradle.properties will look something like this:
```Gradle
GITLAB_FABRIC_KEY = FABRIC_KEY_GOES_HERE_BUT_ONLY_REALLY_NEEDED_FOR_RELEASE_BUILDS
```
## Libraries
The following 3rd party libraries are the reason this app works. Rapid development is easily attainable thanks to these fine folks and the work they do:

- AppCompat (https://developer.android.com/tools/support-library/features.html)
- Design (https://developer.android.com/tools/support-library/features.html)
- RecyclerView (https://developer.android.com/tools/support-library/features.html)
- Retrofit (http://square.github.io/retrofit/)
- OkHttp (http://square.github.io/okhttp/)
- Picasso (http://square.github.io/picasso/)
- Butter Knife (http://jakewharton.github.io/butterknife/)
- Timber (https://github.com/JakeWharton/timber)
- GSON (https://github.com/google/gson)
- Parceler (https://github.com/johncarl81/parceler)
- Bypass (https://github.com/Uncodin/bypass)
- Material-ish Progress (https://github.com/pnikosis/materialish-progress)
- PhysicsLayout (https://github.com/Jawnnypoo/PhysicsLayout)
- CircleImageView (https://github.com/hdodenhof/CircleImageView)

## Contributing
Please fork this repository and contribute back! All Merge Requests should be made agains the `develop` branch, as it is the active branch for develop. Please make your best effort to break up commits as much as possible to improve the reviewing process.

License
--------

    Copyright 2015 Commit 451

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.