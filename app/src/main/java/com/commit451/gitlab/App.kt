package com.commit451.gitlab

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.support.annotation.VisibleForTesting
import android.support.multidex.MultiDex
import com.commit451.gitlab.api.*
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.util.FabricUtil
import com.commit451.lift.Lift
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.squareup.leakcanary.LeakCanary
import com.squareup.picasso.Picasso
import net.danlew.android.joda.JodaTimeAndroid
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.util.*

/**
 * App for one time init things and to house singletons
 */
open class App : Application() {

    companion object {

        var bus: EventBus = EventBus.getDefault()
        lateinit private var instance: App

        fun bus(): EventBus {
            return bus
        }

        fun get(): App {
            return instance
        }
    }

    lateinit var gitLab: GitLab
    lateinit var currentAccount: Account
    lateinit var picasso: Picasso

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        setupLeakCanary()
        instance = this

        GitLab.init()

        Prefs.init(this)
        //So that we don't get weird half translations
        forceLocale(Locale.ENGLISH)
        setupCrashReporting()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        JodaTimeAndroid.init(this)
        SimpleChromeCustomTabs.initialize(this)

        val accounts = Account.getAccounts()
        if (!accounts.isEmpty()) {
            setAccount(accounts[0])
        }

        Lift.track(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        setupMultidex()
    }

    fun setAccount(account: Account) {
        currentAccount = account
        //This is kinda weird, but basically, I don't want to see all the annoying logs from bitmap
        //decoding since the OkHttpClient is going to log everything, but it does not matter in release
        //builds, and will actually speed up the init time to share the same client between all these
        val clientBuilder = OkHttpClientFactory.create(account)
        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        val client = clientBuilder.build()
        initGitLab(account, client)
        if (BuildConfig.DEBUG) {
            initPicasso(OkHttpClientFactory.create(account).build())
        } else {
            initPicasso(client)
        }
    }

    fun getAccount(): Account {
        return currentAccount
    }

    @VisibleForTesting
    protected open fun setupMultidex() {
        MultiDex.install(this)
    }

    @VisibleForTesting
    protected open fun setupCrashReporting() {
        FabricUtil.init(this)
    }

    @VisibleForTesting
    protected open fun setupLeakCanary() {
        LeakCanary.install(this)
    }

    private fun forceLocale(locale: Locale) {
        try {
            Locale.setDefault(locale)

            val resources = arrayOf(Resources.getSystem(), baseContext.resources)
            for (res in resources) {
                val configuration = res.configuration
                configuration.locale = locale
                res.updateConfiguration(configuration, res.displayMetrics)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

    }

    private fun initGitLab(account: Account, client: OkHttpClient) {
        val gitLabService = GitLabFactory.create(account, client)
        val gitLabRss = GitLabRssFactory.create(account, client)
        gitLab = GitLab(gitLabService, gitLabRss)
    }

    private fun initPicasso(client: OkHttpClient) {
        picasso = PicassoFactory.createPicasso(client)
    }
}
