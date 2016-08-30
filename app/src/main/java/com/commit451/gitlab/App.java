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
import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.util.FabricUtil;
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;

import net.danlew.android.joda.JodaTimeAndroid;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

/**
 * App for one time init things and to house singletons
 */
public class App extends Application {

    /**
     * Register our type converters on our singleton LoganSquare instance. Needs to be set here
     * since we are fetching accounts immediately with LoganSquare
     */
    static {
        LoganSquare.registerTypeConverter(Uri.class, new UriTypeConverter());
    }

    private static EventBus sBus;
    private static App sInstance;

    public static EventBus bus() {
        if (sBus == null) {
            sBus = EventBus.getDefault();
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
    private Prefs mPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        mPrefs = new Prefs(this);
        forceLocale(Locale.ENGLISH);
        setupCrashReporting();
        setupLeakCanary();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        JodaTimeAndroid.init(this);
        SimpleChromeCustomTabs.initialize(this);

        List<Account> accounts = Account.getAccounts();
        if(!accounts.isEmpty()) {
            setAccount(accounts.get(0));
        }
    }

    @VisibleForTesting
    protected void setupCrashReporting() {
        FabricUtil.init(this);
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
        OkHttpClient.Builder clientBuilder = OkHttpClientFactory.create(account);
        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        OkHttpClient client = clientBuilder.build();
        initGitLab(account, client);
        initGitLabRss(account, client);
        //This is kinda weird, but basically, I don't want to see all the annoying logs from bitmap
        //decoding since the Okhttpclient is going to log everything, but it does not matter in release
        //builds, and will actually speed up the init time to share the same client between all these
        if (BuildConfig.DEBUG) {
            initPicasso(OkHttpClientFactory.create(account).build());
        } else {
            initPicasso(client);
        }
    }

    public Prefs getPrefs() {
        return mPrefs;
    }

    private void initGitLab(Account account, OkHttpClient client) {
        mGitLab = GitLabFactory.create(account, client);
    }

    private void initGitLabRss(Account account, OkHttpClient client) {
        mGitLabRss = GitLabRssFactory.create(account, client);
    }

    private void initPicasso(OkHttpClient client) {
        mPicasso = PicassoFactory.createPicasso(client);
    }
}
