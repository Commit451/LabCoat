package com.commit451.gitlab.api;

import com.commit451.gitlab.BuildConfig;
import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.ssl.CustomTrustManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;
import java.util.Date;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.SimpleXmlConverterFactory;

/**
 * Pulls all the GitLab stuff from the API
 * Created by Jawn on 7/28/2015.
 */
public class GitLabClient {

    private static GitLab sGitLab;
    private static GitLabRss sGitLabRss;
    private static CustomTrustManager sCustomTrustManager = new CustomTrustManager();

    public static GitLab instance() {
        if (sGitLab == null) {
            // Configure Gson to handle dates correctly
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                @Override
                public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                    return ISODateTimeFormat.dateTimeParser().parseDateTime(json.getAsString()).toDate();
                }
            });
            Gson gson = gsonBuilder.create();

            OkHttpClient client = new OkHttpClient();
            client.setSslSocketFactory(sCustomTrustManager.getSSLSocketFactory());
            client.interceptors().add(new ApiKeyRequestInterceptor());
            client.interceptors().add(new TimberRequestInterceptor());

            Retrofit restAdapter = new Retrofit.Builder()
                    .baseUrl(Prefs.getServerUrl(GitLabApp.instance()))
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            sGitLab = restAdapter.create(GitLab.class);
        }

        return sGitLab;
    }

    public static GitLabRss rssInstance() {
        if (sGitLabRss == null) {
            OkHttpClient client = new OkHttpClient();
            client.setSslSocketFactory(sCustomTrustManager.getSSLSocketFactory());
            if (BuildConfig.DEBUG) {
                client.networkInterceptors().add(new TimberRequestInterceptor());
            }

            Retrofit restAdapter = new Retrofit.Builder()
                    .baseUrl(Prefs.getServerUrl(GitLabApp.instance()))
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .client(client)
                    .build();
            sGitLabRss = restAdapter.create(GitLabRss.class);
        }
        return sGitLabRss;
    }

    public static void reset() {
        sGitLab = null;
    }

    public static void setTrustedCertificate(String trustedCertificate) {
        sCustomTrustManager.setTrustedCertificate(trustedCertificate);
    }
}
