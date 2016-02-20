package com.commit451.gitlab.ssl;

import timber.log.Timber;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class CustomTrustManager implements X509TrustManager {
    private static X509TrustManager sDefaultTrustManager;

    static {
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance("X509");
            factory.init((KeyStore) null);

            TrustManager[] trustManagers = factory.getTrustManagers();
            if (trustManagers != null) {
                for (TrustManager trustManager : trustManagers) {
                    if (trustManager instanceof X509TrustManager) {
                        sDefaultTrustManager = (X509TrustManager) trustManager;
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

    private String mTrustedCertificate;
    private String mTrustedHostname;
    private SSLSocketFactory mSSLSocketFactory;
    private HostnameVerifier mHostnameVerifier;

    public CustomTrustManager() {}

    public void setTrustedCertificate(String trustedCertificate) {
        if ((mTrustedCertificate == null && trustedCertificate == null) || (mTrustedCertificate != null && mTrustedCertificate.equals(trustedCertificate))) {
            return;
        }

        mTrustedCertificate = trustedCertificate;
        mSSLSocketFactory = null;
    }

    public void setTrustedHostname(String trustedHostname) {
        if ((mTrustedHostname == null && trustedHostname == null) || (mTrustedHostname != null && mTrustedHostname.equals(trustedHostname))) {
            return;
        }

        mTrustedHostname = trustedHostname;
        mHostnameVerifier = null;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (sDefaultTrustManager == null) {
            throw new IllegalStateException("No default TrustManager available");
        }

        sDefaultTrustManager.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (sDefaultTrustManager == null) {
            throw new IllegalStateException("No default TrustManager available");
        }

        CertificateException cause;
        try {
            sDefaultTrustManager.checkServerTrusted(chain, authType);
            return;
        } catch (CertificateException e) {
            cause = e;
        }

        if (mTrustedCertificate != null && mTrustedCertificate.equals(X509Util.getFingerPrint(chain[0]))) {
            return;
        }

        throw new X509CertificateException(cause.getMessage(), cause, chain);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        if (sDefaultTrustManager == null) {
            throw new IllegalStateException("No default TrustManager available");
        }

        return sDefaultTrustManager.getAcceptedIssuers();
    }

    public SSLSocketFactory getSSLSocketFactory() {
        if (mSSLSocketFactory != null) {
            return mSSLSocketFactory;
        }

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{this}, null);
            mSSLSocketFactory = new CustomSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return mSSLSocketFactory;
    }

    public HostnameVerifier getHostnameVerifier() {
        if (mHostnameVerifier == null) {
            mHostnameVerifier = new CustomHostnameVerifier(mTrustedHostname);
        }

        return mHostnameVerifier;
    }
}
