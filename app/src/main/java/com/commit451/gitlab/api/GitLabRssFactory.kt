package com.commit451.gitlab.api

import com.commit451.gitlab.api.rss.SimpleXmlPersisterFactory
import com.commit451.gitlab.model.Account
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

/**
 * Creates RSS get for GitLabService
 */
object GitLabRssFactory {

    fun create(account: Account, client: OkHttpClient): GitLabRss {
        val persister = SimpleXmlPersisterFactory.createPersister()
        val restAdapter = Retrofit.Builder()
                .baseUrl(account.serverUrl.toString())
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create(persister))
                .build()
        return restAdapter.create(GitLabRss::class.java)
    }
}
