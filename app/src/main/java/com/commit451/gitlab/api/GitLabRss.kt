package com.commit451.gitlab.api

import com.commit451.gitlab.model.rss.Feed
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface GitLabRss {

    @GET
    fun getFeed(@Url url: String): Single<Response<Feed>>
}
