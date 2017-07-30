package com.commit451.gitlab.api

import com.commit451.gitlab.model.Account
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Pulls all the GitLabService stuff from the API
 */
object GitLabFactory {

    fun createGitLab(account: Account, clientBuilder: OkHttpClient.Builder): GitLab {
        return GitLab.Builder(account)
                .clientBuilder(clientBuilder)
                .build()
    }

    /**
     * Create a GitLabService get with the current account passed.
     * @param account the account to try and log in with
     * *
     * @return the GitLabService configured client
     */
    fun create(account: Account, client: OkHttpClient): GitLabService {
        val retrofitBuilder = Retrofit.Builder()
                .baseUrl(account.serverUrl.toString())
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(MoshiProvider.moshi))

        return retrofitBuilder.build().create(GitLabService::class.java)
    }
}

