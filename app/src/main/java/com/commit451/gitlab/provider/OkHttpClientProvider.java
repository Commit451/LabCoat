package com.commit451.gitlab.provider;

import com.commit451.gitlab.BuildConfig;
import com.commit451.gitlab.api.AuthenticationRequestInterceptor;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.ssl.CustomTrustManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Creates an OkHttpClient with the needed defaults
 */
public final class OkHttpClientProvider {
    private static Account sAccount;

    private static CustomTrustManager sCustomTrustManager = new CustomTrustManager();
    private static OkHttpClient sOkHttpClient;

    private OkHttpClientProvider() {}

    public static OkHttpClient getInstance(Account account) {
        if (sAccount != account) {
            sOkHttpClient = null;
        }
        if (sOkHttpClient == null) {
            sOkHttpClient = createInstance(account);
            sAccount = account;
        }
        return sOkHttpClient;
    }

    private static OkHttpClient createInstance(Account account) {
        sCustomTrustManager.setTrustedCertificate(account.getTrustedCertificate());
        sCustomTrustManager.setTrustedHostname(account.getTrustedHostname());
        sCustomTrustManager.setPrivateKeyAlias(account.getPrivateKeyAlias());

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .sslSocketFactory(sCustomTrustManager.getSSLSocketFactory())
                .hostnameVerifier(sCustomTrustManager.getHostnameVerifier());
        clientBuilder.addInterceptor(new AuthenticationRequestInterceptor(account));
        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        return clientBuilder.build();
    }
}
