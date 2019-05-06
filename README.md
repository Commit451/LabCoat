# LabCoat for GitLab

[![build status](https://gitlab.com/Commit451/LabCoat/badges/master/build.svg)](https://gitlab.com/Commit451/LabCoat/commits/master) [![Discord](https://img.shields.io/discord/304078613114781696.svg)](https://discord.gg/SDxsaKE)

![Image](art/screenshot-1.png)

[![Google Play](https://gitlab.com/Commit451/LabCoat/raw/master/art/google-play-badge.png)](https://play.google.com/store/apps/details?id=com.commit451.gitlab)

## Issues
Please see the [issues](https://gitlab.com/Commit451/LabCoat/issues) section to report any bugs or feature requests and to see the list of known issues.

## Building
You should be able to build the project from Android Studio without any further setup. The app Firebase Fabric for crash reporting, so if you wanted to do a release build, you would need to generate your own Firebase project and place the `google-services.json` file in the `app` directory. Additionally, you would want to make a new keystore, placing the name of the keystore and password in the `app/gradle.properties` file:
```
KEYSTORE_NAME=keystore.jks
KEYSTORE_PASSWORD=keystore_password_here
KEY_PASSWORD=key_password_here
```
To build, run the following:
```bash
./gradlew assembleDebug
```
or, for release builds:
```bash
./gradlew assembleRelease
```

## Contributing
Please fork this repository and contribute back! Please make your best effort to break up commits as much as possible to improve the reviewing process.

If you are making substantial changes, please refer to Commit 451's style [guidelines](https://github.com/Commit451/guidelines) for Android/Kotlin. Add major changes/features to the changelog file as well.

License
--------

    Copyright 2019 Commit 451

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
