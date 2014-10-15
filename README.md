# GitLab for Android [![Google Play](http://developer.android.com/images/brand/en_generic_rgb_wo_45.png)](https://play.google.com/store/apps/details?id=com.bd.gitlab)

This is the source code for the unofficial GitLab Android app.

Please see the [issues](https://github.com/ekx/GitLabAndroid/issues) section to
report any bugs or feature requests and to see the list of known issues.

## License

* [Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

## Building

To build the project create a file called 'signing.gradle' in the 'app' directory. It should contain your signing information as below.

```
android {
    signingConfigs { 
        release {
            storeFile file("..")
            storePassword ".."
            keyAlias ".."
            keyPassword ".."
        }
    }
}
```

Then execute 'gradlew assembleRelease' in the project's root directory. If everthing went fine, the APK file can be found in 'pathtoproject/app/build/outputs/apk'

## Acknowledgements

This project uses many open source libraries such as:

* [Retrofit](https://github.com/square/retrofit)
* [Picasso](https://github.com/square/picasso)
* [Butter Knife](https://github.com/JakeWharton/butterknife)
* [Gson](https://code.google.com/p/google-gson/)
* [joda-time-android](https://github.com/dlew/joda-time-android)
* [Crouton](https://github.com/keyboardsurfer/Crouton)
* [FloatingActionButton](https://github.com/makovkastar/FloatingActionButton)

## Contributing

Please fork this repository and contribute back using
[pull requests](https://github.com/ekx/GitLabAndroid/pulls).