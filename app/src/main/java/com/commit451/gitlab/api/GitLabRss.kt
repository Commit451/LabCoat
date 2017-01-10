package com.commit451.gitlab.api

import com.commit451.gitlab.model.rss.Feed

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Url

interface GitLabRss {

    @GET
    fun getFeed(@Url url: String): Single<Feed>
}
