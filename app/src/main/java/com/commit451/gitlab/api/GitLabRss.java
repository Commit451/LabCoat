package com.commit451.gitlab.api;

import com.commit451.gitlab.model.rss.Feed;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface GitLabRss {

    @GET
    Single<Feed> getFeed(@Url String url);
}
