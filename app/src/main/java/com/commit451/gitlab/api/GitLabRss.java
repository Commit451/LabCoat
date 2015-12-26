package com.commit451.gitlab.api;

import com.commit451.gitlab.model.rss.Feed;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Url;

/**
 * Gets the RSS of stuff
 * Created by John on 10/8/15.
 */
public interface GitLabRss {

    @GET
    Call<Feed> getFeed(@Url String url);
}
