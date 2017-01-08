package com.commit451.gitlab.api;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Gets the X509TrustManager on the system and caches it
 */
public class X509TrustManagerProvider {

    private static X509TrustManager x509TrustManager;

    /**
     * Get the static {@link X509TrustManager} for the system
     *
     * @return the static get
     */
    public static X509TrustManager get() {
        if (x509TrustManager == null) {
            try {
                init();
            } catch (Exception any) {
                //If they don't have X509 trust manager, they have bigger problems
                throw new RuntimeException(any);
            }
        }
        return x509TrustManager;
    }

    /**
     * Getting the {@link X509TrustManager} as shown in the {@link okhttp3.OkHttpClient.Builder#sslSocketFactory(SSLSocketFactory, X509TrustManager)} docs
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    private static void init() throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        x509TrustManager = (X509TrustManager) trustManagers[0];
    }
}
