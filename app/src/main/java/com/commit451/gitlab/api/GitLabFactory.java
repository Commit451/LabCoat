package com.commit451.gitlab.api;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.commit451.gitlab.model.Account;
import com.github.aurae.retrofit2.LoganSquareConverterFactory;

import java.util.concurrent.Executor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

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
        return create(account, client, false);
    }

    @VisibleForTesting
    public static GitLab create(Account account, OkHttpClient client, boolean dummyExecutor) {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(account.getServerUrl().toString())
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(LoganSquareConverterFactory.create());
        if (dummyExecutor) {
            retrofitBuilder.callbackExecutor(new Executor() {
                @Override
                public void execute(@NonNull Runnable command) {
                    //dumb, to prevent tests from failing
                }
            });
        }
        return retrofitBuilder.build().create(GitLab.class);
    }
}
