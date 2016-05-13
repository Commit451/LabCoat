package com.commit451.gitlab;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.VisibleForTesting;

import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.otto.Bus;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Locale;

import timber.log.Timber;

/**
 * App for one time init things and to house singletons
 */
public class LabCoatApp extends Application {

    private static Bus sBus;
    public static Bus bus() {
        if (sBus == null) {
            sBus = new Bus();
        }
        return sBus;
    }

    private static LabCoatApp sInstance;
    public static LabCoatApp instance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        forceLocale(Locale.ENGLISH);
        setupLeakCanary();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        JodaTimeAndroid.init(this);
        SimpleChromeCustomTabs.initialize(this);
    }

    @VisibleForTesting
    protected void setupLeakCanary() {
        LeakCanary.install(this);
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
