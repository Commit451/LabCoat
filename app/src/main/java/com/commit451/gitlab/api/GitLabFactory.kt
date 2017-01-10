package com.commit451.gitlab.api

import android.support.annotation.VisibleForTesting
import com.commit451.gitlab.model.Account
import com.github.aurae.retrofit2.LoganSquareConverterFactory
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Pulls all the GitLab stuff from the API
 */
object GitLabFactory {

    /**
     * Create a GitLab get with the current account passed.
     * @param account the account to try and log in with
     * *
     * @return the GitLab get
     */
    fun create(account: Account, client: OkHttpClient): GitLab {
        return create(account, client, false)
    }

    @VisibleForTesting
    fun create(account: Account, client: OkHttpClient, dummyExecutor: Boolean): GitLab {
        val retrofitBuilder = Retrofit.Builder()
                .baseUrl(account.serverUrl.toString())
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(LoganSquareConverterFactory.create())
        if (dummyExecutor) {
            retrofitBuilder.callbackExecutor {
                //dumb, to prevent tests from failing }
            }
        }
        return retrofitBuilder.build().create(GitLab::class.java)
    }
}

