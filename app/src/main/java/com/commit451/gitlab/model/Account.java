package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * The information associated with a signed in account
 * Created by Jawn on 12/4/2015.
 */
@Parcel
public class Account {

    @SerializedName("server_url")
    String mServerUrl;
    @SerializedName("private_token")
    String mPrivateToken;
    @SerializedName("trusted_certificate")
    String mTrustedCertificate;
    @SerializedName("user")
    User mUser;

    public Account() {

    }

    public String getServerUrl() {
        return mServerUrl;
    }

    public void setServerUrl(String url) {
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
}
