package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import android.net.Uri;

import java.util.Date;

@Parcel
public class User extends UserBasic {
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("is_admin")
    boolean mIsAdmin;
    @SerializedName("bio")
    String mBio;
    @SerializedName("skype")
    String mSkype;
    @SerializedName("linkedin")
    String mLinkedin;
    @SerializedName("twitter")
    String mTwitter;
    @SerializedName("website_url")
    Uri mWebsiteUrl;

    public User() {}

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public boolean isAdmin() {
        return mIsAdmin;
    }

    public String getBio() {
        return mBio;
    }

    public String getSkype() {
        return mSkype;
    }

    public String getLinkedin() {
        return mLinkedin;
    }

    public String getTwitter() {
        return mTwitter;
    }

    public Uri getWebsiteUrl() {
        return mWebsiteUrl;
    }
}
