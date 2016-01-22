package com.commit451.gitlab.api;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.provider.GsonProvider;
import com.commit451.gitlab.provider.OkHttpClientProvider;
import com.commit451.gitlab.provider.SimpleXmlProvider;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.List;

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

    /**
     * Get a GitLab instance with the current account passed. Used for login only
     * @param account the account to try and log in with
     * @return the GitLab instance
     */
    public static GitLab instance(Account account) {
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(account.getServerUrl().toString())
                .client(OkHttpClientProvider.getInstance(account))
                .addConverterFactory(GsonConverterFactory.create(GsonProvider.createInstance(account)))
                .build();
        return restAdapter.create(GitLab.class);
    }

    public static GitLab instance() {
        if (sGitLab == null) {
            checkAccountSet();
            sGitLab = instance(sAccount);
        }

        return sGitLab;
    }

    public static GitLabRss rssInstance(Account account) {
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(account.getServerUrl().toString())
                .client(OkHttpClientProvider.getInstance(account))
                .addConverterFactory(SimpleXmlConverterFactory.create(SimpleXmlProvider.createPersister(account)))
                .build();
        return restAdapter.create(GitLabRss.class);
    }

    public static GitLabRss rssInstance() {
        if (sGitLabRss == null) {
            checkAccountSet();
            sGitLabRss = rssInstance(sAccount);
        }

        return sGitLabRss;
    }

    public static Picasso getPicasso(Account account) {
        return new Picasso.Builder(LabCoatApp.instance())
                .downloader(new OkHttpDownloader(OkHttpClientProvider.getInstance(account)))
                .build();
    }

    public static Picasso getPicasso() {
        if (sPicasso == null) {
            checkAccountSet();
            sPicasso = getPicasso(sAccount);
        }

        return sPicasso;
    }

    private static void checkAccountSet() {
        if (sAccount == null) {
            List<Account> accounts = Account.getAccounts(LabCoatApp.instance());
            if (accounts.isEmpty()) {
                throw new IllegalStateException("No accounts found");
            }

            GitLabClient.setAccount(accounts.get(0));
        }
    }
}
