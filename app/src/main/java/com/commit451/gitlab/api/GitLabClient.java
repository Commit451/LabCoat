package com.commit451.gitlab.api;

import com.commit451.gitlab.BuildConfig;
import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.tools.Prefs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;
import java.util.Date;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by Jawn on 7/28/2015.
 */
public class GitLabClient {

    private static final String API_VERSION = "/api/v3";
    private static GitLab gitLab;

    public static GitLab instance() {

        if(gitLab == null) {
            // Configure Gson to handle dates correctly
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                @Override
                public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                    return ISODateTimeFormat.dateTimeParser().parseDateTime(json.getAsString()).toDate();
                }
            });
            Gson gson = gsonBuilder.create();

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setRequestInterceptor(new GitLabInterceptor())
                    .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC)
                    .setConverter(new GsonConverter(gson))
                    .setEndpoint(Prefs.getServerUrl(GitLabApp.instance()) + API_VERSION)
                    .build();
            gitLab = restAdapter.create(GitLab.class);
        }

        return gitLab;
    }

    public static void reset() {
        gitLab = null;
    }
}
