package com.commit451.gitlab.api

import android.support.annotation.VisibleForTesting
import com.commit451.gitlab.model.Account
import com.github.aurae.retrofit2.LoganSquareConverterFactory
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Pulls all the GitLabService stuff from the API
 */
object GitLabFactory {

    /**
     * Create a GitLabService get with the current account passed.
     * @param account the account to try and log in with
     * *
     * @return the GitLabService configured client
     */
    fun create(account: Account, client: OkHttpClient): GitLabService {
        return create(account, client, false)
    }

    @VisibleForTesting
    fun create(account: Account, client: OkHttpClient, dummyExecutor: Boolean): GitLabService {
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
        return retrofitBuilder.build().create(GitLabService::class.java)
    }
}

