package com.commit451.gitlab.model;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * The information associated with a signed in account
 * Created by Jawn on 12/4/2015.
 */
@Parcel
public class Account {

    @SerializedName("server_url")
    Uri mServerUrl;
    @SerializedName("private_token")
    String mPrivateToken;
    @SerializedName("trusted_certificate")
    String mTrustedCertificate;
    @SerializedName("user")
    User mUser;

    public Account() {

    }

    public Uri getServerUrl() {
        return mServerUrl;
    }

    public void setServerUrl(Uri url) {
        mServerUrl = url;
    }

    public String getTrustedCertificate() {
        return mTrustedCertificate;
    }

    public void setTrustedCertificate(String trustedCertificate) {
        mTrustedCertificate = trustedCertificate;
    }

    public String getPrivateToken() {
        return mPrivateToken;
    }

    public void setPrivateToken(String privateToken) {
        mPrivateToken = privateToken;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (mServerUrl != null ? !mServerUrl.equals(account.mServerUrl) : account.mServerUrl != null)
            return false;
        if (mPrivateToken != null ? !mPrivateToken.equals(account.mPrivateToken) : account.mPrivateToken != null)
            return false;
        if (mTrustedCertificate != null ? !mTrustedCertificate.equals(account.mTrustedCertificate) : account.mTrustedCertificate != null)
            return false;
        return !(mUser != null ? !mUser.equals(account.mUser) : account.mUser != null);

    }

    @Override
    public int hashCode() {
        int result = mServerUrl != null ? mServerUrl.hashCode() : 0;
        result = 31 * result + (mPrivateToken != null ? mPrivateToken.hashCode() : 0);
        result = 31 * result + (mTrustedCertificate != null ? mTrustedCertificate.hashCode() : 0);
        result = 31 * result + (mUser != null ? mUser.hashCode() : 0);
        return result;
    }
}
