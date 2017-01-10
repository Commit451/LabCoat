package com.commit451.gitlab.model;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.commit451.gitlab.App;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.util.ObjectUtil;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * An account, stored locally, which references the needed info to connect to a server
 */
@Parcel
@JsonObject
public class Account implements Comparable<Account>{

    @NonNull
    public static List<Account> getAccounts() {
        List<Account> accounts = new ArrayList<>(App.Companion.get().getPrefs().getAccounts());
        Collections.sort(accounts);
        Collections.reverse(accounts);
        return accounts;
    }

    @JsonField(name = "server_url")
    Uri serverUrl;
    @JsonField(name = "authorization_header")
    String authorizationHeader;
    @JsonField(name = "private_token")
    String privateToken;
    @JsonField(name = "trusted_certificate")
    String trustedCertificate;
    @JsonField(name = "trusted_hostname")
    String trustedHostname;
    @JsonField(name = "private_key_alias")
    String privateKeyAlias;
    @JsonField(name = "user")
    UserFull user;
    @JsonField(name = "last_used")
    Date lastUsed;

    public Account() {}

    public Uri getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(Uri url) {
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
        return ObjectUtil.equals(serverUrl, account.serverUrl)
                && ObjectUtil.equals(user, account.user);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hash(serverUrl, user);
    }
}
