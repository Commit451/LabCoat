package com.commit451.gitlab.api

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.rx2.rx
import com.apollographql.apollo.rx2.rxQuery
import com.commit451.gitlab.CurrentUserQuery
import com.commit451.gitlab.api.graphql.ResponseException
import com.commit451.gitlab.api.graphql.rxQueryMapErrors
import com.commit451.gitlab.model.Account
import com.squareup.moshi.Moshi
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient

/**
 * Provides access to all the GitLab things. Wraps RSS and the Retrofit service, in
 * case we need to do overrides or global
 */
class GitLab private constructor(
        builder: Builder,
        gitLabService: GitLabService,
        gitLabRss: GitLabRss
) : GitLabService by gitLabService, GitLabRss by gitLabRss {

    private val moshi: Moshi
    private val apolloClient: ApolloClient
    val client: OkHttpClient
    val account: Account

    init {
        client = builder.clientBuilder?.build() ?: OkHttpClient()
        account = builder.account
        moshi = MoshiProvider.moshi
        val apolloUrl = account.serverUrl?.toHttpUrlOrNull()?.newBuilder()
                ?.addPathSegment("api")
                ?.addPathSegment("graphql")
                ?.build()
                ?.toString()!!
        apolloClient = ApolloClient.builder()
                .serverUrl(apolloUrl)
                .okHttpClient(client)
                .build()
    }

    fun currentUser(): Single<CurrentUserQuery.CurrentUser> {
        val query = CurrentUserQuery.builder()
                .build()
        return apolloClient.rxQueryMapErrors(query)
                .singleOrError()
                .map { it.currentUser() }
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
