package com.commit451.gitlab;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.otto.Bus;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * App for one time init things and to house singletons
 */
public class GitLabApp extends Application {

    private static Bus sBus;
    public static Bus bus() {
        if (sBus == null) {
            sBus = new Bus();
        }
        return sBus;
    }

    private static GitLabApp sInstance;
    public static GitLabApp instance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        forceLocale(Locale.ENGLISH);
        setupCrashReporting();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        JodaTimeAndroid.init(this);
        LeakCanary.install(this);
    }

    protected void setupCrashReporting() {
        CrashlyticsCore core = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());
    }

    private void forceLocale(Locale locale){
        try {
            Locale.setDefault(locale);

            Resources[] resources = new Resources[]{
                    Resources.getSystem(),
                    getBaseContext().getResources()
            };
            for (Resources res : resources) {
                Configuration configuration = res.getConfiguration();
                configuration.locale = locale;
                res.updateConfiguration(configuration, res.getDisplayMetrics());
            }
        } catch (Exception e) {
            Timber.e(e, null);
        }
    }
}
