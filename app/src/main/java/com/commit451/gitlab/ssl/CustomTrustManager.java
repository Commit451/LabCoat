package com.commit451.gitlab.ssl;

import android.util.Log;
import timber.log.Timber;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class CustomTrustManager implements X509TrustManager {
    private static X509TrustManager DEFAULT_TRUST_MANAGER;
    private static Set<String> TRUSTED_CERTIFICATES = Collections.emptySet();

    static {
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance("X509");
            factory.init((KeyStore) null);

            TrustManager[] trustManagers = factory.getTrustManagers();
            if (trustManagers != null) {
                for (TrustManager trustManager : trustManagers) {
                    if (trustManager instanceof X509TrustManager) {
                        DEFAULT_TRUST_MANAGER = (X509TrustManager) trustManager;
                        break;
                    }
                }
            }
        } catch (NoSuchAlgorithmException e) {
            Timber.e(e, "Unable to get X509 TrustManager");
        } catch (KeyStoreException e) {
            Timber.e(e, "Exception while initializing CustomTrustManager");
        }
    }

    public static void setTrustedCertificates(Set<String> trustedCertificates) {
        TRUSTED_CERTIFICATES = trustedCertificates;
    }

    public CustomTrustManager() {}

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (DEFAULT_TRUST_MANAGER == null) {
            throw new IllegalStateException("No default TrustManager available");
        }

        DEFAULT_TRUST_MANAGER.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (DEFAULT_TRUST_MANAGER == null) {
            throw new IllegalStateException("No default TrustManager available");
        }

        CertificateException cause;
        try {
            DEFAULT_TRUST_MANAGER.checkServerTrusted(chain, authType);
            return;
        } catch (CertificateException e) {
            cause = e;
        }

        if (TRUSTED_CERTIFICATES.contains(X509Util.getFingerPrint(chain[0]))) {
            return;
        }

        throw new X509CertificateException(cause.getMessage(), cause, chain);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        if (DEFAULT_TRUST_MANAGER == null) {
            throw new IllegalStateException("No default TrustManager available");
        }

        return DEFAULT_TRUST_MANAGER.getAcceptedIssuers();
    }
}
