package com.commit451.gitlab.api;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Gets the X509TrustManager on the system and caches it
 */
public class X509TrustManagerProvider {

    private static X509TrustManager sX509TrustManager;

    /**
     * Get the static {@link X509TrustManager} for the system
     * @return the static instance
     */
    public static X509TrustManager get() {
        if (sX509TrustManager == null) {
            try {
                init();
            } catch (Exception any) {
                //If they don't have X509 trust manager, they have bigger problems
                throw new RuntimeException(any);
            }
        }
        return sX509TrustManager;
    }

    private static void init() throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance("X509");
        factory.init((KeyStore) null);

        TrustManager[] trustManagers = factory.getTrustManagers();
        if (trustManagers != null) {
            for (TrustManager trustManager : trustManagers) {
                if (trustManager instanceof X509TrustManager) {
                    sX509TrustManager = (X509TrustManager) trustManager;
                    break;
                }
            }
        }
    }
}
