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
     *
     * @param account the account
     * @return a configured {@link okhttp3.OkHttpClient.Builder}
     */
    public static OkHttpClient.Builder create(Account account) {
        //Do we even need a custom trust manager?
        // Yep. Otherwise SSL won't work properly with some configurations :) -Michi
        CustomTrustManager customTrustManager = new CustomTrustManager();
        customTrustManager.setTrustedCertificate(account.getTrustedCertificate());
        customTrustManager.setTrustedHostname(account.getTrustedHostname());
        customTrustManager.setPrivateKeyAlias(account.getPrivateKeyAlias());

        OpenSignInAuthenticator authenticator = new OpenSignInAuthenticator(account);
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .authenticator(authenticator)
                .proxyAuthenticator(authenticator)
                .addInterceptor(new AuthenticationRequestInterceptor(account))
                .sslSocketFactory(customTrustManager.getSSLSocketFactory(), X509TrustManagerProvider.get())
                .hostnameVerifier(customTrustManager.getHostnameVerifier());
    }
}
