package com.commit451.gitlab.providers;

import android.text.TextUtils;

import com.commit451.gitlab.BuildConfig;
import com.commit451.gitlab.api.PrivateTokenRequestInterceptor;
import com.commit451.gitlab.api.TimberRequestInterceptor;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.ssl.CustomTrustManager;
import com.squareup.okhttp.OkHttpClient;

/**
 * Creates an OkHttpClient with the needed defaults
 * Created by Jawn on 12/4/2015.
 */
public class OkHttpClientProvider {

    private static CustomTrustManager sCustomTrustManager = new CustomTrustManager();
    private static OkHttpClient sOkHttpClient;

    public static OkHttpClient getInstance(Account account) {
        if (sOkHttpClient == null) {
            sOkHttpClient = createInstance(account);
        }
        return sOkHttpClient;
    }

    public static OkHttpClient createInstance(Account account) {
        return createInstance(account, true);
    }

    public static OkHttpClient createPicassoInstance(Account account) {
        return createInstance(account, false);
    }

    private static OkHttpClient createInstance(Account account, boolean header) {
        OkHttpClient client = new OkHttpClient();
        if (!TextUtils.isEmpty(account.getTrustedCertificate())) {
            sCustomTrustManager.setTrustedCertificate(account.getTrustedCertificate());
            client.setSslSocketFactory(sCustomTrustManager.getSSLSocketFactory());
        }
        client.interceptors().add(new PrivateTokenRequestInterceptor(account, header));
        if (BuildConfig.DEBUG) {
            client.networkInterceptors().add(new TimberRequestInterceptor());
        }
        return client;
    }
}
