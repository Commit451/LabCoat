package com.commit451.gitlab.model;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.commit451.gitlab.data.Prefs;
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
    public static List<Account> getAccounts(Context context) {
        List<Account> accounts = new ArrayList<>(Prefs.getAccounts(context));
        Collections.sort(accounts);
        Collections.reverse(accounts);
        return accounts;
    }

    @JsonField(name = "server_url")
    Uri mServerUrl;
    @JsonField(name = "authorization_header")
    String mAuthorizationHeader;
    @JsonField(name = "private_token")
    String mPrivateToken;
    @JsonField(name = "trusted_certificate")
    String mTrustedCertificate;
    @JsonField(name = "trusted_hostname")
    String mTrustedHostname;
    @JsonField(name = "private_key_alias")
    String mPrivateKeyAlias;
    @JsonField(name = "user")
    UserFull mUser;
    @JsonField(name = "last_used")
    Date mLastUsed;

    public Account() {}

    public Uri getServerUrl() {
        return mServerUrl;
    }

    public void setServerUrl(Uri url) {
        mServerUrl = url;
    }

    public String getAuthorizationHeader() {
        return mAuthorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        mAuthorizationHeader = authorizationHeader;
    }

    public String getPrivateToken() {
        return mPrivateToken;
    }

    public void setPrivateToken(String privateToken) {
        mPrivateToken = privateToken;
    }

    public String getTrustedCertificate() {
        return mTrustedCertificate;
    }

    public void setTrustedCertificate(String trustedCertificate) {
        mTrustedCertificate = trustedCertificate;
    }

    public String getTrustedHostname() {
        return mTrustedHostname;
    }

    public void setTrustedHostname(String trustedHostname) {
        mTrustedHostname = trustedHostname;
    }

    public String getPrivateKeyAlias() {
        return mPrivateKeyAlias;
    }

    public void setPrivateKeyAlias(String privateKeyAlias) {
        mPrivateKeyAlias = privateKeyAlias;
    }

    public UserFull getUser() {
        return mUser;
    }

    public void setUser(UserFull user) {
        mUser = user;
    }

    public Date getLastUsed() {
        return mLastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        mLastUsed = lastUsed;
    }

    @Override
    public int compareTo(@NonNull Account another) {
        return mLastUsed.compareTo(another.getLastUsed());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Account)) {
            return false;
        }

        Account account = (Account) o;
        return ObjectUtil.equals(mServerUrl, account.mServerUrl)
                && ObjectUtil.equals(mUser, account.mUser);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hash(mServerUrl, mUser);
    }
}
