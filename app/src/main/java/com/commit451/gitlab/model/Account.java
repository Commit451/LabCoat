package com.commit451.gitlab.model;

import android.support.annotation.NonNull;

import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.util.ObjectUtil;
import com.squareup.moshi.Json;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * An account, stored locally, which references the needed info to connect to a server
 */
@Parcel
public class Account implements Comparable<Account>{

    @NonNull
    public static List<Account> getAccounts() {
        List<Account> accounts = new ArrayList<>(Prefs.INSTANCE.getAccounts());
        Collections.sort(accounts);
        Collections.reverse(accounts);
        return accounts;
    }

    @Json(name = "server_url")
    String serverUrl;
    @Json(name = "authorization_header")
    String authorizationHeader;
    @Json(name = "private_token")
    String privateToken;
    @Json(name = "trusted_certificate")
    String trustedCertificate;
    @Json(name = "trusted_hostname")
    String trustedHostname;
    @Json(name = "private_key_alias")
    String privateKeyAlias;
    @Json(name = "user")
    UserFull user;
    @Json(name = "last_used")
    Date lastUsed;

    public Account() {}

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String url) {
        serverUrl = url;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public String getPrivateToken() {
        return privateToken;
    }

    public void setPrivateToken(String privateToken) {
        this.privateToken = privateToken;
    }

    public String getTrustedCertificate() {
        return trustedCertificate;
    }

    public void setTrustedCertificate(String trustedCertificate) {
        this.trustedCertificate = trustedCertificate;
    }

    public String getTrustedHostname() {
        return trustedHostname;
    }

    public void setTrustedHostname(String trustedHostname) {
        this.trustedHostname = trustedHostname;
    }

    public String getPrivateKeyAlias() {
        return privateKeyAlias;
    }

    public void setPrivateKeyAlias(String privateKeyAlias) {
        this.privateKeyAlias = privateKeyAlias;
    }

    public UserFull getUser() {
        return user;
    }

    public void setUser(UserFull user) {
        this.user = user;
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }

    @Override
    public int compareTo(@NonNull Account another) {
        return lastUsed.compareTo(another.getLastUsed());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Account)) {
            return false;
        }

        Account account = (Account) o;
        return ObjectUtil.INSTANCE.equals(serverUrl, account.serverUrl)
                && ObjectUtil.INSTANCE.equals(user, account.user);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.INSTANCE.hash(serverUrl, user);
    }
}
