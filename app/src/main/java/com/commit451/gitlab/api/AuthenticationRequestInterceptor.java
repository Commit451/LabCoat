package com.commit451.gitlab.api;

import com.commit451.gitlab.model.Account;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Adds the private token to all requests
 */
public class AuthenticationRequestInterceptor implements Interceptor {

    public static final String AUTHORIZATION_HEADER_FIELD = "Authorization";
    public static final String PRIVATE_TOKEN_HEADER_FIELD = "PRIVATE-TOKEN";
    private static final String PRIVATE_TOKEN_GET_PARAMETER = "private_token";

    private Account account;

    public AuthenticationRequestInterceptor(Account account) {
        this.account = account;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        HttpUrl url = request.url();

        String cleanUrl = url.toString();
        cleanUrl = cleanUrl.substring(cleanUrl.indexOf(':'));

        String cleanServerUrl = account.getServerUrl().toString();
        cleanServerUrl = cleanServerUrl.substring(cleanServerUrl.indexOf(':'));

        if (cleanUrl.startsWith(cleanServerUrl)) {
            String authorizationHeader = account.getAuthorizationHeader();
            if (authorizationHeader != null) {
                request = request.newBuilder()
                        .header(AUTHORIZATION_HEADER_FIELD, authorizationHeader)
                        .build();
            }

            String privateToken = account.getPrivateToken();
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
