package com.commit451.gitlab.api

import com.commit451.gitlab.model.Account
import okhttp3.OkHttpClient

/**
 * Provides access to all the GitLab things. Wraps RSS and the Retrofit service, in
 * case we need to do overrides or global
 */
class GitLab private constructor(val account: Account, val client: OkHttpClient, gitLabService: GitLabService, gitLabRss: GitLabRss): GitLabService by gitLabService,
        GitLabRss by gitLabRss {

    class Builder(private val account: Account) {

        private var clientBuilder: OkHttpClient.Builder? = null

        fun clientBuilder(clientBuilder: OkHttpClient.Builder): Builder {
            this.clientBuilder = clientBuilder
            return this
        }

        fun build(): GitLab {
            var clientBuilder = clientBuilder
            if (clientBuilder == null) {
                clientBuilder = OkHttpClient.Builder()
            }
            val client = clientBuilder.build()
            val gitLabService = GitLabFactory.create(account, client)

            val gitLab = GitLab(account, client, gitLabService, GitLabRssFactory.create(account, client))
            return gitLab
        }
    }
}