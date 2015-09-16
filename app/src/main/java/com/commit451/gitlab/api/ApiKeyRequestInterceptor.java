package com.commit451.gitlab.api;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.tools.Prefs;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Adds the private token to all requests
 * Created by Jawn on 9/15/2015.
 */
public class ApiKeyRequestInterceptor implements Interceptor {

    private static final String KEY_PRIVATE_TOKEN = "PRIVATE-TOKEN";

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder()
                .header(KEY_PRIVATE_TOKEN, Prefs.getPrivateToken(GitLabApp.instance()))
                .build());
    }
}
