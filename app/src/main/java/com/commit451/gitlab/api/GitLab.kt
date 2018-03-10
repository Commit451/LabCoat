package com.commit451.gitlab.api

import com.commit451.gitlab.model.Account
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient

/**
 * Provides access to all the GitLab things. Wraps RSS and the Retrofit service, in
 * case we need to do overrides or global
 */
class GitLab private constructor(builder: Builder, gitLabService: GitLabService, gitLabRss: GitLabRss) : GitLabService by gitLabService,
        GitLabRss by gitLabRss {

    val client: OkHttpClient
    val moshi: Moshi
    val account: Account

    init {
        client = builder.clientBuilder?.build() ?: OkHttpClient()
        account = builder.account
        moshi = MoshiProvider.moshi
    }

    class Builder(internal val account: Account) {

        internal var clientBuilder: OkHttpClient.Builder? = null

        fun clientBuilder(clientBuilder: OkHttpClient.Builder): Builder {
            this.clientBuilder = clientBuilder
            return this
        }

        fun build(): GitLab {
            val client = clientBuilder?.build() ?: OkHttpClient()
            val gitLabService = GitLabFactory.create(account, client)
            val gitLabRss = GitLabRssFactory.create(account, client)
            return GitLab(this, gitLabService, gitLabRss)
        }
    }
}