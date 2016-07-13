package com.commit451.gitlab.api;

import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.ssl.CustomTrustManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Creates an OkHttpClient with the needed defaults
 */
public final class OkHttpClientFactory {

    /**
     * Creates an {@link OkHttpClient} configured with the account configuration
     * @param account the account
     * @return a configured okhttpclient
     */
    public static OkHttpClient.Builder create(Account account) {
        CustomTrustManager customTrustManager = new CustomTrustManager();
        customTrustManager.setTrustedCertificate(account.getTrustedCertificate());
        customTrustManager.setTrustedHostname(account.getTrustedHostname());
        customTrustManager.setPrivateKeyAlias(account.getPrivateKeyAlias());

        OpenSignInAuthenticator authenticator = new OpenSignInAuthenticator(account);
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .sslSocketFactory(customTrustManager.getSSLSocketFactory())
                .hostnameVerifier(customTrustManager.getHostnameVerifier())
                .authenticator(authenticator)
                .proxyAuthenticator(authenticator);
        clientBuilder.addInterceptor(new AuthenticationRequestInterceptor(account));
        return clientBuilder;
    }
}
