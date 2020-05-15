package com.commit451.gitlab.api

import com.apollographql.apollo.ApolloClient
import com.commit451.gitlab.CurrentUserQuery
import com.commit451.gitlab.api.graphql.rxQueryMapErrors
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.model.rss.Entry
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.Single
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Response

/**
 * Provides access to all the GitLab things. Wraps RSS and the Retrofit service, in
 * case we need to do overrides or global
 */
class GitLab private constructor(
        builder: Builder,
        gitLabService: GitLabService,
        gitLabRss: GitLabRss
) : GitLabService by gitLabService, GitLabRss by gitLabRss {

    val moshi: Moshi
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

    inline fun <reified T> loadAny(url: String): Single<retrofit2.Response<T>> {
        return get(url)
                .map {
                    val source = it.body()?.source() ?: throw NullBodyException()
                    val body = moshi.adapter(T::class.java).fromJson(source)
                    retrofit2.Response.success(body, it.raw())
                }
    }

    inline fun <reified T> loadAnyList(url: String): Single<retrofit2.Response<List<T>>> {
        return get(url)
                .map {
                    val source = it.body()?.source() ?: throw NullBodyException()
                    val type = Types.newParameterizedType(List::class.java, T::class.java)
                    val body = moshi.adapter<List<T>>(type).fromJson(source)
                    retrofit2.Response.success(body, it.raw())
                }
    }

    fun currentUser(): Single<CurrentUserQuery.CurrentUser> {
        val query = CurrentUserQuery.builder()
                .build()
        return apolloClient.rxQueryMapErrors(query)
                .singleOrError()
                .map { it.currentUser() }
    }

    fun feed(url: String): Single<Response<List<Entry>>> {
        return getFeed(url).map { Response.success(it.body()?.entries, it.raw()) }
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
