package com.commit451.gitlab.api;

import com.commit451.gitlab.model.Account;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import timber.log.Timber;

/**
 * Adds the private token to all requests
 * Created by Jawn on 9/15/2015.
 */
public class PrivateTokenRequestInterceptor implements Interceptor {

    private static final String PRIVATE_TOKEN_HEADER_FIELD = "PRIVATE-TOKEN";
    private static final String PRIVATE_TOKEN_GET_PARAMETER = "private_token";

    private Account mAccount;
    private boolean mHeader;

    public PrivateTokenRequestInterceptor(Account account, boolean header) {
        mAccount = account;
        mHeader = header;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        HttpUrl url = chain.request().httpUrl();
        HttpUrl serverUrl = HttpUrl.parse(mAccount.getServerUrl());
        if (!url.toString().startsWith(serverUrl.toString())) {
            return chain.proceed(chain.request());
        }

        String privateToken = mAccount.getPrivateToken();
        if (privateToken == null) {
            Timber.e("The private token was null");
        }

        Request.Builder builder = chain.request().newBuilder();

        if (privateToken != null) {
            if (mHeader) {
                builder.header(PRIVATE_TOKEN_HEADER_FIELD, privateToken);
            } else {
                url = url.newBuilder()
                        .addQueryParameter(PRIVATE_TOKEN_GET_PARAMETER, privateToken)
                        .build();

                builder.url(url);
            }
        }

        return chain.proceed(builder.build());
    }
}
