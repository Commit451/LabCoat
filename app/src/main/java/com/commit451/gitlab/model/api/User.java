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
    Date createdAt;
    @JsonField(name = "is_admin")
    boolean isAdmin;
    @JsonField(name = "bio")
    String bio;
    @JsonField(name = "skype")
    String skype;
    @JsonField(name = "linkedin")
    String linkedin;
    @JsonField(name = "twitter")
    String twitter;
    @JsonField(name = "website_url")
    Uri websiteUrl;

    public User() {}

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public String getBio() {
        return bio;
    }

    public String getSkype() {
        return skype;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public String getTwitter() {
        return twitter;
    }

    public Uri getWebsiteUrl() {
        return websiteUrl;
    }
}
