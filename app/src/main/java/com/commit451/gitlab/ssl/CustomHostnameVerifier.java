package com.commit451.gitlab.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.internal.tls.OkHostnameVerifier;

public class CustomHostnameVerifier implements HostnameVerifier {
    private static final HostnameVerifier DEFAULT_HOSTNAME_VERIFIER = OkHostnameVerifier.INSTANCE;
    private static String sLastFailedHostname;

    private final String mTrustedHostname;

    public CustomHostnameVerifier(String trustedHostname) {
        this.mTrustedHostname = trustedHostname;
    }

    @Override
    public boolean verify(String hostname, SSLSession session) {
        if (DEFAULT_HOSTNAME_VERIFIER.verify(hostname, session)) {
            sLastFailedHostname = null;
            return true;
        }

        if (mTrustedHostname != null && mTrustedHostname.equals(hostname)) {
            sLastFailedHostname = null;
            return true;
        }

        sLastFailedHostname = hostname;
        return false;
    }

    public static String getLastFailedHostname() {
        return sLastFailedHostname;
    }
}
