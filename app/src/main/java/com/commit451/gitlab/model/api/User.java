package com.commit451.gitlab.model.api;

import android.net.Uri;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
@JsonObject
public class User extends UserBasic {
    @JsonField(name = "created_at")
    Date mCreatedAt;
    @JsonField(name = "is_admin")
    boolean mIsAdmin;
    @JsonField(name = "bio")
    String mBio;
    @JsonField(name = "skype")
    String mSkype;
    @JsonField(name = "linkedin")
    String mLinkedin;
    @JsonField(name = "twitter")
    String mTwitter;
    @JsonField(name = "website_url")
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
