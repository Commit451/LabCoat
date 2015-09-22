package com.commit451.gitlab;

import android.app.Application;

import com.commit451.gitlab.tools.Repository;
import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Bus;

import net.danlew.android.joda.JodaTimeAndroid;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * App for one time init things
 * Created by Jawn on 7/27/2015.
 */
public class GitLabApp extends Application {

    private static Bus bus;
    public static Bus bus() {
        if (bus == null) {
            bus = new Bus();
        }
        return bus;
    }

    private static GitLabApp instance;
    public static GitLabApp instance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Fabric.with(this, new Crashlytics());
        }
        Repository.init();
        JodaTimeAndroid.init(this);
    }
}
