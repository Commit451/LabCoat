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
        CustomTrustManager customTrustManager = null;
        //Do we even need a custom trust manager?
        if (account.getTrustedCertificate() != null
                || account.getTrustedHostname() != null
                || account.getPrivateKeyAlias() != null) {
            customTrustManager = new CustomTrustManager();
            customTrustManager.setTrustedCertificate(account.getTrustedCertificate());
            customTrustManager.setTrustedHostname(account.getTrustedHostname());
            customTrustManager.setPrivateKeyAlias(account.getPrivateKeyAlias());
        }
        OpenSignInAuthenticator authenticator = new OpenSignInAuthenticator(account);
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .authenticator(authenticator)
                .proxyAuthenticator(authenticator)
                .addInterceptor(new AuthenticationRequestInterceptor(account));
        //Only apply these custom things when needed, since they slow down the init
        if (customTrustManager != null && customTrustManager.getSSLSocketFactory() != null) {
            clientBuilder.sslSocketFactory(customTrustManager.getSSLSocketFactory(), X509TrustManagerProvider.get());
        }
        if (customTrustManager != null && customTrustManager.getHostnameVerifier() != null) {
            clientBuilder.hostnameVerifier(customTrustManager.getHostnameVerifier());
        }
        return clientBuilder;
    }
}
