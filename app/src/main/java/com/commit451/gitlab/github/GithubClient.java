package com.commit451.gitlab.github;


import java.util.List;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * GitHub is cool too
 * Created by Jawn on 6/30/2015.
 */
public class GithubClient {

    public static final String API_URL = "https://api.github.com";

    public interface GitHub {
        @GET("/repos/{owner}/{repo}/contributors")
        Call<List<Contributor>> contributors(
                @Path("owner") String owner,
                @Path("repo") String repo);
    }

    private static GitHub mGithub;
    public static GitHub instance() {
        if (mGithub == null) {
            Retrofit restAdapter = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            mGithub = restAdapter.create(GitHub.class);
        }
        return mGithub;
    }
}
