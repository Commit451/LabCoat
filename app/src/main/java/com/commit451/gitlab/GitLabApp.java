package com.commit451.gitlab;

import android.app.Application;

import com.commit451.gitlab.tools.Repository;
import com.squareup.otto.Bus;

import net.danlew.android.joda.JodaTimeAndroid;

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

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        Repository.init(this);
        JodaTimeAndroid.init(this);
    }
}
