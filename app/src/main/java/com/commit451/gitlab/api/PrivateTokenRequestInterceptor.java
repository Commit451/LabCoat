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

    public PrivateTokenRequestInterceptor(Account account) {
        mAccount = account;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        HttpUrl url = request.httpUrl();
        if (url.toString().startsWith(mAccount.getServerUrl().toString())) {
            String privateToken = mAccount.getPrivateToken();
            if (privateToken == null) {
                Timber.e("The private token was null");
            } else {
                url = url.newBuilder()
                        .addQueryParameter(PRIVATE_TOKEN_GET_PARAMETER, privateToken)
                        .build();

                request = request.newBuilder()
                        .header(PRIVATE_TOKEN_HEADER_FIELD, privateToken)
                        .url(url)
                        .build();
            }
        }

        return chain.proceed(request);
    }
}
