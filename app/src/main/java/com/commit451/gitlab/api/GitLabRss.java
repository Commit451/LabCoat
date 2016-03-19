package com.commit451.gitlab.api;

import com.commit451.gitlab.model.rss.Feed;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface GitLabRss {

    @GET
    Call<Feed> getFeed(@Url String url);
}
