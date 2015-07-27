package com.bd.gitlab;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * App for one time init things
 * Created by Jawn on 7/27/2015.
 */
public class GitLabApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}
