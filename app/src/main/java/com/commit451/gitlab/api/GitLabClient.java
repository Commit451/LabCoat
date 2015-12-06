package com.commit451.gitlab.api;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.providers.GsonProvider;
import com.commit451.gitlab.providers.OkHttpClientProvider;
import com.commit451.gitlab.providers.SimpleXmlProvider;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.SimpleXmlConverterFactory;

/**
 * Pulls all the GitLab stuff from the API
 * Created by Jawn on 7/28/2015.
 */
public final class GitLabClient {

    private static Account sAccount;

    private static GitLab sGitLab;
    private static GitLabRss sGitLabRss;
    private static Picasso sPicasso;

    private GitLabClient() {}

    public static void setAccount(Account account) {
        sAccount = account;
        sGitLab = null;
        sGitLabRss = null;
        sPicasso = null;
    }

    public static Account getAccount() {
        return sAccount;
    }

    public static GitLab instance() {
        if (sGitLab == null) {
            checkAccountSet();
            Retrofit restAdapter = new Retrofit.Builder()
                    .baseUrl(sAccount.getServerUrl().toString())
                    .client(OkHttpClientProvider.getInstance(sAccount))
                    .addConverterFactory(GsonConverterFactory.create(GsonProvider.getInstance()))
                    .build();
            sGitLab = restAdapter.create(GitLab.class);
        }

        return sGitLab;
    }

    public static GitLabRss rssInstance() {
        if (sGitLabRss == null) {
            checkAccountSet();
            Retrofit restAdapter = new Retrofit.Builder()
                    .baseUrl(sAccount.getServerUrl().toString())
                    .client(OkHttpClientProvider.getInstance(sAccount))
                    .addConverterFactory(SimpleXmlConverterFactory.create(SimpleXmlProvider.getPersister()))
                    .build();
            sGitLabRss = restAdapter.create(GitLabRss.class);
        }

        return sGitLabRss;
    }

    public static Picasso getPicasso() {
        if (sPicasso == null) {
            checkAccountSet();
            sPicasso = new Picasso.Builder(GitLabApp.instance())
                    .downloader(new OkHttpDownloader(OkHttpClientProvider.getInstance(sAccount)))
                    .build();
        }

        return sPicasso;
    }

    private static void checkAccountSet() {
        if (sAccount == null) {
            throw new IllegalStateException("You cannot do any network calls before the account is set!");
        }
    }
}
