package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class User extends UserBasic {
    @Json(name = "created_at")
    Date createdAt;
    @Json(name = "is_admin")
    boolean isAdmin;
    @Json(name = "bio")
    String bio;
    @Json(name = "skype")
    String skype;
    @Json(name = "linkedin")
    String linkedin;
    @Json(name = "twitter")
    String twitter;
    @Json(name = "website_url")
    String websiteUrl;

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

    public String getWebsiteUrl() {
        return websiteUrl;
    }
}
