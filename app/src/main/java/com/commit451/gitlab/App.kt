package com.commit451.gitlab

import android.app.Application
import com.commit451.firebaseshim.FirebaseShim
import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.api.GitLabFactory
import com.commit451.gitlab.api.OkHttpClientFactory
import com.commit451.gitlab.api.PicassoFactory
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.model.Account
import com.commit451.lift.Lift
import com.jakewharton.threetenabp.AndroidThreeTen
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.squareup.leakcanary.LeakCanary
import com.squareup.picasso.Picasso
import io.reactivex.plugins.RxJavaPlugins
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

/**
 * App for one time init things and to house singletons
 */
class App : Application() {

    companion object {

        var bus: EventBus = EventBus.getDefault()
        private lateinit var instance: App

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
        RxJavaPlugins.setErrorHandler { error ->
            //In case an error cannot be thrown properly anywhere else in the app
            Timber.e(error)
        }

        setupThreeTen()

        Prefs.init(this)
        FirebaseShim.init(this, BuildConfig.DEBUG)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        SimpleChromeCustomTabs.initialize(this)

        val accounts = Prefs.getAccounts()
        if (!accounts.isEmpty()) {
            setAccount(accounts[0])
        }

        Lift.track(this)
    }

    fun setAccount(account: Account) {
        currentAccount = account
        // This is kinda weird, but basically, I don't want to see all the annoying logs from bitmap
        // decoding since the OkHttpClient is going to log everything, but it does not matter in release
        // builds, and will actually speed up the init time to share the same client between all these
        val clientBuilder = OkHttpClientFactory.create(account)
        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        val client = clientBuilder.build()
        initGitLab(account, clientBuilder)
        if (BuildConfig.DEBUG) {
            initPicasso(OkHttpClientFactory.create(account).build())
        } else {
            initPicasso(client)
        }
    }

    fun getAccount(): Account {
        return currentAccount
    }

    private fun setupLeakCanary() {
        LeakCanary.install(this)
    }

    private fun setupThreeTen() {
        AndroidThreeTen.init(this)
    }

    private fun initGitLab(account: Account, clientBuilder: OkHttpClient.Builder) {
        gitLab = GitLabFactory.createGitLab(account, clientBuilder)
    }

    private fun initPicasso(client: OkHttpClient) {
        picasso = PicassoFactory.createPicasso(client)
    }
}
