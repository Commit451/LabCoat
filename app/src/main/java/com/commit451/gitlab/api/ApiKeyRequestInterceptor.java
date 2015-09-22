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

    //TODO change this to where it does not read from prefs every time (inefficient)
    @Override
    public Response intercept(Chain chain) throws IOException {
        String privateToken = Prefs.getPrivateToken(GitLabApp.instance());
        if (privateToken == null) {
            throw new IllegalStateException("The private token was null");
        }
        return chain.proceed(chain.request().newBuilder()
                .header(KEY_PRIVATE_TOKEN, Prefs.getPrivateToken(GitLabApp.instance()))
                .build());
    }
}
