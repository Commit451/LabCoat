package com.commit451.gitlab.api;

import com.commit451.gitlab.model.Account;
import com.github.aurae.retrofit2.LoganSquareConverterFactory;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Pulls all the GitLab stuff from the API
 */
public final class GitLabFactory {

    /**
     * Create a GitLab instance with the current account passed.
     * @param account the account to try and log in with
     * @return the GitLab instance
     */
    public static GitLab create(Account account, OkHttpClient client) {
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(account.getServerUrl().toString())
                .client(client)
                .addConverterFactory(LoganSquareConverterFactory.create())
                .build();
        return restAdapter.create(GitLab.class);
    }
}
