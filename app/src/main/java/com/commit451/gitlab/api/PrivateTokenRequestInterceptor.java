package com.commit451.gitlab.api;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.data.Prefs;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Adds the private token to all requests
 * Created by Jawn on 9/15/2015.
 */
public class PrivateTokenRequestInterceptor implements Interceptor {

    private static final String PRIVATE_TOKEN_HEADER_FIELD = "PRIVATE-TOKEN";
    private static final String PRIVATE_TOKEN_GET_PARAMETER = "private_token";

    private boolean mHeader;

    public PrivateTokenRequestInterceptor(boolean header) {
        mHeader = header;
    }

    //TODO change this to where it does not read from prefs every time (inefficient)
    @Override
    public Response intercept(Chain chain) throws IOException {
        String privateToken = Prefs.getPrivateToken(GitLabApp.instance());
        if (privateToken == null) {
            throw new IllegalStateException("The private token was null");
        }

        Request.Builder builder = chain.request().newBuilder();

        if (mHeader) {
            builder.header(PRIVATE_TOKEN_HEADER_FIELD, privateToken);
        } else {
            HttpUrl url = chain.request().httpUrl().newBuilder()
                    .addQueryParameter(PRIVATE_TOKEN_GET_PARAMETER, privateToken)
                    .build();

            builder.url(url);
        }

        return chain.proceed(builder.build());
    }
}
