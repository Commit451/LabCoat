package com.commit451.gitlab.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.internal.tls.OkHostnameVerifier;

public class CustomHostnameVerifier implements HostnameVerifier {
    private static final HostnameVerifier DEFAULT_HOSTNAME_VERIFIER = OkHostnameVerifier.INSTANCE;

    //TODO make this not static, its kinda dirty
    private static String lastFailedHostname;

    public static String getLastFailedHostname() {
        return lastFailedHostname;
    }

    private final String trustedHostname;

    public CustomHostnameVerifier(String trustedHostname) {
        this.trustedHostname = trustedHostname;
    }

    @Override
    public boolean verify(String hostname, SSLSession session) {
        if (DEFAULT_HOSTNAME_VERIFIER.verify(hostname, session)) {
            lastFailedHostname = null;
            return true;
        }

        if (trustedHostname != null && trustedHostname.equals(hostname)) {
            lastFailedHostname = null;
            return true;
        }

        lastFailedHostname = hostname;
        return false;
    }
}
