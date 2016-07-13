package com.commit451.gitlab;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;

import com.bluelinelabs.logansquare.LoganSquare;
import com.commit451.gitlab.api.GitLab;
import com.commit451.gitlab.api.GitLabFactory;
import com.commit451.gitlab.api.GitLabRss;
import com.commit451.gitlab.api.GitLabRssFactory;
import com.commit451.gitlab.api.OkHttpClientFactory;
import com.commit451.gitlab.api.PicassoFactory;
import com.commit451.gitlab.api.converter.UriTypeConverter;
import com.commit451.gitlab.model.Account;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

/**
 * App for one time init things and to house singletons
 */
public class App extends Application {

    /**
     * Register our type converters on our singleton LoganSquare create
     */
    static {
        LoganSquare.registerTypeConverter(Uri.class, new UriTypeConverter());
    }

    private static Bus sBus;
    private static App sInstance;

    public static Bus bus() {
        if (sBus == null) {
            sBus = new Bus();
        }
        return sBus;
    }

    public static App instance() {
        return sInstance;
    }

    private Account mAccount;
    private GitLab mGitLab;
    private GitLabRss mGitLabRss;
    private Picasso mPicasso;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        forceLocale(Locale.ENGLISH);
        setupCrashReporting();
        setupLeakCanary();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        JodaTimeAndroid.init(this);
        SimpleChromeCustomTabs.initialize(this);
    }

    @VisibleForTesting
    protected void setupCrashReporting() {
        CrashlyticsCore core = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());
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

    public GitLab getGitLab() {
        return mGitLab;
    }

    public GitLabRss getGitLabRss() {
        return mGitLabRss;
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

    public Account getAccount() {
        return mAccount;
    }

    public void setAccount(Account account) {
        mAccount = account;
        initGitLab(account);
        initGitLabRss(account);
        initPicasso(account);
    }

    private void initGitLab(Account account) {
        OkHttpClient.Builder gitlabClientBuilder = OkHttpClientFactory.create(account);
        if (BuildConfig.DEBUG) {
            gitlabClientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        mGitLab = GitLabFactory.create(account, gitlabClientBuilder.build());
    }

    private void initGitLabRss(Account account) {
        OkHttpClient.Builder gitlabRssClientBuilder = OkHttpClientFactory.create(account);
        if (BuildConfig.DEBUG) {
            gitlabRssClientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        mGitLabRss = GitLabRssFactory.create(account, gitlabRssClientBuilder.build());
    }

    private void initPicasso(Account account) {
        OkHttpClient.Builder clientBuilder = OkHttpClientFactory.create(account);
        mPicasso = PicassoFactory.createPicasso(clientBuilder.build());
    }
}
