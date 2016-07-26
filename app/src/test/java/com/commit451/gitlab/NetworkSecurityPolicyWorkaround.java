package com.commit451.gitlab;

import android.security.NetworkSecurityPolicy;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(NetworkSecurityPolicy.class)
public class NetworkSecurityPolicyWorkaround {
    @Implementation
    public static NetworkSecurityPolicy getInstance() {
        try {
            Class<?> shadow = Class.forName("android.security.NetworkSecurityPolicy");
            return (NetworkSecurityPolicy) shadow.newInstance();
        } catch (Exception e) {
            throw new AssertionError();
        }
    }

    @Implementation
    public boolean isCleartextTrafficPermitted() {
        return true;
    }

    @Implementation
    public boolean isCleartextTrafficPermitted(String hostname) {
        return true;
    }
}