package com.commit451.gitlab.api;

import com.commit451.gitlab.model.rss.UserFeed;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Url;

/**
 * Gets the RSS of stuff
 * Created by John on 10/8/15.
 */
public interface GitLabRss {

    @GET
    Call<UserFeed> getUserFeed(@Url String url);
}
